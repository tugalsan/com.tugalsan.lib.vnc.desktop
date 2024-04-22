package com.tugalsan.api.list.client;

import com.tugalsan.api.validator.client.TGS_ValidatorType2;
import java.util.*;

public class TGS_ListDistinctizeUtils {

    // NOT TESTED
//    public static int[] makeUnique(int... values) {
//        return Arrays.stream(values).distinct().toArray();
//    }
    // NOT TESTED
//    public static <T> T[] makeUnique(T... values) {
//        return Arrays.stream(values).distinct().toArray(new IntFunction<T[]>() {
//
//            @Override
//            public T[] apply(int length) {
//                return (T[]) Array.newInstance(values.getClass().getComponentType(), length);
//            }
//
//        });
//    }
//    public static <T> List<T> makeUnique(List<T> values) {
//        return values.stream().distinct()
//                            .TGS_StreamUtils.toLst(Collectors.toCollection(ArrayList::new));
//    }
    public static List<String> toUniqueString(List<String> values) {
        return new ArrayList(new HashSet(values));
    }

    public static List<Long> toUniqueLong(List<Long> values) {
        var primative = TGS_ListCastUtils.toPrimativeLong(values);
        var primativeUnique = toUniquePrimativeArrayLong(primative);
        return TGS_ListCastUtils.toLong(primativeUnique);
    }

    public static List<Double> toUniqueDouble(List<Double> values) {
        var primative = TGS_ListCastUtils.toPrimativeDouble(values);
        var primativeUnique = toUniquePrimativeArrayDouble(primative);
        return TGS_ListCastUtils.toDouble(primativeUnique);
    }

    public static long[] toUniquePrimativeArrayLong(long... values) {
        return Arrays.stream(values).distinct().toArray();
    }

    public static double[] toUniquePrimativeArrayDouble(double... values) {
        return Arrays.stream(values).distinct().toArray();
    }

    //BELOW CODE NOT WORKING
//    public static <T> List<T> distinctizeT(List<T> array, boolean parallel) {
//        if (parallel) {
//            return array.parallelStream().distinct()
//                            .TGS_StreamUtils.toLst(Collectors.toCollection(ArrayList::new));
//        }
//        return array.stream().distinct()
//                            .TGS_StreamUtils.toLst(Collectors.toCollection(ArrayList::new));
//    }
// NOT TESTED
//    public static int[] distinctizeArrayInt(int[] values, boolean parallel) {
//        if (parallel) {
//            return Arrays.stream(values).parallel().distinct().toArray();
//        }
//        return Arrays.stream(values).distinct().toArray();
//    }
// NOT TESTED
//    public static Integer[] distinctizeArrayInteger(Integer[] values, boolean parallel) {
//        if (parallel) {
//            return Arrays.stream(values).parallel().distinct().toArray(new IntFunction<Integer[]>() {
//
//                @Override
//                public Integer[] apply(int length) {
//                    return new Integer[length];
//                }
//
//            });
//        }
//        return Arrays.stream(values).distinct().toArray(new IntFunction<Integer[]>() {
//
//            @Override
//            public Integer[] apply(int length) {
//                return new Integer[length];
//            }
//
//        });
//    }
    public static <T> List<T> getUnique(List<T> listContainingDuplicates, TGS_ValidatorType2<T, T> equals) {
        List<T> listUnique = TGS_ListUtils.of();
        listContainingDuplicates.forEach(dirtyItem -> {
            if (listUnique.stream().filter(uniqueItem -> equals.validate(uniqueItem, dirtyItem)).findAny().isPresent()) {
                return;
            }
            listUnique.add(dirtyItem);
        });
        return listUnique;
    }

    public static <T> void makeUnique(List<T> listContainingDuplicates, TGS_ValidatorType2<T, T> equals) {
        var listUnique = getUnique(listContainingDuplicates, equals);
        listContainingDuplicates.clear();
        listContainingDuplicates.addAll(listUnique);
    }

    public static <T> List<T> getRepeatationsAsList(List<T> listContainingDuplicates) {
        return new ArrayList(getRepeatations(listContainingDuplicates));
    }

    public static <T> Set<T> getRepeatations(List<T> listContainingDuplicates) {
        var repeations = new HashSet<T>();
        var uniques = new HashSet<T>();
        for (var item : listContainingDuplicates) {
            if (!uniques.add(item)) {
                repeations.add(item);
            }
        }
        return repeations;
    }

    public static <T> List<T> getUniquestAsList(List<T> listContainingDuplicates) {
        return new ArrayList(getUniquest(listContainingDuplicates));
    }

    public static <T> Set<T> getUniquest(List<T> listContainingDuplicates) {
        var uniques = new HashSet<T>();
        for (var item : listContainingDuplicates) {
            uniques.add(item);
        }
        return uniques;
    }

}
