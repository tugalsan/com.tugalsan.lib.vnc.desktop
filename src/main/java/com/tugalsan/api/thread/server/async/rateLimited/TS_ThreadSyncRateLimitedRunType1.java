package com.tugalsan.api.thread.server.async.rateLimited;

import com.tugalsan.api.runnable.client.TGS_RunnableType1;
import com.tugalsan.api.unsafe.client.TGS_UnSafe;

import java.time.Duration;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class TS_ThreadSyncRateLimitedRunType1<A> {

    private TS_ThreadSyncRateLimitedRunType1(Semaphore lock) {
        this.lock = lock;
    }
    final private Semaphore lock;

    public static <A> TS_ThreadSyncRateLimitedRunType1<A> of(Semaphore lock) {
        return new TS_ThreadSyncRateLimitedRunType1(lock);
    }

    public static <A> TS_ThreadSyncRateLimitedRunType1<A> of(int simultaneouslyCount) {
        return of(new Semaphore(simultaneouslyCount));
    }

    public void run(TGS_RunnableType1<A> run, A inputA) {
        runUntil(run, null, inputA);
    }

    public void runUntil(TGS_RunnableType1<A> run, Duration timeout, A inputA) {
        try {
            if (timeout == null) {
                lock.acquire();
            } else {
                if (!lock.tryAcquire(timeout.toSeconds(), TimeUnit.SECONDS)) {
                    return;
                }
            }
            run.run(inputA);
        } catch (InterruptedException ex) {
            TGS_UnSafe.throwIfInterruptedException(ex);
        } finally {
            lock.release();
        }
    }
}
