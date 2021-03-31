package com.seckill.dis.mq;

import com.seckill.dis.common.api.mq.MqProviderApi;
import com.seckill.dis.common.api.mq.vo.SkMessage;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rocketmq")
public class RocketMQController {

    @Autowired
    private MqProviderApi mqProducerService;

    @GetMapping("/send")
    public void send() {
        mqProducerService.sendSkMessage(new SkMessage());
    }



}