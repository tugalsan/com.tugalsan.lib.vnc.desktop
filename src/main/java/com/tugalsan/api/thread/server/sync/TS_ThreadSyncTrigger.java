package com.tugalsan.api.thread.server.sync;

import java.util.concurrent.atomic.AtomicBoolean;

public class TS_ThreadSyncTrigger {

    private TS_ThreadSyncTrigger() {

    }

    public static TS_ThreadSyncTrigger of() {
        return new TS_ThreadSyncTrigger();
    }

    private AtomicBoolean value = new AtomicBoolean(false);

    public void trigger() {
        value.set(true);
    }

    public boolean hasTriggered() {
        return value.get();
    }

    public boolean hasNotTriggered() {
        return !hasTriggered();
    }

}
