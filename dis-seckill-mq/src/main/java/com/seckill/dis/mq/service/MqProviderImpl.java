package com.seckill.dis.mq.service;

import com.alibaba.fastjson.JSON;
import com.seckill.dis.common.api.mq.MqProviderApi;
import com.seckill.dis.common.api.mq.vo.SkMessage;
import com.seckill.dis.mq.config.MQConfig;
import org.apache.dubbo.config.annotation.Service;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;


/**
 * 消息队列服务化（消息生产者）
 *
 * @author noodle
 */

@Service(interfaceClass = MqProviderApi.class)
public class MqProviderImpl implements MqProviderApi {

    private static Logger logger = LoggerFactory.getLogger(MqProviderImpl.class);

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Override
    public void sendSkMessage(SkMessage message) {
        logger.info("MQ send message: " + message);
        // 秒杀消息关联的数据
//        Message message1 = new Message();
//        message1.setBody(JSON.toJSONBytes(message));
//        try {
//            defaultMQProducer.send(message1);
//        } catch (MQClientException e) {
//            e.printStackTrace();
//        } catch (RemotingException e) {
//            e.printStackTrace();
//        } catch (MQBrokerException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        rocketMQTemplate.convertAndSend(MQConfig.SECKILL_QUEUE , message);
//        rocketMQTemplate.send(topic + ":tag1", MessageBuilder.withPayload(user).build()); // 等价于上面一行
    }

}
