package com.tugalsan.api.thread.server;

import com.tugalsan.api.log.server.*;
import com.tugalsan.api.unsafe.client.*;
import java.lang.ref.*;
import java.lang.reflect.*;
import java.util.stream.*;

public class TS_ThreadCleanse {

    final private static TS_Log d = TS_Log.of(TS_ThreadCleanse.class);

    @Deprecated //IT IS POWERFULL, DO NOT USE ITs
    public static void cleanse() {
        TGS_UnSafe.run(() -> {
            var fieldLocal = Thread.class.getDeclaredField("threadLocals");
            fieldLocal.setAccessible(true);
            var fielsInheritable = Thread.class.getDeclaredField("inheritableThreadLocals");
            fielsInheritable.setAccessible(true);
            Thread.getAllStackTraces().keySet().forEach(thread -> {
                TGS_UnSafe.run(() -> {
                    cleanse(fieldLocal.get(thread));
                    cleanse(fielsInheritable.get(thread));
                }, e -> {
                    if (d.infoEnable) {
                        d.ce("cleanse.forEach", e.getMessage());
                    }
                });
            });
        }, e -> {
            if (d.infoEnable) {
                d.ce("cleanse", e.getMessage());
            }
        });
    }

    private static void cleanse(Object mapThread) {
        TGS_UnSafe.run(() -> {
            if (mapThread == null) {
                return;
            }
            var fieldTable = mapThread.getClass().getDeclaredField("table");
            fieldTable.setAccessible(true);
            var mapTable = fieldTable.get(mapThread);
            IntStream.range(0, Array.getLength(mapTable)).parallel().forEach(i -> {
                var entry = Array.get(mapTable, i);
                if (entry == null) {
                    return;
                }
                var threadLocal = ((WeakReference) entry).get();
                if (threadLocal == null) {
                    return;
                }
                if (threadLocal.getClass() != null
                        && threadLocal.getClass().getEnclosingClass() != null
                        && threadLocal.getClass().getEnclosingClass().getName() != null) {
                    d.ci("cleanse.map", threadLocal.getClass().getEnclosingClass().getName());
                } else if (threadLocal.getClass() != null && threadLocal.getClass().getName() != null) {
                    d.ci("cleanse.map", threadLocal.getClass().getName());
                } else {
                    d.ci("cleanse.map", "cannot identify threadlocal class name");
                }
                Array.set(mapTable, i, null);
            });
        }, e -> {
            if (d.infoEnable) {
                d.ce("cleanse.map", e.getMessage());
            }
        });
    }
}
