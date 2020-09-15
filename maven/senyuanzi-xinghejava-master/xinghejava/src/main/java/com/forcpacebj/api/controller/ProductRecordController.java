package com.forcpacebj.api.controller;

import com.forcpacebj.api.business.ProductRecordBusiness;
import com.forcpacebj.api.utils.JSONUtil;
import lombok.val;
import spark.Route;

/**
 * Created by pc on 2019/11/15.
 */
public class ProductRecordController extends BaseController {

    public static Route find = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);
        val conditions = JSONUtil.toMap(request.body());

        val list = ProductRecordBusiness.find(conditions);
        return toJson(list);
    };

    public static Route count = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);
        val conditions = JSONUtil.toMap(request.body());

        return ProductRecordBusiness.count(conditions);
    };

    public static Route accountBeDownloadedCount = (request, response) -> {
        val user = ensureUserIsLoggedIn(request);
        val conditions = JSONUtil.toMap(request.body());
        return toJson(ProductRecordBusiness.accountBeDownloadedCount(conditions));
    };

    public static Route accountDownloadDetail = (request, response) -> {
        val user = ensureUserIsLoggedIn(request);
        val conditions = JSONUtil.toMap(request.body());
        return toJson(ProductRecordBusiness.accountDownloadDetail(conditions));
    };

    public static Route productData = (request, response) -> {
        val user = ensureUserIsLoggedIn(request);
        val conditions = JSONUtil.toMap(request.body());
        return toJson(ProductRecordBusiness.productData((String) conditions.get("accountId")));
    };

    public static Route downloadedProductDetail = (request, response) -> {
        val user = ensureUserIsLoggedIn(request);
        val conditions = JSONUtil.toMap(request.body());
        return toJson(ProductRecordBusiness.downloadedProductDetail(conditions));
    };
}
