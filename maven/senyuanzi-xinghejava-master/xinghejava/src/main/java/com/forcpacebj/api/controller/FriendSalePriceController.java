package com.forcpacebj.api.controller;

import com.forcpacebj.api.business.FriendSalePriceBusiness;
import com.forcpacebj.api.entity.FriendSalePriceInfo;
import com.forcpacebj.api.utils.CollectionUtil;
import com.forcpacebj.api.utils.JSONUtil;
import lombok.val;
import spark.Route;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static spark.Spark.halt;

/**
 * Created by pc on 2019/12/25.
 */
public class FriendSalePriceController extends BaseController {

    public static Route insert = (req, res) -> {
        val user = ensureUserIsLoggedIn(req);
        val obj = JSONUtil.toMap(req.body());
        FriendSalePriceBusiness.insert(user.getAccountId(),
                (String) obj.get("productId"), (String) obj.get("groupId"), (BigDecimal) obj.get("salePrice"));
        return true;
    };

    public static Route update = (req, res) -> {
        ensureUserIsLoggedIn(req);
        val obj = JSONUtil.toBean(req.body(), FriendSalePriceInfo.class);
        FriendSalePriceBusiness.update(obj);
        return true;
    };

    public static Route delete = (req, res) -> {
        ensureUserIsLoggedIn(req);
        FriendSalePriceBusiness.delete(req.params(":id"));
        return true;
    };

    public static Route batchDelete = (req, res) -> {
        ensureUserIsLoggedIn(req);
        val obj = JSONUtil.toMap(req.body());
        if (obj.get("ids") == null) throw halt(400, "未选中产品");
        FriendSalePriceBusiness.batchDelete(obj.get("ids").toString());
        return true;
    };

    public static Route batchInsertOrUpdate = (req, res) -> {
        val user = ensureUserIsLoggedIn(req);
        val obj = JSONUtil.toMap(req.body());
        if (obj.get("friendSalePrice") == null) throw halt(400, "未选中产品");

        List<FriendSalePriceInfo> friendSalePrices = JSONUtil.toBeanList(obj.get("friendSalePrice").toString(), FriendSalePriceInfo.class);
        if (CollectionUtil.isEmpty(friendSalePrices)) throw halt(400, "未选中产品");
        Map<String, BigDecimal> friendSalePriceMap = new HashMap<>();
        friendSalePrices.forEach(fsp -> friendSalePriceMap.put(fsp.getProductId(), fsp.getSalePrice()));

        FriendSalePriceBusiness.batchInsertOrUpdate(friendSalePriceMap, user.getAccountId(), (String) obj.get("friendGroupId"));
        return true;
    };
}
