package com.forcpacebj.api.controller;

import com.forcpacebj.api.business.ProjectFileBusiness;
import com.forcpacebj.api.entity.ProjectFileInfo;
import com.forcpacebj.api.utils.JSONUtil;
import lombok.val;
import spark.Route;

/**
 * Created by pc on 2020/2/8.
 */
public class ProjectFileController extends BaseController {

    public static Route list = (req, res) -> {
        val user = ensureUserIsLoggedIn(req);
        return toJson(ProjectFileBusiness.list(req.params("projectId"), user.getAccountId()));
    };

    public static Route insert = (req, res) -> {
        val user = ensureUserIsLoggedIn(req);
        val file = JSONUtil.toBean(req.body(), ProjectFileInfo.class);
        file.setAccountId(user.getAccountId());
        ProjectFileBusiness.insert(file);
        return true;
    };

    public static Route update = (req, res) -> {
        val user = ensureUserIsLoggedIn(req);
        val fileList = JSONUtil.toBeanList(req.body(), ProjectFileInfo.class);
        ProjectFileBusiness.update(fileList);
        return true;
    };

    public static Route delete = (req, res) -> {
        val user = ensureUserIsLoggedIn(req);
        ProjectFileBusiness.delete(Integer.valueOf(req.params("id")));
        return true;
    };

}
