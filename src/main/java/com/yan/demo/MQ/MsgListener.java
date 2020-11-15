package com.yan.demo.MQ;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@Component
// 接受 my-dlx-queue 这个死信队列的消息
@RabbitListener(queues = "my-dlx-queue")
public class MsgListener {
    @RabbitHandler

    public void receive(Map<String, Object> msg) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(sdf.format(new Date()));
        System.out.println(msg.get("msg"));
    }

}
