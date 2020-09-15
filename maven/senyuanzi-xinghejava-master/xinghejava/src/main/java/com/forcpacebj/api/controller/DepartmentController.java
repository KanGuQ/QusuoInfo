package com.forcpacebj.api.controller;

import com.forcpacebj.api.business.DepartmentBusiness;
import com.forcpacebj.api.business.UserBusiness;
import com.forcpacebj.api.entity.DepartmentInfo;
import com.forcpacebj.api.utils.JSONUtil;
import lombok.extern.log4j.Log4j;
import lombok.val;
import lombok.var;
import spark.Route;

import java.util.stream.Collectors;

import static spark.Spark.halt;

/**
 * Created by pc on 2020/3/24.
 */
@Log4j
public class DepartmentController extends BaseController {

    public static Route list = (request, response) -> {
        val user = ensureUserIsLoggedIn(request);
        val list = DepartmentBusiness.list(user.getAccountId());
        return toJson(list);
    };

    public static Route load = (request, response) -> {
        val user = ensureUserIsLoggedIn(request);
        val id = JSONUtil.toMap(request.body()).get("id");
        val list = DepartmentBusiness.load(id);
        return toJson(list);
    };

    public static Route insert = (request, response) -> {
        val user = ensureUserIsLoggedIn(request);
        val data = JSONUtil.toBean(request.body(), DepartmentInfo.class);
        data.setAccountId(user.getAccountId());
        DepartmentBusiness.insert(data);
        return true;
    };

    public static Route update = (request, response) -> {
        val user = ensureUserIsLoggedIn(request);
        val data = JSONUtil.toBean(request.body(), DepartmentInfo.class);
        DepartmentBusiness.update(data);
        return true;
    };

    public static Route batchUpdate = (request, response) -> {
        val user = ensureUserIsLoggedIn(request);
        val data = JSONUtil.toBeanList(request.body(), DepartmentInfo.class);
        DepartmentBusiness.update(data);
        return true;
    };

    public static Route delete = (request, response) -> {
        val user = ensureUserIsLoggedIn(request);
        val data = JSONUtil.toBean(request.body(), DepartmentInfo.class);
        DepartmentBusiness.delete(data.getId(), user.getAccountId());
        return true;
    };

    public static Route optionalDepartment = (request, response) -> {
        val user = ensureUserIsLoggedIn(request);
        if (checkPower(user))
            return toJson(DepartmentBusiness.list(user.getAccountId()));
        else
            return toJson(DepartmentBusiness.optionalDepartment(user.getUserId()));
    };

    public static Route chooseDepartment = (request, response) -> {
        var user = ensureUserIsLoggedIn(request);
        val id = (Integer) JSONUtil.toMap(request.body()).get("departmentId");
        var res = DepartmentBusiness.checkDepartment(user.getUserId(), id, user.getAccountId(), checkPower(user));
//                    DepartmentBusiness.optionalDepartment(user.getUserId())
//                    .stream().map(DepartmentInfo::getId).collect(Collectors.toList())
//                    .contains(id);
        if (!res)
            throw halt(400, "你不在此部门");
        user = UserBusiness.chooseDepartment(user.getUserId(), id);
        generatorToken(user);
        return toJson(user);
    };
}
