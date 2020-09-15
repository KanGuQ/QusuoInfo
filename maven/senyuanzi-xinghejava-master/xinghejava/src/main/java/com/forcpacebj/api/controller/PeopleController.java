package com.forcpacebj.api.controller;

import com.forcpacebj.api.business.PeopleBusiness;
import com.forcpacebj.api.config.StaticParam;
import com.forcpacebj.api.entity.PeopleInfo;
import com.forcpacebj.api.entity.UserInfo;
import com.forcpacebj.api.utils.DateUtil;
import com.forcpacebj.api.utils.IdGenerator;
import com.forcpacebj.api.utils.JSONUtil;
import lombok.extern.log4j.Log4j;
import lombok.val;
import spark.Route;

import java.util.Map;

import static spark.Spark.halt;

@Log4j
public class PeopleController extends BaseController {

    public static Route find = (req, res) -> {

        val user = ensureUserIsLoggedIn(req);
        val condition = JSONUtil.toMap(req.body());
        isShowAll(condition, user);
        condition.put("departmentId", user.getDepartment().getId());
        condition.put("accountId", user.getAccountId());
        val result = PeopleBusiness.find(condition);
        return toJson(result);
    };

    public static Route count = (req, res) -> {

        val user = ensureUserIsLoggedIn(req);
        val condition = JSONUtil.toMap(req.body());
        isShowAll(condition, user);
        condition.put("departmentId", user.getDepartment().getId());
        condition.put("accountId", user.getAccountId());
        return PeopleBusiness.count(condition);
    };

    public static Route list = (req, res) -> {
        val user = ensureUserIsLoggedIn(req);
        val result = PeopleBusiness.list(user.getAccountId(), user.getDepartment().getId());
        return toJson(result);
    };

    public static Route load = (req, res) -> {
        val user = ensureUserIsLoggedIn(req);
        val result = PeopleBusiness.load(user.getAccountId(), req.params("id"));
        return toJson(result);
    };

    public static Route loadPeopleBaseInfo = (req, res) -> {
        val user = ensureUserIsLoggedIn(req);
        val result = PeopleBusiness.loadPeopleBaseInfo(user.getAccountId(), req.params("id"));
        return toJson(result);
    };

    public static Route delete = (req, res) -> {
        val user = ensureUserIsLoggedIn(req);
        powerCheck(user);
        PeopleBusiness.delete(user.getAccountId(), req.params("id"));
        return true;
    };

    public static Route safeDelete = (req, res) -> {
        val user = ensureUserIsLoggedIn(req);
        powerCheck(user);
        val data = JSONUtil.toMap(req.body());
        PeopleBusiness.safeDelete(user.getAccountId(), data.get("peopleIds").toString(), (Boolean) data.get("state"));
        return true;
    };

    public static Route insert = (req, res) -> {

        val user = ensureUserIsLoggedIn(req);
        val now = DateUtil.now();
        val obj = JSONUtil.toBean(req.body(), PeopleInfo.class);
        val newPeopleId = IdGenerator.NewId();

        obj.setPeopleId(newPeopleId);
        obj.setCreateUser(user);
        obj.setCreateDateTime(now);
        obj.setUser(user);
        obj.setUpdateTime(now);

        obj.setDepartmentId(user.getDepartment().getId());
        obj.setAccountId(user.getAccountId());
        PeopleBusiness.insert(obj);
        return toJson(newPeopleId);

    };

    public static Route update = (req, res) -> {
        val user = ensureUserIsLoggedIn(req);
        powerCheck(user);
        val obj = JSONUtil.toBean(req.body(), PeopleInfo.class);
        obj.setUser(user);
        obj.setUpdateTime(DateUtil.now());
        PeopleBusiness.update(user.getAccountId(), obj);
        return toJson(obj.getPeopleId());
    };

    private static void powerCheck(UserInfo user) {
        if (!checkPower(user))
            if (!(user.getUserRole().getPower().contains(StaticParam.ALL_PEOPLES) || user.getUserRole().getPower().contains(StaticParam.ALL_CUSTOMERS) || user.getUserRole().getPower().contains(StaticParam.DEPARTMENT_MANAGER)))
                throw halt(400, "无操作权限");
    }

    private static void isShowAll(Map<String, Object> condition, UserInfo user) {
        if (condition.containsKey("showAll"))
            if ((Boolean) condition.get("showAll"))
                powerCheck(user);
            else
                condition.put("userId", user.getUserId());
        else
            condition.put("userId", user.getUserId());
    }
}
