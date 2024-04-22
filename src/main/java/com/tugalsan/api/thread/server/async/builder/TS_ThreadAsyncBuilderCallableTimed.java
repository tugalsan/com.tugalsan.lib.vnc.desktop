package com.tugalsan.api.thread.server.async.builder;

import com.tugalsan.api.callable.client.TGS_Callable;
import java.time.Duration;
import java.util.Optional;

public class TS_ThreadAsyncBuilderCallableTimed<R> {

    private TS_ThreadAsyncBuilderCallableTimed(Optional<Duration> max, Optional<TGS_Callable<R>> call) {
        this.max = max;
        this.call = call;
    }
    final public Optional<Duration> max;
    final public Optional<TGS_Callable<R>> call;

    public static TS_ThreadAsyncBuilderCallableTimed<Object> of() {
        return new TS_ThreadAsyncBuilderCallableTimed(Optional.empty(), Optional.empty());
    }

    public static <R> TS_ThreadAsyncBuilderCallableTimed<R> of(TGS_Callable<R> call) {
        return new TS_ThreadAsyncBuilderCallableTimed(Optional.empty(), Optional.of(call));
    }

    public static <R> TS_ThreadAsyncBuilderCallableTimed<R> of(Duration max, TGS_Callable<R> call) {
        return new TS_ThreadAsyncBuilderCallableTimed(Optional.of(max), Optional.of(call));
    }

    @Override
    public String toString() {
        return TS_ThreadAsyncBuilderCallableTimed.class.getSimpleName() + "{" + "max=" + max + ", call=" + call + '}';
    }
}
