package com.tugalsan.api.union.client;

import com.tugalsan.api.callable.client.TGS_CallableType1;
import java.util.Objects;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public record TGS_UnionExcuse<T>(T value, Throwable excuse) {

    public static <T> TGS_UnionExcuse<T> ofExcuse(CharSequence className, CharSequence funcName, Object excuse) {
        return ofExcuse(
                new RuntimeException(
                        "CLASS[" + className + "].FUNC[" + funcName + "].EXCUSE: " + excuse
                )
        );
    }

    public T value() {
        if (value == null) {
            throw new UnsupportedOperationException("union is an excuse");
        }
        return value;
    }

    public Throwable excuse() {
        if (excuse == null) {
            throw new UnsupportedOperationException("union is a value");
        }
        return excuse;
    }

    public <T> TGS_UnionExcuse<T> toExcuse() {
        return TGS_UnionExcuse.ofExcuse(excuse);
    }

    public TGS_UnionExcuseVoid toExcuseVoid() {
        return TGS_UnionExcuseVoid.ofExcuse(excuse);
    }

    public static <T> TGS_UnionExcuse<T> ofExcuse(Throwable excuse) {
        return new TGS_UnionExcuse(null, excuse);
    }

    public static <T> TGS_UnionExcuse<T> ofEmpty_NullPointerException() {
        return of(null);
    }

    public static <T> TGS_UnionExcuse<T> of(T value) {
        return value == null
                ? ofExcuse(new UnsupportedOperationException("value is not introduced"))
                : new TGS_UnionExcuse(value, null);
    }

    public boolean isExcuse() {
        return excuse != null;
    }

    public void ifPresent(Consumer<? super T> action) {
        if (value != null) {
            action.accept(value);
        }
    }

    public T orElse(TGS_CallableType1<T, Throwable> excuse) {
        return value != null ? value : excuse.call(this.excuse);
    }

    public boolean isPresent() {
        return value != null;
    }

    public boolean isExcuseTimeout() {
        return excuse != null && excuse instanceof TimeoutException;
    }

    public boolean isExcuseInterrupt() {
        return excuse != null && excuse instanceof InterruptedException;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + Objects.hashCode(this.value);
        hash = 59 * hash + Objects.hashCode(this.excuse);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TGS_UnionExcuse<?> other = (TGS_UnionExcuse<?>) obj;
        if (!Objects.equals(this.value, other.value)) {
            return false;
        }
        return Objects.equals(this.excuse, other.excuse);
    }

    @Override
    public String toString() {
        if (isExcuse()) {
            return TGS_UnionExcuse.class.getSimpleName() + "{excuse=" + excuse + '}';
        }
        return String.valueOf(value);
    }
}
