package com.tugalsan.api.thread.server.sync;

import com.tugalsan.api.callable.client.TGS_CallableType4;
import com.tugalsan.api.unsafe.client.TGS_UnSafe;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class TS_ThreadSyncCallType4<R, A, B, C, D> {

    private TS_ThreadSyncCallType4(TGS_CallableType4<R, A, B, C, D> call) {
        this.call = call;
    }
    final private ReentrantLock lock = new ReentrantLock();
    final private TGS_CallableType4<R, A, B, C, D> call;

    public static <R, A, B, C, D> TS_ThreadSyncCallType4<R, A, B, C, D> of(TGS_CallableType4<R, A, B, C, D> call) {
        return new TS_ThreadSyncCallType4(call);
    }

    public R call(A inputA, B inputB, C inputC, D inputD) {
        return TGS_UnSafe.call(() -> {
            if (!lock.tryLock()) {
                return null;
            }
            return TGS_UnSafe.call(() -> call.call(inputA, inputB, inputC, inputD),
                    ex -> {
                        TGS_UnSafe.thrw(ex);
                        return null;
                    }, () -> lock.unlock());
        }, e -> null);
    }

    public R callUntil(Duration timeout, A inputA, B inputB, C inputC, D inputD) {
        return TGS_UnSafe.call(() -> {
            if (!lock.tryLock(timeout.toSeconds(), TimeUnit.SECONDS)) {
                return null;
            }
            return TGS_UnSafe.call(() -> call.call(inputA, inputB, inputC, inputD),
                    ex -> {
                        TGS_UnSafe.thrw(ex);
                        return null;
                    }, () -> lock.unlock());
        }, e -> null);
    }
}
