package com.tugalsan.api.thread.server.async.core;

import com.tugalsan.api.callable.client.TGS_CallableType1;
import com.tugalsan.api.list.client.TGS_ListUtils;
import com.tugalsan.api.thread.server.sync.TS_ThreadSyncTrigger;
import com.tugalsan.api.time.server.TS_TimeElapsed;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.Future;

//IMPLEMENTATION OF https://www.youtube.com/watch?v=_fRN7tpLyPk
public class TS_ThreadAsyncCoreParallelUntilFirstSuccess<T> {

    private TS_ThreadAsyncCoreParallelUntilFirstSuccess(TS_ThreadSyncTrigger killTrigger, Duration duration, List<TGS_CallableType1<T, TS_ThreadSyncTrigger>> callables) {
        var elapsedTracker = TS_TimeElapsed.of();
        var resultsForSuccessfulOnes = callables.stream().map(c -> c.call(killTrigger)).toList();
        if (!resultsForSuccessfulOnes.isEmpty()) {
            resultIfAnySuccessful = resultsForSuccessfulOnes.get(0);
        }
        states = resultsForSuccessfulOnes.stream().map(r -> Future.State.SUCCESS).toList();
        exceptions = TGS_ListUtils.of();
        this.elapsed = elapsedTracker.elapsed_now();
    }
    final public Duration elapsed;

    public boolean timeout() {
        return exceptions.stream()
                .filter(e -> e instanceof TS_ThreadAsyncCoreTimeoutException)
                .findAny().isPresent();
    }
    public List<Future.State> states;
    public List<Exception> exceptions = TGS_ListUtils.of();
    public T resultIfAnySuccessful;

    public boolean hasError() {
        return !exceptions.isEmpty();
    }

    public static <T> TS_ThreadAsyncCoreParallelUntilFirstSuccess<T> of(TS_ThreadSyncTrigger killTrigger, Duration duration, TGS_CallableType1<T, TS_ThreadSyncTrigger>... callables) {
        return of(killTrigger, duration, List.of(callables));
    }

    public static <T> TS_ThreadAsyncCoreParallelUntilFirstSuccess<T> of(TS_ThreadSyncTrigger killTrigger, Duration duration, List<TGS_CallableType1<T, TS_ThreadSyncTrigger>> callables) {
        return new TS_ThreadAsyncCoreParallelUntilFirstSuccess(killTrigger, duration, callables);
    }
}
