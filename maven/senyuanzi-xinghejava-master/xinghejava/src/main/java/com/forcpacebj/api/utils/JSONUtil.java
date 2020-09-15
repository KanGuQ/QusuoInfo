/*
 * Copyright (c) 2016 cocoon-data.com All rights reserved
 */

package com.forcpacebj.api.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.util.List;
import java.util.Map;

public class JSONUtil {

    private JSONUtil() {
    }

    /**
     * 默认json格式化方式
     */
    public static final SerializerFeature[] DEFAULT_FORMAT = {
            SerializerFeature.WriteDateUseDateFormat,
            SerializerFeature.WriteEnumUsingToString,
            //SerializerFeature.WriteMapNullValue, //null字段也需要解析,否则下面的不起作用
            //SerializerFeature.WriteNullNumberAsZero,
            //SerializerFeature.WriteNullListAsEmpty,
            //SerializerFeature.WriteNullStringAsEmpty,
            //SerializerFeature.WriteNullBooleanAsFalse,
            //SerializerFeature.DisableCircularReferenceDetect
    };

    /**
     * 从json获取指定key的字符串
     *
     * @param json json字符串
     * @param key  字符串的key
     * @return 指定key的值
     */
    public static Object getStringFromJSONObject(final String json, final String key) {

        return JSON.parseObject(json).getString(key);
    }

    /**
     * 将字符串转换成JSON字符串
     *
     * @param jsonString json字符串
     * @return 转换成的json对象
     */
    public static JSONObject getJSONFromString(final String jsonString) {

        return JSON.parseObject(jsonString);
    }

    /**
     * 将json字符串，转换成指定java bean
     *
     * @param jsonStr   json串对象
     * @param beanClass 指定的bean
     * @param <T>       任意bean的类型
     * @return 转换后的java bean对象
     */
    public static <T> T toBean(String jsonStr, Class<T> beanClass) {

        JSONObject jo = JSON.parseObject(jsonStr);
        jo.put(JSON.DEFAULT_TYPE_KEY, beanClass.getName());
        return JSON.parseObject(jo.toJSONString(), beanClass);
    }

    /**
     * @param obj 需要转换的java bean
     * @param <T> 入参对象类型泛型
     * @return 对应的json字符串
     */
    public static <T> String toJson(T obj) {

        return JSON.toJSONString(obj, DEFAULT_FORMAT);
    }

    /**
     * 通过Map生成一个json字符串
     *
     * @param map 需要转换的map
     * @return json串
     */
    public static String toJson(Map<String, Object> map) {

        return JSON.toJSONString(map, DEFAULT_FORMAT);
    }

    /**
     * 美化传入的json,使得该json字符串容易查看
     *
     * @param jsonString 需要处理的json串
     * @return 美化后的json串
     */
    public static String prettyFormatJson(String jsonString) {

        return JSON.toJSONString(getJSONFromString(jsonString), true);
    }

    /**
     * 美化传入的json,使得该json字符串容易查看
     */
    public static <T> String prettyFormatJson(T obj) {

        return JSON.toJSONString(obj, true);
    }

    /**
     * 将传入的json字符串转换成Map
     *
     * @param jsonString 需要处理的json串
     * @return 对应的map
     */
    public static Map<String, Object> toMap(String jsonString) {

        return getJSONFromString(jsonString);
    }


    /**
     * 将传入的json字符串转换成集合
     *
     * @param <T>
     * @param jsonStr
     * @param beanClass
     * @return
     */
    public static <T> List<T> toBeanList(String jsonStr, Class<T> beanClass) {

        return JSON.parseArray(jsonStr, beanClass);
    }
}
