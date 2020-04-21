package com.toolkit.scantaskmng.service.mq;

import com.toolkit.scantaskmng.global.rabbitmq.RabbitConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TopicSender {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void send(String topic, String msg) {
        logger.info("---> send MQ Topic: " + topic);
        logger.info("---> send message: " + msg);
        this.rabbitTemplate.convertAndSend(RabbitConfig.MAIN_EXCHANGE, topic, msg);
    }

    public void sendMainTopic(String msg) {
        send(RabbitConfig.DEFAULT_TOPIC, msg);
    }

//    public void sendRunStatusTopic(String msg) {
//        send(RabbitConfig.TASK_RUN_STATUS_TOPIC, msg);
//    }

    public void sendNodeTopic(String nodeIP, String msg) {
        String topic = "topic.ip." + nodeIP;
        RabbitConfig.bindTopic(topic, "topic.ip.#");
        send(topic, msg);
    }
}
