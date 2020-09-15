package com.forcpacebj.api.controller;

import com.forcpacebj.api.business.MessageBusiness;
import com.forcpacebj.api.utils.JSONUtil;
import lombok.val;
import spark.Route;

public class MessageController extends BaseController {

    public static Route count = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);
        val condition = JSONUtil.toMap(request.body());
        condition.put("accountId", user.getAccountId());

        return MessageBusiness.count(condition);
    };
    public static Route find = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);
        val condition = JSONUtil.toMap(request.body());
        condition.put("accountId", user.getAccountId());

        return toJson(MessageBusiness.find(condition));
    };
}
