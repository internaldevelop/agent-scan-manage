package com.toolkit.scantaskmng.global.rabbitmq;

import com.toolkit.scantaskmng.global.utils.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class RabbitConfig {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${spring.rabbitmq.host}")
    private String host;

    @Value("${spring.rabbitmq.port}")
    private int port;

    @Value("${spring.rabbitmq.username}")
    private String username;

    @Value("${spring.rabbitmq.password}")
    private String password;


    public static final String MAIN_EXCHANGE = "main-mq-exchange";

    public static final String DEFAULT_TOPIC = "topic.main";
    public static final String TASK_RUN_STATUS_TOPIC = "topic.run-status";
    public static final String NODE_TOPIC = "topic.ip." + SystemUtils.getLocalHostLANAddress().getHostAddress();

    public static final String MAIN_ROUTINGKEY = "topic.main";
    public static final String TASK_RUN_STATUS_ROUTINGKEY = "topic.run-status";

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(host,port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        connectionFactory.setVirtualHost("/");
        connectionFactory.setPublisherConfirms(true);
        return connectionFactory;
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    //必须是prototype类型
    public RabbitTemplate rabbitTemplate() {
        RabbitTemplate template = new RabbitTemplate(connectionFactory());
        return template;
    }

    /**
     * 针对消费者配置
     * 1. 设置交换机类型
     * 2. 将队列绑定到交换机
     FanoutExchange: 将消息分发到所有的绑定队列，无routingkey的概念
     HeadersExchange ：通过添加属性key-value匹配
     DirectExchange:按照routingkey分发到指定队列
     TopicExchange:多关键字匹配
     */
    @Bean
    public TopicExchange defaultExchange() {
        return new TopicExchange(MAIN_EXCHANGE);
    }

    /**
     * 创建缺省主题 (topic)
     * 各节点向此主题发送消息，消费者是中心服务器
     * @return
     */
    @Bean
    public Queue mainQueue() {
        return new Queue(DEFAULT_TOPIC, true); //队列持久
    }

    /**
     * 创建任务运行状态主题 (topic)
     * 各节点向此主题发送包含本节点任务运行状态的消息，消费者是中心服务器
     * @return
     */
    @Bean
    public Queue taskRunStatusQueue() {
        return new Queue(TASK_RUN_STATUS_TOPIC, true); //队列持久
    }

    @Bean
    public Queue nodeQueue() {
        return new Queue(NODE_TOPIC, true);
    }

    /**
     * 绑定缺省主题到交换机
     * @return
     */
    @Bean
    public Binding bindDefaultTopic() {
        return BindingBuilder.bind(mainQueue()).to(defaultExchange()).with(RabbitConfig.MAIN_ROUTINGKEY);
    }

    /**
     * 绑定任务运行状态主题到交换机
     * @return
     */
    @Bean
    public Binding bindTaskRunStatusTopic() {
        return BindingBuilder.bind(taskRunStatusQueue()).to(defaultExchange()).with(RabbitConfig.TASK_RUN_STATUS_ROUTINGKEY);
    }

    @Bean Binding bindTaskRunTopic() {
        return BindingBuilder.bind(nodeQueue()).to(defaultExchange()).with(RabbitConfig.NODE_TOPIC);
//        return BindingBuilder.bind(nodeQueue()).to(defaultExchange()).with("topic.ip.#");
    }

    /**
     * 绑定节点的队列主题到交换机
     * @param topicName
     * @param routingKey
     * @return
     */
    public static Binding bindTopic(String topicName, String routingKey) {
        return BindingBuilder.bind(new Queue(topicName, true))
                .to(new TopicExchange(MAIN_EXCHANGE))
                .with(routingKey);
    }

}
