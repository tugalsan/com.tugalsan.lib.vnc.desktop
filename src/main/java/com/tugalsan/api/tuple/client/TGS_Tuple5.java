package com.tugalsan.api.tuple.client;

import java.io.Serializable;
import java.util.Objects;

public class TGS_Tuple5<A, B, C, D, E> implements Serializable {

    public TGS_Tuple5() {//DTO
    }

    public TGS_Tuple5(A value0, B value1, C value2, D value3, E value4) {
        this.value0 = value0;
        this.value1 = value1;
        this.value2 = value2;
        this.value3 = value3;
        this.value4 = value4;
    }
    public A value0;
    public B value1;
    public C value2;
    public D value3;
    public E value4;

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + value0 + "," + value1 + "," + value2 + "," + value3 + "," + value4 + "]";
    }

    public boolean isEmpty() {
        return value0 == null;
    }

    public boolean isPresent() {
        return !isEmpty();
    }

    public static <A, B, C, D, E> TGS_Tuple5<A, B, C, D, E> of(A value0, B value1, C value2, D value3, E value4) {
        return new TGS_Tuple5(value0, value1, value2, value3, value4);
    }

    public static <A, B, C, D, E> TGS_Tuple5<A, B, C, D, E> of() {
        return new TGS_Tuple5();
    }

    public TGS_Tuple5<A, B, C, D, E> cloneIt() {
        return TGS_Tuple5.of(value0, value1, value2, value3, value4);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TGS_Tuple5)) {
            return false;
        }
        var t = (TGS_Tuple5) obj;
        return Objects.equals(t.value0, value0)
                && Objects.equals(t.value1, value1)
                && Objects.equals(t.value2, value2)
                && Objects.equals(t.value3, value3)
                && Objects.equals(t.value4, value4);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value0, value1, value2, value3, value4);
    }
}
