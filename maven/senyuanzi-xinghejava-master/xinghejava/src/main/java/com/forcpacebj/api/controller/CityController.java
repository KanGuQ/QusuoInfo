package com.forcpacebj.api.controller;

import com.forcpacebj.api.business.CityBusiness;
import com.forcpacebj.api.utils.JSONUtil;
import lombok.extern.log4j.Log4j;
import lombok.val;
import spark.Route;

@Log4j
public class CityController extends BaseController {

    public static Route find = (req, res) -> {

        val condition = JSONUtil.toMap(req.body());
        val result = CityBusiness.find(condition);
        return toJson(result);
    };
}
