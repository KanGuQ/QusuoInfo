package com.forcpacebj.api.controller;

import com.forcpacebj.api.business.ProductSetCategoryBusiness;
import com.forcpacebj.api.entity.ProductSetCategoryInfo;
import com.forcpacebj.api.utils.IdGenerator;
import com.forcpacebj.api.utils.JSONUtil;
import lombok.extern.log4j.Log4j;
import lombok.val;
import spark.Route;

@Log4j
public class ProductSetCategoryController extends BaseController {

    public static Route list = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        val list = ProductSetCategoryBusiness.list(user.getAccountId());
        return toJson(list);
    };

    public static Route delete = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        ProductSetCategoryBusiness.delete(user.getAccountId(), request.params(":id"));
        return true;
    };

    public static Route insert = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        val obj = JSONUtil.toBean(request.body(), ProductSetCategoryInfo.class);
        obj.setId(IdGenerator.NewId());
        ProductSetCategoryBusiness.insert(user.getAccountId(), obj);
        return toJson(obj);
    };

    public static Route update = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        val obj = JSONUtil.toBean(request.body(), ProductSetCategoryInfo.class);
        ProductSetCategoryBusiness.update(user.getAccountId(), obj);
        return true;
    };
}
