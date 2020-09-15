package com.forcpacebj.api.business;

import com.forcpacebj.api.entity.AccountInfo;
import com.forcpacebj.api.entity.PresentBillInfo;
import com.forcpacebj.api.entity.QuotationBillProductInfo;
import com.forcpacebj.api.utils.CollectionUtil;
import lombok.val;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PresentBillBusiness {

    /**
     * 获取报价单可以提报的好友列表
     */
    public static List<PresentBillInfo> list(String accountId, String quotationBillId) {

        val sql = "select B.BillId ,B.ProductId 'product.productId' ,P.FromProductId 'product.fromProductId' ,B.ProductName 'product.productName' ," +
                "         P.OriginAccountId 'product.originAccount.accountId' ,O.AccountName 'product.originAccount.accountName' ," +
                "         P.FromAccountId 'product.fromAccount.accountId' ,F.AccountName 'product.fromAccount.accountName' ," +
                "         P.PurchPrice 'product.purchPrice' ,B.Quantity ,B.Unit 'product.unit'" +
                " from tbQuotationBillProduct B " +
                " inner join tbProduct P on B.ProductId = P.ProductId And B.AccountId = P.AccountId " +
                " left join tbAccount O on P.OriginAccountId = O.AccountId " +
                " left join tbAccount F on P.FromAccountId = F.AccountId " +
                " where B.AccountId =:accountId and B.BillId =:quotationBillId and P.IsSharedProduct = 1 ";

        val presentBillList = new ArrayList<PresentBillInfo>();

        try (val con = db.sql2o.open()) {
            val productList = con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .addParameter("quotationBillId", quotationBillId)
                    .executeAndFetch(QuotationBillProductInfo.class);

            for (val product : productList) {
                val toAccountId = product.getProduct().getFromAccount().getAccountId(); //来自谁共享的产品那就向谁提报
                if (presentBillList.stream().noneMatch(u -> u.getToAccount().getAccountId().equals(toAccountId))) {

                    //创建对象，前端还需要补充完整其他属性数据
                    val presentBill = new PresentBillInfo();
                    presentBill.setQuotationBillId(quotationBillId);

                    val toAccount = new AccountInfo();
                    toAccount.setAccountId(product.getProduct().getFromAccount().getAccountId());
                    toAccount.setAccountName(product.getProduct().getFromAccount().getAccountName());
                    presentBill.setToAccount(toAccount);

                    presentBill.setDetails(productList.stream().filter(p -> p.getProduct().getFromAccount().getAccountId().equals(toAccountId)).collect(Collectors.toList()));

                    //订货总金额
                    BigDecimal totalAmount = BigDecimal.valueOf(0);
                    for (val detail : presentBill.getDetails()) {
                        totalAmount = totalAmount.add(detail.getQuantity().multiply(detail.getProduct().getPurchPrice()));
                    }
                    presentBill.setTotalAmount(totalAmount);

                    presentBillList.add(presentBill);
                }
            }
        }

        return presentBillList;
    }

    public static void insert(String accountId, PresentBillInfo presentBill) {

        val sql = "INSERT INTO tbPresentBill(ToAccountId ,FromAccountId ,QuotationBillId ,Title ,City ,TotalAmount ,EstimatedTime ,Remark ,CreateTime) " +
                " values(:toAccountId ,:fromAccountId ,:quotationBillId ,:title ,:city ,:totalAmount ,:estimatedTime ,:remark ,:createTime)";

        try (val con = db.sql2o.beginTransaction()) {
            con.createQuery(sql).bind(presentBill)
                    .addParameter("toAccountId", presentBill.getToAccount().getAccountId())
                    .addParameter("fromAccountId", accountId)
                    .executeUpdate();

            //保存角色进价
            if (CollectionUtil.isNotEmpty(presentBill.getDetails())) {
                presentBill.getDetails().forEach(detail -> {
                    val detailSql = " insert into tbPresentBillProduct(ToAccountId,FromAccountId,QuotationBillId,ProductId,FromProductId,ProductName,PurchPrice,Quantity,Unit)" +
                            "   values(:toAccountId,:fromAccountId,:quotationBillId,:productId,:fromProductId ,:productName,:purchPrice,:quantity,:unit)";
                    con.createQuery(detailSql)
                            .addParameter("toAccountId", presentBill.getToAccount().getAccountId())
                            .addParameter("fromAccountId", accountId)
                            .addParameter("quotationBillId", presentBill.getQuotationBillId())
                            .addParameter("productId", detail.getProduct().getProductId())
                            .addParameter("fromProductId", detail.getProduct().getFromProductId())
                            .addParameter("productName", detail.getProduct().getProductName())
                            .addParameter("purchPrice", detail.getProduct().getPurchPrice())
                            .addParameter("quantity", detail.getQuantity())
                            .addParameter("unit", detail.getProduct().getUnit())
                            .executeUpdate();
                });
            }

            con.commit();
        }
    }

    public static List<PresentBillInfo> find(Map conditions) {

        val sql = " SELECT B.ToAccountId 'toAccount.accountId' ,T.AccountName 'toAccount.accountName' ," +
                "          B.FromAccountId 'fromAccount.accountId' ,F.AccountName 'fromAccount.accountName' ," +
                "          B.QuotationBillId ,B.Title ,B.City ,B.TotalAmount ,B.EstimatedTime ,B.Remark ,B.CreateTime" +
                " FROM tbPresentBill B " +
                " LEFT JOIN tbAccount T on B.ToAccountId = T.AccountId " +
                " LEFT JOIN tbAccount F on B.FromAccountId = F.AccountId " +
                " WHERE 1=1" +
                "   AND (B.ToAccountId LIKE CONCAT('%',:toAccountId,'%') OR :toAccountId is null) " +
                "   AND (B.FromAccountId LIKE CONCAT('%',:fromAccountId,'%') OR :fromAccountId is null) " +
                "   AND (B.Title LIKE CONCAT('%',:title,'%') OR :title is null) " +
                "   AND (B.Remark LIKE CONCAT('%',:remark,'%') OR :remark is null) " +
                " Order By B.CreateTime DESC" +
                " LIMIT :PAGEOFFSET ,:PAGESIZE";

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("toAccountId", conditions.get("toAccountId"))
                    .addParameter("fromAccountId", conditions.get("fromAccountId"))
                    .addParameter("title", conditions.get("title"))
                    .addParameter("remark", conditions.get("remark"))
                    .addParameter("PAGEOFFSET", conditions.get("PAGEOFFSET"))
                    .addParameter("PAGESIZE", conditions.get("PAGESIZE"))
                    .executeAndFetch(PresentBillInfo.class);
        }
    }

    public static int count(Map conditions) {

        val sql = " SELECT count(*) xcount FROM tbPresentBill B " +
                " LEFT JOIN tbAccount T on B.ToAccountId = T.AccountId " +
                " LEFT JOIN tbAccount F on B.FromAccountId = F.AccountId " +
                " WHERE 1=1" +
                "   AND (B.ToAccountId LIKE CONCAT('%',:toAccountId,'%') OR :toAccountId is null) " +
                "   AND (B.FromAccountId LIKE CONCAT('%',:fromAccountId,'%') OR :fromAccountId is null) " +
                "   AND (B.Title LIKE CONCAT('%',:title,'%') OR :title is null) " +
                "   AND (B.Remark LIKE CONCAT('%',:remark,'%') OR :remark is null) ";

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("toAccountId", conditions.get("toAccountId"))
                    .addParameter("fromAccountId", conditions.get("fromAccountId"))
                    .addParameter("title", conditions.get("title"))
                    .addParameter("remark", conditions.get("remark"))
                    .executeScalar(int.class);
        }
    }

    public static List<QuotationBillProductInfo> loadDetails(String toAccountId, String fromAccountId, String quotationBillId) {

        val sql = "select QuotationBillId 'billId' ,ProductId 'product.productId' ,FromProductId 'product.fromProductId' ,ProductName 'product.productName' ,PurchPrice 'product.purchPrice' ,Quantity ,Unit 'product.unit'" +
                " from tbPresentBillProduct" +
                " where ToAccountId =:toAccountId and FromAccountId =:fromAccountId and QuotationBillId=:quotationBillId ";

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("toAccountId", toAccountId)
                    .addParameter("fromAccountId", fromAccountId)
                    .addParameter("quotationBillId", quotationBillId)
                    .executeAndFetch(QuotationBillProductInfo.class);
        }
    }

    public static void delete(String toAccountId, String fromAccountId, String quotationBillId) {

        try (val con = db.sql2o.beginTransaction()) {

            Arrays.asList(
                    " DELETE FROM tbPresentBillProduct WHERE ToAccountId =:toAccountId and FromAccountId =:fromAccountId and QuotationBillId=:quotationBillId ; ",
                    " DELETE FROM tbPresentBill WHERE ToAccountId =:toAccountId and FromAccountId =:fromAccountId and QuotationBillId=:quotationBillId ; ")
                    .forEach(s ->
                            con.createQuery(s)
                                    .addParameter("toAccountId", toAccountId)
                                    .addParameter("fromAccountId", fromAccountId)
                                    .addParameter("quotationBillId", quotationBillId)
                                    .executeUpdate()
                    );

            con.commit();
        }
    }
}
