package com.toolkit.scantaskmng.service.mq;

import com.toolkit.scantaskmng.global.rabbitmq.RabbitConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
// 测试联调时，解除 RabbitListener 注释
//@RabbitListener(queues = RabbitConfig.TASK_RUN_STATUS_TOPIC )
public class TaskRunStatusTopicReceiver {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @RabbitHandler
    public void process(String message) {
        logger.info("<--- receiver topics: " + RabbitConfig.TASK_RUN_STATUS_TOPIC);
        logger.info("<--- receive message: " + message);
    }
}
