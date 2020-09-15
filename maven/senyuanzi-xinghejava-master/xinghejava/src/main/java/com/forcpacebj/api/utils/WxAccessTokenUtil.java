package com.forcpacebj.api.utils;

import com.forcpacebj.api.Program;
import lombok.val;

import java.util.HashMap;

import static spark.Spark.halt;

public class WxAccessTokenUtil {
    public static String getCachedAccessToken(String appid, String secret) {

        if (Program.redisDB == Program.redisPreset) return null;

        val tokenKey = "AccessToken-" + appid;

        try (val jedis = JedisUtil.getJedis()) {

            if (jedis.exists(tokenKey)) {
                return jedis.get(tokenKey);
            } else {
                val accessTokenJSON = get(appid, secret);
                val accessToken = JSONUtil.getJSONFromString(accessTokenJSON).getString("access_token");
                if (StrUtil.isNotBlank(accessToken)) {
                    jedis.set(tokenKey, accessToken);
                    jedis.expire(tokenKey, 3600);
                    return accessToken;
                } else {
                    return null;
                }
            }
        }
    }

    private static final String get = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential";

    private static String get(String appid, String secret) {

        if (StrUtil.isNotBlank(appid) && StrUtil.isNotBlank(secret)) {

            val queryParas = new HashMap<String, String>();
            queryParas.put("appid", appid);
            queryParas.put("secret", secret);
            val result = HttpUtil.get(get, queryParas);

            if (result.contains("errcode")) {
                throw halt(400, "获取AccessToken错误" + result);
            }

            return result;
        } else {
            throw halt(400, "获取AccessToken参数错误");
        }
    }
}
