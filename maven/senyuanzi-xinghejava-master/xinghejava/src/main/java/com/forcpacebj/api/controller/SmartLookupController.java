package com.forcpacebj.api.controller;

import com.forcpacebj.api.business.SmartLookupBusiness;
import com.forcpacebj.api.business.UserBusiness;
import com.forcpacebj.api.entity.SmartLookupItemInfo;
import com.forcpacebj.api.utils.CollectionUtil;
import lombok.extern.log4j.Log4j;
import lombok.val;
import spark.Route;

import java.util.ArrayList;

@Log4j
public class SmartLookupController extends BaseController {

    public static Route loadUserList = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);
        val list = new ArrayList<SmartLookupItemInfo>();
        val userList = UserBusiness.list(user.getAccountId());

        if (CollectionUtil.isNotEmpty(userList)) {
            userList.forEach(obj -> list.add(new SmartLookupItemInfo(obj.getUserId(), obj.getUserName())));
        }
        list.add(new SmartLookupItemInfo("未设置", "(未设置)"));
        return toJson(list);
    };

    public static Route product = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        val list = SmartLookupBusiness.searchProduct(user.getAccountId(), request.body(), user.getIsAdmin() ? -999999 : user.getUserRole().getRoleId());
        return toJson(list);
    };

    public static Route brand = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        val list = SmartLookupBusiness.searchBrand(user.getAccountId(), request.body());
        return toJson(list);
    };

    public static Route companyName = (request, response) -> {

        val list = SmartLookupBusiness.searchCompanyName(request.body());
        return toJson(list);
    };

    public static Route userList = (req, res) -> {

        val user = ensureUserIsLoggedIn(req);

        val list = new ArrayList<SmartLookupItemInfo>();
        val userList = UserBusiness.list(user.getAccountId());
        if (CollectionUtil.isNotEmpty(userList)) {
            userList.forEach(u -> list.add(new SmartLookupItemInfo(u.getUserId(), u.getUserName())));
        }
        return toJson(list);
    };

    public static Route project = (request, response) -> {
        val user = ensureUserIsLoggedIn(request);
        val userId = user.getIsAdmin() ? null : user.getUserId();
        val list = SmartLookupBusiness.searchProject(user.getAccountId(), request.body(), userId);
        return toJson(list);
    };

    public static Route people = (request, response) -> {
        val user = ensureUserIsLoggedIn(request);
        val userId = user.getIsAdmin() ? null : user.getUserId();
        val list = SmartLookupBusiness.searchPeople(user.getAccountId(), request.body(), userId);
        return toJson(list);
    };

    public static Route peopleUnit = (request, response) -> {
        val user = ensureUserIsLoggedIn(request);
        val list = SmartLookupBusiness.searchPeopleUnit(user.getAccountId(), request.body());
        return toJson(list);
    };

    public static Route peopleName = (request, response) -> {
        val user = ensureUserIsLoggedIn(request);
        val list = SmartLookupBusiness.searchPeopleName(user.getAccountId(), request.body());
        return toJson(list);
    };

    public static Route projectName = (request, response) -> {
        val user = ensureUserIsLoggedIn(request);
        val list = SmartLookupBusiness.searchProjectName(user.getAccountId(), request.body());
        return toJson(list);
    };
}
