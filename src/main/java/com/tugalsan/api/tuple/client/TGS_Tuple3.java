package com.tugalsan.api.tuple.client;

import java.io.Serializable;
import java.util.Objects;

public class TGS_Tuple3<A, B, C> implements Serializable {

    public TGS_Tuple3() {//DTO
    }

    public TGS_Tuple3(A value0, B value1, C value2) {
        this.value0 = value0;
        this.value1 = value1;
        this.value2 = value2;
    }
    public A value0;
    public B value1;
    public C value2;

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + value0 + "," + value1 + "," + value2 + "]";
    }

    public boolean isEmpty() {
        return value0 == null;
    }

    public boolean isPresent() {
        return !isEmpty();
    }

    public static <A, B, C> TGS_Tuple3<A, B, C> of(A value0, B value1, C value2) {
        return new TGS_Tuple3(value0, value1, value2);
    }

    public static <A, B, C> TGS_Tuple3<A, B, C> of() {
        return new TGS_Tuple3();
    }

    public TGS_Tuple3<A, B, C> cloneIt() {
        return TGS_Tuple3.of(value0, value1, value2);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TGS_Tuple3)) {
            return false;
        }
        var t = (TGS_Tuple3) obj;
        return Objects.equals(t.value0, value0)
                && Objects.equals(t.value1, value1)
                && Objects.equals(t.value2, value2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value0, value1, value2);
    }
}
