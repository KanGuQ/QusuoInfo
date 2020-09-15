/*
 * Copyright (c) 2016 cocoon-data.com All rights reserved
 */
package com.forcpacebj.api.controller;

import com.forcpacebj.api.business.ProductTagBusiness;
import com.forcpacebj.api.entity.ProductTagInfo;
import com.forcpacebj.api.utils.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import spark.Route;

@Slf4j
public class ProductTagController extends BaseController {

    public static Route list = (req, res) -> {

        val user = ensureUserIsLoggedIn(req);

        val result = ProductTagBusiness.list(user.getAccountId());
        return toJson(result);
    };

    public static Route insert = (req, res) -> {

        val user = ensureUserIsLoggedIn(req);

        val obj = JSONUtil.toBean(req.body(), ProductTagInfo.class);
        val newObj = ProductTagBusiness.insert(user.getAccountId(), obj);
        return toJson(newObj);
    };

    public static Route delete = (req, res) -> {

        val user = ensureUserIsLoggedIn(req);

        ProductTagBusiness.delete(user.getAccountId(), req.params("id"));
        return true;
    };

    public static Route update = (req, res) -> {

        val user = ensureUserIsLoggedIn(req);

        val obj = JSONUtil.toBean(req.body(), ProductTagInfo.class);
        ProductTagBusiness.update(user.getAccountId(), obj);
        return true;
    };
}
