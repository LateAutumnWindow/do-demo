package com.yan.demo.MQ;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/test")
public class SendMsgController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @GetMapping("/send")
    public String sendMsg(String msg) {
        Map<String, Object> map = new HashMap<>();
        map.put("msg", msg);
        // delayQueueName 就是没有消费者的队列名称
        rabbitTemplate.convertAndSend("delayQueueName", map, new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                // 20000 是消息的过时时间 20 S
                message.getMessageProperties().setExpiration("20000");
                message.getMessageProperties().setMessageId("kkeee11111");
                return message;
            }
        });
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(sdf.format(new Date()));
        return "ok";
    }
}
