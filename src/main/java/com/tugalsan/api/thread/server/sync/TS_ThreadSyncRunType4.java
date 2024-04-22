package com.tugalsan.api.thread.server.sync;

import com.tugalsan.api.runnable.client.TGS_RunnableType4;
import com.tugalsan.api.stream.client.TGS_StreamUtils;
import com.tugalsan.api.unsafe.client.TGS_UnSafe;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class TS_ThreadSyncRunType4<A, B, C, D> {

    private TS_ThreadSyncRunType4(TGS_RunnableType4<A, B, C, D> run) {
        this.run = run;
    }
    final private ReentrantLock lock = new ReentrantLock();
    final private TGS_RunnableType4<A, B, C, D> run;

    public static <A, B, C, D> TS_ThreadSyncRunType4<A, B, C, D> of(TGS_RunnableType4<A, B, C, D> run) {
        return new TS_ThreadSyncRunType4(run);
    }

    public void run(A inputA, B inputB, C inputC, D inputD) {
        TGS_UnSafe.run(() -> {
            if (!lock.tryLock()) {
                return;
            }
            TGS_UnSafe.run(() -> run.run(inputA, inputB, inputC, inputD), ex -> TGS_UnSafe.thrw(ex), () -> lock.unlock());
        }, e -> TGS_StreamUtils.runNothing());
    }

    public void runUntil(Duration timeout, A inputA, B inputB, C inputC, D inputD) {
        TGS_UnSafe.run(() -> {
            if (!lock.tryLock(timeout.toSeconds(), TimeUnit.SECONDS)) {
                return;
            }
            TGS_UnSafe.run(() -> run.run(inputA, inputB, inputC, inputD), ex -> TGS_UnSafe.thrw(ex), () -> lock.unlock());
        }, e -> TGS_StreamUtils.runNothing());
    }
}
