package com.forcpacebj.api.controller;

import com.forcpacebj.api.business.ProductSetBusiness;
import com.forcpacebj.api.entity.ProductSetInfo;
import com.forcpacebj.api.utils.IdGenerator;
import com.forcpacebj.api.utils.JSONUtil;
import lombok.extern.log4j.Log4j;
import lombok.val;
import spark.Route;

@Log4j
public class ProductSetController extends BaseController {

    public static Route find = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        val condition = JSONUtil.toMap(request.body());
        val list = ProductSetBusiness.find(condition, user.getAccountId());
        return toJson(list);
    };

    public static Route count = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        val condition = JSONUtil.toMap(request.body());
        return ProductSetBusiness.count(condition, user.getAccountId());
    };

    public static Route load = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        val obj = ProductSetBusiness.load(user.getAccountId(), request.params(":id"));
        return toJson(obj);
    };

    public static Route delete = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        ProductSetBusiness.delete(user.getAccountId(), request.params(":id"));
        return true;
    };

    public static Route insert = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        val obj = JSONUtil.toBean(request.body(), ProductSetInfo.class);
        obj.setId(IdGenerator.NewId());
        ProductSetBusiness.insert(user.getAccountId(), obj);
        return toJson(obj);
    };

    public static Route update = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        val obj = JSONUtil.toBean(request.body(), ProductSetInfo.class);
        ProductSetBusiness.update(user.getAccountId(), obj);
        return toJson(obj);
    };
}
