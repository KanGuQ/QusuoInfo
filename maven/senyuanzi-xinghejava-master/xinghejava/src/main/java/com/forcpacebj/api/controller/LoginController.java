package com.forcpacebj.api.controller;

import com.forcpacebj.api.business.AccountBusiness;
import com.forcpacebj.api.business.GoEasyMessageBusiness;
import com.forcpacebj.api.business.LoginBusiness;
import com.forcpacebj.api.business.UserBusiness;
import com.forcpacebj.api.entity.*;
import com.forcpacebj.api.utils.JSONUtil;
import com.forcpacebj.api.utils.StrUtil;
import lombok.extern.log4j.Log4j;
import lombok.val;
import spark.Route;

import static spark.Spark.halt;

@Log4j
public class LoginController extends BaseController {

    public static Route check = (request, response) -> {

        val login = JSONUtil.toBean(request.body(), UserInfo.class);

        if (StrUtil.isNotBlank(login.getUserId()) && StrUtil.isNotBlank(login.getPwd())) {
            val user = LoginBusiness.checkLogin(login.getUserId(), login.getPwd());
            if (user != null) {
                UserBusiness.lastLogin(user);
                if (StrUtil.isBlank(user.getAccountId())) {
                    return false;
                } else {
                    if (!AccountBusiness.check(user.getAccountId())) throw halt(400, "您所在的企业账号已到期，请联系管理员续费");
                }
                generatorToken(user);
                GoEasyMessageBusiness.pushOffLineTimeOutMessage(user.getUserId());
                return toJson(user);
            }
        }

        throw halt(401, "用户名或者密码错误");
    };

    public static Route modify = (request, response) -> {

        ensureUserIsLoggedIn(request);

        val modifyPassword = JSONUtil.toBean(request.body(), ModifyPasswordInfo.class);
        if (LoginBusiness.checkLogin(modifyPassword.getUserId(), modifyPassword.getOriginPwd()) != null) {
            LoginBusiness.modifyPwd(modifyPassword.getUserId(), modifyPassword.getNewPwd());
            return true;
        } else {
            throw halt(401, "原密码错误");
        }
    };

    public static Route reset = (request, response) -> {

        ensureUserIsLoggedIn(request);

        val login = JSONUtil.toBean(request.body(), UserInfo.class);
        LoginBusiness.modifyPwd(login.getUserId(), Default_Pwd);
        return true;
    };

    public static Route updateContent = (request, response) -> {
        return toJson(LoginBusiness.updateContent());
    };

}