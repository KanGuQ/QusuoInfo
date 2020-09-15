package com.forcpacebj.api.controller;

import com.forcpacebj.api.business.RecordMsgBusiness;
import com.forcpacebj.api.entity.RecordMsgInfo;
import com.forcpacebj.api.utils.DateUtil;
import com.forcpacebj.api.utils.JSONUtil;
import lombok.extern.log4j.Log4j;
import lombok.val;
import spark.Route;

import static spark.Spark.halt;

@Log4j
public class RecordMsgController extends BaseController {

    public static Route list = (req, res) -> {
        val user = ensureUserIsLoggedIn(req);
        val result = RecordMsgBusiness.list(user.getAccountId(), req.params("recordId"));
        return toJson(result);
    };

    public static Route insert = (req, res) -> {

        val user = ensureUserIsLoggedIn(req);
        try {
            val obj = JSONUtil.toBean(req.body(), RecordMsgInfo.class);
            obj.setPostUser(user);
            obj.setPostTime(DateUtil.now());
            val msgId = RecordMsgBusiness.insert(user.getAccountId(), obj);
            obj.setMsgId(msgId);
            return toJson(obj);
        } catch (Exception ex) {
            log.error("RecordMsg 保存错误：", ex);
            throw halt(500, "新增错误");
        }
    };

    public static Route delete = (req, res) -> {
        val user = ensureUserIsLoggedIn(req);
        RecordMsgBusiness.delete(user.getAccountId(), req.params("id"));
        return true;
    };

}
