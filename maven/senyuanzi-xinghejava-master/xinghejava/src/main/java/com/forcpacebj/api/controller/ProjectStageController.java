package com.forcpacebj.api.controller;

import com.forcpacebj.api.business.ProjectStageBusiness;
import com.forcpacebj.api.config.StaticParam;
import com.forcpacebj.api.entity.ProjectStageInfo;
import com.forcpacebj.api.entity.UserInfo;
import com.forcpacebj.api.utils.CollectionUtil;
import com.forcpacebj.api.utils.JSONUtil;
import lombok.val;
import spark.Route;

import static spark.Spark.halt;

/**
 * Created by pc on 2020/2/24.
 */
public class ProjectStageController extends BaseController {

    public static Route list = (req, res) -> {
        val user = ensureUserIsLoggedIn(req);
        val list = ProjectStageBusiness.getStage(user.getAccountId(), Integer.parseInt(req.params("type")));
        return toJson(list);
    };

    public static Route insert = (req, res) -> {
        val user = ensureUserIsLoggedIn(req);
        val stage = JSONUtil.toBean(req.body(), ProjectStageInfo.class);
        powerCheck(user,stage);
        if (stage.getAccountId() == null) stage.setAccountId(user.getAccountId());
        ProjectStageBusiness.insert(stage);
        return true;
    };

    public static Route update = (req, res) -> {
        val user = ensureUserIsLoggedIn(req);
        val stages = JSONUtil.toBeanList(req.body(), ProjectStageInfo.class);
        if (CollectionUtil.isEmpty(stages)) throw halt(400, "无效操作");
        powerCheck(user,stages.get(0));
        ProjectStageBusiness.update(stages);
        return true;
    };

    public static Route delete = (req, res) -> {
        val user = ensureUserIsLoggedIn(req);
        val stage = JSONUtil.toBean(req.body(), ProjectStageInfo.class);
        powerCheck(user,stage);
        ProjectStageBusiness.delete(stage.getId());
        return true;
    };

    private static void powerCheck(UserInfo user, ProjectStageInfo stage) {
        if (!checkPower(user))
            if (stage.getType().equals(StaticParam.OPPORTUNITY_STAGE)) {
                if (!user.getUserRole().getPower().contains(StaticParam.CUSTOM_OPPORTUNITY_STAGE))
                    throw halt(400, "无操作权限");
            } else if (stage.getType().equals(StaticParam.ENGINEERING_STAGE))
                if (!user.getUserRole().getPower().contains(StaticParam.CUSTOM_PROJECT_STAGE))
                    throw halt(400, "无操作权限");
    }

}
