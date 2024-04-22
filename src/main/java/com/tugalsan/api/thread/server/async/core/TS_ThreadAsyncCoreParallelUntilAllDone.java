package com.tugalsan.api.thread.server.async.core;

import com.tugalsan.api.callable.client.TGS_CallableType1;
import com.tugalsan.api.list.client.TGS_ListUtils;
import com.tugalsan.api.thread.server.sync.TS_ThreadSyncTrigger;
import com.tugalsan.api.time.server.TS_TimeElapsed;
import java.time.Duration;
import java.util.List;

//IMPLEMENTATION OF https://www.youtube.com/watch?v=_fRN7tpLyPk
public class TS_ThreadAsyncCoreParallelUntilAllDone<T> {

    //until: Instant.now().plusMillis(10)
    private TS_ThreadAsyncCoreParallelUntilAllDone(TS_ThreadSyncTrigger killTrigger, Duration duration, List<TGS_CallableType1<T, TS_ThreadSyncTrigger>> callables) {
        var elapsedTracker = TS_TimeElapsed.of();
        resultsForSuccessfulOnes = callables.stream().map(c -> c.call(killTrigger)).toList();
        exceptions = TGS_ListUtils.of();
        this.elapsed = elapsedTracker.elapsed_now();
    }
    final public Duration elapsed;

    public boolean timeout() {
        return exceptions.stream()
                .filter(e -> e instanceof TS_ThreadAsyncCoreTimeoutException)
                .findAny().isPresent();
    }
    public List<T> resultsForSuccessfulOnes;
    public List<Throwable> exceptions;

    public boolean hasError() {
        return !exceptions.isEmpty();
    }

    public T findAny() {
        return resultsForSuccessfulOnes.stream().findAny().orElse(null);
    }

    public static <T> TS_ThreadAsyncCoreParallelUntilAllDone<T> of(TS_ThreadSyncTrigger killTrigger, Duration duration, TGS_CallableType1<T, TS_ThreadSyncTrigger>... callables) {
        return of(killTrigger, duration, List.of(callables));
    }

    public static <T> TS_ThreadAsyncCoreParallelUntilAllDone<T> of(TS_ThreadSyncTrigger killTrigger, Duration duration, List<TGS_CallableType1<T, TS_ThreadSyncTrigger>> callables) {
        return new TS_ThreadAsyncCoreParallelUntilAllDone(killTrigger, duration, callables);
    }
}
