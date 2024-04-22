package com.tugalsan.api.thread.server.sync;

import java.util.concurrent.atomic.AtomicBoolean;

public class TS_ThreadSyncTrigger {

    private TS_ThreadSyncTrigger() {

    }

    public static TS_ThreadSyncTrigger of() {
        return new TS_ThreadSyncTrigger();
    }
    final public TS_ThreadSyncLst<TS_ThreadSyncTrigger> parents = TS_ThreadSyncLst.of();
    final private AtomicBoolean value = new AtomicBoolean(false);

    public void trigger() {
        value.set(true);
    }

    public boolean hasTriggered() {
        return value.get() || parents.findFirst(t -> t.hasTriggered()) != null;
    }

    public boolean hasNotTriggered() {
        return !hasTriggered();
    }

}
