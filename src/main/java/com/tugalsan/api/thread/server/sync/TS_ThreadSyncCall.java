package com.tugalsan.api.thread.server.sync;

import com.tugalsan.api.unsafe.client.TGS_UnSafe;
import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class TS_ThreadSyncCall<R> {

    private TS_ThreadSyncCall(Callable<R> call) {
        this.call = call;
    }
    final private ReentrantLock lock = new ReentrantLock();
    final private Callable<R> call;

    public static <R> TS_ThreadSyncCall<R> of(Callable<R> call) {
        return new TS_ThreadSyncCall(call);
    }

    public R call() {
        return TGS_UnSafe.call(() -> {
            if (!lock.tryLock()) {
                return null;
            }
            return TGS_UnSafe.call(() -> call.call(),
                    ex -> {
                        TGS_UnSafe.thrw(ex);
                        return null;
                    }, () -> lock.unlock());
        }, e -> null);
    }

    public R callUntil(Duration timeout) {
        return TGS_UnSafe.call(() -> {
            if (!lock.tryLock(timeout.toSeconds(), TimeUnit.SECONDS)) {
                return null;
            }
            return TGS_UnSafe.call(() -> call.call(),
                    ex -> {
                        TGS_UnSafe.thrw(ex);
                        return null;
                    }, () -> lock.unlock());
        }, e -> null);
    }
}
