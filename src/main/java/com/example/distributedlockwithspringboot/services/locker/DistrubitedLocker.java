package com.example.distributedlockwithspringboot.services.locker;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
@Service
public class DistrubitedLocker {

    private static final long DEFAULT_RETRY_TIME = 100L;
    private ValueOperations<String, String> valueOps;

    public DistrubitedLocker(final RedisTemplate redisTemplate) {
        this.valueOps = redisTemplate.opsForValue();
    }

    public <T> LockExecutionResult<T> lock(
            final String key,
            final int howLongShouldLockAcquireInSeconds,
            final int lockTimeoutSecond,
            final Callable<T> task
        ){
        try {
            return tryToGetLock(() -> {
                final boolean lockAcquired = this.valueOps.setIfAbsent(key, key, lockTimeoutSecond, TimeUnit.SECONDS);

                if (lockAcquired == false) {
                    log.error("Failed to acquire lock for key {}", key);
                    return null;
                }

                log.info("Successful acquire lock for key {}", key);

                try {
                    T taskResult = task.call();
                    return LockExecutionResult.buildLockAcquiredResult(taskResult);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    return LockExecutionResult.buildLockAcquireWithException(e);
                } finally {
                    releaseLock(key);
                }

            }, key, howLongShouldLockAcquireInSeconds);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return LockExecutionResult.buildLockAcquireWithException(ex);
        }
    }

    private void releaseLock(final String key) {
        this.valueOps.getOperations().delete(key);
    }

    private static <T> T tryToGetLock(
            final Supplier<T> task,
            final String lockKey,
            final int howLongShouldLockAcquireInSeconds
            ) throws Exception {
        final long tryToGetLogTimeOut = TimeUnit.SECONDS.toMillis(howLongShouldLockAcquireInSeconds);

        final long startTimestamp = System.currentTimeMillis();
        while (true) {
            log.info("try to get the lock with key: {} ", lockKey);
            final T response = task.get();
            if (response != null) {
                return response;
            }

            Thread.sleep(DEFAULT_RETRY_TIME);
            if (System.currentTimeMillis() - startTimestamp > tryToGetLogTimeOut) {
                throw new Exception("Failed to acquire lock in " + tryToGetLogTimeOut +" milliseconds");
            }
        }
    }
}
