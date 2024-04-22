package com.tugalsan.api.thread.server.async.core;

import com.tugalsan.api.callable.client.TGS_CallableType1;
import com.tugalsan.api.list.client.TGS_ListUtils;
import com.tugalsan.api.stream.client.TGS_StreamUtils;
import com.tugalsan.api.thread.server.sync.TS_ThreadSyncTrigger;
import com.tugalsan.api.thread.server.sync.TS_ThreadSyncLst;
import com.tugalsan.api.time.server.TS_TimeElapsed;
import com.tugalsan.api.time.server.TS_TimeUtils;
import com.tugalsan.api.unsafe.client.TGS_UnSafe;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.TimeoutException;

public class TS_ThreadAsyncCoreParallelUntilFirstFail<T> {

    private static class InnerScope<T> implements AutoCloseable {

        private final StructuredTaskScope.ShutdownOnFailure innerScope = new StructuredTaskScope.ShutdownOnFailure();
        public volatile TimeoutException timeoutException = null;
        public final TS_ThreadSyncLst<StructuredTaskScope.Subtask<T>> subTasks = TS_ThreadSyncLst.of();

        public void throwIfFailed() throws ExecutionException {
            innerScope.throwIfFailed();
        }

        public InnerScope<T> join() throws InterruptedException {
            innerScope.join();
            return this;
        }

        public InnerScope<T> joinUntil(Instant deadline) throws InterruptedException {
            try {
                innerScope.joinUntil(deadline);
            } catch (TimeoutException e) {
                setTimeout(true, e);
            }
            return this;
        }

        public StructuredTaskScope.Subtask<T> fork(Callable<? extends T> task) {
            StructuredTaskScope.Subtask<T> subTask = innerScope.fork(task);
            subTasks.add(subTask);
            return subTask;
        }

        public void shutdown() {
            innerScope.shutdown();
        }

        @Override
        public void close() {
            innerScope.close();
        }

        public Throwable exception() {
            return innerScope.exception().orElse(null);
        }

        public List<T> resultsForSuccessfulOnes() {
            return TGS_StreamUtils.toLst(subTasks.stream()
                    .filter(st -> st.state() == StructuredTaskScope.Subtask.State.SUCCESS)
                    .map(f -> f.get())
                    .filter(r -> r != null)
            );
        }

        public List<StructuredTaskScope.Subtask.State> states() {
            return TGS_StreamUtils.toLst(subTasks.stream()
                    .map(st -> st.state())
            );
        }

        public void setTimeout(boolean triggerShutDown, TimeoutException te) {
            if (triggerShutDown) {
                innerScope.shutdown();
            }
            timeoutException = te == null ? new TimeoutException() : te;
        }
    }

    private TS_ThreadAsyncCoreParallelUntilFirstFail(TS_ThreadSyncTrigger killTrigger, Duration duration, List<TGS_CallableType1<T, TS_ThreadSyncTrigger>> callables) {
        var elapsedTracker = TS_TimeElapsed.of();
        var o = new Object() {
            InnerScope<T> scope = null;
        };
        try (var scope = new InnerScope<T>()) {
            o.scope = scope;
            callables.forEach(c -> scope.fork(() -> c.call(killTrigger)));
            if (duration == null) {
                scope.join();
            } else {
                scope.joinUntil(TS_TimeUtils.toInstant(duration));
            }
            scope.throwIfFailed();
            if (scope.timeoutException != null) {
                exceptions.add(new TimeoutException());
            }
            if (scope.exception() != null) {
                exceptions.add(scope.exception());
            }
            resultsForSuccessfulOnes = scope.resultsForSuccessfulOnes();
            states = scope.states();
        } catch (InterruptedException | ExecutionException | IllegalStateException e) {
//            if (e instanceof TimeoutException te) {
//                o.scope.setTimeout(true, te);
//            }
            if (e instanceof IllegalStateException ei && ei.getMessage().contains("Owner did not join after forking subtasks")) {
                var te = new TimeoutException(ei.getMessage());
                o.scope.setTimeout(false, te);
                exceptions.add(te);
            } else {
                exceptions.add(e);
            }
            TGS_UnSafe.throwIfInterruptedException(e);
        } finally {
            this.elapsed = elapsedTracker.elapsed_now();
        }
    }
    final public Duration elapsed;

    public boolean timeout() {
        var timeoutExists = exceptions.stream()
                .filter(e -> e instanceof TimeoutException)
                .findAny().isPresent();
        var shutdownBugExists = exceptions.stream()
                .filter(e -> e instanceof IllegalStateException ei && ei.getMessage().contains("Owner did not join after forking subtasks"))
                .findAny().isPresent();
        return timeoutExists || shutdownBugExists;
    }
    public List<StructuredTaskScope.Subtask.State> states = TGS_ListUtils.of();
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
//            return TGS_CallableVoid.of();
//        }));
//        return TS_ThreadAsyncCoreParallelUntilFirstFail.of(killTrigger, duration, callables);
//    }
}
