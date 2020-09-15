/*
 * Copyright (c) 2016 cocoon-data.com All rights reserved
 */

package com.forcpacebj.api.utils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Slf4j
public class JedisUtil {

    private static JedisPool jedisPool;

    /**
     * 初始化连接池
     *
     * @param host
     * @param port
     * @param database
     */
    public static void init(String host, int port, int database) {
        try {
            val config = new JedisPoolConfig();
            config.setMaxTotal(300); //最大连接数
            config.setMaxIdle(20); //最大空闲连接数
            config.setMaxWaitMillis(10000);  //等待可用连接的最大时间，单位毫秒，默认值为-1，表示永不超时。如果超过等待时间，则直接抛出
            config.setTestOnBorrow(true); //在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的；

            jedisPool = new JedisPool(config, host, port, 10000, "159357zx/C", database);
        } catch (Exception ex) {
            log.error("初始化Jedis异常", ex);
        }
    }

    /**
     * 获取jedis实例
     *
     * @return
     */
    public synchronized static Jedis getJedis() {
        try {
            if (jedisPool != null) {
                return jedisPool.getResource();
            } else {
                return null;
            }
        } catch (Exception ex) {
            log.error("JedisPool连接池中获取Jedis资源异常", ex);
            return null;
        }
    }
}
