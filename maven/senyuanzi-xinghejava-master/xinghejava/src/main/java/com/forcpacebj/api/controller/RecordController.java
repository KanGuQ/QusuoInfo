package com.forcpacebj.api.controller;

import com.forcpacebj.api.business.RecordBusiness;
import com.forcpacebj.api.core.ResponseWrapper;
import com.forcpacebj.api.entity.RecordInfo;
import com.forcpacebj.api.utils.HttpUtils;
import com.forcpacebj.api.utils.JSONUtil;
import com.forcpacebj.api.utils.MapUtil;
import com.forcpacebj.api.utils.WxAccessTokenUtil;
import lombok.extern.log4j.Log4j;
import lombok.val;
import spark.Route;

import static spark.Spark.halt;

@Log4j
public class RecordController extends BaseController {

    public static Route find = (req, res) -> {

        val user = ensureUserIsLoggedIn(req);

        val condition = JSONUtil.toMap(req.body());
        if (!user.getIsAdmin() || (condition.containsKey("showAll") && !(boolean) condition.get("showAll"))) {
            condition.put("userId", user.getUserId());
        }
        condition.put("departmentId", user.getDepartment().getId());
        condition.put("accountId", user.getAccountId());
        val result = RecordBusiness.find(condition);
        return toJson(result);
    };

    public static Route findList = (req, res) -> {

        val user = ensureUserIsLoggedIn(req);

        val condition = JSONUtil.toMap(req.body());
        if (!user.getIsAdmin() || (condition.containsKey("showAll") && !(boolean) condition.get("showAll"))) {
            condition.put("userId", user.getUserId());
        }
        condition.put("departmentId", user.getDepartment().getId());
        condition.put("accountId", user.getAccountId());
        condition.put("isAdmin", user.getIsAdmin());
        val result = RecordBusiness.findCheckIsAdmin(condition);
        return toJson(result);
    };

    public static Route count = (req, res) -> {
        val user = ensureUserIsLoggedIn(req);

        val condition = JSONUtil.toMap(req.body());
        if (!user.getIsAdmin() || (condition.containsKey("showAll") && !(boolean) condition.get("showAll"))) {
            condition.put("userId", user.getUserId());
        }
        condition.put("departmentId", user.getDepartment().getId());
        condition.put("accountId", user.getAccountId());
        return RecordBusiness.count(condition);
    };

    public static Route findRecords = (req, res) -> {

        val user = ensureUserIsLoggedIn(req);

        val condition = JSONUtil.toMap(req.body());
        if (!user.getIsAdmin() || (condition.containsKey("showAll") && !(boolean) condition.get("showAll"))) {
            condition.put("userId", user.getUserId());
        }

        condition.put("departmentId", user.getDepartment().getId());
        condition.put("accountId", user.getAccountId());
        val result = RecordBusiness.find(condition);
        return toJson(ResponseWrapper.page(RecordBusiness.count(condition), result));
    };

    public static Route load = (req, res) -> {
        val user = ensureUserIsLoggedIn(req);
        val result = RecordBusiness.load(user.getAccountId(), req.params("id"));
        return toJson(result);
    };

    public static Route list = (req, res) -> {
        val user = ensureUserIsLoggedIn(req);
        val condition = JSONUtil.toMap(req.body());
        condition.put("departmentId", user.getDepartment().getId());
        condition.put("accountId", user.getAccountId());
        val result = RecordBusiness.list(condition);
        return toJson(result);
    };

    public static Route insert = (req, res) -> {
        val user = ensureUserIsLoggedIn(req);
        try {
            val obj = JSONUtil.toBeanList(req.body(), RecordInfo.class);
            val result = RecordBusiness.insert(obj, user);
            return toJson(result);

        } catch (Exception ex) {
            log.error("Record 保存错误：", ex);
            log.error("Record Data=" + req.body());
            throw halt(500, "新增错误");
        }
    };

    public static Route update = (req, res) -> {
        val user = ensureUserIsLoggedIn(req);
        val obj = JSONUtil.toBean(req.body(), RecordInfo.class);
        RecordBusiness.update(user.getAccountId(), obj);
        return true;
    };

    public static Route updateRecordState = (req, res) -> {
        val user = ensureUserIsLoggedIn(req);
        val obj = JSONUtil.toBean(req.body(), RecordInfo.class);
        RecordBusiness.updateRecordState(user.getAccountId(), obj);
        return true;
    };

    public static Route delete = (req, res) -> {
        val user = ensureUserIsLoggedIn(req);
        RecordBusiness.delete(user.getAccountId(), req.params("id"));
        return true;
    };

    public static Route findPeopleRecord = (req, res) -> {
        val user = ensureUserIsLoggedIn(req);
        val condition = JSONUtil.toMap(req.body());
        condition.put("accountId", user.getAccountId());
        condition.put("departmentId", user.getDepartment().getId());

        val result = RecordBusiness.findPeopleRecord(condition);
        return toJson(result);
    };

    public static Route findProjectRecord = (req, res) -> {
        val user = ensureUserIsLoggedIn(req);
        val condition = JSONUtil.toMap(req.body());
        condition.put("accountId", user.getAccountId());
        condition.put("departmentId", user.getDepartment().getId());

        val result = RecordBusiness.findProjectRecord(condition);
        return toJson(ResponseWrapper.page(RecordBusiness.count(condition), result));
    };

    public static Route deletePeopleRecord = (req, res) -> {
        val user = ensureUserIsLoggedIn(req);
        RecordBusiness.deletePeopleRecord(user.getAccountId(), req.params("recordId"), req.params("peopleId"));
        return true;
    };

    public static Route deleteProjectRecord = (req, res) -> {
        val user = ensureUserIsLoggedIn(req);
        RecordBusiness.deleteProjectRecord(user.getAccountId(), req.params("recordId"), req.params("projectId"));
        return true;
    };

    public static Route openLoad = (req, res) -> {
        val result = RecordBusiness.load(req.params("accountId"), req.params("recordId"));
        return toJson(result);
    };

    public static Route createMPQRCode = (req, res) -> {

        val accountId = req.params("accountId");
        val recordId = req.params("recordId");

        //获取AccessToken
        val accessToken = WxAccessTokenUtil.getCachedAccessToken("wx9790cb68b7c7cc27", "b6073763bfb58429c7991c7472e01819");

        //获取临时小程序码
        val parameters = MapUtil.instance("scene", accountId + "#" + recordId);
        try (val out = res.raw().getOutputStream();
             val inputStream = HttpUtils.download("https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token=" + accessToken, JSONUtil.toJson(parameters))) {
            int bt;
            while ((bt = inputStream.read()) != -1) {
                out.write(bt);
            }
        }

        return "";
    };
}
