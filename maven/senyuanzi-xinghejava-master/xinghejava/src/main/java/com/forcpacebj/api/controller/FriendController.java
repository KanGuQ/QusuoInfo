package com.forcpacebj.api.controller;

import com.forcpacebj.api.business.AccountBusiness;
import com.forcpacebj.api.business.FriendBusiness;
import com.forcpacebj.api.entity.FriendInfo;
import com.forcpacebj.api.utils.DateUtil;
import com.forcpacebj.api.utils.JSONUtil;
import lombok.val;
import spark.Route;

import static spark.Spark.halt;

public class FriendController extends BaseController {

    public static Route find = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        val condition = JSONUtil.toMap(request.body());
        val list = FriendBusiness.find(condition, user.getAccountId());
        return toJson(list);
    };

    public static Route count = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        val condition = JSONUtil.toMap(request.body());
        return FriendBusiness.count(condition, user.getAccountId());
    };

    public static Route load = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        val obj = FriendBusiness.load(user.getAccountId(), request.params(":friendAccountId"));
        return toJson(obj);
    };

    public static Route delete = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        FriendBusiness.delete(user.getAccountId(), request.params(":friendAccountId"));
        return true;
    };

    public static Route myInviteCode = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        return AccountBusiness.load(user.getAccountId()).getInviteCode();
    };

    public static Route insert = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);
        val condition = JSONUtil.toMap(request.body());

        val friendAccount = AccountBusiness.loadByName((String) condition.get("accountName"));
        if (friendAccount == null) {
            throw halt(400, "该公司无效");
        }

        if (user.getAccountId().equals(friendAccount.getAccountId())) {
            throw halt(400, "不允许添加自己");
        }

        val friend = FriendBusiness.load(user.getAccountId(), friendAccount.getAccountId());
        if (friend != null) {
            throw halt(400, "已经存在一个邀请，请等待接受邀请");
        }

        val obj = new FriendInfo();
        obj.setFriendAccount(friendAccount);
        obj.setAccountId(user.getAccountId());
        obj.setCreateTime(DateUtil.now());
        FriendBusiness.insert(user.getAccountId(), obj);
        return true;
    };

    public static Route accept = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        val friendAccountId = request.params(":friendAccountId");
        FriendBusiness.accept(user.getAccountId(), friendAccountId);
        return true;
    };

    public static Route update = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        val obj = JSONUtil.toBean(request.body(), FriendInfo.class);
        FriendBusiness.update(user.getAccountId(), obj);
        return toJson(obj);
    };

    public static Route canVisit = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);
        val condition = JSONUtil.toMap(request.body());

        val friendAccountIds = condition.get("friendAccountIds");
        val canVisit = condition.get("canVisit");

        FriendBusiness.canVisit(user.getAccountId(), String.valueOf(friendAccountIds), Boolean.valueOf(String.valueOf(canVisit)));
        return true;
    };
}
