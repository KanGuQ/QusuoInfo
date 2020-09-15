package com.forcpacebj.api.controller;

import com.alibaba.fastjson.JSONObject;
import com.forcpacebj.api.business.*;
import com.forcpacebj.api.config.StaticParam;
import com.forcpacebj.api.core.ResponseWrapper;
import com.forcpacebj.api.entity.UserInfo;
import com.forcpacebj.api.entity.UserRoleInfo;
import com.forcpacebj.api.utils.*;
import lombok.extern.log4j.Log4j;
import lombok.val;
import lombok.var;
import redis.clients.jedis.Jedis;
import spark.Route;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static spark.Spark.halt;

@Log4j
public class UserController extends BaseController {

    public static Route find = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        val condition = JSONUtil.toMap(request.body());
        val accountId = condition.get("accountId") == null ? user.getAccountId() : condition.get("accountId").toString();
        val list = UserBusiness.find(condition, accountId);
        return toJson(list);
    };

    public static Route count = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        val condition = JSONUtil.toMap(request.body());
        val accountId = condition.containsKey("accountId") ? (String) condition.get("accountId") : user.getAccountId();
        return UserBusiness.count(condition, accountId);
    };

    public static Route findEmployees = (request, response) -> {
        val user = ensureUserIsLoggedIn(request);
        if (!user.getIsAdmin()) throw halt(400, "没有操作权限");
        val condition = JSONUtil.toMap(request.body());
        val accountId = condition.get("accountId") == null ? user.getAccountId() : condition.get("accountId").toString();
        val list = UserBusiness.find(condition, accountId);
        return toJson(ResponseWrapper.page(UserBusiness.count(condition, accountId), list));
    };

    public static Route findAllUsers = (request, response) -> {
        val user = ensureUserIsLoggedIn(request);
        if (!user.getIsAdmin()) throw halt(400, "没有操作权限");
        val condition = JSONUtil.toMap(request.body());
        val list = UserBusiness.find(condition);
        return toJson(ResponseWrapper.page(UserBusiness.count(condition), list));
    };

    public static Route list = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        val list = UserBusiness.list(user.getAccountId());
        return toJson(list);
    };

    public static Route load = (request, response) -> {

        String accountId = request.queryParams("account-id");
        if (StrUtil.isBlank(accountId)) {
            val user = ensureUserIsLoggedIn(request);
            accountId = user.getAccountId();
        } else if ("DEFAULT".equals(accountId)) {
            accountId = null;
        }

        val obj = UserBusiness.load(accountId, request.params(":id"));
        return toJson(obj);
    };

    public static Route delete = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        val condition = JSONUtil.toMap(request.body());
        val accountId = condition.get("accountId") == null ? user.getAccountId() : condition.get("accountId").toString();

        UserBusiness.delete(accountId, request.params(":id"));
        return true;
    };

    public static Route isAlreadyReg = (request, response) -> {
        val phone = JSONUtil.toMap(request.body()).get("phoneNum").toString();
        return UserBusiness.load(null, phone) == null;
    };

    public static Route register = (request, response) -> {
        try (Jedis jedis = JedisUtil.getJedis()) {
            val obj = JSONUtil.toBean(request.body(), UserInfo.class);
            val code = JSONUtil.toMap(request.body()).get("code").toString();

            if (UserBusiness.load(null, obj.getPhoneNumber()) != null)
                throw halt(400, "该手机号码已注册！");
            if (StrUtil.isNotBlank(code)) {
                if (!code.equals(jedis.get(obj.getPhoneNumber() + "register"))) {
                    throw halt(400, "验证码有误");
                }
            }
            obj.setUserId(obj.getPhoneNumber());
            obj.setUserRole(new UserRoleInfo(1, null, null));
            UserBusiness.insert(obj);
            return true;
        } catch (Exception ex) {

            if (StrUtil.isNotBlank(ex.getMessage()) && ex.getMessage().contains("Duplicate entry")) {
                throw halt(400, "该账号已注册！");
            } else {
                throw ex;
            }
        }
    };

    public static Route registerByCode = (request, response) -> {
        val obj = JSONUtil.toBean(request.body(), UserInfo.class);
        val code = JSONUtil.toMap(request.body()).get("inviteCode").toString();
        if (UserBusiness.load(null, obj.getPhoneNumber()) != null)
            throw halt(400, "该手机号码已注册！");
        if (StrUtil.isBlank(code)) {
            throw halt(400, "邀请码为空！");
        }
        var account = AccountBusiness.checkInviteCode(obj.getPhoneNumber(), code);
        if (account.getAccountId() == null) throw halt(400, "邀请码无效");
        if (account.getUserNum() >= account.getUserLimit()) throw halt(400, "该公司用户人数已满");

        obj.setUserId(obj.getPhoneNumber());
        obj.setUserRole(new UserRoleInfo(1, null, null));
        return StaffJoinRecordBusiness.registerFromInviteCode(obj, code);
    };

    public static Route toApplyFor = (request, response) -> {
        val data = JSONUtil.toMap(request.body());
        val account = AccountBusiness.loadByName((String) data.get("accountName"));
        if (account == null) throw halt(400, "请输入正确公司名称");
        val accountId = account.getAccountId();
        if (AccountBusiness.checkUserLimit(accountId)) throw halt(400, "该公司用户人数已满");
        List<String> userId = new ArrayList<>();
        userId.add((String) data.get("userId"));
        val conditions = new HashMap<>();
        conditions.put("accountId", accountId);
        conditions.put("userId", userId);
        conditions.put("state", 0);
        if (StaffJoinRecordBusiness.getJoinRecordCount(conditions, accountId) != 0) {
            throw halt(400, "已发送申请，请联系管理员通过！");
        }
        StaffJoinRecordBusiness.insert(accountId, null, userId);
        return true;
    };

    public static Route toJoinCompany = (request, response) -> {
        val data = JSONUtil.toMap(request.body());
        val userId = data.get("userId").toString();
        UserBusiness.load(null, userId);
        val inviteCode = data.get("inviteCode").toString();

        var account = AccountBusiness.checkInviteCode(userId, inviteCode);
        if (account.getAccountId() == null) throw halt(400, "邀请码无效");
        if (account.getUserNum() >= account.getUserLimit()) throw halt(400, "该公司用户人数已满");
        return StaffJoinRecordBusiness.joinFromInviteCode(userId, inviteCode);
    };

    public static Route getApplyForList = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);
        val condition = JSONUtil.toMap(request.body());
        val list = StaffJoinRecordBusiness.getJoinRecordList(condition, user.getAccountId());
        return toJson(list);
    };

    public static Route getApplyForCount = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        val condition = JSONUtil.toMap(request.body());
        return StaffJoinRecordBusiness.getJoinRecordCount(condition, user.getAccountId());
    };

    public static Route getInviteList = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);
        val condition = JSONUtil.toMap(request.body());
        condition.put("code", "NOT NULL");
        val list = StaffJoinRecordBusiness.getJoinRecordList(condition, user.getAccountId());
        return toJson(list);
    };

    public static Route getInviteCount = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        val condition = JSONUtil.toMap(request.body());
        condition.put("code", "NOT NULL");
        return StaffJoinRecordBusiness.getJoinRecordCount(condition, user.getAccountId());
    };

    public static Route isAcceptJoinCompany = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);
        if (AccountBusiness.checkUserLimit(user.getAccountId())) throw halt(400, "该公司用户人数已满");
        val condition = JSONUtil.toMap(request.body());
        val isAccept = Boolean.valueOf(condition.get("isAccept").toString());
        val applicant = UserBusiness.load(null, condition.get("userId").toString());
        val departmentId = DepartmentBusiness.getDefaultDepartment(user.getAccountId());

        if (isAccept && StrUtil.isNotBlank(applicant.getAccountId())) {
            if (applicant.getAccountId().equals(user.getAccountId())) {
                throw halt(400, "该用户已加入本企业！");
            } else {
                throw halt(400, "该用户已加入其他企业！");
            }
        }
        val jsonObject = new JSONObject();
        jsonObject.put("enterprise", AccountBusiness.load(user.getAccountId()).getAccountName());
        if (isAccept)
            PhoneCode.getPhonemsg(applicant.getUserId(), StaticParam.SIGN_NAME, StaticParam.AGREE_JOIN_CODE, jsonObject);

        StaffJoinRecordBusiness.isAcceptJoinCompany(user.getAccountId(), departmentId, applicant.getUserId(), isAccept);
        return true;
    };

    @SuppressWarnings("unchecked")
    public static Route inviteToCompany = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        val condition = JSONUtil.toMap(request.body());
        val phoneNumbers = (List<String>) condition.get("phoneNumber");
        val departmentId = DepartmentBusiness.getDefaultDepartment(user.getAccountId());

        val invalidPhoneNums = new ArrayList<String>();

        phoneNumbers.forEach(phoneNum -> {
            val applicant = UserBusiness.load(null, phoneNum);
            if (applicant != null && StrUtil.isNotBlank(applicant.getAccountId())) {
                invalidPhoneNums.add(phoneNum);
            }
        });
        phoneNumbers.removeAll(invalidPhoneNums);
        if (phoneNumbers.isEmpty()){
            throw halt(400, "该用户已注册！");
        }
        StaffJoinRecordBusiness.insert(user.getAccountId(), departmentId, phoneNumbers);
        return CollectionUtil.isEmpty(invalidPhoneNums) ? true : invalidPhoneNums;
    };

    public static Route getRegisterVcode = (request, response) -> {
        val phone = JSONUtil.toMap(request.body()).get("phoneNumber").toString();
        JSONObject jsonObject = new JSONObject();
        val code = PhoneCode.vcode();
        jsonObject.put("code", code);
        val returnCode = PhoneCode.getPhonemsg(phone, StaticParam.SIGN_NAME, StaticParam.VERIFICATION_CODE, jsonObject);
        if ("OK".equals(returnCode))
            try (Jedis jedis = JedisUtil.getJedis()) {
                jedis.set(phone + "register", code);
                jedis.set(phone + "register", code, "XX", "EX", 300);
            }
        return returnCode;
    };

    public static Route checkCompany = (request, response) -> {
        val accountName = request.params(":accountName");
        return toJson(AccountBusiness.loadByName(accountName));
    };

    public static Route update = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        val obj = JSONUtil.toBean(request.body(), UserInfo.class);

        val condition = JSONUtil.toMap(request.body());
        val accountId = condition.get("accountId") == null ? user.getAccountId() : condition.get("accountId").toString();
        UserBusiness.update(accountId, obj);
        return true;
    };


    public static Route findAllByAccountId = (request, response) -> {
        val user = ensureUserIsLoggedIn(request);

        val accountId = request.queryParams("accountId");
        val pageSize = request.queryParams("pageSize");
        val pageOffset = request.queryParams("pageOffset");

        return toJson(UserBusiness.findAllByAccountId(accountId, Integer.parseInt(pageSize), Integer.parseInt(pageOffset)));

    };

    public static Route getUser = (request, response) -> toJson(ensureUserIsLoggedIn(request));

    public static Route trial = (request, response) -> {

        val userId = (String) JSONUtil.toMap(request.body()).get("userId");
        val user = UserBusiness.load(null, userId);

        if (user.getAccountId() == null) {
            halt(400, "试用失败");
        }
        user.setAccountName("试用企业");
        user.setIsAdmin(true);
        generatorToken(user);
        return toJson(user);
    };

    public static Route expel = (request, response) -> {
        val user = ensureUserIsLoggedIn(request);
        val condition = JSONUtil.toMap(request.body());
        val expelUser = UserBusiness.load(user.getAccountId(), (String) condition.get("userId"));

        if (expelUser == null) halt(400, "用户不存在");
        if (expelUser.getIsAdmin()) halt(400, "用户为管理员");
        if (!user.getIsAdmin()) halt(400, "无权限");
        UserBusiness.expel(condition);
        return true;
    };

    public static Route changeDepartment = (request, response) -> {
        val user = ensureUserIsLoggedIn(request);
        val condition = JSONUtil.toMap(request.body());
        val expelUser = UserBusiness.load(user.getAccountId(), (String) condition.get("userId"));

        if (expelUser == null) halt(400, "用户不存在");
        if (!user.getIsAdmin()) halt(400, "无权限");
        UserBusiness.changeDepartment(condition);
        return true;
    };

    public static Route loginRecordByDate = (request, response) -> {
        val user = ensureUserIsLoggedIn(request);
        val condition = JSONUtil.toMap(request.body());
        return toJson(UserBusiness.loginRecordByDate(condition));
    };

}
