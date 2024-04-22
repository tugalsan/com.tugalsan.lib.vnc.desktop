package com.tugalsan.api.thread.server.async.builder;

import com.tugalsan.api.callable.client.TGS_Callable;
import com.tugalsan.api.runnable.client.TGS_RunnableType2;
import com.tugalsan.api.thread.server.sync.TS_ThreadSyncTrigger;
import java.time.Duration;

public class TS_ThreadAsyncBuilder1Name {

    public TS_ThreadAsyncBuilder1Name(TS_ThreadSyncTrigger killTrigger, String name) {
        this.killTrigger = killTrigger;
        this.name = name;
    }
    final private TS_ThreadSyncTrigger killTrigger;
    final private String name;

    public <T> TS_ThreadAsyncBuilder2Init<T> initEmpty() {
        return new TS_ThreadAsyncBuilder2Init(killTrigger, name, TS_ThreadAsyncBuilderCallableTimed.of());
    }

    public <T> TS_ThreadAsyncBuilder2Init<T> init(TGS_Callable<T> call) {
        return new TS_ThreadAsyncBuilder2Init(killTrigger, name, TS_ThreadAsyncBuilderCallableTimed.of(call));
    }

    public <T> TS_ThreadAsyncBuilder2Init<T> initTimed(Duration max, TGS_Callable<T> call) {
        return new TS_ThreadAsyncBuilder2Init(killTrigger, name, TS_ThreadAsyncBuilderCallableTimed.of(max, call));
    }

    public <T> TS_ThreadAsyncBuilder3Main<T> main(TGS_RunnableType2<TS_ThreadSyncTrigger, T> killTrigger_initObj) {
        return new TS_ThreadAsyncBuilder3Main(killTrigger, name, TS_ThreadAsyncBuilderCallableTimed.of(), TS_ThreadAsyncBuilderRunnableTimedType2.run(killTrigger_initObj));
    }

    public <T> TS_ThreadAsyncBuilder3Main<T> mainTimed(Duration max, TGS_RunnableType2<TS_ThreadSyncTrigger, T> killTrigger_initObj) {
        return new TS_ThreadAsyncBuilder3Main(killTrigger, name, TS_ThreadAsyncBuilderCallableTimed.of(), TS_ThreadAsyncBuilderRunnableTimedType2.maxTimedRun(max, killTrigger_initObj));
    }

}
