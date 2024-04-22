package com.tugalsan.api.thread.server.async;

import com.tugalsan.api.thread.server.sync.TS_ThreadSyncTrigger;
import com.tugalsan.api.thread.server.async.builder.TS_ThreadAsyncBuilder0Kill;

public class TS_ThreadAsyncBuilder {

    public static TS_ThreadAsyncBuilder0Kill of() {
        return new TS_ThreadAsyncBuilder0Kill(TS_ThreadSyncTrigger.of());
    }

    public static TS_ThreadAsyncBuilder0Kill of(TS_ThreadSyncTrigger killTrigger) {
        return new TS_ThreadAsyncBuilder0Kill(killTrigger);
    }
}
