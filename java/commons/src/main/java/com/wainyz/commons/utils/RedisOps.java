package com.wainyz.commons.utils;

import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.temporal.TemporalAmount;
import java.util.concurrent.TimeUnit;

/**
 * @author Yanion_gwgzh
 */
@Component
public class RedisOps {

    private final RedisTemplate<String, String> redisTemplate;
    public RedisOps (@Autowired RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    public void set(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }
    public void setAndExpire(String key, String value, long timeout, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
    }
    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }
    public Boolean expire(String key, long timeout, TimeUnit timeUnit) {
        return Boolean.TRUE.equals(redisTemplate.expire(key, timeout, timeUnit));
    }
    public Boolean delete(String key) {
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }
    //-----------------2------------------
    public enum RedisKeyPrefix{
        Register_Email("emailCode:");
        RedisKeyPrefix(String prefix) {
            this.prefix = prefix;
        }
        @JsonValue
        public final String prefix;
    }
    /**
     * 更多不容易错的方法(包括前缀混淆，前缀冲突等问题)
     */
    public void set(RedisKeyPrefix prefix, String key, String value){
        set(prefix.prefix + key, value);
    }
    public void setAndExpire(RedisKeyPrefix prefix, String key, String value, long timeout, TimeUnit timeUnit){
        setAndExpire(prefix.prefix + key, value, timeout, timeUnit);
    }
    public String get(RedisKeyPrefix prefix, String key){
        return get(prefix.prefix + key);
    }
    public Boolean expire(RedisKeyPrefix prefix, String key, long timeout, TimeUnit timeUnit){
        return expire(prefix.prefix + key, timeout, timeUnit);
    }
    public Boolean delete(RedisKeyPrefix prefix, String key){
        return delete(prefix.prefix + key);
    }
}
