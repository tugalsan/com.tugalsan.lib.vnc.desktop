package com.tugalsan.api.thread.server.sync.lockLimited;

import com.tugalsan.api.runnable.client.TGS_RunnableType5;
import com.tugalsan.api.unsafe.client.TGS_UnSafe;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class TS_ThreadSyncLockLimitedRunType5<A, B, C, D, E> {

    private TS_ThreadSyncLockLimitedRunType5(ReentrantLock lock) {
        this.lock = lock;
    }
    final private ReentrantLock lock;

    public static <A, B, C, D, E> TS_ThreadSyncLockLimitedRunType5<A, B, C, D, E> of(ReentrantLock lock) {
        return new TS_ThreadSyncLockLimitedRunType5(lock);
    }

    public static <A, B, C, D, E> TS_ThreadSyncLockLimitedRunType5<A, B, C, D, E> of() {
        return of(new ReentrantLock());
    }

    public void run(TGS_RunnableType5<A, B, C, D, E> run, A inputA, B inputB, C inputC, D inputD, E inputE) {
        runUntil(run, null, inputA, inputB, inputC, inputD, inputE);
    }

    public void runUntil(TGS_RunnableType5<A, B, C, D, E> run, Duration timeout, A inputA, B inputB, C inputC, D inputD, E inputE) {
        try {
            if (timeout == null) {
                lock.lock();
            } else {
                if (!lock.tryLock(timeout.toSeconds(), TimeUnit.SECONDS)) {
                    return;
                }
            }
            run.run(inputA, inputB, inputC, inputD, inputE);
        } catch (InterruptedException ex) {
            TGS_UnSafe.throwIfInterruptedException(ex);
        } finally {
            lock.unlock();
        }
    }
}
