package com.forcpacebj.api.controller;

import com.forcpacebj.api.business.QuotationTemplateBusiness;
import com.forcpacebj.api.utils.JSONUtil;
import lombok.extern.log4j.Log4j;
import lombok.val;
import spark.Route;

@Log4j
public class QuotationTemplateController extends BaseController {

    public static Route list = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        val list = QuotationTemplateBusiness.list(user.getAccountId());
        return toJson(list);
    };
    public static Route clearQuotationBookId = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);
        val condition = JSONUtil.toMap(request.body());

        QuotationTemplateBusiness.clearQuotationBookId((String) condition.get("quotationBookId"));
        return true;
    };
}
