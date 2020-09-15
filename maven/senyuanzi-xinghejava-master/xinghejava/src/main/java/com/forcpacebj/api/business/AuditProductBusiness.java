package com.forcpacebj.api.business;

import com.forcpacebj.api.entity.Message;
import com.forcpacebj.api.entity.ProductInfo;
import com.forcpacebj.api.entity.ProductRecordInfo;
import com.forcpacebj.api.entity.UserInfo;
import lombok.val;

import java.util.List;
import java.util.Map;

public class AuditProductBusiness {

    /**
     * 符合条件的 审核产品列表 总数
     * * @param status 产品状态：1自建的 2待上架 3审核中 4已拒绝 5已通过，进入大公海 6协助中 7协助完毕，待确认 8同步中
     *
     * @return
     */
    public static int count(Map conditions) {

        val sql = " SELECT count(1)  FROM tbProduct " +
                " WHERE (status = :status) ";

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("status", conditions.get("status"))
                    .executeScalar(int.class);
        }
    }

    public static List<ProductInfo> find(Map conditions) {
        val sql = " SELECT P.ProductId, P.ProductName,P.SalePrice,A.AccountId 'originAccount.accountId',A.AccountName 'originAccount.accountName' " +
                " FROM tbProduct P  " +
                " LEFT JOIN tbAccount A on P.OriginAccountId=A.AccountId" +
                " WHERE P.status = :status " +//审核中
                " Order By P.updateTime DESC" +
                " LIMIT :PAGEOFFSET ,:PAGESIZE";

        try (val con = db.sql2o.open()) {
            val list = con.createQuery(sql)
                    .addParameter("status", conditions.get("status"))
                    .addParameter("PAGEOFFSET", conditions.get("PAGEOFFSET"))
                    .addParameter("PAGESIZE", conditions.get("PAGESIZE"))
                    .executeAndFetch(ProductInfo.class);

            val recordSql = "SELECT PR.userId 'user.userId',U.userName 'user.userName',PR.ApproverId,PR.ApproverName,PR.CreateTime FROM ProductRecord PR " +
                    " LEFT JOIN tbUser U ON U.userId = PR.userId " +
                    " WHERE PR.ProductId = :id AND PR.TYPE = 0 ORDER BY PR.createTime DESC";
            list.forEach(product -> product.setProductRecord(con.createQuery(recordSql).addParameter("id", product.getProductId())
                    .executeAndFetchFirst(ProductRecordInfo.class)));
            return list;
        }
    }

    /**
     * 审核 批发商上传到公有库的产品
     * <p>
     * //同步 ： 生成 审核结果消息
     *
     * @return 是否成功更新
     */
    public static boolean audit(Map conditions, UserInfo user) {

        //更新产品审核状态
        val sql = " UPDATE tbProduct  SET " +
                " status = :status " +
                " WHERE (ProductId = :productId) ";

        val status = (Boolean) conditions.get("approve") ? 5 : 4;

        try (val con = db.sql2o.beginTransaction()) {
            con.createQuery(sql)
                    .addParameter("status", status)
                    .addParameter("productId", conditions.get("productId"))
                    .executeUpdate();
            //todo 更新失败的状态？ 还有try外面 是否有 全局错误处理？

            //创建 审核结果 消息
            val message = new Message();
            message.setOriginAccountId(user.getAccountId());
            message.setOriginUserId(user.getUserId());
            message.setTargetAccountId((String) conditions.get("targetAccountId"));
            message.setType(2);

            String auditResult = String.valueOf(status);
            if ("5".equals(auditResult)) {
                auditResult = "已通过";
                //更新记录
                ProductRecordBusiness.update((String) conditions.get("productId"), 1, user);
            } else if ("4".equals(auditResult)) {
                auditResult = "已拒绝";
                ProductRecordBusiness.update((String) conditions.get("productId"), 2, user);
            }
            message.setMessage("您好，贵公司上传产品" + conditions.get("productName") + "的申请结果：" + auditResult);
            MessageBusiness.createMessage(con, message);

            con.commit();
        }
        return true;
    }
}