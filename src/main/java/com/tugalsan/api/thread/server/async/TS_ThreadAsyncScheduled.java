package com.tugalsan.api.thread.server.async;

import com.tugalsan.api.runnable.client.TGS_RunnableType1;
import com.tugalsan.api.thread.server.sync.TS_ThreadSyncTrigger;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

//USE TS_ThreadAsyncBuilder with killTrigger if possible
public class TS_ThreadAsyncScheduled {

    final private static ScheduledExecutorService SCHEDULED = Executors.newSingleThreadScheduledExecutor(Thread.ofVirtual().factory());

    public static void destroy() {
        SCHEDULED.shutdown();
    }

    private static void _scheduleAtFixedRate(TS_ThreadSyncTrigger killTrigger, Runnable exe, long initialDelay, long period, TimeUnit unit) {
        var future = SCHEDULED.scheduleAtFixedRate(exe, initialDelay, period, unit);
        TS_ThreadAsyncBuilder.of(killTrigger).mainDummyForCycle()
                .fin(() -> future.cancel(false))
                .cycle_mainValidation_mainPeriod(o -> !future.isCancelled() && !future.isDone(), Duration.ofMinutes(1))
                .asyncRun();
    }

    private static void _scheduleAtFixedRate(TS_ThreadSyncTrigger killTrigger, Duration until, TGS_RunnableType1<TS_ThreadSyncTrigger> exe, long initialDelay, long period, TimeUnit unit) {
        Runnable exe2 = () -> {
            TS_ThreadAsyncAwait.runUntil(killTrigger, until, kt -> {
                exe.run(kt);
            });
        };
        _scheduleAtFixedRate(killTrigger, exe2, initialDelay, period, unit);
    }

    public static void every(TS_ThreadSyncTrigger killTrigger, Duration until, boolean startNow, Duration initialDelayAndPeriod, TGS_RunnableType1<TS_ThreadSyncTrigger> exe) {
        everySeconds(killTrigger, until, startNow, initialDelayAndPeriod.toSeconds(), exe);
    }

    public static void everySeconds(TS_ThreadSyncTrigger killTrigger, Duration until, boolean startNow, long initialDelayAndPeriod, TGS_RunnableType1<TS_ThreadSyncTrigger> exe) {
        _scheduleAtFixedRate(killTrigger, until, exe, startNow ? 0 : initialDelayAndPeriod, initialDelayAndPeriod, TimeUnit.SECONDS);
    }

    public static void everyMinutes(TS_ThreadSyncTrigger killTrigger, Duration until, boolean startNow, long initialDelayAndPeriod, TGS_RunnableType1<TS_ThreadSyncTrigger> exe) {
        _scheduleAtFixedRate(killTrigger, until, exe, startNow ? 0 : initialDelayAndPeriod, initialDelayAndPeriod, TimeUnit.MINUTES);
    }

    public static void everyHours(TS_ThreadSyncTrigger killTrigger, Duration until, boolean startNow, long initialDelayAndPeriod, TGS_RunnableType1<TS_ThreadSyncTrigger> exe) {
        _scheduleAtFixedRate(killTrigger, until, exe, startNow ? 0 : initialDelayAndPeriod, initialDelayAndPeriod, TimeUnit.HOURS);
    }

    public static void everyDays(TS_ThreadSyncTrigger killTrigger, Duration until, boolean startNow, long initialDelayAndPeriod, TGS_RunnableType1<TS_ThreadSyncTrigger> exe) {
        _scheduleAtFixedRate(killTrigger, until, exe, startNow ? 0 : initialDelayAndPeriod, initialDelayAndPeriod, TimeUnit.DAYS);
    }
}
