/*
 * Copyright (c) 2016 cocoon-data.com All rights reserved
 */

package com.forcpacebj.api.utils;


import java.util.*;

/**
 * 集合常用方法工具类
 */
public final class CollectionUtil {

    /**
     * 此类不需要实例化
     */
    private CollectionUtil() {
    }

    /**
     * 判断一个集合是否为空 null或者空集合都会返回true
     *
     * @param collection 需要判断的集合
     * @return 是否有值，null或者空集合都是返回true
     */
    public static boolean isEmpty(Collection<?> collection) {

        return null == collection || collection.isEmpty();
    }

    /**
     * 判断一个集合是否不为空
     *
     * @param collection 需要判断的集合
     * @return 是否不为空
     */
    public static boolean isNotEmpty(Collection<?> collection) {

        return null != collection && !collection.isEmpty();
    }

    /**
     * 创建一个有默认内容的集合
     *
     * @param <T>          泛型
     * @param initElements 初始化内容
     * @return 集合对象
     */
    public static <T> List<T> newArrayList(T... initElements) {
        List<T> list = null;
        if (null != initElements && 0 != initElements.length) {
            list = new ArrayList<>(initElements.length);
            Collections.addAll(list, initElements);
        }
        return list;
    }

    /**
     * 判断一个集合是否为空 null或者空集合都会返回true
     *
     * @param collection 需要判断的集合
     * @return 是否有值，null或者空集合都是返回true
     */
    public static boolean isEmpty(Map<?, ?> collection) {

        return null == collection || collection.isEmpty();
    }

    /**
     * 判断一个集合是否不为空
     *
     * @param collection 需要判断的集合
     * @return 是否不为空
     */
    public static boolean isNotEmpty(Map<?, ?> collection) {

        return null != collection && !collection.isEmpty();
    }

    /**
     * 数据元素中是否包含目标元素
     *
     * @param arr
     * @param targetValue
     * @return
     */
    public static boolean isContains(String[] arr, String targetValue) {

        for (String s : arr) {
            if (s.equals(targetValue))
                return true;
        }
        return false;
    }

    /**
     * 数据元素中是否包含目标元素
     *
     * @param arr
     * @param targetValue
     * @return
     */
    public static boolean isContains(int[] arr, int targetValue) {

        for (int s : arr) {
            if (s == targetValue)
                return true;
        }
        return false;
    }

    /**
     * 数据元素中是否包含目标元素
     *
     * @param arr
     * @param targetValue
     * @return
     */
    public static <T> boolean isContains(List<T> arr, T targetValue) {

        for (T s : arr) {
            if (s == targetValue || s.equals(targetValue))
                return true;
        }
        return false;
    }


    /**
     * 将集合按指定每批大小分割
     *
     * @param list
     * @param batchSize
     * @param <T>
     * @return
     */
    public static <T> List<List<T>> split(List<T> list, int batchSize) {

        int page = list.size() % batchSize > 0 ? (list.size() / batchSize) + 1 : list.size() / batchSize;

        List<List<T>> result = new ArrayList<>();
        for (int j = 1; j <= page; j++) {
            List<T> batchList = new ArrayList<T>();
            for (int i = (j - 1) * batchSize; i < j * batchSize && i < list.size(); i++) {
                batchList.add(list.get(i));
            }
            result.add(batchList);
        }

        return result;
    }

    /**
     * 判断一个数组是否为空 null或者空数组都会返回true
     *
     * @param array 需要判断的数组
     * @return 是否有值，null或者空集合都是返回true
     */
    public static <T> boolean isEmpty(T[] array) {

        return null == array || array.length == 0;
    }

    /**
     * 判断一个数组是否不为空
     *
     * @param array 需要判断的数组
     * @return 是否不为空
     */
    public static <T> boolean isNotEmpty(T[] array) {

        return null != array && array.length > 0;
    }


}