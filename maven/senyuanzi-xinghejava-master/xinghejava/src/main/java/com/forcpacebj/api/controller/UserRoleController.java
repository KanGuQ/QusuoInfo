package com.forcpacebj.api.controller;

import com.forcpacebj.api.business.UserRoleBusiness;
import com.forcpacebj.api.core.ResponseWrapper;
import com.forcpacebj.api.entity.UserRoleInfo;
import com.forcpacebj.api.utils.IdGenerator;
import com.forcpacebj.api.utils.JSONUtil;
import lombok.extern.log4j.Log4j;
import lombok.val;
import spark.Route;

@Log4j
public class UserRoleController extends BaseController {

    public static Route find = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        val condition = JSONUtil.toMap(request.body());
        val list = UserRoleBusiness.find(condition, user.getAccountId());
        return toJson(list);
    };

    public static Route findRoles = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        val condition = JSONUtil.toMap(request.body());
        val list = UserRoleBusiness.find(condition, user.getAccountId());
        return toJson(ResponseWrapper.page(UserRoleBusiness.count(user.getAccountId()), list));
    };

    public static Route count = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        return UserRoleBusiness.count(user.getAccountId());
    };

    public static Route list = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        val list = UserRoleBusiness.list(user.getAccountId());
        return toJson(list);
    };

    public static Route load = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        val obj = UserRoleBusiness.load(user.getAccountId(), request.params(":roleId"));
        return toJson(obj);
    };

    public static Route delete = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        UserRoleBusiness.delete(user.getAccountId(), request.params(":roleId"));
        return true;
    };

    public static Route insert = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        val obj = JSONUtil.toBean(request.body(), UserRoleInfo.class);
        UserRoleBusiness.insert(user.getAccountId(), obj);
        return toJson(obj);
    };

    public static Route update = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        val obj = JSONUtil.toBean(request.body(), UserRoleInfo.class);
        UserRoleBusiness.update(user.getAccountId(), obj);
        return toJson(obj);
    };
}
