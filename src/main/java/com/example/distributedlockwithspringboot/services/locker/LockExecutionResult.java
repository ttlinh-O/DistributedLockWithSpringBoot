package com.example.distributedlockwithspringboot.services.locker;

public class LockExecutionResult <T> {
    private final boolean lockAcquired;
    public final T resultIfLockAcquired;
    public final Exception exception;

    private LockExecutionResult(boolean lockAcquired, T resultIfLockAcquired, final  Exception exception) {
        this.lockAcquired = lockAcquired;
        this.resultIfLockAcquired = resultIfLockAcquired;
        this.exception = exception;
    }

    public static <T> LockExecutionResult<T> buildLockAcquiredResult(final T resultIfLockAcquired) {
        return new LockExecutionResult<>(true, resultIfLockAcquired, null);
    }

    public static <T> LockExecutionResult<T> buildLockAcquireWithException(final Exception exception) {
        return new LockExecutionResult<>(true, null, exception);
    }

    public  static <T> LockExecutionResult<T> buildLockNotAcquired(final Boolean lockAcquired) {
        return new LockExecutionResult<>(lockAcquired, null, null);
    }

    public boolean isLockAcquired() {
        return this.lockAcquired;
    }

    public  T getResultIfLockAcquired() {
        return this.resultIfLockAcquired;
    }

    public Exception getException() {
        return this.exception;
    }
}
