package com.wainyz.commons.utils;

import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
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
        Register_Email("emailCode:"), Friends_Message("friends:"), Class_Message( "class:");
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
    // --------------------- 有序集合（Sorted Set）操作 ---------------------
    /**
     * 检查 Key 是否存在
     */
    public Boolean exists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
    private static String friendsMessageContentTemplate = "${direct}:${type}:${content}";
    private static String classMessageContentTemplate = "${direct}:${type}:${content}";
    // 使用明确的分隔符和格式化，避免内容污染
    public static String buildFriendsMessageContent(Long senderId, Long receiverId, int type, String content) {
        int direct = (receiverId < senderId) ? 1 : 0;
        // 格式化为 direct:type:content，确保内容中的冒号被转义（可选）
        return String.format("%d:%d:%s", direct, type, content.replace(":", "\\:"));
    }
    public static String buildClassMessageContent(Long senderId, String name, String content) {
        // 格式化为 direct:type:content，确保内容中的冒号被转义（可选）
        return String.format("%d:%s:%s", senderId, name, content.replace(":", "\\:"));
    }
    // --------------------- List 顺序存储增强方法 ---------------------

    /**
     * 安全地向列表尾部追加元素（保留插入顺序）
     * 若 Key 不存在则创建列表（原子操作）
     *
     * @param key       Redis键
     * @param values    要追加的值（按顺序插入尾部）
     * @return 操作后列表长度
     */
    public Long safeAppendToList(String key, List<String> values) {
        String script =
                "local exists = redis.call('EXISTS', KEYS[1])\n" +
                        "for i, value in ipairs(ARGV) do\n" +
                        "    redis.call('RPUSH', KEYS[1], value)\n" +  // 尾部追加保证顺序
                        "end\n" +
                        "return redis.call('LLEN', KEYS[1])";          // 直接返回列表长度，不处理过期时间

        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(script, Long.class);

        return redisTemplate.execute(
                redisScript,
                Collections.singletonList(key),
                values.toArray()  // 参数仅为待插入的值
        );
    }

    /**
     * 分页查询列表（正序，保留插入顺序）
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页数量
     */
    public List<String> getListByPage(String key, int pageNum, int pageSize) {
        long start = (pageNum - 1) * pageSize;
        long end = start + pageSize - 1;
        return redisTemplate.opsForList().range(key, start, end);
    }

    /**
     * 获取列表全部内容（按插入顺序）
     */
    public List<String> getEntireList(String key) {
        return redisTemplate.opsForList().range(key, 0, -1);
    }

    /**
     * 获取列表长度
     */
    public Long getListSize(String key) {
        return redisTemplate.opsForList().size(key);
    }

    // --------------------- 带前缀的方法重载 ---------------------

    public Long safeAppendToList(RedisKeyPrefix prefix, String key, List<String> values, long timeout, TimeUnit timeUnit) {
        return safeAppendToList(prefix.prefix + key, values);
    }

    public List<String> getListByPage(RedisKeyPrefix prefix, String key, int pageNum, int pageSize) {
        return getListByPage(prefix.prefix + key, pageNum, pageSize);
    }

    public List<String> getEntireList(RedisKeyPrefix prefix, String key) {
        return getEntireList(prefix.prefix + key);
    }

    public Long getListSize(RedisKeyPrefix prefix, String key) {
        return getListSize(prefix.prefix + key);
    }

}
