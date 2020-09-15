package com.forcpacebj.api.controller;

import com.forcpacebj.api.business.ConstructStageBusiness;
import com.forcpacebj.api.entity.ConstructStageInfo;
import com.forcpacebj.api.utils.CollectionUtil;
import com.forcpacebj.api.utils.JSONUtil;
import lombok.val;
import lombok.var;
import spark.Route;

import static spark.Spark.halt;

/**
 * Created by pc on 2020/3/18.
 */
public class ConstructStageController extends BaseController {

    public static Route list = (req, res) -> {
        var user = ensureUserIsLoggedIn(req);
        var list = ConstructStageBusiness.list(user.getAccountId());
        return toJson(list);
    };

    public static Route insert = (req, res) -> {
        var user = ensureUserIsLoggedIn(req);
        val stage = JSONUtil.toBean(req.body(), ConstructStageInfo.class);
        if (stage.getAccountId() == null) stage.setAccountId(user.getAccountId());
        ConstructStageBusiness.insert(stage);
        return true;
    };

    public static Route update = (req, res) -> {
        val user = ensureUserIsLoggedIn(req);
        val stages = JSONUtil.toBeanList(req.body(), ConstructStageInfo.class);
        if (CollectionUtil.isEmpty(stages)) throw halt(400, "无效操作");
        ConstructStageBusiness.update(stages);
        return true;
    };

    public static Route delete = (req, res) -> {
        val user = ensureUserIsLoggedIn(req);
        val stage = JSONUtil.toBean(req.body(), ConstructStageInfo.class);
        ConstructStageBusiness.delete(stage.getId());
        return true;
    };

}
