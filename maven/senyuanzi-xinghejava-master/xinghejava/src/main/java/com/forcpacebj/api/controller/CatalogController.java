package com.forcpacebj.api.controller;

import com.forcpacebj.api.business.CatalogBusiness;
import com.forcpacebj.api.entity.CatalogInfo;
import com.forcpacebj.api.utils.JSONUtil;
import lombok.extern.log4j.Log4j;
import lombok.val;
import spark.Route;

/**
 * Created by pc on 2019/10/9.
 */
@Log4j
public class CatalogController extends BaseController {

    public static Route list = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        val list = CatalogBusiness.list(request.queryParams("parentId"));
        return toJson(list);
    };

    public static Route allList = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        val list = CatalogBusiness.allList();
        return toJson(list);
    };

    public static Route effectiveList = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);
        val conditions = JSONUtil.toMap(request.body());
        if (conditions.get("accountId") == null)
            conditions.put("accountId", user.getAccountId());

        val list = CatalogBusiness.effectiveList(conditions);
        return toJson(list);
    };

    public static Route publicEffectiveList = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);
        val conditions = JSONUtil.toMap(request.body());
        if (conditions.get("relatedAccountId") == null)
            conditions.put("relatedAccountId", user.getAccountId());

        val list = CatalogBusiness.publicEffectiveList(conditions);
        return toJson(list);
    };

    public static Route delete = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        CatalogBusiness.delete(request.params(":id"));
        return true;
    };

    public static Route insert = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        val obj = JSONUtil.toBean(request.body(), CatalogInfo.class);
        obj.setId(CatalogBusiness.insert(obj));
        return toJson(obj);
    };

    public static Route update = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        val obj = JSONUtil.toBeanList(request.body(), CatalogInfo.class);
        CatalogBusiness.update(obj);
        return true;
    };
}
