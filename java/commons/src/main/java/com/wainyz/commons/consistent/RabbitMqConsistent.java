package com.wainyz.commons.consistent;

/**
 * 定义了mq中的一些名称
 * @author Yanion_gwgzh
 */
public class RabbitMqConsistent {
    /**
     * deepseek的请求队列名
     */
    public static final String QUEUE_NAME = "deepseek_requests";
    public static final String EXCHANGE_NAME = "deepseek_exchange";
    public static final String ROUTING_KEY = "deepseek_requests";
    public static final String DEEPSEEK_RESPONSE_QUEUE = "deepseek_response";
    public static final String DEEPSEEK_SCORING_RESPONSE_QUEUE = "deepseek_scoring_response";
}
