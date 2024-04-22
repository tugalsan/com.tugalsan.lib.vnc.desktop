package com.tugalsan.api.tuple.client;

import java.io.Serializable;
import java.util.Objects;

public class TGS_Tuple1<A> implements Serializable {

    public TGS_Tuple1() {//DTO
    }

    public TGS_Tuple1(A value0) {
        this.value0 = value0;
    }
    public A value0;

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + value0 + "]";
    }

    public boolean isEmpty() {
        return value0 == null;
    }

    public boolean isPresent() {
        return !isEmpty();
    }

    public static <A> TGS_Tuple1<A> of(A value0) {
        return new TGS_Tuple1(value0);
    }

    public static <A> TGS_Tuple1<A> of() {
        return new TGS_Tuple1();
    }

    public TGS_Tuple1<A> cloneIt() {
        return TGS_Tuple1.of(value0);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TGS_Tuple1)) {
            return false;
        }
        var t = (TGS_Tuple1) obj;
        return Objects.equals(t.value0, value0);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value0);
    }
}
