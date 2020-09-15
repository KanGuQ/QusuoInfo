package com.forcpacebj.api.controller;

import com.forcpacebj.api.business.ProjectBusiness;
import com.forcpacebj.api.business.ProjectStageBusiness;
import com.forcpacebj.api.business.db;
import com.forcpacebj.api.config.StaticParam;
import com.forcpacebj.api.core.ResponseWrapper;
import com.forcpacebj.api.entity.AccountInfo;
import com.forcpacebj.api.entity.ProjectInfo;
import com.forcpacebj.api.entity.UserInfo;
import com.forcpacebj.api.utils.DateUtil;
import com.forcpacebj.api.utils.IdGenerator;
import com.forcpacebj.api.utils.JSONUtil;
import com.forcpacebj.api.utils.MapUtil;
import lombok.extern.log4j.Log4j;
import lombok.val;
import spark.Route;

import java.awt.*;
import java.util.ArrayList;
import java.util.Map;

import static spark.Spark.halt;

@Log4j
public class ProjectController extends BaseController {

    public static Route quotedProjectFind = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        val condition = JSONUtil.toMap(request.body());
        isShowAll(condition, user);
        condition.put("departmentId", user.getDepartment().getId());
        condition.put("accountId", user.getAccountId());
        val list = ProjectBusiness.quotedProjectFind(condition);
        return toJson(list);
    };

    public static Route quotedProjectCount = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        val condition = JSONUtil.toMap(request.body());
        isShowAll(condition, user);
        condition.put("departmentId", user.getDepartment().getId());
        condition.put("accountId", user.getAccountId());
        return ProjectBusiness.quotedProjectCount(condition);
    };

    public static Route find = (req, res) -> {

        val user = ensureUserIsLoggedIn(req);
        val condition = JSONUtil.toMap(req.body());

        isShowAll(condition, user);
        condition.put("departmentId", user.getDepartment().getId());
        condition.put("accountId", user.getAccountId());
        val result = ProjectBusiness.find(condition);
//        if ("知晓".equals(condition.get("projectStage")) && ProjectBusiness.count(new HashMap(), user.getAccountId()) == 0)
//            result.add(ProjectBusiness.load("test", "projectStage"));
//        if ("立项".equals(condition.get("engineeringStage")) && ProjectBusiness.count(new HashMap(), user.getAccountId()) == 0)
//            result.add(ProjectBusiness.load("test", "engineeringStage"));
        return toJson(result);
    };

    public static Route count = (req, res) -> {
        val user = ensureUserIsLoggedIn(req);
        val condition = JSONUtil.toMap(req.body());
        isShowAll(condition, user);
        condition.put("departmentId", user.getDepartment().getId());
        condition.put("accountId", user.getAccountId());
        return ProjectBusiness.count(condition);
    };

    public static Route findKanbanProjects = (req, res) -> {

        val user = ensureUserIsLoggedIn(req);

        val condition = JSONUtil.toMap(req.body());
        isShowAll(condition, user);
        condition.put("departmentId", user.getDepartment().getId());
        condition.put("accountId", user.getAccountId());
        val result = ProjectBusiness.findKanbanProjects(condition);
        return toJson(result);
    };

    public static Route list = (req, res) -> {
        val user = ensureUserIsLoggedIn(req);

        val condition = MapUtil.instance();
        if (!user.getIsAdmin()) {
            condition.put("userId", user.getUserId());
        }
        condition.put("departmentId", user.getDepartment().getId());
        condition.put("accountId", user.getAccountId());
        val result = ProjectBusiness.list(condition);
        return toJson(result);
    };

    public static Route load = (req, res) -> {

        val user = ensureUserIsLoggedIn(req);
        val result = ProjectBusiness.load(user.getAccountId(), req.params("id"));
        return toJson(result);
    };

    public static Route delete = (req, res) -> {
        val user = ensureUserIsLoggedIn(req);
        ProjectBusiness.delete(user.getAccountId(), req.params("id"));
        return true;
    };

    public static Route safeDelete = (req, res) -> {
        val user = ensureUserIsLoggedIn(req);
        val data = JSONUtil.toMap(req.body());
        ProjectBusiness.safeDelete(user.getAccountId(), data.get("projectIds").toString(), (Boolean) data.get("state"));
        return true;
    };

    public static Route insert = (req, res) -> {

        val user = ensureUserIsLoggedIn(req);
        val now = DateUtil.now();
        val obj = JSONUtil.toBean(req.body(), ProjectInfo.class);
        val newProjectId = IdGenerator.NewId();

        obj.setProjectId(newProjectId);
        obj.setCreateUser(user);
        obj.setCreateDateTime(now);
        obj.setModifyUser(user);
        obj.setModifyDateTime(now);
        obj.setDepartmentId(user.getDepartment().getId());
        obj.setAccountId(user.getAccountId());

        ProjectBusiness.insert(obj);
        ProjectBusiness.insertPublish(obj);
        return toJson(newProjectId);

    };

    public static Route update = (req, res) -> {
        val user = ensureUserIsLoggedIn(req);
        val obj = JSONUtil.toBean(req.body(), ProjectInfo.class);
        obj.setModifyUser(user);
        obj.setModifyDateTime(DateUtil.now());
        ProjectBusiness.update(user.getAccountId(), obj);
        ProjectBusiness.updatePublish(obj);
        return toJson(obj.getProjectId());
    };

    public static Route updateProjectStage = (req, res) -> {
        val user = ensureUserIsLoggedIn(req);
        val params = JSONUtil.toMap(req.body());
        ProjectBusiness.updateProjectStage((String) params.get("projectId"), (String) params.get("stageId"));
        return true;
    };

    public static Route getStageCount = (req, res) -> {
        val user = ensureUserIsLoggedIn(req);
        val condition = JSONUtil.toMap(req.body());
//        if (!user.getIsAdmin() || (condition.containsKey("showAll") && !(boolean) condition.get("showAll"))) {
//            condition.put("userId", user.getUserId());
//        }
        isShowAll(condition, user);
        condition.put("departmentId", user.getDepartmentId());
        condition.put("accountId", user.getAccountId());
        condition.put("isAdmin", user.getIsAdmin());
        //如果是管理员则显示所有的，否则显示个人的
       return (user.getIsAdmin()) ? ProjectBusiness.getStageAllCount(condition) : ProjectBusiness.getStageCount(condition);
    };

    public static Route cooperativeProviders = (request, response) -> {
        val user = ensureUserIsLoggedIn(request);
        val condition = JSONUtil.toMap(request.body());

        val list = new ArrayList<AccountInfo>();

        val permissionAccounts = new ArrayList<String>();
        permissionAccounts.add("ylst");
        permissionAccounts.add("1229685924707696640");
        if (permissionAccounts.contains(user.getAccountId())) {


            val account = new AccountInfo();
            account.setAccountId("ZNBJ");
            account.setAccountName("者尼智能影音");


            val sql = "select count(1) from cooperation " +
                      "where projectId=:projectId " +
                      "and cooperativeAccountId=:cooperativeAccountId";

            try (val con = db.sql2o.open()) {
                val count = con.createQuery(sql)
                        .addParameter("projectId", condition.get("projectId"))
                        .addParameter("cooperativeAccountId", account.getAccountId())
                        .executeScalar(int.class);
                if (count > 0) {
                    account.setIsCooperative(true);
                } else {
                    account.setIsCooperative(false);
                }
            }

            list.add(account);

            val account2 = new AccountInfo();
            account2.setAccountId("ZNSM");
            account2.setAccountName("者尼贸易");


            try (val con = db.sql2o.open()) {
                val count = con.createQuery(sql)
                        .addParameter("projectId", condition.get("projectId"))
                        .addParameter("cooperativeAccountId", account2.getAccountId())
                        .executeScalar(int.class);
                if (count > 0) {
                    account2.setIsCooperative(true);
                } else {
                    account2.setIsCooperative(false);
                }
            }

            list.add(account2);
        } else {

        }
        return toJson(list);
    };

    public static Route applyCooperation = (request, response) -> {
        val user = ensureUserIsLoggedIn(request);
        val params = JSONUtil.toMap(request.body());
        ProjectBusiness.applyCooperation(user.getAccountId(), (String) params.get("projectId"), (String) params.get("accountId"));
        return true;
    };
    public static Route auditCooperation = (request, response) -> {
        val user = ensureUserIsLoggedIn(request);
        val params = JSONUtil.toMap(request.body());
        ProjectBusiness.auditCooperation((Boolean) params.get("isOK"), (String) params.get("id"));
        return true;
    };

    public static Route cooperations = (request, response) -> {
        val user = ensureUserIsLoggedIn(request);
        val params = JSONUtil.toMap(request.body());
        params.put("accountId", user.getAccountId());
        val list = ProjectBusiness.cooperations(params);
        return toJson(ResponseWrapper.page(ProjectBusiness.countCooperation(params), list));
    };


    private static void powerCheck(Map condition, UserInfo user) {

        val stageType = condition.get("stageType") == null ?
                ProjectStageBusiness.getStageType(condition.get("stageId")) : condition.get("stageType");
        if (!(checkPower(user) || user.getUserRole().getPower().contains(StaticParam.DEPARTMENT_MANAGER))) {
            if (stageType == StaticParam.OPPORTUNITY_STAGE) {
                if (!user.getUserRole().getPower().contains(StaticParam.ALL_OPPORTUNITIES))
                    throw halt(400, "无操作权限");
            } else if (stageType == (StaticParam.ENGINEERING_STAGE))
                if (!user.getUserRole().getPower().contains(StaticParam.ALL_PROJECTS))
                    throw halt(400, "无操作权限");
        }
    }

    private static void isShowAll(Map condition, UserInfo user) {
        if (condition.containsKey("showAll"))
            if ((Boolean) condition.get("showAll"))
                powerCheck(condition, user);
            else
                condition.put("userId", user.getUserId());
        else
            condition.put("userId", user.getUserId());
    }

}
