package com.tugalsan.api.thread.server.async.rateLimited;

import com.tugalsan.api.runnable.client.TGS_Runnable;
import com.tugalsan.api.unsafe.client.TGS_UnSafe;

import java.time.Duration;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class TS_ThreadSyncRateLimitedRun {

    private TS_ThreadSyncRateLimitedRun(Semaphore lock) {
        this.lock = lock;
    }
    final private Semaphore lock;

    public static TS_ThreadSyncRateLimitedRun of(Semaphore lock) {
        return new TS_ThreadSyncRateLimitedRun(lock);
    }

    public static TS_ThreadSyncRateLimitedRun of(int simultaneouslyCount) {
        return of(new Semaphore(simultaneouslyCount));
    }

    public void run(TGS_Runnable run) {
        runUntil(run, null);
    }

    public void runUntil(TGS_Runnable run, Duration timeout) {
        try {
            if (timeout == null) {
                lock.acquire();
            } else {
                if (!lock.tryAcquire(timeout.toSeconds(), TimeUnit.SECONDS)) {
                    return;
                }
            }
            run.run();
        } catch (InterruptedException ex) {
            TGS_UnSafe.throwIfInterruptedException(ex);
        } finally {
            lock.release();
        }
    }
}
