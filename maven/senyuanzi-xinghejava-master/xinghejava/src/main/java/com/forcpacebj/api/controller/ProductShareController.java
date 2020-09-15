package com.forcpacebj.api.controller;

import com.alibaba.fastjson.JSONArray;
import com.forcpacebj.api.business.FriendBusiness;
import com.forcpacebj.api.business.ProductBusiness;
import com.forcpacebj.api.business.ProductShareBusiness;
import com.forcpacebj.api.core.ResponseWrapper;
import com.forcpacebj.api.entity.AccountInfo;
import com.forcpacebj.api.entity.ProductShareInfo;
import com.forcpacebj.api.utils.JSONUtil;
import lombok.val;
import spark.Route;


import java.util.HashMap;

import static spark.Spark.halt;

/**
 * Created by shu on 2020/4/23.
 */
public class ProductShareController extends BaseController {

    public static Route insert = (req, res) -> {
        val user = ensureUserIsLoggedIn(req);
        val data = JSONUtil.toMap(req.body());
        if (data.get("productIds") == null || data.get("toAccountIds") == null)
            halt(400, "未选择产品或公司");
        data.put("accountId", user.getAccountId());
        ProductShareBusiness.insert(data);
        return true;
    };

    public static Route update = (req, res) -> {
        ensureUserIsLoggedIn(req);
        val obj = JSONUtil.toBean(req.body(), ProductShareInfo.class);
        ProductShareBusiness.update(obj);
        return true;
    };

    public static Route batchDelete = (req, res) -> {
        ensureUserIsLoggedIn(req);
        val obj = JSONUtil.toMap(req.body());
        if (obj.get("ids") == null) throw halt(400, "未选中产品");
        ProductShareBusiness.batchDelete(obj.get("ids").toString());
        return true;
    };

    public static Route findShareProductByAccountBySelf = (req, res) -> {

        val user = ensureUserIsLoggedIn(req);
        val condition = JSONUtil.toMap(req.body());

        val accountId = (String) condition.get("accountId");
        val friend = FriendBusiness.load(user.getAccountId(), accountId);

        if (friend != null) {
            condition.put("toAccountId", accountId);
            val list = ProductBusiness.find(condition, user.getAccountId());
            list.forEach(product -> {
                val friendSalePrice = ProductShareBusiness.load(user.getAccountId(), product.getProductId(), accountId);
                if (friendSalePrice != null) {
                    product.setPurchPrice(friendSalePrice.getSalePrice());
                    product.setProductShareId(friendSalePrice.getId());
                }
            });
            return toJson(ResponseWrapper.page(ProductBusiness.count(condition, user.getAccountId()), list));
        } else {
            return "";
        }
    };


    public static Route findSharedProductByAccount = (req, res) -> {

        val user = ensureUserIsLoggedIn(req);
        val condition = JSONUtil.toMap(req.body());
        val accountId = (String) condition.get("accountId");
        val friend = FriendBusiness.load(accountId, user.getAccountId());

        if (friend != null) {
            condition.put("toAccountId", user.getAccountId());
            val list = ProductBusiness.find(condition, accountId);
            list.forEach(product -> {
                val friendSalePrice = ProductShareBusiness.load(accountId, product.getProductId(), user.getAccountId());
                if (friendSalePrice != null) {
                    product.setPurchPrice(friendSalePrice.getSalePrice());
                }
            });
            return toJson(ResponseWrapper.page(ProductBusiness.count(condition, accountId), list));
        } else {
            return "";
        }
    };

    public static Route findSharedProduct = (req, res) -> {

        val user = ensureUserIsLoggedIn(req);
        val condition = JSONUtil.toMap(req.body());

        condition.put("toAccountIdAll", user.getAccountId());
        val list = ProductBusiness.find(condition);
        return toJson(list);
    };

    public static Route countProduct = (req, res) -> {

        val user = ensureUserIsLoggedIn(req);
        val condition = JSONUtil.toMap(req.body());
        val accountId = (String) condition.get("accountId");
        val friend = FriendBusiness.load(accountId, user.getAccountId());

        if (friend != null) {
            condition.put("toAccountId", user.getAccountId());
            return ProductBusiness.count(condition, accountId);
        } else {
            return 0;
        }
    };

    public static Route loadProduct = (req, res) -> {

        val user = ensureUserIsLoggedIn(req);
        val friendAccountId = req.params(":accountId");
        val productId = req.params(":productId");

        val obj = ProductBusiness.load(productId);
        if (obj != null) {
            obj.setIsSharedProduct(true);
            val fromAccount = new AccountInfo();
            fromAccount.setAccountId(friendAccountId);
            obj.setFromAccount(fromAccount);
            obj.setFromProductId(productId);
            obj.setRolePurchPriceList(null);
            obj.setFriendSalePriceList(null);
            obj.setProductDetail2(null);
            val fs = ProductShareBusiness.load(friendAccountId, productId, user.getAccountId()).getSalePrice();
            obj.setPurchPrice(fs == null ? obj.getGuidePrice() : fs);
        }

        return toJson(obj);
    };

    public static Route batchInsertByShare = (req, res) -> {
        val user = ensureUserIsLoggedIn(req);
        val data = JSONUtil.toMap(req.body());
        val friendAccountId = (String) data.get("accountId");
        val productIds = JSONArray.parseArray(data.get("productIds").toString(), String.class);
        for (String productId : productIds) {
            val fs = ProductShareBusiness.load(friendAccountId, productId, user.getAccountId());
            if (fs != null) {
                val obj = ProductBusiness.loadByIdHidePrice(productId, fs.getAccountId());
                if (obj != null) {
                    val condition = new HashMap<String, String>();
                    condition.put("originProductId", obj.getOriginProductId());
                    condition.put("originAccountId", obj.getOriginAccount().getAccountId());
                    if (ProductBusiness.count(condition, user.getAccountId()) > 0) {
                        continue;
                    }
                    obj.setIsSharedProduct(true);
                    val fromAccount = new AccountInfo();
                    fromAccount.setAccountId(fs.getAccountId());
                    obj.setFromAccount(fromAccount);
                    obj.setFromProductId(productId);
                    obj.setRolePurchPriceList(null);
                    obj.setFriendSalePriceList(null);
                    obj.setProductDetail2(null);
                    obj.setPurchPrice(fs.getSalePrice() == null ? obj.getGuidePrice() : fs.getSalePrice());
                    obj.setCategory(null);
                    obj.setPublicCategoryId(null);
                    obj.setStatus(1);
                    obj.setSalePrice(obj.getGuidePrice());
                    ProductBusiness.insert(user.getAccountId(), obj, user, "接收产品");
                }
            }
        }
        return true;
    };
}
