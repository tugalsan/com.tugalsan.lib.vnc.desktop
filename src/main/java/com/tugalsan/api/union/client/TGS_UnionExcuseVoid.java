package com.tugalsan.api.union.client;

import java.util.concurrent.TimeoutException;

public record TGS_UnionExcuseVoid(Throwable excuse) {

    public static TGS_UnionExcuseVoid ofExcuse(CharSequence className, CharSequence funcName, Object excuse) {
        return ofExcuse(
                new RuntimeException(
                        "CLASS[" + className + "].FUNC[" + funcName + "].EXCUSE: " + excuse
                )
        );
    }

    public Throwable excuse() {
        if (excuse == null) {
            throw new UnsupportedOperationException("union is a void");
        }
        return excuse;
    }

    public static TGS_UnionExcuseVoid ofVoid() {
        return VOID;
    }
    final private static TGS_UnionExcuseVoid VOID = new TGS_UnionExcuseVoid(null);

    public <T> TGS_UnionExcuse<T> toExcuse() {
        return TGS_UnionExcuse.ofExcuse(excuse);
    }

    public static TGS_UnionExcuseVoid ofExcuse(Throwable excuse) {
        return new TGS_UnionExcuseVoid(excuse);
    }

    public boolean isVoid() {
        return excuse == null;
    }

    public boolean isExcuse() {
        return excuse != null;
    }

    public boolean isExcuseTimeout() {
        return isExcuseTimeout(excuse);
    }

    public boolean isExcuseInterrupt() {
        return isExcuseInterrupt(excuse);
    }

    private static boolean isExcuseInterrupt(Throwable t) {
        if (t == null) {
            return false;
        }
        if (t instanceof InterruptedException) {
            return true;
        }
        if (t.getCause() != null) {
            return isExcuseInterrupt(t.getCause());
        }
        return false;
    }

    private static boolean isExcuseTimeout(Throwable t) {
        if (t == null) {
            return false;
        }
        if (t instanceof TimeoutException) {
            return true;
        }
        if (t.getCause() != null) {
            return isExcuseTimeout(t.getCause());
        }
        return false;
    }

    @Override
    public String toString() {
        if (isVoid()) {
            return TGS_UnionExcuse.class.getSimpleName() + "{excuse=" + excuse + '}';
        }
        return "";
    }
}
