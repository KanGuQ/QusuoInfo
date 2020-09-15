package com.forcpacebj.api.controller;

import com.forcpacebj.api.business.ProductBusiness;
import com.forcpacebj.api.business.ProductDataBusiness;
import com.forcpacebj.api.business.QuotationBillBusiness;
import com.forcpacebj.api.business.db;
import com.forcpacebj.api.entity.ProductData;
import com.forcpacebj.api.entity.QuotationBillInfo;
import com.forcpacebj.api.entity.QuotationBillProductInfo;
import com.forcpacebj.api.newModule.business.NewProductBusiness;
import com.forcpacebj.api.utils.*;
import lombok.extern.log4j.Log4j;
import lombok.val;
import lombok.var;
import org.jsoup.select.Collector;
import spark.Route;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Log4j
public class QuotationBillController extends BaseController {

    public static Route list = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        val list = QuotationBillBusiness.list(user, request.params(":projectId"));
        return toJson(list);
    };

    public static Route displayList = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        val list = QuotationBillBusiness.displayList(user);
        return toJson(list);
    };

    public static Route count = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        return QuotationBillBusiness.count(user, request.params(":projectId"));
    };

    public static Route load = (request, response) -> {

        Integer roleId = null;
        val headerAuth = request.headers("Authorization");
        if (StrUtil.isNotBlank(headerAuth) && !decodeToken(headerAuth).getIsAdmin()) {
            roleId = decodeToken(headerAuth).getUserRole().getRoleId();
        }

        val obj = QuotationBillBusiness.load(request.params(":id"), roleId);
        return toJson(obj);
    };

    public static Route delete = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        QuotationBillBusiness.delete(user.getAccountId(), request.params(":id"));
        return true;
    };

    public static Route insert = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        val obj = JSONUtil.toBean(request.body(), QuotationBillInfo.class);
        obj.setBillId(IdGenerator.NewId());
        obj.setUser(user);
        obj.setQuotationDate(DateUtil.now());
        obj.setUpdateTime(DateUtil.now());
        obj.setAccountId(user.getAccountId());
        obj.setDepartmentId(user.getDepartment().getId());

        QuotationBillBusiness.insert(obj);
        var allProducts = new ArrayList<QuotationBillProductInfo>();
        for (val sheet : obj.getSheetList()) {
            for (val section : sheet.getSectionList()) {
                allProducts.addAll(section.getProductList());
            }
        }
        val products = allProducts.stream().filter(distinctByKey(it -> it.getProduct().getProductId())).collect(Collectors.toList());
        try (val con = db.sql2o.beginTransaction()) {
            for (val qProduct : products) {
                val product = NewProductBusiness.counterFindInTransaction(con, qProduct.getProduct().getProductId());
                if (product == null) continue;
                ProductDataBusiness.recordInTransaction(con, product.getOriginProductId() == null ? product.getProductId() : product.getOriginProductId(), product.getOriginAccountId(), 0L, 1L);
            }
            con.commit();
        }
        return true;
    };

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    public static Route insertAll = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        return toJson(QuotationBillBusiness.insertAll(user));
    };

    public static Route update = (request, response) -> {
        val user = ensureUserIsLoggedIn(request);
        val obj = JSONUtil.toBean(request.body(), QuotationBillInfo.class);
        obj.setUpdateTime(DateUtil.now());
        QuotationBillBusiness.update(user.getAccountId(), obj);
        return true;
    };

    public static Route createMPQRCode = (req, res) -> {

        val bill = QuotationBillBusiness.load(req.params(":id"), null);
        if (bill != null) {

            val accessToken = WxAccessTokenUtil.getCachedAccessToken("wxe5036a40e1a922bd", "c12cb96c8139a3f70c5664b3e792f989");

            val parameters = MapUtil.instance("scene", bill.getBillId() + "#" + bill.getTemplate().getTemplateId());  //#是分隔符，小程序里面解析
            try (val out = res.raw().getOutputStream();
                 val inputStream = HttpUtils.download("https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token=" + accessToken, JSONUtil.toJson(parameters))) {
                int bt;
                while ((bt = inputStream.read()) != -1) {
                    out.write(bt);
                }
            }
        }

        return "";
    };
}
