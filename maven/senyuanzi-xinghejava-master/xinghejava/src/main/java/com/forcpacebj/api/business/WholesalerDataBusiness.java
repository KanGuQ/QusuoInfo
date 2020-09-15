package com.forcpacebj.api.business;

import com.forcpacebj.api.entity.ProductData;
import com.forcpacebj.api.entity.WholesalerData;
import lombok.val;
import lombok.var;
import org.sql2o.Connection;

import java.util.List;
import java.util.Map;

public class WholesalerDataBusiness {

    /**
     * 新建更新二合一
     */
    public static void save(Connection con, ProductData data) {
        var sql = "";
        if (data.getId() == null) {//insert
            sql = "INSERT INTO ProductData (productId,companyId,downloadCount,quotationCount) VALUES (:productId,:companyId,:downloadCount,:quotationCount)";
        } else {//update
            sql = "UPDATE ProductData set " +
                  " productId=:productId, " +
                  " companyId=:companyId, " +
                  " downloadCount=:downloadCount, " +
                  " quotationCount=:quotationCount " +
                  " where productId=:productId";
        }

        con.createQuery(sql)
                .bind(data)
                .executeUpdate();
    }


    /**
     * @return WholesalerData
     */
    public static WholesalerData findWholesalerData(Connection con, String companyId) {
        var sql = "select * from wholesalerData where companyId=:companyId";

        return con.createQuery(sql)
                .addParameter("companyId", companyId)
                .executeAndFetchFirst(WholesalerData.class);
    }

    /**
     * @return WholesalerData
     */
    public static List<ProductData> findProductData(Connection con, Map conditions) {

        String orderSql = " order by P.updateTime desc";
        if (conditions.get("orderBy") != null)
            switch ((String) conditions.get("orderBy")) {
                case "downloadCount desc":
                    orderSql = " order by P.downloadCount desc";
                    break;
                case "downloadCount asc":
                    orderSql = " order by P.downloadCount asc";
                    break;
                case "quotationCount desc":
                    orderSql = " order by P.quotationCount desc";
                    break;
                case "quotationCount asc":
                    orderSql = " order by P.quotationCount asc";
                    break;
            }


        var sql = "select brand,role,shortName,downloadCount,quotationCount from ProductData P " +
                  " left join tbproduct T ON P.productId=T.productId" +
                  " where P.companyId=:companyId" +
                  "  and  (T.brand like CONCAT('%',:brand,'%') OR :brand is null) " +
                  "  and  (T.role like CONCAT('%',:role,'%') OR :role is null) " +
                  "  and  (T.shortName like CONCAT('%',:shortName,'%') OR :shortName is null) " +
                  orderSql +
                  " LIMIT :PAGEOFFSET ,:PAGESIZE";

        return con.createQuery(sql)
                .addParameter("companyId", conditions.get("companyId"))
                .addParameter("PAGEOFFSET", conditions.get("PAGEOFFSET"))
                .addParameter("PAGESIZE", conditions.get("PAGESIZE"))
                .addParameter("brand", conditions.get("brand"))
                .addParameter("role", conditions.get("role"))
                .addParameter("shortName", conditions.get("shortName"))
                .executeAndFetch(ProductData.class);
    }


    /**
     * @return ProductData
     */
    public static ProductData find(Connection con, String companyId) {
        var sql = "select * from wholesalerData where companyId=:companyId";

        return con.createQuery(sql)
                .addParameter("companyId", companyId)
                .executeAndFetchFirst(ProductData.class);
    }


    private List<ProductData> findProductsDownloadCount(Connection con, String companyId) {
        var sql = "SELECT P.OriginProductId productId,P.OriginAccountId companyId, COUNT(P.ProductId) downloadCount " +
                  "FROM tbproduct P " +
                  "WHERE P.OriginAccountId=:companyId AND P.AccountId!=P.OriginAccountId " +
                  "GROUP BY P.OriginProductId ";

        return con.createQuery(sql)
                .addParameter("companyId", companyId)
                .executeAndFetch(ProductData.class);
    }

    private List<ProductData> findProductsQuotationCount(Connection con, String companyId) {
        var sql = "SELECT P.OriginProductId productId, P.OriginAccountId companyId,COUNT(DISTINCT Q.BillId) quotationCount " +
                  "FROM tbproduct P " +
                  "LEFT JOIN tbquotationbillproduct Q " +
                  "ON P.ProductId=Q.ProductId " +
                  "WHERE P.OriginAccountId=:companyId " +
                  "GROUP BY P.OriginProductId ";

        return con.createQuery(sql)
                .addParameter("companyId", companyId)
                .executeAndFetch(ProductData.class);
    }


}
