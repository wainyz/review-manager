package com.wainyz.core.config;


import com.wainyz.commons.consistent.RabbitMqConsistent;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;

/**
 * 配置rabbitMq需要的一些对象
 * @author Yanion_gwgzh
 */
@SpringBootConfiguration
public class RabbitMqConfig {
    @Value("${spring.rabbitmq.host:localhost}")
    private String host;
    @Value("${spring.rabbitmq.username:guest}")
    private String username ;
    @Value("${spring.rabbitmq.password:guest}")
    private String password ;
    /**
     * 配置RabbitMQ连接工厂
     * @return CachingConnectionFactory的实例，配置了默认的主机地址"localhost"
     */
    @Bean
    public CachingConnectionFactory connectionFactory() {
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory(host);
        cachingConnectionFactory.setUsername(username);
        cachingConnectionFactory.setPassword(password);
        return cachingConnectionFactory;
    }


    /**
     * @return 交换机
     */
    @Bean
    public Exchange exchange() {
        return ExchangeBuilder.directExchange(RabbitMqConsistent.EXCHANGE_NAME)
                .durable(true)
                .build();
    }
    /**
     * 绑定队列到交换机
     */
    @Bean
    public Binding binding(Queue deepseekRequestsQueue, Exchange exchange) {
        return BindingBuilder.bind(deepseekRequestsQueue)
                .to(exchange)
                .with(RabbitMqConsistent.ROUTING_KEY) // 绑定键
                .noargs();
    }

    /**
     * @return RabbitTemplate 实例，用于执行与RabbitMQ相关的操作
     */
    @Bean
    public RabbitTemplate rabbitTemplate() {
        RabbitTemplate template = new RabbitTemplate(connectionFactory());
        // 添加重试策略，用于处理发送消息时出现的异常
        RetryTemplate retryTemplate = new RetryTemplate();
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(500);
        backOffPolicy.setMultiplier(10.0);
        backOffPolicy.setMaxInterval(10000);
        retryTemplate.setBackOffPolicy(backOffPolicy);
        template.setRetryTemplate(retryTemplate);
        template.setMessageConverter(new Jackson2JsonMessageConverter());
        return template;
    }

    /**
     * 创建deepseek请求队列
     */
    @Bean
    public Queue deepseekRequestsQueue() {
        return new Queue(RabbitMqConsistent.QUEUE_NAME, true);
    }

    /**
     * 用于转换消息格式
     * @return
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitListenerContainerFactory<?> rabbitListenerContainerFactory() {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory());
        factory.setMessageConverter(jsonMessageConverter());
        return factory;
    }
}
