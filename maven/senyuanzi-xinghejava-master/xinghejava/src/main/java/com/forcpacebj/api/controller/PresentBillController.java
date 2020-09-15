package com.forcpacebj.api.controller;

import com.forcpacebj.api.business.PresentBillBusiness;
import com.forcpacebj.api.entity.PresentBillInfo;
import com.forcpacebj.api.utils.DateUtil;
import com.forcpacebj.api.utils.JSONUtil;
import com.forcpacebj.api.utils.StrUtil;
import lombok.val;
import spark.Route;

import static spark.Spark.halt;

public class PresentBillController extends BaseController {

    public static Route list = (req, res) -> {

        val user = ensureUserIsLoggedIn(req);

        val list = PresentBillBusiness.list(user.getAccountId(), req.params(":billId"));
        return toJson(list);
    };

    public static Route insert = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        try {
            val obj = JSONUtil.toBean(request.body(), PresentBillInfo.class);
            obj.setCreateTime(DateUtil.now());
            PresentBillBusiness.insert(user.getAccountId(), obj);
            return toJson(obj);
        } catch (Exception ex) {
            if (StrUtil.isNotBlank(ex.getMessage()) && ex.getMessage().contains("Duplicate entry")) {
                throw halt(400, "请不要重复提报");
            } else {
                throw ex;
            }
        }
    };

    public static Route find = (request, response) -> {

        ensureUserIsLoggedIn(request);

        val condition = JSONUtil.toMap(request.body());
        val list = PresentBillBusiness.find(condition);
        return toJson(list);
    };

    public static Route count = (request, response) -> {

        ensureUserIsLoggedIn(request);

        val condition = JSONUtil.toMap(request.body());
        return PresentBillBusiness.count(condition);
    };

    public static Route listDetails = (req, res) -> {

        ensureUserIsLoggedIn(req);

        val list = PresentBillBusiness.loadDetails(req.params(":toAccountId"), req.params(":fromAccountId"), req.params(":quotationBillId"));
        return toJson(list);
    };

    public static Route delete = (req, res) -> {

        ensureUserIsLoggedIn(req);

        PresentBillBusiness.delete(req.params(":toAccountId"), req.params(":fromAccountId"), req.params(":quotationBillId"));
        return true;
    };
}
