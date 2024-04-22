package com.tugalsan.api.thread.server.sync;

import com.tugalsan.api.callable.client.TGS_CallableType3;
import com.tugalsan.api.unsafe.client.TGS_UnSafe;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class TS_ThreadSyncCallType3<R, A, B, C> {

    private TS_ThreadSyncCallType3(TGS_CallableType3<R, A, B, C> call) {
        this.call = call;
    }
    final private ReentrantLock lock = new ReentrantLock();
    final private TGS_CallableType3<R, A, B, C> call;

    public static <R, A, B, C> TS_ThreadSyncCallType3<R, A, B, C> of(TGS_CallableType3<R, A, B, C> call) {
        return new TS_ThreadSyncCallType3(call);
    }

    public R call(A inputA, B inputB, C inputC) {
        return TGS_UnSafe.call(() -> {
            if (!lock.tryLock()) {
                return null;
            }
            return TGS_UnSafe.call(() -> call.call(inputA, inputB, inputC),
                    ex -> {
                        TGS_UnSafe.thrw(ex);
                        return null;
                    }, () -> lock.unlock());
        }, e -> null);
    }

    public R callUntil(Duration timeout, A inputA, B inputB, C inputC) {
        return TGS_UnSafe.call(() -> {
            if (!lock.tryLock(timeout.toSeconds(), TimeUnit.SECONDS)) {
                return null;
            }
            return TGS_UnSafe.call(() -> call.call(inputA, inputB, inputC),
                    ex -> {
                        TGS_UnSafe.thrw(ex);
                        return null;
                    }, () -> lock.unlock());
        }, e -> null);
    }
}
