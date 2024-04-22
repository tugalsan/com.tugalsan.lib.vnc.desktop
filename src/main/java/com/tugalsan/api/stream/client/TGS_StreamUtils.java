package com.tugalsan.api.stream.client;

import com.tugalsan.api.unsafe.client.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import java.util.stream.*;

public class TGS_StreamUtils {

    public static Consumer<Object> runNothing() {
        Consumer<Object> NOOP = whatever -> {
        };
        return NOOP;
    }

    //https://stackoverflow.com/questions/23699371/java-8-distinct-by-property
    public static <T> Predicate<T> filterDistinct(Function<? super T, ?> key) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(key.apply(t));
    }

    public static <T> List<T> toLst(Stream<T> map) {
        return map.collect(Collectors.toCollection(ArrayList::new));
    }

    public static List<Integer> toLst(IntStream map) {
        return map.boxed().collect(Collectors.toCollection(ArrayList::new));
    }

    public static List<Long> toLst(LongStream map) {
        return map.boxed().collect(Collectors.toCollection(ArrayList::new));
    }

    public static List<Double> toLst(DoubleStream map) {
        return map.boxed().collect(Collectors.toCollection(ArrayList::new));
    }

    public static <T> Stream<T> of(Iterable<T> iterable) {
        return TGS_StreamUtils.of(iterable.spliterator());
    }

    public static <T> Stream<T> of(Spliterator<T> spliterator) {
        return StreamSupport.stream(spliterator, false);
    }

    @FunctionalInterface
    public static interface FunctionWithException<T, R, E extends Exception> {

        R apply(T t) throws E;
    }

    public static <T, R, E extends Exception> Function<T, R> thr1(FunctionWithException<T, R, E> fe) {//USE IT FOR CATCING EXCP IN STREAMS
        return arg -> TGS_UnSafe.call(() -> fe.apply(arg));
    }

//    public static IntStream iterate(int start, int end) {
//        return iterate(start, end, 1);
//    }
//
//    public static IntStream iterate(int start, int end, int by) {
//        if (start <= end) {
//            return forward(start, end, by);
//        } else {
//            return reverse(start, end, by);
//        }
//    }
    public static IntStream countDownTo0(int from) {
        return reverse(0, from + 1);
    }

    public static IntStream forward(int from0, int to10_notEnclosed, int by) {
        if (by == 0 || to10_notEnclosed <= from0) {
            return IntStream.empty();
        }
//        return IntStream.iterate(from0, i -> i < to10_notEnclosed, i -> i + (by < 0 ? -by : by));//gwt does not like u
//        return IntStream.iterate(from0, i -> i + (by < 0 ? -by : by)).takeWhile(i -> i < to10_notEnclosed);//gwt does not like u
//        return IntStream.iterate(from0, i -> i + (by < 0 ? -by : by)).limit((to10_notEnclosed - from0) / by);//not working
        List<Integer> buffer = new ArrayList();
        for (var i = from0; i < to10_notEnclosed; i += by) {
            buffer.add(i);
        }
        return buffer.stream().mapToInt(i -> i);
    }

    public static IntStream forward(int from0, int to10_notEnclosed) {
        return forward(from0, to10_notEnclosed, 1);
    }

    public static IntStream reverse(int from0, int to10_notEnclosed, int by) {
        var forward = forward(from0, to10_notEnclosed, by).toArray();
        if (forward.length == 0) {
            return IntStream.empty();
        }
        List<Integer> list = Arrays.stream(forward).boxed()
                .collect(Collectors.toCollection(ArrayList<Integer>::new));
        Collections.reverse(list);
        return list.stream().mapToInt(Integer::intValue);
    }

    public static IntStream reverse(int from0, int to10_notEnclosed) {
//        System.out.println("reverse from/to:" + from + "/" + to);
        return reverse(from0, to10_notEnclosed, 1);
    }

    public static <T> Stream<T> of(Enumeration<T> enumeration) {
        //return of(enumeration.asIterator());//GWT does not like u; check on 2.10 version again!
        return of(new Iterator<>() {
            @Override
            public boolean hasNext() {
                return enumeration.hasMoreElements();
            }

            @Override
            public T next() {
                return enumeration.nextElement();
            }
        });
    }

    public static <T> Stream<T> of(Iterator<T> sourceIterator) {
        return of(sourceIterator, false);
    }

    public static <T> Stream<T> of(Iterator<T> sourceIterator, boolean parallel) {
        Iterable<T> iterable = () -> sourceIterator;
        return StreamSupport.stream(iterable.spliterator(), parallel);
    }

    public static Stream<Boolean> of(boolean[] array) {
        Stream.Builder<Boolean> builder = Stream.builder();
        IntStream.range(0, array.length).forEachOrdered(i -> builder.add(array[i]));// there is no Arrays.stream(boolean[]) :(
        return builder.build();
    }

    public static class Options {

        public void stop() {
            isStop = true;
        }
        private boolean isStop = false;

        boolean isStop() {
            return isStop;
        }

        //-----------------------------------
        public void setOffet(int newValue) {
            value = newValue;
        }
        private int value = 0;

        private int getOffset() {
            return value;
        }

        public void incOffset() {
            value++;
        }

        public void decOffset() {
            value--;
        }
    }

    public static void forEachOptions(IntStream stream, BiConsumer<Integer, Options> consumer) {
        Spliterator<Integer> s = stream.spliterator();
        var hadNext = true;
        var b = new Options();
        while (hadNext && !b.isStop()) {
            hadNext = s.tryAdvance(i -> consumer.accept(i, b));
        }
    }

    public static <T> void forEachOptions(Stream<T> stream, BiConsumer<T, Options> consumer) {
        Spliterator<T> s = stream.spliterator();
        var hadNext = true;
        var b = new Options();
        while (hadNext && !b.isStop()) {
            hadNext = s.tryAdvance(o -> consumer.accept(o, b));
        }
    }
}
