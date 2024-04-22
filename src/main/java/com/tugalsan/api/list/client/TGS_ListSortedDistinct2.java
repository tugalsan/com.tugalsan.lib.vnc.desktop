package com.tugalsan.api.list.client;

import java.util.*;
import java.util.stream.*;

public class TGS_ListSortedDistinct2<K, V> {

    final public TreeMap<K, V> driver = new TreeMap();

    public TGS_ListSortedDistinct2<K, V> clear() {
        driver.clear();
        return this;
    }

    public int size() {
        return driver.size();
    }

    public TGS_ListSortedDistinct2<K, V> add(K distinctKey, V value) {
        driver.put(distinctKey, value);
        return this;
    }

    public V getValue(int idx) {
        return streamValues(false).skip(idx).findFirst().orElse(null);
    }

    public K getKey(int idx) {
        return streamKeys(false).skip(idx).findFirst().orElse(null);
    }

    public TGS_ListSortedDistinct2<K, V> removeItem(K distinctKey) {
        driver.remove(distinctKey);
        return this;
    }

    public Stream<V> streamValues(boolean parallel) {
        return parallel ? driver.values().parallelStream() : driver.values().stream();
    }

    public Stream<K> streamKeys(boolean parallel) {
        return parallel ? driver.keySet().parallelStream() : driver.keySet().stream();
    }

    public List<K> toListKeys() {
        return new ArrayList(driver.keySet());
    }

    public List<V> toListValues() {
        return new ArrayList(driver.values());
    }

}
