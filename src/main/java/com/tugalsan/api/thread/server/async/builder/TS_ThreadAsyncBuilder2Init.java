package com.tugalsan.api.thread.server.async.builder;

import com.tugalsan.api.runnable.client.TGS_RunnableType2;
import com.tugalsan.api.thread.server.sync.TS_ThreadSyncTrigger;
import java.time.Duration;

public class TS_ThreadAsyncBuilder2Init<T> {

    public TS_ThreadAsyncBuilder2Init(TS_ThreadSyncTrigger killTrigger, String name, TS_ThreadAsyncBuilderCallableTimed<T> init) {
        this.killTrigger = killTrigger;
        this.name = name;
        this.init = init;
    }
    final private TS_ThreadSyncTrigger killTrigger;
    final private String name;
    final private TS_ThreadAsyncBuilderCallableTimed<T> init;

    public <T> TS_ThreadAsyncBuilder3Main<T> mainEmpty() {
        return new TS_ThreadAsyncBuilder3Main(killTrigger, name, init, TS_ThreadAsyncBuilderRunnableTimedType2.empty());
    }

    public <T> TS_ThreadAsyncBuilder3Main<T> main(TGS_RunnableType2<TS_ThreadSyncTrigger, T> killTrigger_initObj) {
        return new TS_ThreadAsyncBuilder3Main(killTrigger, name, init, TS_ThreadAsyncBuilderRunnableTimedType2.run(killTrigger_initObj));
    }

    public <T> TS_ThreadAsyncBuilder3Main<T> mainTimed(Duration max, TGS_RunnableType2<TS_ThreadSyncTrigger, T> killTrigger_initObj) {
        return new TS_ThreadAsyncBuilder3Main(killTrigger, name, init, TS_ThreadAsyncBuilderRunnableTimedType2.maxTimedRun(max, killTrigger_initObj));
    }
}
