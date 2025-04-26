package com.wainyz.core.Serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.Instant;

/**
 * 用于在json字符串转instant 类型属性的时候使用
 * @author Yanion_gwgzh
 */
public class InstantToTimestampSerializer extends JsonSerializer<Instant> {
    @Override
    public void serialize(Instant value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        // 获取秒数和纳秒数
        long seconds = value.getEpochSecond();
        // 写入时间戳到 JSON
        gen.writeNumber(seconds);
    }
}