/*
 * Copyright (c) 2016 cocoon-data.com All rights reserved
 */

package com.forcpacebj.api.utils;

/**
 * ID生成器
 *
 * @author gelingfeng
 */
public class IdGenerator {

    public static IdWorker idWorker = new IdWorker(0, 0);

    /*
     * 生成ID
     */
    public static String NewId() {
        long id = idWorker.nextId();
        return Long.toString(id);
    }
}
