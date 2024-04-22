package com.tugalsan.api.thread.server.sync;

import com.tugalsan.api.runnable.client.TGS_RunnableType1;
import com.tugalsan.api.stream.client.TGS_StreamUtils;
import com.tugalsan.api.unsafe.client.TGS_UnSafe;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class TS_ThreadSyncRunType1<A> {

    private TS_ThreadSyncRunType1(TGS_RunnableType1<A> run) {
        this.run = run;
    }
    final private ReentrantLock lock = new ReentrantLock();
    final private TGS_RunnableType1<A> run;

    public static <A> TS_ThreadSyncRunType1<A> of(TGS_RunnableType1<A> run) {
        return new TS_ThreadSyncRunType1(run);
    }

    public void run(A inputA) {
        TGS_UnSafe.run(() -> {
            if (!lock.tryLock()) {
                return;
            }
            TGS_UnSafe.run(() -> run.run(inputA), ex -> TGS_UnSafe.thrw(ex), () -> lock.unlock());
        }, e -> TGS_StreamUtils.runNothing());
    }

    public void runUntil(Duration timeout, A inputA) {
        TGS_UnSafe.run(() -> {
            if (!lock.tryLock(timeout.toSeconds(), TimeUnit.SECONDS)) {
                return;
            }
            TGS_UnSafe.run(() -> run.run(inputA), ex -> TGS_UnSafe.thrw(ex), () -> lock.unlock());
        }, e -> TGS_StreamUtils.runNothing());
    }
}
