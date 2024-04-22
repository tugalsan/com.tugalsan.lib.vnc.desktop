package com.tugalsan.api.unsafe.client;

import com.tugalsan.api.callable.client.TGS_CallableType1;
import com.tugalsan.api.runnable.client.TGS_Runnable;
import com.tugalsan.api.runnable.client.TGS_RunnableType1;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;

public class TGS_UnSafe {

    //-------------------CUSTOM EXCEPTION
    public static RuntimeException toRuntimeException(CharSequence className, CharSequence funcName, Object errorContent) {
        return new RuntimeException(TGS_UnSafe.class + ".toRuntimeException->CLASS[" + className + "] -> FUNC[" + funcName + "] -> ERR: " + errorContent);
    }

    public static <R> R thrw(CharSequence className, CharSequence funcName, Object errorContent) {
        throw toRuntimeException(className, funcName, errorContent);
    }

    //-------------------- INTERRUPTED EXCEPTION ----------------
    public static <R> R throwIfInterruptedException(Throwable t) {
        if (isInterruptedException(t)) {// U NEED THIS SO STRUCTURED SCOPE CAN ABLE TO SHUT DOWN
            Thread.currentThread().interrupt();//WARNING FOR GWT: https://stackoverflow.com/questions/78271237/adding-standard-java-classes-that-are-missing-in-gwt
//        if (t instanceof RuntimeException) {// I WANNA WATCH PROPAGATION
//            throw (RuntimeException) t;
//        }
            throw new RuntimeException(t);
        }
        return callNull();
    }

    public static Optional<TimeoutException> getTimeoutException(Throwable t) {
        if (t instanceof TimeoutException) {
            return Optional.of((TimeoutException) t);
        }
        if (t.getCause() != null) {
            return getTimeoutException(t.getCause());
        }
        return Optional.empty();
    }

    public static Optional<InterruptedException> getInterruptedException(Throwable t) {
        if (t instanceof InterruptedException) {
            return Optional.of((InterruptedException) t);
        }
        if (t.getCause() != null) {
            return getInterruptedException(t.getCause());
        }
        return Optional.empty();
    }

    public static boolean isInterruptedException(Throwable t) {
        if (t instanceof InterruptedException) {
            return true;
        }
        if (t.getCause() != null) {
            return isInterruptedException(t.getCause());
        }
        return false;
    }

    public static <R> R thrw(Throwable t) {
        throwIfInterruptedException(t);
//        if (t instanceof RuntimeException) {// I WANNA WATCH PROPAGATION
//            throw (RuntimeException) t;
//        }
        throw new RuntimeException(t);
    }

    //--------------------------DO NOTHING
    public static void runNothing() {
    }

    public static <R> R callNull() {
        return null;
    }

//    public static <R> R callValue(R result) {
//        return result;
//    }
    //---------------------RUN---------------------
    public static void run(TGS_UnSafeRunnable exe) {
        run(exe, null);
    }

    public static void run(TGS_UnSafeRunnable exe, TGS_RunnableType1<Exception> exception) {
        run(exe, exception, null);
    }

    public static void run(TGS_UnSafeRunnable exe, TGS_RunnableType1<Exception> exception, TGS_Runnable finalExe) {
        try {
            if (exe != null) {
                exe.run();
            }
        } catch (Exception e) {
            throwIfInterruptedException(e);
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

    //---------------------CALL---------------------
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
}
