package com.tugalsan.api.log.server;

import com.tugalsan.api.callable.client.*;
import com.tugalsan.api.log.client.*;
import com.tugalsan.api.tuple.client.*;
import com.tugalsan.api.string.client.*;
import com.tugalsan.api.unsafe.client.*;
import java.util.*;
import java.util.stream.*;

public class TS_Log implements TGS_LogInterface {

    public static TS_Log of(Class clazz) {
        return new TS_Log(clazz);
    }

    public static TS_Log of(boolean infoEnable, Class clazz) {
        return new TS_Log(infoEnable, clazz);
    }

    public TS_Log(Class clazz) {
        this(false, clazz);
    }

    public TS_Log(boolean infoEnable, Class clazz) {
        this(infoEnable, TGS_Log.isFullNamed(clazz) ? clazz.getName() : clazz.getSimpleName());
    }

    private TS_Log(boolean infoEnable, CharSequence className) {
        this.className = className.toString();
        this.infoEnable = infoEnable;
    }
    final public String className;

    public boolean infoEnable = false;

    @Override
    public void cl(CharSequence funcName, CharSequence text, CharSequence url) {
        debug(TGS_Log.TYPE_LNK(), className, funcName, text, url);
    }

    @Override
    public void ci(CharSequence funcName, TGS_Callable<Object> callable) {
        if (!infoEnable) {
            return;
        }
        ci(funcName, callable.call());
    }

    @Override
    public void ci(CharSequence funcName, Object... oa) {
        if (!infoEnable) {
            return;
        }
        debug(TGS_Log.TYPE_INF(), className, funcName, oa);
    }

    @Override
    public void cr(CharSequence funcName, Object... oa) {
        debug(TGS_Log.TYPE_RES(), className, funcName, oa);
    }

    @Override
    public void ct(CharSequence funcName, Throwable t) {
        TGS_UnSafe.run(() -> debug(TGS_Log.TYPE_THR(), className, funcName, t), e -> TGS_UnSafe.runNothing());
    }

    @Override
    public void ce(CharSequence funcName, Object... oa) {
        debug(TGS_Log.TYPE_ERR(), className, funcName, oa);
    }

    private static void debug(int type, Object... oa) {
        if (oa == null || oa.length == 0) {
            return;
        }
        var sjMain = new StringJoiner("}, {", "{", "}");
        Arrays.stream(oa).forEachOrdered(o -> {
            String str;
            if (o == null) {
                str = String.valueOf(o);
            } else if (o instanceof Throwable thr) {
                str = TGS_StringUtils.toString(thr);
            } else if (o instanceof Stream stream) {
                var sjList = new StringJoiner("], [", "[", "]");
                stream.forEachOrdered(oi -> sjList.add(String.valueOf(oi)));
                str = sjList.toString();
            } else if (o instanceof List lst) {
                var sjList = new StringJoiner("], [", "[", "]");
                lst.stream().forEachOrdered(oi -> sjList.add(String.valueOf(oi)));
                str = sjList.toString();
            } else if (o instanceof Object[] arr) {
                var sjList = new StringJoiner("], [", "[", "]");
                Arrays.stream(arr).forEachOrdered(oi -> sjList.add(String.valueOf(oi)));
                str = sjList.toString();
            } else {
                str = String.valueOf(o);
            }
            sjMain.add(str);
        });
        if (Objects.equals(type, TGS_Log.TYPE_LNK())) {
            TS_LogUtils.link(sjMain.toString());
            return;
        }
        if (Objects.equals(type, TGS_Log.TYPE_INF())) {
            TS_LogUtils.info(sjMain.toString());
            return;
        }
        if (Objects.equals(type, TGS_Log.TYPE_RES())) {
            TS_LogUtils.result(sjMain.toString());
            return;
        }
        if (Objects.equals(type, TGS_Log.TYPE_THR())) {
            TS_LogUtils.error(sjMain.toString());
            return;
        }
        if (Objects.equals(type, TGS_Log.TYPE_ERR())) {
            TS_LogUtils.error(sjMain.toString());
            return;
        }
        TS_LogUtils.plain(sjMain.toString());
    }

    public TGS_Tuple3<String, Boolean, String> createFuncBoolean(CharSequence funcName) {
        return new TGS_Tuple3(className + "." + funcName, false, "init");
    }

    public TGS_Tuple3<String, Boolean, String> returnError(TGS_Tuple3<String, Boolean, String> result, CharSequence errText) {
        result.value2 = errText.toString();
        ce(result.value0, result.value2);
        return result;
    }

    public TGS_Tuple3<String, Boolean, String> returnTrue(TGS_Tuple3<String, Boolean, String> result) {
        result.value1 = true;
        return result;
    }
}
