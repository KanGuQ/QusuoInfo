package com.forcpacebj.api.controller;

import com.forcpacebj.api.business.AccountBusiness;
import com.forcpacebj.api.business.CommunityInfoBusiness;
import com.forcpacebj.api.business.FriendBusiness;
import com.forcpacebj.api.business.UserRoleBusiness;
import com.forcpacebj.api.core.ResponseWrapper;
import com.forcpacebj.api.entity.CommunityInfo;
import com.forcpacebj.api.entity.FriendInfo;
import com.forcpacebj.api.entity.UserInfo;
import com.forcpacebj.api.entity.UserRoleInfo;
import com.forcpacebj.api.utils.DateUtil;
import com.forcpacebj.api.utils.IdGenerator;
import com.forcpacebj.api.utils.JSONUtil;
import lombok.val;
import spark.Route;

import static spark.Spark.halt;
import static spark.Spark.redirect;

/**
 * {@link CommunityController}
 * Author: ACL
 * Date:2020/02/22
 * Description: 小区 Controller
 * Created by ACL on 2020/02/22.
 */
public class CommunityController extends BaseController {

    public static Route find = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        val condition = JSONUtil.toMap(request.body());
        condition.put( "userId",user.getUserId() );
        val list = CommunityInfoBusiness.find(condition, user.getAccountId());

           return toJson( ResponseWrapper.page( CommunityInfoBusiness.count(condition, user.getAccountId()), list));
    };

    public static Route count = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        val condition = JSONUtil.toMap(request.body());
        return CommunityInfoBusiness.count(condition, user.getAccountId());
    };

    public static Route load = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);
        val condition = JSONUtil.toMap(request.body());
        condition.put( "userId",user.getUserId() );
        val obj = CommunityInfoBusiness.load(user.getAccountId(), condition);
        return toJson(obj);
    };

    public static Route delete = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        CommunityInfoBusiness.delete(user.getAccountId(), request.params(":id"));
        return true;
    };

    public static Route insert = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        val obj = JSONUtil.toBean(request.body(), CommunityInfo.class);
        obj.setCreateUser( user ); //设置创建用户
        val newProjectId = IdGenerator.NewId();
        obj.setId( newProjectId );
        CommunityInfoBusiness.insert(user.getAccountId(), obj);
        return toJson(obj);
    };

    public static Route update=(request,response)->{
        val user = ensureUserIsLoggedIn(request);
        val obj = JSONUtil.toBean(request.body(), CommunityInfo.class);
        CommunityInfoBusiness.update(user.getAccountId(), obj);
        return toJson(obj);
    };
}
