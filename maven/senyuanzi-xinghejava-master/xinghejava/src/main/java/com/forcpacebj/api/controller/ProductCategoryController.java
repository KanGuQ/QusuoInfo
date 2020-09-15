package com.forcpacebj.api.controller;

import com.forcpacebj.api.business.ProductCategoryBusiness;
import com.forcpacebj.api.config.StaticParam;
import com.forcpacebj.api.entity.ProductCategoryInfo;
import com.forcpacebj.api.utils.IdGenerator;
import com.forcpacebj.api.utils.JSONUtil;
import lombok.extern.log4j.Log4j;
import lombok.val;
import spark.Route;

import static spark.Spark.halt;

@Log4j
public class ProductCategoryController extends BaseController {

    public static Route associateList = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);
        val obj = JSONUtil.toMap(request.body());
        val accountId = obj == null ? user.getAccountId() : (String) obj.get("accountId");

        val list = ProductCategoryBusiness.associateList(accountId == null ? user.getAccountId() : accountId);
        return toJson(list);
    };

    public static Route associateInsert = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);
        if (!user.getIsAdmin()) throw halt(400, "操作未授权！");

        val obj = JSONUtil.toBean(request.body(), ProductCategoryInfo.class);
        obj.setId(IdGenerator.NewId());
        obj.setRelatedAccountId(user.getAccountId());
        ProductCategoryBusiness.insert(StaticParam.PUBLIC_PRODUCT, obj);
        return toJson(obj);
    };


    public static Route list = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        val list = ProductCategoryBusiness.list(user.getAccountId());
        return toJson(list);
    };

    public static Route tList = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        val list = ProductCategoryBusiness.tList(user.getAccountId());
        return toJson(list);
    };

    public static Route delete = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);
        if (!user.getIsAdmin())
            if (!user.getUserRole().getPower().contains(StaticParam.MY_PRODUCT))
                throw halt(400, "无操作权限");
        val obj = JSONUtil.toMap(request.body());
        val accountId = obj == null ? user.getAccountId() : (String) obj.get("accountId");

        ProductCategoryBusiness.delete(accountId == null ? user.getAccountId() : accountId, request.params(":id"));
        return true;
    };

    public static Route insert = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);
        if (!user.getIsAdmin())
            if (!user.getUserRole().getPower().contains(StaticParam.MY_PRODUCT))
                throw halt(400, "无操作权限");

        val obj = JSONUtil.toBean(request.body(), ProductCategoryInfo.class);
        obj.setId(IdGenerator.NewId());
        ProductCategoryBusiness.insert(user.getAccountId(), obj);
        return toJson(obj);
    };

    public static Route update = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);
        if (!user.getIsAdmin())
            if (!user.getUserRole().getPower().contains(StaticParam.MY_PRODUCT))
                throw halt(400, "无操作权限");
        val obj = JSONUtil.toBeanList(request.body(), ProductCategoryInfo.class);
        ProductCategoryBusiness.update(user.getAccountId(), obj);
        return true;
    };

    public static Route publicList = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        val list = ProductCategoryBusiness.list(StaticParam.PUBLIC_PRODUCT);
        return toJson(list);
    };

    public static Route getPublicCategory = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        val list = ProductCategoryBusiness.getPublicCategory(user.getAccountId());
        return toJson(list);
    };
}
