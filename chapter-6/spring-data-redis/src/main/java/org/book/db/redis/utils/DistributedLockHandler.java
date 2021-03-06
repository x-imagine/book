package org.book.db.redis.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

@Component
public class DistributedLockHandler {

    public final static long LOCK_EXPIRE = 30 * 1000L;
    private final static long LOCK_TRY_INTERVAL = 30L;
    private final static long LOCK_TRY_TIMEOUT = 20 * 1000L;

    @Autowired
    private RedisTemplate redisTemplate;

    public boolean getLock(String key, String value) {
        try {
            if (StringUtils.isEmpty(key) || StringUtils.isEmpty(value)) {
                return false;
            }
            long startTime = System.currentTimeMillis();
            do {
                if (!redisTemplate.hasKey(key)) {
                    redisTemplate.opsForValue().set(key, value, LOCK_EXPIRE, TimeUnit.MILLISECONDS);
                    return true;
                }
                if (System.currentTimeMillis() - startTime > LOCK_TRY_TIMEOUT) {
                    return false;
                }
                Thread.sleep(LOCK_TRY_INTERVAL);
            } while (redisTemplate.hasKey(key));
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public void releaseLock(String key) {
        if (!StringUtils.isEmpty(key)) {
            redisTemplate.delete(key);
        }
    }
}
