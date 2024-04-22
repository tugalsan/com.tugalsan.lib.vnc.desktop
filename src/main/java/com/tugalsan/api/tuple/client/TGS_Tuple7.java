package com.tugalsan.api.tuple.client;

import java.io.Serializable;
import java.util.Objects;

public class TGS_Tuple7<A, B, C, D, E, F, G> implements Serializable/*implements IsSerializable*/ {

    public TGS_Tuple7() {//DTO
    }

    public TGS_Tuple7(A value0, B value1, C value2, D value3, E value4, F value5, G value6) {
        this.value0 = value0;
        this.value1 = value1;
        this.value2 = value2;
        this.value3 = value3;
        this.value4 = value4;
        this.value5 = value5;
        this.value6 = value6;
    }
    public A value0;
    public B value1;
    public C value2;
    public D value3;
    public E value4;
    public F value5;
    public G value6;

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + value0 + "," + value1 + "," + value2 + "," + value3 + "," + value4 + "," + value5 + "," + value6 + "]";
    }

    public boolean isEmpty() {
        return value0 == null;
    }

    public boolean isPresent() {
        return !isEmpty();
    }

    public static <A, B, C, D, E, F, G> TGS_Tuple7<A, B, C, D, E, F, G> of(A value0, B value1, C value2, D value3, E value4, F value5, G value6) {
        return new TGS_Tuple7(value0, value1, value2, value3, value4, value5, value6);
    }

    public static <A, B, C, D, E, F, G> TGS_Tuple7<A, B, C, D, E, F, G> of() {
        return new TGS_Tuple7();
    }

    public TGS_Tuple7<A, B, C, D, E, F, G> cloneIt() {
        return TGS_Tuple7.of(value0, value1, value2, value3, value4, value5, value6);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TGS_Tuple7)) {
            return false;
        }
        var t = (TGS_Tuple7) obj;
        return Objects.equals(t.value0, value0)
                && Objects.equals(t.value1, value1)
                && Objects.equals(t.value2, value2)
                && Objects.equals(t.value3, value3)
                && Objects.equals(t.value4, value4)
                && Objects.equals(t.value5, value5)
                && Objects.equals(t.value6, value6);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value0, value1, value2, value3, value4, value5, value6);
    }
}
