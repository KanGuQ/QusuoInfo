package com.forcpacebj.api.controller;

import com.forcpacebj.api.business.CatalogBusiness;
import com.forcpacebj.api.business.ProductDataBusiness;
import com.forcpacebj.api.business.WholesalerDataBusiness;
import com.forcpacebj.api.business.db;
import com.forcpacebj.api.core.ResponseWrapper;
import com.forcpacebj.api.entity.CatalogInfo;
import com.forcpacebj.api.utils.JSONUtil;
import lombok.extern.log4j.Log4j;
import lombok.val;
import lombok.var;
import spark.Route;

/**
 * Created by pc on 2019/10/9.
 */
@Log4j
public class WholesalerController extends BaseController {

    public static Route findWholesalerData = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);
        try (val con = db.sql2o.open()) {
            val wholesalerData = WholesalerDataBusiness.findWholesalerData(con, request.queryParams("companyId"));
            return toJson(wholesalerData);
        }

    };

    public static Route findProductData = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);
        val condition = JSONUtil.toMap(request.body());
        try (val con = db.sql2o.open()) {
            val wholesalerData = WholesalerDataBusiness.findProductData(con, condition);
            return toJson(ResponseWrapper.page(ProductDataBusiness.count(con, condition), wholesalerData));
        }
    };
}
