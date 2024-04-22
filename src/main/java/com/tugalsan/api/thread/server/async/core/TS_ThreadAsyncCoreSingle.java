package com.tugalsan.api.thread.server.async.core;

import com.tugalsan.api.callable.client.TGS_CallableType1;
import com.tugalsan.api.thread.server.sync.TS_ThreadSyncTrigger;
import com.tugalsan.api.time.server.TS_TimeElapsed;
import java.time.Duration;
import java.util.Optional;

//IMPLEMENTATION OF https://www.youtube.com/watch?v=_fRN7tpLyPk
public class TS_ThreadAsyncCoreSingle<T> {

    //until: Instant.now().plusMillis(10)
    private TS_ThreadAsyncCoreSingle(TS_ThreadSyncTrigger killTrigger, Duration duration, TGS_CallableType1<T, TS_ThreadSyncTrigger> callable) {
        var elapsedTracker = TS_TimeElapsed.of();
        resultIfSuccessful = Optional.of(callable.call(killTrigger));
        exceptionIfFailed = Optional.empty();
        this.elapsed = elapsedTracker.elapsed_now();
    }
    final public Duration elapsed;

    public boolean timeout() {
        return exceptionIfFailed.isPresent() && exceptionIfFailed.get() instanceof TS_ThreadAsyncCoreTimeoutException;
    }
    public Optional<T> resultIfSuccessful;
    public Optional<Throwable> exceptionIfFailed;

    public boolean hasError() {
        return exceptionIfFailed.isPresent();
    }

    public static <T> TS_ThreadAsyncCoreSingle<T> of(TS_ThreadSyncTrigger killTrigger, Duration duration, TGS_CallableType1<T, TS_ThreadSyncTrigger> callable) {
        return new TS_ThreadAsyncCoreSingle(killTrigger, duration, callable);
    }
}
