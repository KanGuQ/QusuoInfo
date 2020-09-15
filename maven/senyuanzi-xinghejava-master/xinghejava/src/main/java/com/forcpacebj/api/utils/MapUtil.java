/*
 * Copyright (c) 2016 cocoon-data.com All rights reserved
 */
package com.forcpacebj.api.utils;

import lombok.val;

import java.util.HashMap;
import java.util.Map;

/**
 * Map集合类
 * Created by gelingfeng on 2016/12/19.
 */
public class MapUtil {
    public static Map<String, Object> instance() {
        return new HashMap<>();
    }

    public static Map<String, Object> instance(String key, Object value) {
        val map = new HashMap<String, Object>();
        map.put(key, value);
        return map;
    }

    public static Map<String, String> instance(String key, String value) {
        val map = new HashMap<String, String>();
        map.put(key, value);
        return map;
    }
}
