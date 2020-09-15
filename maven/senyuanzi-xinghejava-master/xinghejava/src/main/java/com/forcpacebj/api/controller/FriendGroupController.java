package com.forcpacebj.api.controller;

import com.forcpacebj.api.business.FriendGroupBusiness;
import com.forcpacebj.api.entity.FriendGroupInfo;
import com.forcpacebj.api.utils.IdGenerator;
import com.forcpacebj.api.utils.JSONUtil;
import lombok.val;
import spark.Route;

public class FriendGroupController extends BaseController {

    public static Route list = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        val list = FriendGroupBusiness.list(user.getAccountId());
        return toJson(list);
    };

    public static Route insert = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        val obj = JSONUtil.toBean(request.body(), FriendGroupInfo.class);
        obj.setGroupId(IdGenerator.NewId());
        FriendGroupBusiness.insert(user.getAccountId(), obj);
        return toJson(obj);
    };

    public static Route delete = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        FriendGroupBusiness.delete(user.getAccountId(), request.params(":groupId"));
        return true;
    };

    public static Route update = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        val obj = JSONUtil.toBean(request.body(), FriendGroupInfo.class);
        FriendGroupBusiness.update(user.getAccountId(), obj);
        return toJson(obj);
    };
}
