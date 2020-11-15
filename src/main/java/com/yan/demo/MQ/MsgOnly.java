package com.yan.demo.MQ;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class MsgOnly {

    @RabbitListener(queues = "delayQueueName")
    public void msgOnly(Message msg) {
        String messageId = msg.getMessageProperties().getMessageId();
        System.out.println(messageId);
    }

}
