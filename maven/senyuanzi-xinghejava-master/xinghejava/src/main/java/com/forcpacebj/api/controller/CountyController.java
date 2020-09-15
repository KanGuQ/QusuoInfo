package com.forcpacebj.api.controller;

import com.forcpacebj.api.business.AccountBusiness;
import com.forcpacebj.api.business.CountyBusiness;
import com.forcpacebj.api.entity.AccountInfo;
import com.forcpacebj.api.utils.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import spark.Route;

/**
 * {@link CountyController}
 * Author: ACL
 * Date:2020/02/18
 * Description:
 * Created by ACL on 2020/02/18.
 */
@Slf4j
public class CountyController extends BaseController {

    /**
     * 查询区县 信息列表
     */
    public static Route list = (req, res) -> {
        val user = ensureUserIsLoggedIn(req);
        val condition = JSONUtil.toMap(req.body());
        condition.put("userId", user.getUserId());
        val list = CountyBusiness.list(condition, user.getAccountId());
        return toJson(list);
    };


}
