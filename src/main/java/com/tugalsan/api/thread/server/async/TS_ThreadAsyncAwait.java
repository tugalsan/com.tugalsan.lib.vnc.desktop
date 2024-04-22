package com.tugalsan.api.thread.server.async;

import com.tugalsan.api.callable.client.TGS_CallableType1;
import com.tugalsan.api.list.client.TGS_ListUtils;
import com.tugalsan.api.thread.server.async.core.TS_ThreadAsyncCoreParallelUntilFirstFail;
import com.tugalsan.api.thread.server.async.core.TS_ThreadAsyncCoreParallelUntilFirstSuccess;
import com.tugalsan.api.thread.server.async.core.TS_ThreadAsyncCoreParallelUntilAllDone;
import com.tugalsan.api.runnable.client.TGS_RunnableType1;
import com.tugalsan.api.thread.server.async.core.TS_ThreadAsyncCoreSingle;
import com.tugalsan.api.thread.server.sync.TS_ThreadSyncTrigger;
import java.time.*;
import java.util.*;

//USE TS_ThreadAsyncBuilder with killTrigger if possible
public class TS_ThreadAsyncAwait {

    public static <T> TS_ThreadAsyncCoreParallelUntilFirstFail<T> callParallelUntilFirstFail(TS_ThreadSyncTrigger killTrigger, Duration until, TGS_CallableType1<T, TS_ThreadSyncTrigger> caller, TGS_RunnableType1<TS_ThreadSyncTrigger>... throwingValidators) {
        List<TGS_CallableType1<T, TS_ThreadSyncTrigger>> callables = TGS_ListUtils.of();
        callables.add(caller);
        Arrays.stream(throwingValidators).forEach(tv -> callables.add(kt -> {
            tv.run(kt);
            return null;
        }));
        return TS_ThreadAsyncAwait.callParallelUntilFirstFail(
                killTrigger, until, callables
        );
    }

    public static <T> TS_ThreadAsyncCoreParallelUntilFirstFail<T> callParallelUntilFirstFail(TS_ThreadSyncTrigger killTrigger, Duration until, List<TGS_CallableType1<T, TS_ThreadSyncTrigger>> callers, TGS_RunnableType1<TS_ThreadSyncTrigger>... throwingValidators) {
        List<TGS_CallableType1<T, TS_ThreadSyncTrigger>> callables = TGS_ListUtils.of();
        callables.addAll(callers);
        Arrays.stream(throwingValidators).forEach(tv -> callables.add(kt -> {
            tv.run(kt);
            return null;
        }));
        return TS_ThreadAsyncAwait.callParallelUntilFirstFail(
                killTrigger, until, callables
        );
    }

//    public static <T> TS_ThreadAsyncCoreParallelUntilFirstFail<T> callSingle(TS_ThreadSyncTrigger killTrigger, Duration until, TGS_CallableType1<T, TS_ThreadSyncTrigger> callable) {
//        return TS_ThreadAsyncCoreParallelUntilFirstFail.of(killTrigger, until, callable);
//    }
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

    public static <T> TS_ThreadAsyncCoreParallelUntilAllDone<T> callParallel(TS_ThreadSyncTrigger killTrigger, Duration until, TGS_CallableType1<T, TS_ThreadSyncTrigger>... callables) {
        return TS_ThreadAsyncCoreParallelUntilAllDone.of(killTrigger, until, callables);
    }

    public static <T> TS_ThreadAsyncCoreParallelUntilAllDone<T> callParallel(TS_ThreadSyncTrigger killTrigger, Duration until, List<TGS_CallableType1<T, TS_ThreadSyncTrigger>> callables) {
        return TS_ThreadAsyncCoreParallelUntilAllDone.of(killTrigger, until, callables);
    }

    public static TS_ThreadAsyncCoreSingle<Void> runUntil(TS_ThreadSyncTrigger killTrigger, Duration until, TGS_RunnableType1<TS_ThreadSyncTrigger> exe) {
        return callSingle(killTrigger, until, kt -> {
            exe.run(kt);
            return null;
        });
    }
}
