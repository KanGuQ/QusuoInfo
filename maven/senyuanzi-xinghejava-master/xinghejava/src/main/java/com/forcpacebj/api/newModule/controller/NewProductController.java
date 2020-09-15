package com.forcpacebj.api.newModule.controller;

import com.forcpacebj.api.business.ProductBusiness;
import com.forcpacebj.api.business.ProductRecordBusiness;
import com.forcpacebj.api.controller.BaseController;
import com.forcpacebj.api.core.ResponseWrapper;
import com.forcpacebj.api.entity.ProductRecordInfo;
import com.forcpacebj.api.newModule.business.NewProductBusiness;
import com.forcpacebj.api.utils.JSONUtil;
import lombok.val;
import spark.Route;

import java.util.List;

import static spark.Spark.halt;

public class NewProductController extends BaseController {


    /**
     * 产品状态：1自建的 2待上架 3审核中 4已拒绝 5已通过，进入大公海 6协助中 7协助完毕，待确认 8同步中 9面对同步或复制的选择
     */
    public static Route findProductByCondition = (request, response) -> {
        val user = ensureUserIsLoggedIn(request);
        if (!user.getIsAdmin())
            if (!user.getUserRole().getPower().contains("provider"))
                throw halt(400, "没有操作权限");

        val condition = JSONUtil.toMap(request.body());

        condition.put("accountId", user.getAccountId());

        return toJson(ResponseWrapper.page(
                NewProductBusiness.findProductCountByCondition(condition),
                NewProductBusiness.findProductByCondition(condition)));
    };


    /**
     * 查询企业公海管理中心所有产品
     */
    public static Route findAllProductsInPublicAdmin = (request, response) -> {
        val user = ensureUserIsLoggedIn(request);
        if (!user.getIsAdmin())
            if (!user.getUserRole().getPower().contains("provider"))
                throw halt(400, "没有操作权限");

        val condition = JSONUtil.toMap(request.body());
        condition.put("accountId", user.getAccountId());

        return toJson(ResponseWrapper.page(
                NewProductBusiness.findAllProductsInPublicAdminCount(condition),
                NewProductBusiness.findAllProductsInPublicAdmin(condition)));
    };


    /**
     * 查询待更新 倒计时改变的产品
     */
    public static Route findWillModifyProduct = (request, response) -> {
        val user = ensureUserIsLoggedIn(request);
        if (!user.getIsAdmin())
            if (!user.getUserRole().getPower().contains("provider"))
                throw halt(400, "没有操作权限");

        val condition = JSONUtil.toMap(request.body());
        condition.put("accountId", user.getAccountId());

        return toJson(ResponseWrapper.page(
                NewProductBusiness.findWillModifyProductCount(condition),
                NewProductBusiness.findWillModifyProduct(condition)));
    };


    /**
     * 获取待协助修改的产品
     */
    public static Route findProductToHelpModify = (request, response) -> {
        val user = ensureUserIsLoggedIn(request);
        if (!user.getIsAdmin()) throw halt(400, "没有操作权限");

        val condition = JSONUtil.toMap(request.body());
        condition.put("status", 6);

        return toJson(ResponseWrapper.page(
                NewProductBusiness.findProductCountByCondition(condition),
                NewProductBusiness.findProductByCondition(condition))
        );
    };

    /**
     * 获取已协助修改完毕，等待批发商确认的产品
     */
    public static Route findProductAlreadyHelpModify = (request, response) -> {
        val user = ensureUserIsLoggedIn(request);
        if (!user.getIsAdmin()) throw halt(400, "没有操作权限");

        val condition = JSONUtil.toMap(request.body());
        condition.put("status", 7);

        return toJson(ResponseWrapper.page(
                NewProductBusiness.findProductCountByCondition(condition),
                NewProductBusiness.findProductByCondition(condition)
        ));
    };


    /**
     * 下架产品
     * 从大公海下架产品到 我司公海管理中心
     */
    public static Route offShelvesProduct = (request, response) -> {
        val user = ensureUserIsLoggedIn(request);
        if (!user.getIsAdmin()) throw halt(400, "没有操作权限");

        val map = JSONUtil.toMap(request.body());
        val productId = map.get("productId");

        val newProduct = ProductBusiness.load((String) productId);

        if (newProduct.getStatus() != 5) {
            return "错误：该产品不是上架状态，无法下架。";
        }

        //todo 创建一个新版本的产品
        newProduct.setProductId(null);
        newProduct.setCreateTime(null);
        newProduct.setOriginProductId((String) productId);
        newProduct.setStatus(10);
        ProductBusiness.insert(user.getAccountId(), newProduct, user, "-1");

        //定时器10天后，会将新版本的产品状态覆盖现有产品


//
//        //发送下架消息
//        //todo 集成第三方推送系统或者发送短信
//        //todo 批量创建消息 函数
//        val accountIds = NewProductBusiness.findAccountIdsByOriginProductId((int) productId);
//        accountIds.forEach(it -> {
//
//            val message = new Message();
//            message.setOriginAccountId(user.getAccountId());
//            message.setOriginUserId(user.getUserId());
//            message.setTargetAccountId(it);
//            message.setType(3); // todo message type
//            message.setMessage(user.getAccountName() + "您同步的产品【" + product.getProductName() + "已下架!");
//            //todo 创建消息
////            MessageBusiness.createMessage(con, message);
//        });

        return toJson(ResponseWrapper.ok(null));
    };

    /**
     * 下架产品
     * 从大公海下架产品到 我司公海管理中心
     */
    public static Route offShelvesProductNoWait = (request, response) -> {
        val user = ensureUserIsLoggedIn(request);
        if (!user.getIsAdmin()) throw halt(400, "没有操作权限");

        val map = JSONUtil.toMap(request.body());
        val productId = map.get("productId");

        val product = ProductBusiness.load((String) productId);

        if (product.getStatus() != 5) {
            return "错误：该产品不是上架状态，无法下架。";
        }

        product.setStatus(2);
        ProductBusiness.update(user.getAccountId(), product);

        return toJson(ResponseWrapper.ok(null));
    };


    /**
     * //todo 批量 下架产品
     * 从大公海下架产品到 我司公海管理中心
     */
    public static Route batchOffShelvesProduct = (request, response) -> {
        val user = ensureUserIsLoggedIn(request);
        if (!user.getIsAdmin()) throw halt(400, "没有操作权限");

        val map = JSONUtil.toMap(request.body());
//        val productIds = map.get("productIds");
        for (Object id : (List) (map.get("productIds"))) {

            val newProduct = ProductBusiness.load((String) id);

            if (newProduct.getStatus() != 5) {
                return "错误：该产品不是上架状态，无法下架。";
            }

            //todo 创建一个新版本的产品
            newProduct.setProductId(null);
            newProduct.setCreateTime(null);
            newProduct.setOriginProductId((String) id);
            newProduct.setStatus(10);
            ProductBusiness.insert(user.getAccountId(), newProduct, user, "-1");
        }

        //定时器10天后，会将新版本的产品状态覆盖现有产品


//
//        //发送下架消息
//        //todo 集成第三方推送系统或者发送短信
//        //todo 批量创建消息 函数
//        val accountIds = NewProductBusiness.findAccountIdsByOriginProductId((int) productId);
//        accountIds.forEach(it -> {
//
//            val message = new Message();
//            message.setOriginAccountId(user.getAccountId());
//            message.setOriginUserId(user.getUserId());
//            message.setTargetAccountId(it);
//            message.setType(3); // todo message type
//            message.setMessage(user.getAccountName() + "您同步的产品【" + product.getProductName() + "已下架!");
//            //todo 创建消息
////            MessageBusiness.createMessage(con, message);
//        });

        return toJson(ResponseWrapper.ok(null));
    };

    /**
     * 从大公海下架产品到 我司公海管理中心
     */
    public static Route batchOffShelvesProductNoWait = (request, response) -> {
        val user = ensureUserIsLoggedIn(request);
        if (!user.getIsAdmin()) throw halt(400, "没有操作权限");

        val map = JSONUtil.toMap(request.body());
//        val productIds = map.get("productIds");
        for (Object id : (List) (map.get("productIds"))) {

            val product = ProductBusiness.load((String) id);

            if (product.getStatus() != 5) {
                return "错误：该产品不是上架状态，无法下架。";
            }
            product.setStatus(2);
            ProductBusiness.update(user.getAccountId(), product);
        }

        return toJson(ResponseWrapper.ok(null));
    };


    /**
     * 上传产品
     * 从我的产品库上传到我司公海管理中心
     */
    public static Route uploadProduct = (request, response) -> {
        val user = ensureUserIsLoggedIn(request);
        if (!user.getIsAdmin()) throw halt(400, "没有操作权限");

        val map = JSONUtil.toMap(request.body());
        val productId = map.get("productId");

        NewProductBusiness.updateStatus(Integer.parseInt((String) productId), 2);
        val product = ProductBusiness.load((String) productId);
        //新建 上传记录
        ProductRecordBusiness.insert(new ProductRecordInfo(user.getUserId(), user.getAccountId(), user.getAccountId(), product.getProductId(), product, 0, 0, null, null));
        return toJson(ResponseWrapper.ok(null));
    };

    /**
     * 批量 上传产品
     * 从我的产品库上传到我司公海管理中心
     */
    public static Route batchUploadProduct = (request, response) -> {
        val user = ensureUserIsLoggedIn(request);
        if (!user.getIsAdmin()) throw halt(400, "没有操作权限");

        val map = JSONUtil.toMap(request.body());
        val productIds = (List<String>) map.get("productIds");

        NewProductBusiness.batchUpdateStatus(productIds, 2);
        for (String productId : productIds) {
            val product = ProductBusiness.load(productId);
            ProductRecordBusiness.insert(new ProductRecordInfo(user.getUserId(), user.getAccountId(), user.getAccountId(), product.getProductId(), product, 0, 0, null, null));
        }


        return toJson(ResponseWrapper.ok(null));
    };

    /**
     * 移除产品
     * 将产品移除 我司公海管理中心
     */
    public static Route explantationProduct = (request, response) -> {
        val user = ensureUserIsLoggedIn(request);
        if (!user.getIsAdmin()) throw halt(400, "没有操作权限");

        val map = JSONUtil.toMap(request.body());
        val productId = map.get("productId");

        // 移除产品，变为自建状态
        NewProductBusiness.updateStatus(Integer.parseInt((String) productId), 1);

        return toJson(ResponseWrapper.ok(null));
    };


    /**
     * 批量 移除产品
     * 将产品移除 我司公海管理中心
     */
    public static Route batchExplantationProduct = (request, response) -> {
        val user = ensureUserIsLoggedIn(request);
        if (!user.getIsAdmin()) throw halt(400, "没有操作权限");

        val map = JSONUtil.toMap(request.body());
        val productIds = map.get("productIds");

        // 移除产品，变为自建状态
        NewProductBusiness.batchUpdateStatus((List<String>) productIds, 1);

        return toJson(ResponseWrapper.ok(null));
    };

    /**
     * 确认产品
     * 确认星合协助修改的结果，直接上架
     */
    public static Route confirmHelpedProduct = (request, response) -> {
        val user = ensureUserIsLoggedIn(request);
        if (!user.getIsAdmin()) throw halt(400, "没有操作权限");

        val map = JSONUtil.toMap(request.body());
        val productId = map.get("productId");

        NewProductBusiness.updateStatus(Integer.parseInt((String) productId), 5);

        return toJson(ResponseWrapper.ok(null));
    };

    /**
     * 批量 确认产品
     */
    public static Route batchConfirmHelpedProduct = (request, response) -> {
        val user = ensureUserIsLoggedIn(request);
        if (!user.getIsAdmin()) throw halt(400, "没有操作权限");

        val map = JSONUtil.toMap(request.body());
        val productIds = map.get("productIds");

        // 移除产品，变为自建状态
        NewProductBusiness.batchUpdateStatus((List<String>) productIds, 5);

        return toJson(ResponseWrapper.ok(null));
    };


    /**
     * 上架产品 ， 去审核
     */
    public static Route putawayProduct = (request, response) -> {
        val user = ensureUserIsLoggedIn(request);
        if (!user.getIsAdmin()) throw halt(400, "没有操作权限");

        val map = JSONUtil.toMap(request.body());
        val productId = map.get("productId");

        NewProductBusiness.updateStatus(Integer.parseInt((String) productId), 3);

        return toJson(ResponseWrapper.ok(null));
    };

    /**
     * 批量 上架产品 去审核
     * 将产品移除 我司公海管理中心
     */
    public static Route batchPutawayProduct = (request, response) -> {
        val user = ensureUserIsLoggedIn(request);
        if (!user.getIsAdmin()) throw halt(400, "没有操作权限");

        val map = JSONUtil.toMap(request.body());
        val productIds = map.get("productIds");

        // 移除产品，变为自建状态
        NewProductBusiness.batchUpdateStatus((List<String>) productIds, 3);

        return toJson(ResponseWrapper.ok(null));
    };


    /**
     * 上架产品 ， 去协助
     */
    public static Route putawayProductToHelp = (request, response) -> {
        val user = ensureUserIsLoggedIn(request);
        if (!user.getIsAdmin()) throw halt(400, "没有操作权限");

        val map = JSONUtil.toMap(request.body());
        val productId = map.get("productId");

        NewProductBusiness.updateStatus(Integer.parseInt((String) productId), 6);

        return toJson(ResponseWrapper.ok(null));
    };

    /**
     * 批量 上架产品 去协助
     * 将产品移除 我司公海管理中心
     */
    public static Route batchPutawayProductToHelp = (request, response) -> {
        val user = ensureUserIsLoggedIn(request);
        if (!user.getIsAdmin()) throw halt(400, "没有操作权限");

        val map = JSONUtil.toMap(request.body());
        val productIds = map.get("productIds");

        // 移除产品，变为自建状态
        NewProductBusiness.batchUpdateStatus((List<String>) productIds, 6);

        return toJson(ResponseWrapper.ok(null));
    };


    /**
     * 撤回产品
     * 将产品撤回到待上架状态
     */
    public static Route recallProduct = (request, response) -> {
        val user = ensureUserIsLoggedIn(request);
        if (!user.getIsAdmin()) throw halt(400, "没有操作权限");

        val map = JSONUtil.toMap(request.body());
        val productId = map.get("productId");

        // 移除产品，变为自建状态
        NewProductBusiness.updateStatus(Integer.parseInt((String) productId), 2);

        return toJson(ResponseWrapper.ok(null));
    };


    /**
     * 批量 撤回产品
     * 将产品撤回到待上架状态
     */
    public static Route batchRecallProduct = (request, response) -> {
        val user = ensureUserIsLoggedIn(request);
        if (!user.getIsAdmin()) throw halt(400, "没有操作权限");

        val map = JSONUtil.toMap(request.body());
        val productIds = map.get("productIds");

        // 移除产品，变为自建状态
        NewProductBusiness.batchUpdateStatus((List<String>) productIds, 2);

        return toJson(ResponseWrapper.ok(null));
    };


}
