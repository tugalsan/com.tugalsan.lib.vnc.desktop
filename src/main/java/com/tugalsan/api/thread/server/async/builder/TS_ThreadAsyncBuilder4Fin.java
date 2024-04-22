package com.tugalsan.api.thread.server.async.builder;

import com.tugalsan.api.thread.server.sync.TS_ThreadSyncTrigger;
import com.tugalsan.api.validator.client.TGS_ValidatorType1;
import java.time.Duration;
import java.util.Optional;

public class TS_ThreadAsyncBuilder4Fin<T> {

    public TS_ThreadAsyncBuilder4Fin(TS_ThreadSyncTrigger killTrigger, String name,
            TS_ThreadAsyncBuilderCallableTimed<T> init, TS_ThreadAsyncBuilderRunnableTimedType2<T> main, TS_ThreadAsyncBuilderRunnableTimedType1<T> fin) {
        this.killTrigger = killTrigger;
        this.name = name;
        this.init = init;
        this.main = main;
        this.fin = fin;
    }
    final private TS_ThreadSyncTrigger killTrigger;
    final private String name;
    final private TS_ThreadAsyncBuilderCallableTimed<T> init;
    final private TS_ThreadAsyncBuilderRunnableTimedType2<T> main;
    final private TS_ThreadAsyncBuilderRunnableTimedType1<T> fin;

    @Deprecated//Complicated
    private TS_ThreadAsyncBuilderObject<T> build(Optional<TGS_ValidatorType1<T>> valCycleMain, Optional<Duration> durPeriodCycle) {
        return TS_ThreadAsyncBuilderObject.of(killTrigger, name, init, main, fin, valCycleMain, durPeriodCycle);
    }

    public TS_ThreadAsyncBuilderObject<T> cycle_none() {
        return build(
                Optional.empty(),
                Optional.empty()
        );
    }

    public TS_ThreadAsyncBuilderObject<T> cycle_forever() {
        return cycle_mainValidation(o -> true);
    }

    public TS_ThreadAsyncBuilderObject<T> cycle_mainPeriod(Duration durPeriodCycle) {
        return build(
                Optional.empty(),
                durPeriodCycle == null ? Optional.empty() : Optional.of(durPeriodCycle)
        );
    }

    public TS_ThreadAsyncBuilderObject<T> cycle_mainValidation(TGS_ValidatorType1<T> valCycleMain) {
        return build(
                valCycleMain == null ? Optional.empty() : Optional.of(valCycleMain),
                Optional.empty()
        );
    }

    public TS_ThreadAsyncBuilderObject<T> cycle_mainValidation_mainPeriod(TGS_ValidatorType1<T> valCycleMain, Duration durPeriodCycle) {
        return build(
                valCycleMain == null ? Optional.empty() : Optional.of(valCycleMain),
                durPeriodCycle == null ? Optional.empty() : Optional.of(durPeriodCycle)
        );
    }

    public TS_ThreadAsyncBuilderObject<T> asyncRun() {
        return cycle_none().asyncRun();
    }

    public TS_ThreadAsyncBuilderObject<T> asyncRun(Duration until) {
        return cycle_none().asyncRun(until);
    }

    public TS_ThreadAsyncBuilderObject<T> asyncRunAwait() {
        return cycle_none().asyncRunAwait();
    }

    public TS_ThreadAsyncBuilderObject<T> asyncRunAwait(Duration until) {
        return cycle_none().asyncRunAwait(until);
    }
}
