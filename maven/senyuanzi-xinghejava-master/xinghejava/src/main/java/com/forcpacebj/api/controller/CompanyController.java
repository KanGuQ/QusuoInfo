package com.forcpacebj.api.controller;

import com.alibaba.fastjson.JSONObject;
import com.forcpacebj.api.business.AccountBusiness;
import com.forcpacebj.api.business.UserBusiness;
import com.forcpacebj.api.config.StaticParam;
import com.forcpacebj.api.core.ResponseWrapper;
import com.forcpacebj.api.entity.AccountInfo;
import com.forcpacebj.api.utils.JSONUtil;
import com.forcpacebj.api.utils.PhoneCode;
import com.forcpacebj.api.utils.StrUtil;
import lombok.val;
import lombok.var;
import spark.Route;
import lombok.extern.log4j.Log4j;

import static spark.Spark.halt;

/**
 * Created by pc on 2019/9/28.
 */
@Log4j
public class CompanyController extends BaseController {

    public static Route register = (request, response) -> {

        val company = JSONUtil.toBean(request.body(), AccountInfo.class);
        if (AccountBusiness.loadByName(company.getAccountName()) != null)
            throw halt(400, "公司名已注册");
        if (StrUtil.isNotBlank(UserBusiness.load(null, company.getUserId()).getAccountId()))
            throw halt(400, "您已加入企业！");
        if (AccountBusiness.alreadyExist(company.getUserId()) > 0)
            throw halt(400, "已申请创建！");
        AccountBusiness.register(company);
        return true;
    };

    public static Route checkCompanyName = (request, response) -> {
        var accountName = JSONUtil.toMap(request.body()).get("accountName");
        if (accountName == null) return false;
        var res = AccountBusiness.loadByName((String) accountName);
        return res == null;
    };

    public static Route getCompanyList = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);
        if (!user.getIsAdmin()) throw halt(400, "没有操作权限");
        val condition = JSONUtil.toMap(request.body());
        val list = AccountBusiness.getCompanyList(condition);
        return toJson(ResponseWrapper.page(AccountBusiness.getCompanyCount(condition), list));
    };

    public static Route getPublicCompanyList = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);
        val condition = JSONUtil.toMap(request.body());
        condition.put("accountId", user.getAccountId());
        val list = AccountBusiness.getPublicCompanyList(condition);
        return toJson(ResponseWrapper.page(AccountBusiness.getCompanyCount(condition), list));
    };

    public static Route getCompanyCount = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);
        if (!user.getIsAdmin()) return null;
        val condition = JSONUtil.toMap(request.body());
        return AccountBusiness.getCompanyCount(condition);
    };


    public static Route getCompanyDetail = (request, response) -> {
        val user = ensureUserIsLoggedIn(request);
        val accountId = request.queryParams("accountId");
        return toJson(AccountBusiness.load(accountId));
    };

    public static Route agreeToCreate = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);
        if (!user.getIsAdmin()) return null;
        val condition = JSONUtil.toMap(request.body());
        val isAccept = Boolean.valueOf(condition.get("isAccept").toString());
        val accountId = condition.get("accountId").toString();
        val applicant = UserBusiness.load(null, condition.get("userId").toString());
        if (isAccept && StrUtil.isNotBlank(applicant.getAccountId())) {
            throw halt(400, "该用户已加入企业！无法创建！");
        }
        val jsonObject = new JSONObject();
        jsonObject.put("enterprise", AccountBusiness.load(accountId).getAccountName());
        val message = isAccept ? StaticParam.AGREE_CREATE_COMPANY_CODE : StaticParam.REFUSE_CREATE_COMPANY_CODE;
        if (isAccept)
            PhoneCode.getPhonemsg(applicant.getUserId(), StaticParam.SIGN_NAME, message, jsonObject);
        AccountBusiness.agreeToCreate(applicant.getUserId(), accountId, isAccept);
        return true;
    };

    public static Route payForRenew = (request, response) -> {
        val user = ensureUserIsLoggedIn(request);
        if (!"xh".equals(user.getAccountId())) throw halt(400, "无操作权限");
        val data = JSONUtil.toMap(request.body());
        AccountBusiness.payForRenew(data);
        return true;
    };


    public static Route companyOrderByProductCount = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);
        if (!"xh".equals(user.getUserId())) throw halt(400, "没有操作权限");
        val condition = JSONUtil.toMap(request.body());
        val list = AccountBusiness.companyOrderByProductCount(condition);
        return toJson(ResponseWrapper.page(AccountBusiness.CompanyCount(condition), list));
    };


    public static Route updateCompany = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        val obj = JSONUtil.toBean(request.body(), AccountInfo.class);

        val condition = JSONUtil.toMap(request.body());
        val accountId = condition.get("accountId") == null ? user.getAccountId() : condition.get("accountId").toString();
        AccountBusiness.update(accountId, obj);
        return true;
    };
}
