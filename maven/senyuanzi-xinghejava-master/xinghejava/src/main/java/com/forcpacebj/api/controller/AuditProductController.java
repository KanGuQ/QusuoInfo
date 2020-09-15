package com.forcpacebj.api.controller;

import com.forcpacebj.api.business.AuditProductBusiness;
import com.forcpacebj.api.core.ResponseWrapper;
import com.forcpacebj.api.utils.JSONUtil;
import lombok.extern.log4j.Log4j;
import lombok.val;
import spark.Route;

import static spark.Spark.halt;

@Log4j
public class AuditProductController extends BaseController {

    /**
     * 获取指定类型的 审核列表 数量
     */
    public static Route count = (request, res) -> {
        val user = ensureUserIsLoggedIn(request);
        if (!user.getIsAdmin()) throw halt(400, "操作未授权！");

        val condition = JSONUtil.toMap(request.body());
        return AuditProductBusiness.count(condition);
    };

    /**
     * 获取审核列表
     */
    public static Route find = (request, response) -> {
        val user = ensureUserIsLoggedIn(request);
        if (!user.getIsAdmin()) throw halt(400, "操作未授权！");

        val condition = JSONUtil.toMap(request.body());
        return toJson(ResponseWrapper.page(AuditProductBusiness.count(condition), AuditProductBusiness.find(condition)));
    };

    /**
     * 审核
     */
    public static Route audit = (request, response) -> {
        val user = ensureUserIsLoggedIn(request);
        if (!user.getIsAdmin()) throw halt(400, "操作未授权！");

        val condition = JSONUtil.toMap(request.body());
        condition.put("userId", user.getUserId());
        condition.put("accountId", user.getAccountId());
        val result = AuditProductBusiness.audit(condition, user);

        return toJson(ResponseWrapper.ok(result));
    };
}
