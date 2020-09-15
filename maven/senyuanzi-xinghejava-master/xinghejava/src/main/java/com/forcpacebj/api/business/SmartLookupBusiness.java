package com.forcpacebj.api.business;

import com.forcpacebj.api.entity.SmartLookupItemInfo;
import lombok.val;

import java.util.List;

public class SmartLookupBusiness {

    public static List<SmartLookupItemInfo> searchProduct(String accountId, String search, Integer roleId) {

        val sql = " SELECT P.ProductId Value ,P.ProductName Display ,P.SalePrice Prop1number ,case when R.PurchPrice is null then P.PurchPrice else R.PurchPrice end Prop2number ," +
                "       P.Unit Prop1 ,P.Role Prop2 ,P.ProductType Prop3  " +
                "  FROM tbProduct P " +
                "  LEFT JOIN tbRolePurchPrice R ON P.AccountId = R.AccountId And P.ProductId = R.ProductId And R.RoleId =:roleId" +
                " WHERE P.AccountId= :accountId " +
                "   AND P.IsShow=1 " +
                "   AND P.ProductName LIKE CONCAT('%',:search,'%') " +
                " LIMIT 20";

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .addParameter("search", search)
                    .addParameter("roleId", roleId)
                    .executeAndFetch(SmartLookupItemInfo.class);
        }
    }

    public static List<String> searchBrand(String accountId, String search) {

        val sql = "SELECT DISTINCT P.Brand FROM tbproduct P " +
                "WHERE p.AccountId = :accountId AND IsShow = 1 AND P.Brand LIKE CONCAT('%',:search,'%') LIMIT 20";
        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .addParameter("search", search)
                    .executeAndFetch(String.class);
        }
    }

    public static List<String> searchCompanyName(String search) {

        val sql = "SELECT AccountName FROM tbaccount " +
                " WHERE AccountName LIKE CONCAT('%', :search ,'%') AND AccountId <> 'Public' LIMIT 10";
        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("search", search)
                    .executeAndFetch(String.class);
        }
    }

    public static List<SmartLookupItemInfo> searchPeople(String accountId, String search, String userId) {

        val sql = " SELECT PeopleId Value ,PeopleName Display ," +
                " PeopleSuffix PopupDisplay1 ,Unit PopupDisplay2 ,PeopleRole PopupDisplay3" +
                " FROM tbPeople " +
                " WHERE AccountId= :accountId " +
                "   And PeopleName LIKE CONCAT('%',:search,'%') " +
                "   And (ChargerId = :userId OR CreateUserId = :userId OR :userId is null )" +
                " LIMIT 20";

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .addParameter("search", search)
                    .addParameter("userId", userId)
                    .executeAndFetch(SmartLookupItemInfo.class);
        }
    }

    public static List<SmartLookupItemInfo> searchProject(String accountId, String search, String userId) {

        val sql = " SELECT ProjectId Value ,ProjectName Display  FROM tbProject " +
                " WHERE AccountId= :accountId " +
                "   And ProjectName LIKE CONCAT('%',:search,'%') " +
                "   And (SalesManId = :userId OR CreateUserId = :userId OR ProjectId in (select ProjectId from tbProjectUser where UserId = :userId ) OR :userId is null)" +
                " LIMIT 20";

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .addParameter("search", search)
                    .addParameter("userId", userId)
                    .executeAndFetch(SmartLookupItemInfo.class);
        }
    }

    public static List<String> searchPeopleUnit(String accountId, String search) {

        val sql = " select Unit from (" +
                "   select Distinct Unit  " +
                "   from tbPeople " +
                "   WHERE AccountId= :accountId And Unit LIKE CONCAT('%',:search,'%') " +
                "   And Unit is not null And unit !=''" +
                "   LIMIT 20 )A";

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .addParameter("search", search)
                    .executeAndFetch(String.class);
        }
    }

    public static List<String> searchPeopleName(String accountId, String search) {

        val sql = " SELECT PeopleName from (" +
                " Select Distinct PeopleName" +
                " FROM tbPeople " +
                " WHERE AccountId= :accountId And PeopleName LIKE CONCAT('%',:search,'%') " +
                " LIMIT 20 )A";

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .addParameter("search", search)
                    .executeAndFetch(String.class);
        }
    }

    public static List<String> searchProjectName(String accountId, String search) {

        val sql = " SELECT ProjectName from (" +
                " select Distinct ProjectName " +
                " FROM tbProject " +
                " WHERE AccountId= :accountId And ProjectName LIKE CONCAT('%',:search,'%') " +
                " LIMIT 20 )A";

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .addParameter("search", search)
                    .executeAndFetch(String.class);
        }
    }
}