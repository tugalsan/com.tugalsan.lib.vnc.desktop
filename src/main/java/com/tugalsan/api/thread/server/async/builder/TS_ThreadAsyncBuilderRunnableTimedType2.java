package com.tugalsan.api.thread.server.async.builder;

import com.tugalsan.api.runnable.client.TGS_RunnableType2;
import com.tugalsan.api.thread.server.sync.TS_ThreadSyncTrigger;
import java.time.Duration;
import java.util.Optional;

public class TS_ThreadAsyncBuilderRunnableTimedType2<B> {

    private TS_ThreadAsyncBuilderRunnableTimedType2(Optional<Duration> max, Optional<TGS_RunnableType2<TS_ThreadSyncTrigger, B>> run) {
        this.max = max;
        this.run = run;
    }
    final public Optional<Duration> max;
    final public Optional<TGS_RunnableType2<TS_ThreadSyncTrigger, B>> run;

    public static TS_ThreadAsyncBuilderRunnableTimedType2 empty() {
        return new TS_ThreadAsyncBuilderRunnableTimedType2(Optional.empty(), Optional.empty());
    }

    public static <killTrigger, B> TS_ThreadAsyncBuilderRunnableTimedType2<B> run(TGS_RunnableType2<killTrigger, B> run) {
        return new TS_ThreadAsyncBuilderRunnableTimedType2(Optional.empty(), Optional.of(run));
    }

    public static <killTrigger, B> TS_ThreadAsyncBuilderRunnableTimedType2<B> maxTimedRun(Duration max, TGS_RunnableType2<killTrigger, B> run) {
        return new TS_ThreadAsyncBuilderRunnableTimedType2(Optional.of(max), Optional.of(run));
    }

    @Override
    public String toString() {
        return TS_ThreadAsyncBuilderRunnableTimedType2.class.getSimpleName() + "{" + "max=" + max + ", run=" + run + '}';
    }
}
