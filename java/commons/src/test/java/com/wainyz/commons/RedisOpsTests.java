package com.wainyz.commons;

import com.wainyz.commons.utils.RedisOps;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class RedisOpsTests {
    @Autowired
    RedisOps redisOps;
    /**
     * 测试是否能正常操作redis
     */
    @Test
    public void testRedisOps() {
        // 计划先存一个 1 然后 再取出来
        try {
            redisOps.set("1", "1");
            assert redisOps.get("1").equals("1");
        }finally {
            // 删除
            redisOps.delete("1");
            assert redisOps.get("1") == null;
        }
    }
}
