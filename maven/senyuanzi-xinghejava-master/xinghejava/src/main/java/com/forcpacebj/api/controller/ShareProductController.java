package com.forcpacebj.api.controller;

import com.forcpacebj.api.business.FriendBusiness;
import com.forcpacebj.api.business.FriendSalePriceBusiness;
import com.forcpacebj.api.business.ProductBusiness;
import com.forcpacebj.api.business.ProductTagBusiness;
import com.forcpacebj.api.entity.AccountInfo;
import com.forcpacebj.api.entity.FriendGroupInfo;
import com.forcpacebj.api.utils.IdGenerator;
import com.forcpacebj.api.utils.JSONUtil;
import com.forcpacebj.api.utils.StrUtil;
import lombok.val;
import spark.Route;

public class ShareProductController extends BaseController {

    public static Route listProductTag = (req, res) -> {

        ensureUserIsLoggedIn(req);

        val friendAccountId = req.params(":accountId");
        val list = ProductTagBusiness.list(friendAccountId);
        return toJson(list);
    };

    public static Route findProduct = (req, res) -> {

        val user = ensureUserIsLoggedIn(req);
        val accountId = req.params(":accountId");
        val condition = JSONUtil.toMap(req.body());
        val friend = FriendBusiness.load(accountId, user.getAccountId());

        FriendGroupInfo friendGroup;
        if (friend != null && (friendGroup = friend.getFriendGroup()) != null && StrUtil.isNotBlank(friendGroup.getGroupId())) {
            condition.put("friendGroupId", friendGroup.getGroupId());
            val list = ProductBusiness.find(condition, accountId);
            list.forEach(product -> {
                val friendSalePrice = FriendSalePriceBusiness.load(accountId, product.getProductId(), friendGroup.getGroupId());
                if (friendSalePrice != null) {
                    product.setPurchPrice(friendSalePrice.getSalePrice());
                }
            });
            return toJson(list);
        } else {
            return "";
        }
    };

    public static Route countProduct = (req, res) -> {

        val user = ensureUserIsLoggedIn(req);
        val accountId = req.params(":accountId");
        val condition = JSONUtil.toMap(req.body());
        val friend = FriendBusiness.load(accountId, user.getAccountId());

        FriendGroupInfo friendGroup;
        if (friend != null && (friendGroup = friend.getFriendGroup()) != null && StrUtil.isNotBlank(friendGroup.getGroupId())) {
            condition.put("friendGroupId", friendGroup.getGroupId());
            return ProductBusiness.count(condition, accountId);
        } else {
            return 0;
        }
    };

    public static Route loadProduct = (req, res) -> {

        ensureUserIsLoggedIn(req);

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
//            obj.setProductId(IdGenerator.NewId()); //添加共享的产品新生成一个产品编码
        }

        return toJson(obj);
    };
}
