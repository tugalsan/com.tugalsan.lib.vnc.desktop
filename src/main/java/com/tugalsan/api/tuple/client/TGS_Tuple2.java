package com.tugalsan.api.tuple.client;

import java.io.Serializable;
import java.util.Objects;

public class TGS_Tuple2<A, B> implements Serializable/*implements IsSerializable*/ {

    public TGS_Tuple2() {//DTO
    }

    public TGS_Tuple2(A value0, B value1) {
        this.value0 = value0;
        this.value1 = value1;
    }
    public A value0;
    public B value1;

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + value0 + "," + value1 + "]";
    }

    public TGS_Tuple2<A, B> set(A value0, B value1) {
        this.value0 = value0;
        this.value1 = value1;
        return this;
    }

    public boolean isEmpty() {
        return value0 == null;
    }

    public boolean isPresent() {
        return !isEmpty();
    }

    public static <A, B> TGS_Tuple2<A, B> of(A value0, B value1) {
        return new TGS_Tuple2(value0, value1);
    }

    public static <A, B> TGS_Tuple2<A, B> of() {
        return new TGS_Tuple2();
    }

    public TGS_Tuple2<A, B> cloneIt() {
        return TGS_Tuple2.of(value0, value1);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TGS_Tuple2)) {
            return false;
        }
        var t = (TGS_Tuple2) obj;
        return Objects.equals(t.value0, value0)
                && Objects.equals(t.value1, value1);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value0, value1);
    }
}
