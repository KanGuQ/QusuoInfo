package com.forcpacebj.api.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSONArray;
import com.forcpacebj.api.business.*;
import com.forcpacebj.api.config.StaticParam;
import com.forcpacebj.api.core.ResponseWrapper;
import com.forcpacebj.api.entity.*;
import com.forcpacebj.api.utils.CollectionUtil;
import com.forcpacebj.api.utils.JSONUtil;
import com.forcpacebj.api.utils.ProductInfoListener;
import com.forcpacebj.api.utils.StrUtil;
import lombok.extern.log4j.Log4j;
import lombok.val;
import lombok.var;
import spark.Route;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.*;

import static spark.Spark.halt;

@Log4j
public class ProductController extends BaseController {

    public static Route find = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        val condition = JSONUtil.toMap(request.body());
        condition.put("currentAccountId", user.getAccountId());

        val list = ProductBusiness.find(condition, condition.get("accountId") == null ?
                user.getAccountId() : (String) condition.get("accountId"));
        if (!user.getIsAdmin())
            if (!user.getUserRole().getPower().contains(StaticParam.SALE_PRICE))
                list.forEach(p -> p.setSalePrice(null));
        if (!user.getIsAdmin())
            if (!user.getUserRole().getPower().contains(StaticParam.PURCH_PRICE))
                list.forEach(p -> p.setPurchPrice(null));

        return toJson(list);
    };

    public static Route listIncludeFriendSalePrice = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        val condition = JSONUtil.toMap(request.body());
        condition.put("currentAccountId", user.getAccountId());

        val list = ProductBusiness.listIncludeFriendSalePrice(condition, condition.get("accountId") == null ?
                user.getAccountId() : (String) condition.get("accountId"));

        return toJson(ResponseWrapper.page(ProductBusiness.count(condition, user.getAccountId()), list));
    };

    public static Route findMyProducts = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        val condition = JSONUtil.toMap(request.body());
        condition.put("currentAccountId", user.getAccountId());

        val list = ProductBusiness.find(condition, condition.get("accountId") == null ?
                user.getAccountId() : (String) condition.get("accountId"));

        if (!user.getIsAdmin())
            if (!user.getUserRole().getPower().contains(StaticParam.SALE_PRICE))
                list.forEach(p -> p.setSalePrice(null));
        if (!user.getIsAdmin())
            if (!user.getUserRole().getPower().contains(StaticParam.PURCH_PRICE))
                list.forEach(p -> p.setPurchPrice(null));
        return toJson(ResponseWrapper.page(ProductBusiness.count(condition, condition.get("accountId") == null ?
                user.getAccountId() : (String) condition.get("accountId")), list));
    };


    public static Route count = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        val condition = JSONUtil.toMap(request.body());
        return ProductBusiness.count(condition, condition.get("accountId") == null ?
                user.getAccountId() : (String) condition.get("accountId"));
    };

    public static Route load = (request, response) -> {

        String accountId = request.queryParams("account-id");
        if (StrUtil.isBlank(accountId)) {
            accountId = ensureUserIsLoggedIn(request).getAccountId();
        }

        val obj = ProductBusiness.load(request.params(":id"));
        val user = ensureUserIsLoggedIn(request);
        if (!user.getIsAdmin())
            if (!user.getUserRole().getPower().contains(StaticParam.SALE_PRICE))
                obj.setSalePrice(obj.getGuidePrice());
        if (!user.getIsAdmin())
            if (!user.getUserRole().getPower().contains(StaticParam.PURCH_PRICE))
                obj.setPurchPrice(obj.getGuidePrice());

        return toJson(obj);
    };

    public static Route loadById = (request, response) -> {

        val obj = ProductBusiness.load(request.params(":id"));

        return toJson(obj);
    };

    public static Route loadByIdHidePrice = (request, response) -> {

        String accountId = request.queryParams("account-id");
        if (StrUtil.isBlank(accountId)) {
            accountId = ensureUserIsLoggedIn(request).getAccountId();
        } else if ("DEFAULT".equals(accountId)) {
            accountId = null;
        }

        val obj = ProductBusiness.loadByIdHidePrice(request.params(":id"), accountId);

        return toJson(obj);
    };

    public static Route getProductDetail = (request, response) -> {
        String productId = request.queryParams("productId");
        return toJson(ProductBusiness.getProductDetail(productId));
    };


    public static Route delete = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);
        if (!user.getIsAdmin())
            if (!user.getUserRole().getPower().contains(StaticParam.MY_PRODUCT))
                throw halt(400, "无操作权限");

        ProductBusiness.delete(user.getAccountId(), request.params(":id"));
        return true;
    };

    public static Route safeDelete = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);
        val data = JSONUtil.toMap(request.body());
        if (!user.getIsAdmin())
            if (!user.getUserRole().getPower().contains(StaticParam.MY_PRODUCT))
                throw halt(400, "无操作权限");

        ProductBusiness.safeDelete(data.get("productIds").toString(), (Boolean) data.get("state"), user.getAccountId());
        return true;
    };

    public static Route insert = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        try {
            val obj = JSONUtil.toBean(request.body(), ProductInfo.class);

            if (StrUtil.isNotBlank(obj.getFromProductId())) {
                val condition = new HashMap<String, String>();
                condition.put("fromProductId", obj.getFromProductId());
                condition.put("fromAccountId", obj.getFromAccount().getAccountId());
                if (ProductBusiness.count(condition, user.getAccountId()) > 0) {
                    throw halt(400, "该共享产品已经添加");
                }
            } else {
                val orgAccount = new AccountInfo();
                orgAccount.setAccountId(user.getAccountId());
                obj.setOriginAccount(orgAccount);
                obj.setOriginProductId(obj.getProductId());
            }

            ProductBusiness.insert(user.getAccountId(), obj, user, "");
            return toJson(obj);
        } catch (Exception ex) {
            if (StrUtil.isNotBlank(ex.getMessage()) && ex.getMessage().contains("Duplicate entry")) {
                throw halt(400, "产品编码已经存在");
            } else {
                throw ex;
            }
        }
    };

    public static Route update = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);
        if (!user.getIsAdmin())
            if (!user.getUserRole().getPower().contains(StaticParam.MY_PRODUCT))
                throw halt(400, "无操作权限");

        val obj = JSONUtil.toBean(request.body(), ProductInfo.class);
        val accountId = (String) JSONUtil.toMap(request.body()).get("accountId");

        if ("TEMP".equals(obj.getCategory().getId())) throw halt(400, "无法移动至临时仓库哦");


        val oldProduct = ProductBusiness.load(obj.getProductId());
        if (!user.getIsAdmin())
            if (!user.getUserRole().getPower().contains(StaticParam.SALE_PRICE) && oldProduct.getSalePrice() != null)
                obj.setSalePrice(oldProduct.getSalePrice());
        if (!user.getIsAdmin())
            if (!user.getUserRole().getPower().contains(StaticParam.PURCH_PRICE) && oldProduct.getPurchPrice() != null)
                obj.setPurchPrice(oldProduct.getPurchPrice());


        if (obj.getStatus() == 5 && !"xh".equals(user.getUserId())) {//已上架
            throw halt(400, "该产品在公有库，请下架后再进行修改");
        } else {
            ProductBusiness.update(accountId == null ? user.getAccountId() : accountId, obj);
        }
        return toJson(obj);
    };

    public static Route uploadToPublic = (request, response) -> {
        val user = ensureUserIsLoggedIn(request);
        val obj = JSONUtil.toMap(request.body());

        val category = ProductCategoryBusiness.getPublicCategory(user.getAccountId());
        if (category == null) throw halt(400, "未开通公有目录,无法上传,请联系管理员");

        List<String> duplicate = new ArrayList<>();

        for (Object id : (JSONArray) (obj.get("PIds"))) {
            val productTemp = ProductBusiness.load((String) id);

            if (!productTemp.getProductId().equals(productTemp.getOriginProductId())) {
                duplicate.add(productTemp.getProductName());
                continue;
            }
            val condition = new HashMap<String, String>();
            condition.put("originProductId", productTemp.getOriginProductId());
            condition.put("originAccountId", productTemp.getOriginAccount().getAccountId());
            if (ProductBusiness.count(condition, StaticParam.PUBLIC_PRODUCT) > 0) {
                duplicate.add(productTemp.getProductName());
                continue;
            }

            productTemp.setStatus(2);
            productTemp.setCategory(category);
            productTemp.setPurchPrice(productTemp.getSalePrice());
            productTemp.setRolePurchPriceList(null);
            productTemp.setFriendSalePriceList(null);
            var fromAccountId = productTemp.getAccountId();
            var fromProductId = productTemp.getProductId();

            ProductBusiness.delete(StaticParam.PUBLIC_PRODUCT, ProductBusiness.getIdByOrigin("Public", productTemp.getOriginProductId()));
            ProductBusiness.insert(StaticParam.PUBLIC_PRODUCT, productTemp, user, "上传产品");
            ProductRecordBusiness.insert(new ProductRecordInfo(user.getUserId(), fromAccountId, user.getAccountId(), fromProductId, productTemp, 0, 0, null, null));
        }

        return CollectionUtil.isEmpty(duplicate) ? true : toJson(duplicate);
    };
    public static Route downloadFromPublic = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);
        val obj = JSONUtil.toMap(request.body());

        List<String> duplicate = new ArrayList<>();
        String originAccountId = null;
        for (Object id : (JSONArray) (obj.get("PIds"))) {
            val productTemp = ProductBusiness.loadByIdHidePrice((String) id, user.getAccountId());

            val condition = new HashMap<String, String>();
            condition.put("originProductId", productTemp.getOriginProductId());
            condition.put("originAccountId", productTemp.getOriginAccount().getAccountId());
            if (ProductBusiness.count(condition, user.getAccountId()) > 0) {
                duplicate.add(productTemp.getProductName());
                continue;
            }

            productTemp.setCategory(new ProductCategoryInfo((String) obj.get("categoryId")));
            productTemp.setPublicCategoryId(null);
            productTemp.setRolePurchPriceList(null);
            productTemp.setFriendSalePriceList(null);
            productTemp.setStatus(1);
            productTemp.setSalePrice(productTemp.getGuidePrice());
            productTemp.setPurchPrice(productTemp.getGuidePrice());
            originAccountId = productTemp.getOriginAccount().getAccountId();
            var fromAccountId = productTemp.getAccountId();
            var fromProductId = productTemp.getProductId();
            var originProductId = productTemp.getOriginProductId();
            ProductBusiness.insert(user.getAccountId(), productTemp, user, "下载产品");
            ProductRecordBusiness.insert(new ProductRecordInfo(user.getUserId(), fromAccountId, user.getAccountId(), fromProductId, productTemp, 1, 0, null, null));
            try (val con = db.sql2o.beginTransaction()) {
                ProductDataBusiness.recordInTransaction(con, originProductId, originAccountId, 1L, 0L);
                con.commit();
            }
        }
        if (originAccountId != null) {
            FriendInfo friend = FriendBusiness.load(user.getAccountId(), originAccountId);
            if (friend == null) {
                FriendBusiness.downloadProductBecomeFriend(user.getAccountId(), originAccountId, user.getAccountName() + "同步" + AccountBusiness.load(originAccountId).getAccountName() + "的产品");
            } else if (!friend.getIsAccepted()) {
                FriendBusiness.delete(user.getAccountId(), originAccountId);
                FriendBusiness.downloadProductBecomeFriend(user.getAccountId(), originAccountId, user.getAccountName() + "同步" + AccountBusiness.load(originAccountId).getAccountName() + "的产品");
            }
        }

        return CollectionUtil.isEmpty(duplicate) ? true : toJson(duplicate);
    };

    public static Route updateSpecialProduct = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        val obj = JSONUtil.toBean(request.body(), ProductInfo.class);
        ProductBusiness.updateSpecialProduct(user.getAccountId(), obj);
        return true;
    };

    public static Route moveCategory = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);
        if (!user.getIsAdmin())
            if (!user.getUserRole().getPower().contains(StaticParam.MY_PRODUCT))
                throw halt(400, "无操作权限");

        ProductBusiness.moveCategory(user.getAccountId(), request.params(":sourceId"), request.params(":targetId"));
        return true;
    };

    /**
     * 公有产品
     */
    public static Route publicProductFind = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        val condition = JSONUtil.toMap(request.body());
        condition.put("currentAccountId", user.getAccountId());
        condition.put("fromPage", "public");

        val list = ProductBusiness.find(condition, StaticParam.PUBLIC_PRODUCT);

        return toJson(list);
    };

    public static Route publicProductCount = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        val condition = JSONUtil.toMap(request.body());
        return ProductBusiness.count(condition, StaticParam.PUBLIC_PRODUCT);
    };

    public static Route insertFromPublic = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        try {
            val obj = JSONUtil.toBean(request.body(), ProductInfo.class);

            val condition = new HashMap<String, String>();
            condition.put("originProductId", obj.getOriginProductId());
            condition.put("originAccountId", obj.getOriginAccount().getAccountId());
            if (ProductBusiness.count(condition, user.getAccountId()) > 0) {
                throw halt(400, "该共享产品已经添加");
            }

            val orgAccount = new AccountInfo();
            orgAccount.setAccountId(StaticParam.PUBLIC_PRODUCT);
            obj.setStatus(8);
            obj.setFromAccount(orgAccount);
            obj.setFromProductId(obj.getProductId());
            obj.setProductParameters(CatalogParameterBusiness.listForProductValues(obj.getProductId(), orgAccount.getAccountId()));

            ProductBusiness.insert(user.getAccountId(), obj, user, "下载产品");

            return toJson(obj);
        } catch (Exception ex) {
            if (StrUtil.isNotBlank(ex.getMessage()) && ex.getMessage().contains("Duplicate entry")) {
                throw halt(400, "产品编码已经存在");
            } else {
                throw ex;
            }
        }
    };

    public static Route insertIntoPublic = (request, response) -> {
        val user = ensureUserIsLoggedIn(request);
        if (!user.getIsAdmin()) throw halt(400, "操作未授权");

        val obj = JSONUtil.toBean(request.body(), ProductInfo.class);
        val condition = new HashMap<String, String>();
        condition.put("originProductId", obj.getOriginProductId());
        condition.put("originAccountId", obj.getOriginAccount().getAccountId());
        if (ProductBusiness.count(condition, StaticParam.PUBLIC_PRODUCT) > 0) {
            throw halt(400, "该产品已经上传！");
        }
        val orgAccount = new AccountInfo();
        orgAccount.setAccountId(user.getAccountId());
        obj.setFromAccount(orgAccount);
        obj.setFromProductId(obj.getProductId());

        ProductBusiness.insert(StaticParam.PUBLIC_PRODUCT, obj, user, "");
        return toJson(obj);
    };

    public static Route movePublicCategory = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);

        ProductBusiness.moveCategory(StaticParam.PUBLIC_PRODUCT, request.params(":sourceId"), request.params(":targetId"));
        return true;
    };

    public static Route batchMoveCategory = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);
        val con = JSONUtil.toMap(request.body());
        if (!user.getIsAdmin())
            if (!user.getUserRole().getPower().contains(StaticParam.MY_PRODUCT))
                throw halt(400, "无操作权限");

        ProductBusiness.batchMoveCategory(con.get("PIds").toString(), (String) con.get("categoryId"));
        return true;
    };

    public static Route batchMoveCatalog = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);
        val con = JSONUtil.toMap(request.body());
        if (!"xh".equals(user.getUserId()))
            throw halt(400, "无操作权限");

        ProductBusiness.batchMoveCatalog(con.get("PIds").toString(), (String) con.get("catalogId"));
        return true;
    };

    public static Route batchMovePublicCategory = (request, response) -> {

        val user = ensureUserIsLoggedIn(request);
        if (!user.getIsAdmin()) throw halt(400, "操作未授权");
        val con = JSONUtil.toMap(request.body());

        ProductBusiness.batchMovePublicCategory(con.get("PIds").toString(), (String) con.get("publicCategoryId"));
        return true;
    };

    public static Route importExcel = (request, response) -> {
        val user = ensureUserIsLoggedIn(request);
        if (!user.getIsAdmin())
            if (!user.getUserRole().getPower().contains(StaticParam.MY_PRODUCT))
                throw halt(400, "无操作权限");

        request.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));
        Part uploadFile = request.raw().getPart("file");
        val submittedFileName = uploadFile.getSubmittedFileName();
        val suffix = submittedFileName.substring(submittedFileName.lastIndexOf("."));
        if (!isExcel(suffix))
            throw halt(400, "文件格式错误");
        try (InputStream is = uploadFile.getInputStream()) {
            ProductInfoListener.setAccountId(user.getAccountId());
            EasyExcel.read(is, ProductInfo.class, new ProductInfoListener()).sheet().doRead();
        }

        return true;
    };

    private static boolean isExcel(String suffix) {
        val extend = suffix.toLowerCase();
        return extend.equals(".xlsx") || extend.equals(".xls");
    }

    public static Route updateSortNum = (request, response) -> {
        val user = ensureUserIsLoggedIn(request);
        val list = JSONUtil.toBeanList(request.body(), ProductInfo.class);
        ProductBusiness.updateSortNum(list);
        return true;
    };

    public static Route inExclusive = (request, response) -> {
        val user = ensureUserIsLoggedIn(request);
        val productId = request.queryParams("productId");
        val isInExclusive = request.queryParams("isInExclusive");
        val product = ProductBusiness.load(productId);
        if (!Objects.equals(product.getAccountId(), product.getOriginAccount().getAccountId()))
            throw halt(400, "从公有库下载的产品无法上架");
        ProductBusiness.inExclusive(productId, Boolean.parseBoolean(isInExclusive));
        return true;
    };


    /**
     * 文件下载（失败了会返回一个有部分数据的Excel）
     * <p>1. 创建excel对应的实体对象 参照{@link DownloadData}
     * <p>2. 设置返回的 参数
     * <p>3. 直接写，这里注意，finish的时候会自动关闭OutputStream,当然你外面再关闭流问题不大
     */
    public static Route downloadExcel = (request, response) -> {

//        val user = ensureUserIsLoggedIn(request);


//        if (!Objects.equals(user.getAccountId().toLowerCase(), "znbj") && !Objects.equals(user.getAccountId().toLowerCase(), "znsm")) {
//            throw halt(401, "无权限");
//        }

        val condition = new HashMap<String, Object>();

        condition.put("PAGEOFFSET", 0);
        condition.put("PAGESIZE", 10);
        val data = ProductBusiness.find(condition, "znbj");
//        val data = ProductBusiness.find(condition, user.getAccountId());


        data.forEach(productInfo -> {
            if (productInfo.getGuidePrice()==null) productInfo.setGuidePrice(BigDecimal.ZERO);
            if (productInfo.getSalePrice()==null) productInfo.setSalePrice(BigDecimal.ZERO);
            if (productInfo.getPurchPrice()==null) productInfo.setPurchPrice(BigDecimal.ZERO);
        });


        Set<String> includeColumnFiledNames = new HashSet<String>();
        includeColumnFiledNames.add("role");
        includeColumnFiledNames.add("shortName");
        includeColumnFiledNames.add("brand");
        includeColumnFiledNames.add("productArea");
        includeColumnFiledNames.add("color");
        includeColumnFiledNames.add("unit");
        includeColumnFiledNames.add("remark");
        includeColumnFiledNames.add("brief");
        includeColumnFiledNames.add("feature");
        includeColumnFiledNames.add("guidePrice");
        includeColumnFiledNames.add("salePrice");
        includeColumnFiledNames.add("purchPrice");


        // 这里注意 有同学反应使用swagger 会导致各种问题，请直接用浏览器或者用postman
        response.header("Content-Type", "application/vnd.ms-excel");
        response.raw().setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
        String fileName = URLEncoder.encode("产品数据", "UTF-8");
        response.header("Content-disposition", "attachment;filename=" + fileName + ".xlsx");

        try (OutputStream os = response.raw().getOutputStream()) {
            EasyExcel.write(os, ProductInfo.class).includeColumnFiledNames(includeColumnFiledNames).sheet("产品数据").doWrite(data);
        }
        return true;
    };

}