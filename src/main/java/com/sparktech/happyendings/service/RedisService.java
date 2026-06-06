package com.sparktech.happyendings.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class RedisService {

    private static final Logger log = LoggerFactory.getLogger(RedisService.class);

    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // In-memory fallback map if Redis is not running or accessible
    private final ConcurrentHashMap<String, String> localCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> localExpiry = new ConcurrentHashMap<>();

    public void set(String key, Object value, long ttlSeconds) {
        try {
            String json = objectMapper.writeValueAsString(value);
            if (redisTemplate != null) {
                redisTemplate.opsForValue().set(key, json, ttlSeconds, TimeUnit.SECONDS);
            } else {
                localCache.put(key, json);
                localExpiry.put(key, System.currentTimeMillis() + (ttlSeconds * 1000));
            }
        } catch (Exception e) {
            log.error("Failed to write to cache for key: {}", key, e);
        }
    }

    public <T> T get(String key, Class<T> clazz) {
        try {
            String json;
            if (redisTemplate != null) {
                json = redisTemplate.opsForValue().get(key);
            } else {
                if (localExpiry.containsKey(key) && localExpiry.get(key) < System.currentTimeMillis()) {
                    localCache.remove(key);
                    localExpiry.remove(key);
                }
                json = localCache.get(key);
            }

            if (json == null) {
                return null;
            }
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            log.error("Failed to read from cache for key: {}", key, e);
            return null;
        }
    }

    public void delete(String key) {
        try {
            if (redisTemplate != null) {
                redisTemplate.delete(key);
            } else {
                localCache.remove(key);
                localExpiry.remove(key);
            }
        } catch (Exception e) {
            log.error("Failed to delete key: {}", key, e);
        }
    }

    public void blacklistToken(String token, long expiryMs) {
        String key = "blacklist:jwt:" + token;
        try {
            if (redisTemplate != null) {
                redisTemplate.opsForValue().set(key, "blacklisted", expiryMs, TimeUnit.MILLISECONDS);
            } else {
                localCache.put(key, "blacklisted");
                localExpiry.put(key, System.currentTimeMillis() + expiryMs);
            }
            log.info("Blacklisted token: {} with TTL: {} ms", token.substring(0, Math.min(token.length(), 15)) + "...", expiryMs);
        } catch (Exception e) {
            log.error("Failed to blacklist token", e);
        }
    }

    public boolean isTokenBlacklisted(String token) {
        String key = "blacklist:jwt:" + token;
        try {
            if (redisTemplate != null) {
                return Boolean.TRUE.equals(redisTemplate.hasKey(key));
            } else {
                if (localExpiry.containsKey(key) && localExpiry.get(key) < System.currentTimeMillis()) {
                    localCache.remove(key);
                    localExpiry.remove(key);
                    return false;
                }
                return localCache.containsKey(key);
            }
        } catch (Exception e) {
            log.error("Failed to check token blacklist status", e);
            return false;
        }
    }

    /**
     * Checks if a request should be rate limited under sliding window configuration.
     * key: client identifier + endpoint (e.g., ratelimit:192.168.1.1:/api/rsvp)
     * limit: max requests allowed
     * windowSeconds: size of the rate limit window
     */
    public boolean isRateLimited(String key, int limit, int windowSeconds) {
        try {
            if (redisTemplate != null) {
                Long count = redisTemplate.opsForValue().increment(key);
                if (count != null && count == 1) {
                    redisTemplate.expire(key, windowSeconds, TimeUnit.SECONDS);
                }
                return count != null && count > limit;
            } else {
                String val = localCache.get(key);
                int count = val == null ? 0 : Integer.parseInt(val);
                count++;
                localCache.put(key, String.valueOf(count));
                if (count == 1) {
                    localExpiry.put(key, System.currentTimeMillis() + (windowSeconds * 1000));
                }
                return count > limit;
            }
        } catch (Exception e) {
            log.error("Failed to run rate limiting check for key: {}", key, e);
            return false;
        }
    }
}
