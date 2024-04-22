package com.tugalsan.api.thread.server.sync;

import com.tugalsan.api.callable.client.TGS_CallableType1;
import com.tugalsan.api.unsafe.client.TGS_UnSafe;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class TS_ThreadSyncCallType1<R, A> {

    private TS_ThreadSyncCallType1(TGS_CallableType1<R, A> call) {
        this.call = call;
    }
    final private ReentrantLock lock = new ReentrantLock();
    final private TGS_CallableType1<R, A> call;

    public static <R, A> TS_ThreadSyncCallType1<R, A> of(TGS_CallableType1<R, A> call) {
        return new TS_ThreadSyncCallType1(call);
    }

    public R call(A inputA) {
        return TGS_UnSafe.call(() -> {
            if (!lock.tryLock()) {
                return null;
            }
            return TGS_UnSafe.call(() -> call.call(inputA),
                    ex -> {
                        TGS_UnSafe.thrw(ex);
                        return null;
                    }, () -> lock.unlock());
        }, e -> null);
    }

    public R callUntil(Duration timeout, A inputA) {
        return TGS_UnSafe.call(() -> {
            if (!lock.tryLock(timeout.toSeconds(), TimeUnit.SECONDS)) {
                return null;
            }
            return TGS_UnSafe.call(() -> call.call(inputA),
                    ex -> {
                        TGS_UnSafe.thrw(ex);
                        return null;
                    }, () -> lock.unlock());
        }, e -> null);
    }
}
