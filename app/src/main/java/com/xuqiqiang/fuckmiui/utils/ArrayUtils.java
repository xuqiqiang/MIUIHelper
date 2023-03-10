package com.xuqiqiang.fuckmiui.utils;

import java.util.Collection;

@SuppressWarnings("unused")
public class ArrayUtils {
    public static <T> boolean isEmpty(T[] array) {
        return array == null || array.length <= 0;
    }

    public static <T> boolean isEmpty(Collection<T> collection) {
        return collection == null || collection.size() <= 0;
    }

    public static <T> int size(Collection<T> collection) {
        return collection == null ? 0 : collection.size();
    }
}
