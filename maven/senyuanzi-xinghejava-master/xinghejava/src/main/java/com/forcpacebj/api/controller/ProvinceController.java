package com.forcpacebj.api.controller;

import com.forcpacebj.api.business.ProvinceBusiness;
import lombok.extern.log4j.Log4j;
import lombok.val;
import spark.Route;

@Log4j
public class ProvinceController extends BaseController {

    public static Route list = (req, res) -> {

        val result = ProvinceBusiness.list();
        return toJson(result);
    };
}
