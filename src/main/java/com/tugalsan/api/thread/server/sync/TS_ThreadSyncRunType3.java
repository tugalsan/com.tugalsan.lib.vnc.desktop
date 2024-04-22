package com.tugalsan.api.thread.server.sync;

import com.tugalsan.api.runnable.client.TGS_RunnableType3;
import com.tugalsan.api.stream.client.TGS_StreamUtils;
import com.tugalsan.api.unsafe.client.TGS_UnSafe;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class TS_ThreadSyncRunType3<A, B, C> {

    private TS_ThreadSyncRunType3(TGS_RunnableType3<A, B, C> run) {
        this.run = run;
    }
    final private ReentrantLock lock = new ReentrantLock();
    final private TGS_RunnableType3<A, B, C> run;

    public static <A, B, C> TS_ThreadSyncRunType3<A, B, C> of(TGS_RunnableType3<A, B, C> run) {
        return new TS_ThreadSyncRunType3(run);
    }

    public void run(A inputA, B inputB, C inputC) {
        TGS_UnSafe.run(() -> {
            if (!lock.tryLock()) {
                return;
            }
            TGS_UnSafe.run(() -> run.run(inputA, inputB, inputC), ex -> TGS_UnSafe.thrw(ex), () -> lock.unlock());
        }, e -> TGS_StreamUtils.runNothing());
    }

    public void runUntil(Duration timeout, A inputA, B inputB, C inputC) {
        TGS_UnSafe.run(() -> {
            if (!lock.tryLock(timeout.toSeconds(), TimeUnit.SECONDS)) {
                return;
            }
            TGS_UnSafe.run(() -> run.run(inputA, inputB, inputC), ex -> TGS_UnSafe.thrw(ex), () -> lock.unlock());
        }, e -> TGS_StreamUtils.runNothing());
    }
}
