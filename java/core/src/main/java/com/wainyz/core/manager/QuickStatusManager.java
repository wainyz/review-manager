package com.wainyz.core.manager;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 管理是否得到处理的状态，可以使用redis代替
 */
@Component
public class QuickStatusManager {
    // 这个通过MessageSender和rabbitConsumer共同控制，计算得到。
    public static int average_response_time_seconds = 20;
    public static Instant last_calculate_time = Instant.now();
    public static Duration calculate_duration = Duration.ofSeconds(60);
    public static int calculate_times = 2;
    //---------------------0----------------
    private final Map<String, Instant> statusMap = new ConcurrentHashMap<>();
    /**
     * 只有经常问的才保留记录，否则会释放。当状态被加入到statusMap，也会同时被加到clearMap中，
     * 每到一个周期，就会根据clearMap中的数据进行清除，只有在这段时间内被访问过，才会被保留。
     * 但是如果时间超过了最大时间，那么也会被清除。
     */
    public static Duration max_live_time = Duration.ofMinutes(10);
    //public static Duration clear_status_time = Duration.ofMinutes(1);
    //=======================0======================
    //-----------------------1-----------------------
    public void setStatus(String userId){
        statusMap.put(userId, Instant.now());
    }
    public boolean getStatus(String userId){
        if (statusMap.containsKey(userId)) {
            statusMap.remove(userId);
            return true;
        }
        return false;
    }
    //======================1========================
    //-----------------------2-----------------------
    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.MINUTES)
    public void clearStatus(){
        statusMap.forEach((k, v) -> {
            if (Duration.between(v, Instant.now()).compareTo(max_live_time) > 0) {
                statusMap.remove(k);
            }
        });
    }
    //======================2=========================
    //-----------------------3-----------------------
    // 设置计算平均响应时间的逻辑
    /**
     * 上次计算的时间
     */
    private Instant last_record_time = Instant.now();
    private Duration sumDuration = Duration.ZERO;
    private final Map<String, Instant> recordMap = new ConcurrentHashMap<>(calculate_times);
    public boolean shouldBeginRecordSend(){
        return Instant.now().isAfter(last_record_time.plus(calculate_duration)) && recordMap.size()<calculate_times;
    }
    public boolean shouldBeginRecordReceiver(){
        return Instant.now().isAfter(last_record_time.plus(calculate_duration)) ;
    }
    public void sendRecord(String messageId){
        recordMap.put(messageId, Instant.now());
    }
    public void receiverRecord(String messageId){
        if(recordMap.containsKey(messageId)){
            recordMap.remove(messageId);
            sumDuration = sumDuration.plus(Duration.between(recordMap.get(messageId), Instant.now()));
        }else{
            last_record_time = Instant.now();
            average_response_time_seconds = (int) (sumDuration.getSeconds() / recordMap.size());
            sumDuration = Duration.ZERO;
        }
    }
    //======================3=========================
}
