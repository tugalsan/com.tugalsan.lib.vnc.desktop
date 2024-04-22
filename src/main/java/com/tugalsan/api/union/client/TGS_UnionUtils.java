package com.tugalsan.api.union.client;

public class TGS_UnionUtils {

//    @Deprecated //DO NOT USE IT FOR InterruptedException, SO use TS_UnionUtils on server side
//    public static <R> R throwAsRuntimeException(Throwable t) {
//        throw new RuntimeException(t);
//    }
//    protected static RuntimeException toRuntimeException(CharSequence className, CharSequence funcName, Object errorContent) {
//        return new RuntimeException(TGS_UnionUtils.class + ".toRuntimeException->CLASS[" + className + "] -> FUNC[" + funcName + "] -> ERR: " + errorContent);
//    }
//
//    public static <R> R throwAsRuntimeException(CharSequence className, CharSequence funcName, Object errorContent) {
//        throw toRuntimeException(className, funcName, errorContent);
//    }
    public static <R> R skipOrVoid() {
        return null;
    }
}
