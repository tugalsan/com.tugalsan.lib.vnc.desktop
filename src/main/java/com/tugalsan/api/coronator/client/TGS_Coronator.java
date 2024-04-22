package com.tugalsan.api.coronator.client;

import com.tugalsan.api.callable.client.*;
import com.tugalsan.api.tuple.client.*;
import com.tugalsan.api.unsafe.client.*;
import com.tugalsan.api.validator.client.*;
import java.util.*;

public class TGS_Coronator<T> {

    //CONSTRUCTOR
    private T bufferedValue;

    public TGS_Coronator(T initVal) {
        bufferedValue = initVal;
    }

    public static <T> TGS_Coronator<T> of(Class<T> clazz) {
        return new TGS_Coronator(null);
    }

    public static <T> TGS_Coronator<T> of(T initialValue) {
        return new TGS_Coronator(initialValue);
    }

    public static TGS_Coronator<String> ofStr() {
        return new TGS_Coronator(null);
    }

    public static TGS_Coronator<Long> ofLng() {
        return new TGS_Coronator(null);
    }

    public static TGS_Coronator<Integer> ofInt() {
        return new TGS_Coronator(null);
    }

    public static TGS_Coronator<Double> ofDbl() {
        return new TGS_Coronator(null);
    }

    public static TGS_Coronator<Boolean> ofBool() {
        return new TGS_Coronator(null);
    }

    //LOADERS
    private final List<TGS_Tuple3<TGS_CallableType1<T, T>, TGS_ValidatorType1<T>, Type>> pack = new ArrayList();

    private enum Type {
        SKIPPER, STOPPER
    }

    public TGS_Coronator<T> anoint(TGS_CallableType1<T, T> val) {
        pack.add(new TGS_Tuple3(val, null, Type.SKIPPER));
        return this;
    }

    public TGS_Coronator<T> coronateIf(TGS_ValidatorType1<T> validate) {
        pack.add(new TGS_Tuple3(null, validate, Type.STOPPER));
        return this;
    }

    public TGS_Coronator<T> anointIf(TGS_ValidatorType1<T> validate, TGS_CallableType1<T, T> val) {
        pack.add(new TGS_Tuple3(val, validate, Type.SKIPPER));
        return this;
    }

    public TGS_Coronator<T> anointAndCoronateIf(TGS_ValidatorType1<T> validate, TGS_CallableType1<T, T> val) {
        pack.add(new TGS_Tuple3(val, validate, Type.STOPPER));
        return this;
    }

    //TODO coronateWithException EXCEPTION HANDLING, NOT TESTED
    @Deprecated
    public TGS_Tuple2<T, Exception> coronateWithException() {
        return TGS_UnSafe.call(() -> TGS_Tuple2.of(coronate(), null), e -> TGS_Tuple2.of(null, e));
    }

    public T coronateAs(TGS_CallableType1<T, T> val) {
        pack.add(new TGS_Tuple3(val, null, Type.STOPPER));
        return coronate();
    }

//FETCHER
    public T coronate() {
        for (var comp : pack) {
            var validator = comp.value1;
            if (validator != null && !validator.validate(bufferedValue)) {
                continue;
            }
            var setter = comp.value0;
            if (setter != null) {
                bufferedValue = setter.call(bufferedValue);
            }
            var type = comp.value2;
            if (type == Type.STOPPER) {
                return bufferedValue;
            }
        }
        return bufferedValue;
    }
}
