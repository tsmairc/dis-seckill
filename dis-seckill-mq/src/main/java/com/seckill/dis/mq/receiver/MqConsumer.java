package com.seckill.dis.mq.receiver;

import com.alibaba.fastjson.JSON;
import com.seckill.dis.common.api.cache.RedisServiceApi;
import com.seckill.dis.common.api.cache.vo.OrderKeyPrefix;
import com.seckill.dis.common.api.goods.GoodsServiceApi;
import com.seckill.dis.common.api.goods.vo.GoodsVo;
import com.seckill.dis.common.api.mq.vo.SkMessage;
import com.seckill.dis.common.api.order.OrderServiceApi;
import com.seckill.dis.common.api.seckill.SeckillServiceApi;
import com.seckill.dis.common.api.user.vo.UserVo;
import com.seckill.dis.common.domain.SeckillOrder;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * MQ消息接收者, 消费者
 * 消费者绑定在队列监听，既可以接收到队列中的消息
 *
 * @author noodle
 */
@Service
public class MqConsumer {

    private static Logger logger = LoggerFactory.getLogger(MqConsumer.class);

    @Reference(interfaceClass = GoodsServiceApi.class)
    GoodsServiceApi goodsService;

    @Reference(interfaceClass = OrderServiceApi.class)
    OrderServiceApi orderService;

    @Reference(interfaceClass = SeckillServiceApi.class)
    SeckillServiceApi seckillService;

    @Reference(interfaceClass = RedisServiceApi.class)
    RedisServiceApi redisService;



    // topic需要和生产者的topic一致，consumerGroup属性是必须指定的，内容可以随意
    // selectorExpression的意思指的就是tag，默认为“*”，不设置的话会监听所有消息
    // 处理收到的秒杀成功信息（核心业务实现）
    @Service
    @RocketMQMessageListener(topic = "seckill",  consumerGroup = "Con_Group_One")
    public class ConsumerSend implements RocketMQListener<SkMessage> {
        // 监听到消息就会执行此方法
        @Override
        public void onMessage(SkMessage message) {
            logger.info("监听到消息：user={}", JSON.toJSONString(message));
            logger.info("MQ receive a message: " + message);

            // 获取秒杀用户信息与商品id
            UserVo user = message.getUser();
            long goodsId = message.getGoodsId();

            // 获取商品的库存
            GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
            Integer stockCount = goods.getStockCount();
            if (stockCount <= 0) {
                return;
            }

            // 判断是否已经秒杀到了（保证秒杀接口幂等性）
            SeckillOrder order = this.getSkOrderByUserIdAndGoodsId(user.getUuid(), goodsId);
            if (order != null) {
                return;
            }

            // 1.减库存 2.写入订单 3.写入秒杀订单
            seckillService.seckill(user, goods);
        }
        /**
         * 通过用户id与商品id从订单列表中获取订单信息，这个地方用了唯一索引（unique index!!!!!）
         * <p>
         * 优化，不同每次都去数据库中读取秒杀订单信息，而是在第一次生成秒杀订单成功后，
         * 将订单存储在redis中，再次读取订单信息的时候就直接从redis中读取
         *
         * @param userId
         * @param goodsId
         * @return 秒杀订单信息
         */
        private SeckillOrder getSkOrderByUserIdAndGoodsId(Long userId, long goodsId) {

            // 从redis中取缓存，减少数据库的访问
            SeckillOrder seckillOrder = redisService.get(OrderKeyPrefix.SK_ORDER, ":" + userId + "_" + goodsId, SeckillOrder.class);
            if (seckillOrder != null) {
                return seckillOrder;
            }
            return orderService.getSeckillOrderByUserIdAndGoodsId(userId, goodsId);
        }
    }



}
