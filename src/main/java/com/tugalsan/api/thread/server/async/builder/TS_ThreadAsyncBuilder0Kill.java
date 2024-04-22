package com.tugalsan.api.thread.server.async.builder;

import com.tugalsan.api.callable.client.TGS_Callable;
import com.tugalsan.api.runnable.client.TGS_RunnableType1;
import com.tugalsan.api.runnable.client.TGS_RunnableType2;
import com.tugalsan.api.thread.server.sync.TS_ThreadSyncTrigger;
import java.time.Duration;
import java.util.Optional;

public class TS_ThreadAsyncBuilder0Kill {

    public TS_ThreadAsyncBuilder0Kill(TS_ThreadSyncTrigger killTrigger) {
        this._killTrigger = killTrigger;
    }
    final private TS_ThreadSyncTrigger _killTrigger;

    public TS_ThreadAsyncBuilder1Name name(String name) {
        return new TS_ThreadAsyncBuilder1Name(_killTrigger, name);
    }

    public <T> TS_ThreadAsyncBuilder2Init<T> initEmpty() {
        return new TS_ThreadAsyncBuilder2Init(_killTrigger, "Unnamed", TS_ThreadAsyncBuilderCallableTimed.of());
    }

    public <T> TS_ThreadAsyncBuilder2Init<T> init(TGS_Callable<T> call) {
        return new TS_ThreadAsyncBuilder2Init(_killTrigger, "Unnamed", TS_ThreadAsyncBuilderCallableTimed.of(call));
    }

    public <T> TS_ThreadAsyncBuilder2Init<T> initTimed(Duration max, TGS_Callable<T> call) {
        return new TS_ThreadAsyncBuilder2Init(_killTrigger, "Unnamed", TS_ThreadAsyncBuilderCallableTimed.of(max, call));
    }

    public <T> TS_ThreadAsyncBuilder3Main<T> main(TGS_RunnableType1<TS_ThreadSyncTrigger> killTrigger) {
        TGS_RunnableType2<TS_ThreadSyncTrigger, Object> killTrigger_initObj = (kt, initObj) -> killTrigger.run(kt);
        return new TS_ThreadAsyncBuilder3Main(_killTrigger, "Unnamed", TS_ThreadAsyncBuilderCallableTimed.of(), TS_ThreadAsyncBuilderRunnableTimedType2.run(killTrigger_initObj));
    }

    public <T> TS_ThreadAsyncBuilder3Main<T> mainDummyForCycle() {
        return main(kt -> {
        });
    }

    public <T> TS_ThreadAsyncBuilder3Main<T> mainTimed(Duration max, TGS_RunnableType1<TS_ThreadSyncTrigger> killTrigger) {
        TGS_RunnableType2<TS_ThreadSyncTrigger, Object> killTrigger_initObj = (kt, initObj) -> killTrigger.run(kt);
        return new TS_ThreadAsyncBuilder3Main(_killTrigger, "Unnamed", TS_ThreadAsyncBuilderCallableTimed.of(), TS_ThreadAsyncBuilderRunnableTimedType2.maxTimedRun(max, killTrigger_initObj));
    }

    public <T> TS_ThreadAsyncBuilderObject<T> asyncRun(TGS_RunnableType1<TS_ThreadSyncTrigger> killTrigger) {
        TGS_RunnableType2<TS_ThreadSyncTrigger, Object> killTrigger_initObj = (kt, initObj) -> killTrigger.run(kt);
        var main = new TS_ThreadAsyncBuilder3Main(_killTrigger, "Unnamed", TS_ThreadAsyncBuilderCallableTimed.of(), TS_ThreadAsyncBuilderRunnableTimedType2.run(killTrigger_initObj));
        return TS_ThreadAsyncBuilderObject.of(main.killTrigger, main.name, main.init, main.main, TS_ThreadAsyncBuilderRunnableTimedType1.empty(), Optional.empty(), Optional.empty()).asyncRun();
    }

    public <T> TS_ThreadAsyncBuilderObject<T> asyncRun(Duration max, TGS_RunnableType1<TS_ThreadSyncTrigger> killTrigger) {
        TGS_RunnableType2<TS_ThreadSyncTrigger, Object> killTrigger_initObj = (kt, initObj) -> killTrigger.run(kt);
        var main = new TS_ThreadAsyncBuilder3Main(_killTrigger, "Unnamed", TS_ThreadAsyncBuilderCallableTimed.of(), TS_ThreadAsyncBuilderRunnableTimedType2.maxTimedRun(max, killTrigger_initObj));
        return TS_ThreadAsyncBuilderObject.of(main.killTrigger, main.name, main.init, main.main, TS_ThreadAsyncBuilderRunnableTimedType1.empty(), Optional.empty(), Optional.empty()).asyncRun();
    }

    public <T> TS_ThreadAsyncBuilderObject<T> asyncRunAwait(TGS_RunnableType1<TS_ThreadSyncTrigger> killTrigger) {
        TGS_RunnableType2<TS_ThreadSyncTrigger, Object> killTrigger_initObj = (kt, initObj) -> killTrigger.run(kt);
        var main = new TS_ThreadAsyncBuilder3Main(_killTrigger, "Unnamed", TS_ThreadAsyncBuilderCallableTimed.of(), TS_ThreadAsyncBuilderRunnableTimedType2.run(killTrigger_initObj));
        return TS_ThreadAsyncBuilderObject.of(main.killTrigger, main.name, main.init, main.main, TS_ThreadAsyncBuilderRunnableTimedType1.empty(), Optional.empty(), Optional.empty()).asyncRunAwait();
    }

    public <T> TS_ThreadAsyncBuilderObject<T> asyncRunAwait(Duration max, TGS_RunnableType1<TS_ThreadSyncTrigger> killTrigger) {
        TGS_RunnableType2<TS_ThreadSyncTrigger, Object> killTrigger_initObj = (kt, initObj) -> killTrigger.run(kt);
        var main = new TS_ThreadAsyncBuilder3Main(_killTrigger, "Unnamed", TS_ThreadAsyncBuilderCallableTimed.of(), TS_ThreadAsyncBuilderRunnableTimedType2.run(killTrigger_initObj));
        return TS_ThreadAsyncBuilderObject.of(main.killTrigger, main.name, main.init, main.main, TS_ThreadAsyncBuilderRunnableTimedType1.empty(), Optional.empty(), Optional.empty()).asyncRunAwait(max);
    }
}
