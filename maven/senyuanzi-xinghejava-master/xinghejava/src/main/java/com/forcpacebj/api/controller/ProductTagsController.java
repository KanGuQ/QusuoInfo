/*
 * Copyright (c) 2016 cocoon-data.com All rights reserved
 */
package com.forcpacebj.api.controller;

import com.forcpacebj.api.business.ProductTagsBusiness;
import com.forcpacebj.api.entity.ProductTagInfo;
import com.forcpacebj.api.utils.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import spark.Route;

@Slf4j
public class ProductTagsController extends BaseController {

    public static Route insert = (req, res) -> {

        val user = ensureUserIsLoggedIn(req);

        val tags = JSONUtil.toBeanList(req.body(), ProductTagInfo.class);

        ProductTagsBusiness.insert(user.getAccountId(), req.params("productId"), tags);
        return true;
    };
}
