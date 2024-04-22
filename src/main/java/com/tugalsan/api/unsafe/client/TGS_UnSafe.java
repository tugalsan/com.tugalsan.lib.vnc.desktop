package com.tugalsan.api.unsafe.client;

import com.tugalsan.api.callable.client.*;
import com.tugalsan.api.runnable.client.*;
import java.util.concurrent.Callable;

public class TGS_UnSafe {

    public static RuntimeException toRuntimeException(CharSequence className, CharSequence funcName, Object errorContent) {
        throw new RuntimeException("CLASS[" + className + "] -> FUNC[" + funcName + "] -> ERR: " + errorContent);
    }

    public static void thrw(CharSequence className, CharSequence funcName, Object errorContent) {
        throw toRuntimeException(className, funcName, errorContent);
    }

    public static <R> R thrwReturns(CharSequence className, CharSequence funcName, Object errorContent) {
        throw toRuntimeException(className, funcName, errorContent);
    }

    public static void thrw(Throwable t) {
        throw new RuntimeException(t);
    }

    public static <R> R thrwReturns(Throwable t) {
        throw new RuntimeException(t);
    }

    public static void run(TGS_UnSafeRunnable exe) {
        run(exe, null);
    }

    public static void run(TGS_UnSafeRunnable exe, TGS_RunnableType1<Exception> exception) {
        run(exe, exception, null);
    }

    public static void throwIfInterruptedException(Exception e) {
        if (e instanceof InterruptedException) {// U NEED THIS SO STRUCTURED SCOPE CAN ABLE TO SHUT DOWN
            throw new TGS_UnSafeInterruptedException((InterruptedException) e);
        }
    }

    public static void run(TGS_UnSafeRunnable exe, TGS_RunnableType1<Exception> exception, TGS_Runnable finalExe) {
        try {
            if (exe != null) {
                exe.run();
            }
        } catch (Exception e) {
            if (exception == null) {
                throw new RuntimeException(e);
            }
            exception.run(e);
        } finally {
            if (finalExe != null) {
                finalExe.run();
            }
        }
    }

    public static <R> R call(Callable<R> cmp) {
        return call(cmp, null);
    }

    public static <R> R call(Callable<R> cmp, TGS_CallableType1<R, Exception> exception) {
        return call(cmp, exception, null);
    }

    public static <R> R call(Callable<R> cmp, TGS_CallableType1<R, Exception> exception, TGS_Runnable finalExe) {
        try {
            return cmp.call();
        } catch (Exception e) {
            throwIfInterruptedException(e);
            if (exception == null) {
                throw new RuntimeException(e);
            }
            return exception.call(e);
        } finally {
            if (finalExe != null) {
                finalExe.run();
            }
        }
    }

    public static void runNothing() {
    }

    public static <R> R callNull() {
        return null;
    }

    public static <R> R callValue(R result) {
        return result;
    }
}
