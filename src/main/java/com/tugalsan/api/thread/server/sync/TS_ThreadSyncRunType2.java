package com.tugalsan.api.thread.server.sync;

import com.tugalsan.api.runnable.client.TGS_RunnableType2;
import com.tugalsan.api.stream.client.TGS_StreamUtils;
import com.tugalsan.api.unsafe.client.TGS_UnSafe;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class TS_ThreadSyncRunType2<A, B> {

    private TS_ThreadSyncRunType2(TGS_RunnableType2<A, B> run) {
        this.run = run;
    }
    final private ReentrantLock lock = new ReentrantLock();
    final private TGS_RunnableType2<A, B> run;

    public static <A, B> TS_ThreadSyncRunType2<A, B> of(TGS_RunnableType2<A, B> run) {
        return new TS_ThreadSyncRunType2(run);
    }

    public void run(A inputA, B inputB) {
        TGS_UnSafe.run(() -> {
            if (!lock.tryLock()) {
                return;
            }
            TGS_UnSafe.run(() -> run.run(inputA, inputB), ex -> TGS_UnSafe.thrw(ex), () -> lock.unlock());
        }, e -> TGS_StreamUtils.runNothing());
    }

    public void runUntil(Duration timeout, A inputA, B inputB) {
        TGS_UnSafe.run(() -> {
            if (!lock.tryLock(timeout.toSeconds(), TimeUnit.SECONDS)) {
                return;
            }
            TGS_UnSafe.run(() -> run.run(inputA, inputB), ex -> TGS_UnSafe.thrw(ex), () -> lock.unlock());
        }, e -> TGS_StreamUtils.runNothing());
    }
}
