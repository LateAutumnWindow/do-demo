package com.yan.demo.MQ;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // 定义延时队列
    @Bean("delayQueueName")
    public Queue delayQueue() {
        return QueueBuilder
                // durable 是声明一个持久化的队列
                .durable("delayQueueName")
                // 当 delayQueueName 队列的消息过时会被投递到 my-dlx-exchange 这个交换机
                .withArgument("x-dead-letter-exchange", "my-dlx-exchange")
                // 当这个 my-dlx-exchange 接收到过时的消息，会根据 这个消息的路由key(routing-key-delay)发送到对应的死信队列
                .withArgument("x-dead-letter-routing-key", "routing-key-delay")
                .build();
    }

    // 定义死信队列
    @Bean("dlxQueue")
    public Queue dlxQueue() {
        return QueueBuilder.durable("my-dlx-queue").build();
    }

    // 定义死信交换机
    @Bean("dlxExchange")
    public Exchange dlxExchange() {
        return ExchangeBuilder.directExchange("my-dlx-exchange").build();
    }

    // 绑定死信队列和死信交换机
    @Bean("dlxBinding")
    public Binding dlxBinding(@Qualifier("dlxQueue") Queue dlxQueue, @Qualifier("dlxExchange") Exchange dlxExchange) {
        return BindingBuilder.bind(dlxQueue).to(dlxExchange).with("routing-key-delay").noargs();
    }

}
