package com.deepak.usermanagement.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
public class LoginAttemptService {
    public static final Logger LOG = LoggerFactory.getLogger(LoginAttemptService.class);
    public static final int MAXIMUM_NUMBER_OF_ATTEMPTS = 5;
    public static final int ATTEMPT_INCREMENT = 1;
    private final LoadingCache<String, Integer> loginAttemptCache;

    public LoginAttemptService() {
        CacheLoader loader = new CacheLoader<String, Integer>() {
            @Override
            public Integer load(String key) {
                return 0;
            }
        };
        loginAttemptCache = CacheBuilder.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .maximumSize(100)
                .build(loader);

    }

    public void evictUserFromCache(String username) {
        loginAttemptCache.invalidate(username);
    }

    public void addUserToCache(String username) throws ExecutionException {
        int attempts = 0;
        attempts = ATTEMPT_INCREMENT + loginAttemptCache.get(username);
        loginAttemptCache.put(username, attempts);
    }

    public boolean hasExceedMaxAttempts(String username) throws ExecutionException {
        return loginAttemptCache.get(username) >= MAXIMUM_NUMBER_OF_ATTEMPTS;
    }
}
