package com.tugalsan.api.thread.server.async;

import com.tugalsan.api.callable.client.TGS_CallableType1;
import com.tugalsan.api.list.client.TGS_ListUtils;
import com.tugalsan.api.thread.server.async.core.TS_ThreadAsyncCoreParallelUntilFirstFail;
import com.tugalsan.api.thread.server.async.core.TS_ThreadAsyncCoreParallelUntilFirstSuccess;
import com.tugalsan.api.thread.server.async.core.TS_ThreadAsyncCoreParallelUntilAllDone;
import com.tugalsan.api.runnable.client.TGS_RunnableType1;
import com.tugalsan.api.runnable.client.TGS_RunnableUtils;
import com.tugalsan.api.stream.client.TGS_StreamUtils;
import com.tugalsan.api.thread.server.async.core.TS_ThreadAsyncCoreSingle;
import com.tugalsan.api.thread.server.sync.TS_ThreadSyncTrigger;
import com.tugalsan.api.thread.server.async.rateLimited.TS_ThreadSyncRateLimitedCallType1;
import com.tugalsan.api.union.client.TGS_UnionExcuse;
import java.time.*;
import java.util.*;

//USE TS_ThreadAsyncBuilder with killTrigger if possible
public class TS_ThreadAsyncAwait {

    public static <T> TS_ThreadAsyncCoreParallelUntilFirstFail<T> callParallelUntilFirstFail(TS_ThreadSyncTrigger killTrigger, Duration until, TGS_CallableType1<T, TS_ThreadSyncTrigger> callable, TGS_RunnableType1<TS_ThreadSyncTrigger>... throwingValidators) {
        List<TGS_CallableType1<T, TS_ThreadSyncTrigger>> allCallables = TGS_ListUtils.of();
        allCallables.add(callable);
        Arrays.stream(throwingValidators).forEach(tv -> allCallables.add(TGS_RunnableUtils.toCallable1(tv)));
        return TS_ThreadAsyncAwait.callParallelUntilFirstFail(
                killTrigger, until, allCallables
        );
    }

    public static <T> TS_ThreadAsyncCoreParallelUntilFirstFail<T> callParallelUntilFirstFail(TS_ThreadSyncTrigger killTrigger, Duration until, List<TGS_CallableType1<T, TS_ThreadSyncTrigger>> callables, TGS_RunnableType1<TS_ThreadSyncTrigger>... throwingValidators) {
        List<TGS_CallableType1<T, TS_ThreadSyncTrigger>> allCallables = TGS_ListUtils.of();
        allCallables.addAll(callables);
        Arrays.stream(throwingValidators).forEach(tv -> allCallables.add(TGS_RunnableUtils.toCallable1(tv)));
        return TS_ThreadAsyncAwait.callParallelUntilFirstFail(
                killTrigger, until, allCallables
        );
    }

    public static <T> TS_ThreadAsyncCoreSingle<T> callSingle(TS_ThreadSyncTrigger killTrigger, Duration until, TGS_CallableType1<T, TS_ThreadSyncTrigger> callable) {
        return TS_ThreadAsyncCoreSingle.of(killTrigger, until, callable);
    }

    public static <T> TS_ThreadAsyncCoreParallelUntilFirstFail<T> callParallelUntilFirstFail(TS_ThreadSyncTrigger killTrigger, Duration until, TGS_CallableType1<T, TS_ThreadSyncTrigger>... callables) {
        return TS_ThreadAsyncCoreParallelUntilFirstFail.of(killTrigger, until, callables);
    }

    public static <T> TS_ThreadAsyncCoreParallelUntilFirstFail<T> callParallelUntilFirstFail(TS_ThreadSyncTrigger killTrigger, Duration until, List<TGS_CallableType1<T, TS_ThreadSyncTrigger>> callables) {
        return TS_ThreadAsyncCoreParallelUntilFirstFail.of(killTrigger, until, callables);
    }

    public static <T> TS_ThreadAsyncCoreParallelUntilFirstSuccess<T> callParallelUntilFirstSuccess(TS_ThreadSyncTrigger killTrigger, Duration until, TGS_CallableType1<T, TS_ThreadSyncTrigger>... callables) {
        return TS_ThreadAsyncCoreParallelUntilFirstSuccess.of(killTrigger, until, callables);
    }

    public static <T> TS_ThreadAsyncCoreParallelUntilFirstSuccess<T> callParallelUntilFirstSuccess(TS_ThreadSyncTrigger killTrigger, Duration until, List<TGS_CallableType1<T, TS_ThreadSyncTrigger>> callables) {
        return TS_ThreadAsyncCoreParallelUntilFirstSuccess.of(killTrigger, until, callables);
    }

    public static <T> TS_ThreadAsyncCoreParallelUntilAllDone<TGS_UnionExcuse<T>> callParallelRateLimited(TS_ThreadSyncTrigger killTrigger, int rateLimit, Duration until, TGS_CallableType1<T, TS_ThreadSyncTrigger>... callables) {
        var rateLimitor = TS_ThreadSyncRateLimitedCallType1.<T, TS_ThreadSyncTrigger>of(rateLimit);
        var rateLimitedCallables = TGS_StreamUtils.toLst(
                Arrays.stream(callables).map(c -> {
                    TGS_CallableType1<TGS_UnionExcuse<T>, TS_ThreadSyncTrigger> cs = kt -> rateLimitor.callUntil(c, until, kt);
                    return cs;
                })
        );
        return TS_ThreadAsyncCoreParallelUntilAllDone.of(killTrigger, until, rateLimitedCallables);
    }

    public static <T> TS_ThreadAsyncCoreParallelUntilAllDone<T> callParallel(TS_ThreadSyncTrigger killTrigger, Duration until, TGS_CallableType1<T, TS_ThreadSyncTrigger>... callables) {
        return TS_ThreadAsyncCoreParallelUntilAllDone.of(killTrigger, until, callables);
    }

    public static <T> TS_ThreadAsyncCoreParallelUntilAllDone<T> callParallel(TS_ThreadSyncTrigger killTrigger, Duration until, List<TGS_CallableType1<T, TS_ThreadSyncTrigger>> callables) {
        return TS_ThreadAsyncCoreParallelUntilAllDone.of(killTrigger, until, callables);
    }

    public static <T> TS_ThreadAsyncCoreParallelUntilAllDone<T> callParallelRateLimited(TS_ThreadSyncTrigger killTrigger, int rateLimit, Duration until, List<TGS_CallableType1<T, TS_ThreadSyncTrigger>> callables) {
        var rateLimitor = TS_ThreadSyncRateLimitedCallType1.<T, TS_ThreadSyncTrigger>of(rateLimit);
        var rateLimitedCallables = TGS_StreamUtils.toLst(
                callables.stream().map(c -> {
                    TGS_CallableType1<T, TS_ThreadSyncTrigger> cs = kt -> rateLimitor.callUntil(c, until, kt).orElse(null);
                    return cs;
                })
        );
        return TS_ThreadAsyncCoreParallelUntilAllDone.of(killTrigger, until, rateLimitedCallables);
    }

    public static TS_ThreadAsyncCoreSingle<Void> runUntil(TS_ThreadSyncTrigger killTrigger, Duration until, TGS_RunnableType1<TS_ThreadSyncTrigger> exe) {
        return callSingle(killTrigger, until, TGS_RunnableUtils.toCallable1(exe));
    }
}
