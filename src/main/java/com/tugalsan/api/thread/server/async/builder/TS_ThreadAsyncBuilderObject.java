package com.tugalsan.api.thread.server.async.builder;

import com.tugalsan.api.list.client.TGS_ListUtils;
import com.tugalsan.api.log.server.TS_Log;
import com.tugalsan.api.thread.server.sync.TS_ThreadSyncTrigger;
import com.tugalsan.api.thread.server.async.TS_ThreadAsync;
import com.tugalsan.api.thread.server.async.TS_ThreadAsyncAwait;
import com.tugalsan.api.time.client.TGS_Time;
import com.tugalsan.api.unsafe.client.TGS_UnSafe;
import com.tugalsan.api.validator.client.TGS_ValidatorType1;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class TS_ThreadAsyncBuilderObject<T> {

    public static TS_Log d = TS_Log.of(false, TS_ThreadAsyncBuilderObject.class);

    private TS_ThreadAsyncBuilderObject(TS_ThreadSyncTrigger killTrigger, String name,
            TS_ThreadAsyncBuilderCallableTimed<T> init, TS_ThreadAsyncBuilderRunnableTimedType2<T> main, TS_ThreadAsyncBuilderRunnableTimedType1<T> fin,
            Optional<TGS_ValidatorType1<T>> valCycleMain, Optional<Duration> durPeriodCycle) {
        this.killTrigger = killTrigger;
        this.name = name;
        this.init = init;
        this.main = main;
        this.fin = fin;
        this.valCycleMain = valCycleMain;
        this.durPeriodCycle = durPeriodCycle;
    }
    final public TS_ThreadSyncTrigger killTrigger;
    final public String name;
    final public TS_ThreadAsyncBuilderCallableTimed<T> init;
    final public TS_ThreadAsyncBuilderRunnableTimedType2<T> main;
    final public TS_ThreadAsyncBuilderRunnableTimedType1<T> fin;
    final public Optional<Duration> durPeriodCycle;
    final public Optional<TGS_ValidatorType1<T>> valCycleMain;
    final public AtomicReference<T> initObject = new AtomicReference(null);

    @Override
    public String toString() {
        return TS_ThreadAsyncBuilderObject.class.getSimpleName() + "{" + "name=" + name + ", init=" + init + ", main=" + main + ", fin=" + fin + ", durPeriodCycle=" + durPeriodCycle + ", valCycleMain=" + valCycleMain + ", killTriggered=" + killTrigger + ", dead=" + dead + ", started=" + started + '}';
    }

    public void kill() {
        killTrigger.trigger();
    }

    public boolean isKillTriggered() {
        return killTrigger.hasTriggered();
    }

    public boolean isNotDead() {
        return !isDead();
    }

    public boolean isDead() {
        return dead.hasTriggered();
    }
    private final TS_ThreadSyncTrigger dead = TS_ThreadSyncTrigger.of();

    public boolean isStarted() {
        return started.hasTriggered();
    }
    private final TS_ThreadSyncTrigger started = TS_ThreadSyncTrigger.of();

    public boolean hasError() {
        return !exceptions.isEmpty();
    }
    public List<Throwable> exceptions = TGS_ListUtils.of();

    private void _run_init() {
        d.ci(name, "#init");
        if (init.call.isPresent()) {
            d.ci(name, "#init.call.isPresent()");
            if (init.max.isPresent()) {
                d.ci(name, "#init.max.isPresent()");
                var await = TS_ThreadAsyncAwait.runUntil(killTrigger, init.max.get(), kt -> initObject.set(init.call.get().call()));
                if (await.hasError()) {
                    d.ci(name, "#init.await.hasError()");
                    exceptions.add(await.exceptionIfFailed.get());
                    if (d.infoEnable) {
                        d.ce(name, exceptions);
                    }
                } else {
                    d.ci(name, "#init.await.!hasError()");
                }
            } else {
                d.ci(name, "#init.max.!isPresent()");
                initObject.set(init.call.get().call());
                if (hasError()) {
                    d.ci(name, "#init.run.!hasError()");
                } else {
                    d.ci(name, "#init.run.!hasError()");
                }
            }
        }
    }

    private void _run_main() {
        d.ci(name, "#main");
        if (main.run.isPresent()) {
            d.ci(name, "#main.run.isPresent()");
            while (true) {
                if (d.infoEnable) {
                    d.ci(name, "#main.tick." + TGS_Time.toString_timeOnly_now());
                }
                var msBegin = System.currentTimeMillis();
                if (isKillTriggered()) {
                    break;
                }
                if (valCycleMain.isPresent()) {
                    d.ci(name, "#main.valCycleMain.isPresent()");
                    if (!valCycleMain.get().validate(initObject.get())) {
                        d.ci(name, "#main.!valCycleMain.get().validate(initObject.get())");
                        break;
                    }
                }
                if (main.max.isPresent()) {
                    var await = TS_ThreadAsyncAwait.runUntil(killTrigger, main.max.get(), kt -> main.run.get().run(kt, initObject.get()));
                    if (await.hasError()) {
                        d.ci(name, "#main.await.hasError()");
                        exceptions.add(await.exceptionIfFailed.get());
                        if (d.infoEnable) {
                            d.ce(name, exceptions);
                        }
                        return;
                    } else {
                        d.ci(name, "#main.await.!hasError()");
                    }
                } else {
                    main.run.get().run(killTrigger, initObject.get());
                    if (hasError()) {// DO NOT STOP FINILIZE
                        d.ci(name, "#main.run.hasError()");
                        return;
                    } else {
                        d.ci(name, "#main.run.!hasError()");
                    }
                }
                if (!durPeriodCycle.isPresent() && !valCycleMain.isPresent()) {
                    d.ci(name, "#main.!durPeriodCycle.isPresent() && !valCycleMain.isPresent()");
                    break;
                } else {
                    d.ci(name, "#main.will continue");
                }
                if (durPeriodCycle.isPresent()) {
                    var msLoop = durPeriodCycle.get().toMillis();
                    var msEnd = System.currentTimeMillis();
                    var msSleep = msLoop - (msEnd - msBegin);
                    if (msSleep > 0) {
                        d.ci(name, "#main.later");
                        try {
                            Thread.sleep(msSleep);
                        } catch (InterruptedException ex) {
                            TGS_UnSafe.throwIfInterruptedException(ex);
                        }
                    } else {
                        d.ci(name, "#main.now");
                    }
                    Thread.yield();
                }
            }
        }
    }

    private void _run_fin() {
        d.ci(name, "#fin");
        if (fin.run.isPresent()) {
            d.ci(name, "#fin.run.isPresent()");
            if (fin.max.isPresent()) {
                d.ci(name, "#fin.max.isPresent()");
                var await = TS_ThreadAsyncAwait.runUntil(killTrigger, fin.max.get(), kt -> fin.run.get().run(initObject.get()));
                if (await.hasError()) {
                    d.ci(name, "#fin.await.hasError()");
                    exceptions.add(await.exceptionIfFailed.get());
                    if (d.infoEnable) {
                        d.ce(name, exceptions);
                    }
                } else {
                    d.ci(name, "#fin.await.!hasError()");
                }
            } else {
                d.ci(name, "fin.max.!isPresent()");
                fin.run.get().run(initObject.get());
                if (hasError()) {
                    d.ci(name, "#fin.run.hasError()");
                } else {
                    d.ci(name, "#fin.run.!hasError()");
                }
            }
        }
    }

    private void _run() {
        d.ci(name, "#run.live");
        _run_init();
        _run_main();
        _run_fin();
        d.ci(name, "#run.dead");
        dead.trigger();
    }

    public TS_ThreadAsyncBuilderObject<T> asyncRun() {
        if (isStarted()) {
            return this;
        }
        started.trigger();
        TS_ThreadAsync.now(killTrigger, kt -> _run());
        return this;
    }

    public TS_ThreadAsyncBuilderObject<T> asyncRun(Duration until) {
        if (isStarted()) {
            return this;
        }
        started.trigger();
        TS_ThreadAsync.until(killTrigger, until, kt -> _run());
        return this;
    }

    public TS_ThreadAsyncBuilderObject<T> asyncRunAwait() {
        return asyncRunAwait(null);
    }

    public TS_ThreadAsyncBuilderObject<T> asyncRunAwait(Duration until) {
        if (isStarted()) {
            return this;
        }
        started.trigger();
        TS_ThreadAsyncAwait.runUntil(killTrigger, until, kt -> _run());
        return this;
    }

    public static <T> TS_ThreadAsyncBuilderObject of(TS_ThreadSyncTrigger killTrigger, String name,
            TS_ThreadAsyncBuilderCallableTimed<T> init, TS_ThreadAsyncBuilderRunnableTimedType2<T> main, TS_ThreadAsyncBuilderRunnableTimedType1<T> fin,
            Optional<TGS_ValidatorType1<T>> valCycleMain, Optional<Duration> durPeriodCycle) {
        return new TS_ThreadAsyncBuilderObject(killTrigger, name, init, main, fin, valCycleMain, durPeriodCycle);
    }
}
