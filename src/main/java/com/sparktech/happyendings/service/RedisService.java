package com.sparktech.happyendings.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void set(String key, Object value, long timeoutInSeconds) {
        redisTemplate.opsForValue().set(key, value, timeoutInSeconds, TimeUnit.SECONDS);
    }

    public <T> T get(String key, Class<T> type) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value != null && type.isInstance(value)) {
            return type.cast(value);
        }
        return null;
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public boolean acquireLock(String lockKey, long timeoutInSeconds) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(lockKey, "LOCKED", timeoutInSeconds, TimeUnit.SECONDS));
    }

    public void releaseLock(String lockKey) {
        redisTemplate.delete(lockKey);
    }

    public void blacklistToken(String token, long timeoutInMs) {
        redisTemplate.opsForValue().set("blacklist:" + token, "BLACKLISTED", timeoutInMs, TimeUnit.MILLISECONDS);
    }

    public boolean isTokenBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:" + token));
    }

    public boolean isRateLimited(String key, int limit, int windowInSeconds) {
        Long current = redisTemplate.opsForValue().increment(key);
        if (current == null) {
            return false;
        }
        if (current == 1) {
            redisTemplate.expire(key, windowInSeconds, TimeUnit.SECONDS);
        }
        return current > limit;
    }
}
