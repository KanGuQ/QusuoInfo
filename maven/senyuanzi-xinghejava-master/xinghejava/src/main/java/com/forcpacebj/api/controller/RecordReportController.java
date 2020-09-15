package com.forcpacebj.api.controller;

import com.forcpacebj.api.business.RecordBusiness;
import com.forcpacebj.api.business.UserBusiness;
import com.forcpacebj.api.utils.JSONUtil;
import lombok.extern.log4j.Log4j;
import lombok.val;
import spark.Route;

@Log4j
public class RecordReportController extends BaseController {

    public static Route find = (req, res) -> {

        val condition = JSONUtil.toMap(req.body());
        condition.put("accountId", req.params("accountId"));
        val result = RecordBusiness.list(condition);
        return toJson(result);
    };

    public static Route load = (req, res) -> {

        val result = RecordBusiness.load(req.params("accountId"), req.params("id"));
        return toJson(result);
    };

    public static Route loadUser = (req, res) -> {
        val result = UserBusiness.load(req.params("accountId"), req.params("userId"));
        return toJson(result);
    };
}
