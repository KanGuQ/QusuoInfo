package com.forcpacebj.api.controller;

import com.forcpacebj.api.business.RecordStaticBusiness;
import com.forcpacebj.api.utils.JSONUtil;
import lombok.extern.log4j.Log4j;
import lombok.val;
import spark.Route;

@Log4j
public class RecordStaticController extends BaseController {

    public static Route recordStaticByUser = (req, res) -> {

        val user = ensureUserIsLoggedIn(req);
        val condition = JSONUtil.toMap(req.body());
        val result = RecordStaticBusiness.recordStaticByUser(condition, user.getAccountId());
        return toJson(result);
    };

    public static Route recordDayStatic = (req, res) -> {

        val user = ensureUserIsLoggedIn(req);
        val condition = JSONUtil.toMap(req.body());
        val result = RecordStaticBusiness.recordDayStatic(condition, user.getAccountId());
        return toJson(result);
    };
}
