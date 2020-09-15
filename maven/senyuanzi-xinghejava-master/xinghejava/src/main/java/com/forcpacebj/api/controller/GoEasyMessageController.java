package com.forcpacebj.api.controller;

import com.forcpacebj.api.business.GoEasyMessageBusiness;
import com.forcpacebj.api.business.UserBusiness;
import com.forcpacebj.api.config.StaticParam;
import com.forcpacebj.api.utils.JSONUtil;
import lombok.val;
import spark.Route;

import java.util.List;

/**
 * Created by pc on 2020/4/26.
 */
public class GoEasyMessageController extends BaseController {

    public static Route find = (request, response) -> {
        val user = ensureUserIsLoggedIn(request);
        val conditions = JSONUtil.toMap(request.body());
        conditions.put("channel", user.getUserId());
        return toJson(GoEasyMessageBusiness.find(conditions));
    };

    public static Route alreadyRead = (request, response) -> {
        val user = ensureUserIsLoggedIn(request);
        val conditions = JSONUtil.toMap(request.body());
        if (conditions == null)
            GoEasyMessageBusiness.alreadyRead(user.getUserId());
        else
            GoEasyMessageBusiness.alreadyRead((Integer) conditions.get("id"));
        return true;
    };

    public static Route getSecretKey = (request, response) -> {
        ensureUserIsLoggedIn(request);
        return GoEasyMessageBusiness.goEasyOTP(StaticParam.GOEASY_SECRET_KEY);
    };

    public static Route publish = (request, response) -> {
        val user = ensureUserIsLoggedIn(request);
        val condition = JSONUtil.toMap(request.body());
        GoEasyMessageBusiness.publish((List<String>) condition.get("channels"), (String) condition.get("content"));
        return true;
    };

    public static Route publishAll = (request, response) -> {
        val user = ensureUserIsLoggedIn(request);
        val condition = JSONUtil.toMap(request.body());
        GoEasyMessageBusiness.publish(UserBusiness.userIdList(), (String) condition.get("content"));
        return true;
    };
}
