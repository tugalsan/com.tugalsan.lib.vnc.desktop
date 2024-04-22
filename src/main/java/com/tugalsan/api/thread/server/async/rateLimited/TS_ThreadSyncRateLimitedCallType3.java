package com.tugalsan.api.thread.server.async.rateLimited;

import com.tugalsan.api.callable.client.TGS_CallableType3;
import com.tugalsan.api.union.client.TGS_UnionExcuse;
import com.tugalsan.api.unsafe.client.TGS_UnSafe;

import java.time.Duration;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class TS_ThreadSyncRateLimitedCallType3<R, A, B, C> {

    private TS_ThreadSyncRateLimitedCallType3(Semaphore lock) {
        this.lock = lock;
    }
    final private Semaphore lock;

    public static <R, A, B, C> TS_ThreadSyncRateLimitedCallType3<R, A, B, C> of(Semaphore lock) {
        return new TS_ThreadSyncRateLimitedCallType3(lock);
    }

    public static <R, A, B, C> TS_ThreadSyncRateLimitedCallType3<R, A, B, C> of(int simultaneouslyCount) {
        return of(new Semaphore(simultaneouslyCount));
    }

    public TGS_UnionExcuse<R> call(TGS_CallableType3<R, A, B, C> call, A inputA, B inputB, C inputC) {
        return callUntil(call, null, inputA, inputB, inputC);
    }

    public TGS_UnionExcuse<R> callUntil(TGS_CallableType3<R, A, B, C> call, Duration timeout, A inputA, B inputB, C inputC) {
        try {
            if (timeout == null) {
                lock.acquire();
            } else {
                if (!lock.tryAcquire(timeout.toSeconds(), TimeUnit.SECONDS)) {
                    return TGS_UnionExcuse.ofEmpty_NullPointerException();
                }
            }
            return TGS_UnionExcuse.of(call.call(inputA, inputB, inputC));
        } catch (InterruptedException ex) {
            return TGS_UnSafe.throwIfInterruptedException(ex);
        } finally {
            lock.release();
        }
    }
}
