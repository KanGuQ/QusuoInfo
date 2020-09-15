package com.forcpacebj.api.controller;

import com.forcpacebj.api.business.CatalogParameterBusiness;
import com.forcpacebj.api.entity.CatalogInfo;
import com.forcpacebj.api.utils.JSONUtil;
import lombok.val;
import spark.Route;

/**
 * Created by pc on 2019/10/11.
 */
public class CatalogParameterController extends BaseController {

    public static Route list = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        val list = CatalogParameterBusiness.list(request.params("catalogId"));
        return toJson(list);
    };

    public static Route listForProductValues = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        val condition = JSONUtil.toMap(request.body());
        val list = CatalogParameterBusiness.listForProductValues(
                (String) condition.get("productId"), (String) condition.get("catalogId"));
        return toJson(list);
    };

    public static Route listGroupConcat = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        val condition = JSONUtil.toMap(request.body());
        val list = CatalogParameterBusiness.listGroupConcat(
                (String) condition.get("catalogId"),
                condition.get("accountId") == null ? user.getAccountId() : (String) condition.get("accountId"),
                (String) condition.get("fromPage"));
        return toJson(list);
    };

    public static Route insert = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        val obj = JSONUtil.toBean(request.body(), CatalogInfo.class);
        CatalogParameterBusiness.insert(obj.getParameters());
        return true;
    };

    public static Route update = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        val obj = JSONUtil.toBean(request.body(), CatalogInfo.class);
        CatalogParameterBusiness.update(obj.getParameters(), obj.getId());
        return true;
    };

    public static Route delete = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        CatalogParameterBusiness.delete(request.params(":id"));
        return true;
    };
}
