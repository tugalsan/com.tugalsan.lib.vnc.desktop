package com.tugalsan.api.thread.server.async.core;

import com.tugalsan.api.callable.client.TGS_CallableType1;
import com.tugalsan.api.list.client.TGS_ListUtils;
import com.tugalsan.api.thread.server.sync.TS_ThreadSyncTrigger;
import com.tugalsan.api.time.server.TS_TimeElapsed;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.Future;

public class TS_ThreadAsyncCoreParallelUntilFirstFail<T> {

    private TS_ThreadAsyncCoreParallelUntilFirstFail(TS_ThreadSyncTrigger killTrigger, Duration duration, List<TGS_CallableType1<T, TS_ThreadSyncTrigger>> callables) {
        var elapsedTracker = TS_TimeElapsed.of();
        resultsForSuccessfulOnes = callables.stream().map(c -> c.call(killTrigger)).toList();
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
    public List<Future.State> states = TGS_ListUtils.of();
    public List<Throwable> exceptions = TGS_ListUtils.of();
    public List<T> resultsForSuccessfulOnes = TGS_ListUtils.of();

    public boolean hasError() {
        return !exceptions.isEmpty();
    }

//    public static <T> TS_ThreadAsyncCoreParallelUntilFirstFail<T> of(TS_ThreadSyncTrigger killTrigger, Duration duration, TGS_CallableType1<T, TS_ThreadSyncTrigger> callable) {
//        return of(killTrigger, duration, List.of(callable));
//    }
    public static <T> TS_ThreadAsyncCoreParallelUntilFirstFail<T> of(TS_ThreadSyncTrigger killTrigger, Duration duration, TGS_CallableType1<T, TS_ThreadSyncTrigger>... callables) {
        return of(killTrigger, duration, List.of(callables));
    }

    public static <T> TS_ThreadAsyncCoreParallelUntilFirstFail<T> of(TS_ThreadSyncTrigger killTrigger, Duration duration, List<TGS_CallableType1<T, TS_ThreadSyncTrigger>> callables) {
        return new TS_ThreadAsyncCoreParallelUntilFirstFail(killTrigger, duration, callables);
    }

//    public static <T> TS_ThreadAsyncCoreParallelUntilFirstFail<T> of(TS_ThreadSyncTrigger killTrigger, Duration duration, Callable<T> fetcher, Callable<Void>... throwingValidators) {
//        return of(killTrigger, duration, fetcher, List.of(throwingValidators));
//    }
//
//    public static <T> TS_ThreadAsyncCoreParallelUntilFirstFail<T> of(TS_ThreadSyncTrigger killTrigger, Duration duration, Callable<T> fetcher, List<Callable<Void>> throwingValidators) {
//        List<Callable<T>> fetchers = TGS_ListUtils.of();
//        fetchers.add(fetcher);
//        return of(killTrigger, duration, fetchers, throwingValidators);
//    }
//
//    public static <T> TS_ThreadAsyncCoreParallelUntilFirstFail<T> of(TS_ThreadSyncTrigger killTrigger, Duration duration, List<Callable<T>> fetchers, Callable<Void>... throwingValidators) {
//        return of(killTrigger, duration, fetchers, List.of(throwingValidators));
//    }
//
//    public static <T> TS_ThreadAsyncCoreParallelUntilFirstFail<T> of(TS_ThreadSyncTrigger killTrigger, Duration duration, List<Callable<T>> fetchers, List<Callable<Void>> throwingValidators) {
//        List<Callable<T>> callables = TGS_ListUtils.of();
//        callables.addAll(fetchers);
//        throwingValidators.forEach(tv -> callables.add(() -> {
//            tv.call();
//            return null;
//        }));
//        return TS_ThreadAsyncCoreParallelUntilFirstFail.of(killTrigger, duration, callables);
//    }
}
