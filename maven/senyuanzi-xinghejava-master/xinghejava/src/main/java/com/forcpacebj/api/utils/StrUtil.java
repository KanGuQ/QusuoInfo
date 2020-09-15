/*
 * Copyright (c) 2016 cocoon-data.com All rights reserved
 */

package com.forcpacebj.api.utils;

/**
 * 字符串常用方法工具类
 *
 * @author
 */
public final class StrUtil {

    /**
     * 此类不需要实例化
     */
    private StrUtil() {
    }

    /**
     * 判断一个字符串是否为空，null也会返回true
     *
     * @param str 需要判断的字符串
     * @return 是否为空，null也会返回true
     */
    public static boolean isBlank(String str) {
        return null == str || "".equals(str.trim());
    }

    /**
     * 判断一个字符串是否不为空
     *
     * @param str 需要判断的字符串
     * @return 是否为空
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * 判断一组字符串是否有空值
     *
     * @param strs 需要判断的一组字符串
     * @return 判断结果，只要其中一个字符串为null或者为空，就返回true
     */
    public static boolean hasBlank(String... strs) {
        if (null == strs || 0 == strs.length) {
            return true;
        } else {
            // 这种代码如果用java8就会很优雅了
            for (String str : strs) {
                if (isBlank(str)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 字符左补齐
     *
     * @param originStr
     * @param character
     * @param totalLength
     * @return
     */
    public static String padLeft(String originStr, Character character, int totalLength) {

        if (isBlank(originStr)) {
            originStr = "";
        }

        while (originStr.length() < totalLength) {
            originStr = character + originStr;
        }

        return originStr;
    }

    /**
     * 字符左补齐
     *
     * @param originStr
     * @param character
     * @param totalLength
     * @return
     */
    public static String padRight(String originStr, Character character, int totalLength) {

        if (isBlank(originStr)) {
            originStr = "";
        }

        while (originStr.length() < totalLength) {
            originStr = originStr + character;
        }

        return originStr;
    }
}
