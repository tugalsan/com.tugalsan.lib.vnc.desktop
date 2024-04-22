package com.tugalsan.api.thread.server.sync.lockLimited;

import com.tugalsan.api.callable.client.TGS_CallableType4;
import com.tugalsan.api.union.client.TGS_UnionExcuse;
import com.tugalsan.api.unsafe.client.TGS_UnSafe;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class TS_ThreadSyncLockLimitedCallType4<R, A, B, C, D> {

    private TS_ThreadSyncLockLimitedCallType4(ReentrantLock lock) {
        this.lock = lock;
    }
    final private ReentrantLock lock;

    public static <R, A, B, C, D> TS_ThreadSyncLockLimitedCallType4<R, A, B, C, D> of(ReentrantLock lock) {
        return new TS_ThreadSyncLockLimitedCallType4(lock);
    }

    public static <R, A, B, C, D> TS_ThreadSyncLockLimitedCallType4<R, A, B, C, D> of() {
        return of(new ReentrantLock());
    }

    public TGS_UnionExcuse<R> call(TGS_CallableType4<R, A, B, C, D> call, A inputA, B inputB, C inputC, D inputD) {
        return callUntil(call, null, inputA, inputB, inputC, inputD);
    }

    public TGS_UnionExcuse<R> callUntil(TGS_CallableType4<R, A, B, C, D> call, Duration timeout, A inputA, B inputB, C inputC, D inputD) {
        try {
            if (timeout == null) {
                lock.lock();
            } else {
                if (!lock.tryLock(timeout.toSeconds(), TimeUnit.SECONDS)) {
                    return TGS_UnionExcuse.ofEmpty_NullPointerException();
                }
            }
            return TGS_UnionExcuse.of(call.call(inputA, inputB, inputC, inputD));
        } catch (InterruptedException ex) {
            return TGS_UnSafe.throwIfInterruptedException(ex);
        } finally {
            lock.unlock();
        }
    }
}
