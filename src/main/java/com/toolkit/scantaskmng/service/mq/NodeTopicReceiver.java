package com.toolkit.scantaskmng.service.mq;

import com.toolkit.scantaskmng.global.rabbitmq.RabbitConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
// 测试联调时，解除 RabbitListener 注释
//@RabbitListener(queues = "topic.ip.127.0.0.1" )
//@RabbitListener(queues = "#{RabbitConfig.NODE_TOPIC}" )
public class NodeTopicReceiver {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @RabbitHandler
    public void process(String message) {
        logger.info("<--- receiver topics: " + RabbitConfig.NODE_TOPIC);
        logger.info("<--- receive message: " + message);
    }
}
