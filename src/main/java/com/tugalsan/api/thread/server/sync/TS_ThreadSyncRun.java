package com.tugalsan.api.thread.server.sync;

import com.tugalsan.api.runnable.client.TGS_Runnable;
import com.tugalsan.api.stream.client.TGS_StreamUtils;
import com.tugalsan.api.unsafe.client.TGS_UnSafe;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class TS_ThreadSyncRun {

    private TS_ThreadSyncRun(TGS_Runnable run) {
        this.run = run;
    }
    final private ReentrantLock lock = new ReentrantLock();
    final private TGS_Runnable run;

    public static TS_ThreadSyncRun of(TGS_Runnable run) {
        return new TS_ThreadSyncRun(run);
    }

    public void run() {
        TGS_UnSafe.run(() -> {
            if (!lock.tryLock()) {
                return;
            }
            TGS_UnSafe.run(() -> run.run(), ex -> TGS_UnSafe.thrw(ex), () -> lock.unlock());
        }, e -> TGS_StreamUtils.runNothing());
    }

    public void runUntil(Duration timeout) {
        TGS_UnSafe.run(() -> {
            if (!lock.tryLock(timeout.toSeconds(), TimeUnit.SECONDS)) {
                return;
            }
            TGS_UnSafe.run(() -> run.run(), ex -> TGS_UnSafe.thrw(ex), () -> lock.unlock());
        }, e -> TGS_StreamUtils.runNothing());
    }
}
