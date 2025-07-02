package com.wainyz.core.spring_ai;

import com.wainyz.core.pojo.domain.DeepSeekRequestDO;
import com.wainyz.core.pojo.domain.DeepSeekResponse;
import org.springframework.stereotype.Component;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 管理调用模型的相关任务
 */
@Component
public class SpringAiManager {
    private static final AtomicInteger THREAD_NAME_NUMBER = new AtomicInteger(0);
    /**
     * 取用以及返回结果所用到的线程池
     */
    private final static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5, 10, 1, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(100),
            r -> {
                Thread thread = new Thread(r);
                thread.setName("Spring Ai Manager ThreadPool: Thread"+THREAD_NAME_NUMBER.getAndIncrement());
                return thread;
            } );

    /**
     * 用于调用大模型，得到返回值
     * @param deepSeekRequestDO 模型调用请求
     * @return 模型返回的结果
     */
    public static DeepSeekResponse useAi(DeepSeekRequestDO deepSeekRequestDO){
        return new DeepSeekResponse();
    }

}