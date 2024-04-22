package com.tugalsan.api.thread.server.sync;

import com.tugalsan.api.callable.client.TGS_CallableType2;
import com.tugalsan.api.unsafe.client.TGS_UnSafe;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class TS_ThreadSyncCallType2<R, A, B> {

    private TS_ThreadSyncCallType2(TGS_CallableType2<R, A, B> call) {
        this.call = call;
    }
    final private ReentrantLock lock = new ReentrantLock();
    final private TGS_CallableType2<R, A, B> call;

    public static <R, A, B> TS_ThreadSyncCallType2<R, A, B> of(TGS_CallableType2<R, A, B> call) {
        return new TS_ThreadSyncCallType2(call);
    }

    public R call(A inputA, B inputB) {
        return TGS_UnSafe.call(() -> {
            if (!lock.tryLock()) {
                return null;
            }
            return TGS_UnSafe.call(() -> call.call(inputA, inputB),
                    ex -> {
                        TGS_UnSafe.thrw(ex);
                        return null;
                    }, () -> lock.unlock());
        }, e -> null);
    }

    public R callUntil(Duration timeout, A inputA, B inputB) {
        return TGS_UnSafe.call(() -> {
            if (!lock.tryLock(timeout.toSeconds(), TimeUnit.SECONDS)) {
                return null;
            }
            return TGS_UnSafe.call(() -> call.call(inputA, inputB),
                    ex -> {
                        TGS_UnSafe.thrw(ex);
                        return null;
                    }, () -> lock.unlock());
        }, e -> null);
    }
}
