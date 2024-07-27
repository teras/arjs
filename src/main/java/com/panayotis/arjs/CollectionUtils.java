package com.panayotis.arjs;

import java.util.*;

public class CollectionUtils {
    private CollectionUtils() {
    }

    static <K, V> Map<V, Collection<K>> reverseMap(Map<K, V> source) {
        Map<V, Collection<K>> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : source.entrySet())
            result.computeIfAbsent(entry.getValue(), k -> new LinkedHashSet<>()).add(entry.getKey());
        return result;
    }

    static <K, V> Collection<V> getValuesByKeys(Map<K, V> data, Collection<K> keys) {
        if (keys == null)
            throw new ArgumentException("No keys provided");
        Collection<V> result = new LinkedHashSet<>();
        for (K key : keys) {
            V value = data.get(key);
            if (value == null)
                throw new ArgumentException("Item not found: " + key);
            result.add(value);
        }
        return result;
    }

    static <K, V> Collection<V> gatherAllValues(Map<K, Collection<V>> data) {
        Collection<V> result = new LinkedHashSet<>();
        for (Collection<V> values : data.values())
            result.addAll(values);
        return result;
    }

    static <K, V> Map<K, V> filterMatchingValues(Map<K, V> data, Collection<V> values) {
        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : data.entrySet())
            if (values.contains(entry.getValue()))
                result.put(entry.getKey(), entry.getValue());
        return result;
    }

    static <K, V> Map<K, V> filterNotMatchingValues(Map<K, V> data, Collection<V> values) {
        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : data.entrySet())
            if (!values.contains(entry.getValue()))
                result.put(entry.getKey(), entry.getValue());
        return result;
    }

    static <K, V> Collection<V> findRemainingValues(Map<K, V> data, Collection<V> otherValues) {
        Collection<V> result = new LinkedHashSet<>();
        for (V value : data.values())
            if (!otherValues.contains(value))
                result.add(value);
        return result;
    }

    static <V> List<V> removeAll(Collection<V> data, Collection<V> toRemove) {
        List<V> result = new ArrayList<>(data);
        result.removeAll(toRemove);
        return result;
    }

    static <T> Collection<T> getCommon(Collection<T> group1, Collection<T> group2) {
        Collection<T> list = new ArrayList<>();
        if (group1 != null && group2 != null)
            for (T item : group1)
                if (group2.contains(item))
                    list.add(item);
        return list;
    }

    static <T> boolean containsAny(Collection<T> base, Collection<T> request) {
        for (T item : request)
            if (base.contains(item))
                return true;
        return false;
    }

    static <T> boolean isInCollection(Collection<Set<T>> collection, T arg) {
        for (Set<T> set : collection)
            if (set.contains(arg))
                return true;
        return false;
    }

    static <T> boolean isUnique(Collection<Set<T>> sets, Collection<T> toCheck) {
        if (toCheck.size() < 2)
            return false;
        for (Set<T> group : sets)
            if (getCommon(group, toCheck).size() == 2)
                return true;
        return false;
    }
}
