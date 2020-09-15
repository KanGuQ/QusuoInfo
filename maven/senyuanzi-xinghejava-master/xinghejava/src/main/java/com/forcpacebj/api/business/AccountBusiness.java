package com.forcpacebj.api.business;

import com.forcpacebj.api.entity.AccountInfo;
import com.forcpacebj.api.utils.IdGenerator;
import lombok.val;
import lombok.var;

import java.util.List;
import java.util.Map;

public class AccountBusiness {

    public static AccountInfo load(String accountId) {

        val sql = " SELECT * FROM tbAccount WHERE AccountId= :accountId";

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .executeAndFetchFirst(AccountInfo.class);
        }
    }

    public static Boolean check(String accountId) {
        val sql = "SELECT EXISTS(SELECT 1 FROM tbAccount WHERE AccountId= :accountId AND EffectiveDate <= NOW() AND ExpireDate >= NOW()) value";
        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .executeAndFetchFirst(Boolean.class);
        }
    }

    public static void payForRenew(Map data) {
        var sql = "UPDATE tbAccount SET EffectiveDate = Now(),ExpireDate = :expireDate," +
                  " UserLimit = :userLimit, ProductLimit = :productLimit, ProjectLimit = :projectLimit " +
                  " WHERE AccountId = :accountId";
        if (check((String) data.get("accountId"))) {
            sql = "UPDATE tbAccount SET ExpireDate = :expireDate," +
                  " UserLimit = :userLimit, ProductLimit = :productLimit, ProjectLimit = :projectLimit " +
                  " WHERE AccountId = :accountId";
        }
        try (val con = db.sql2o.beginTransaction()) {
            con.createQuery(sql)
                    .addParameter("accountId", data.get("accountId"))
                    .addParameter("expireDate", data.get("expireDate"))
                    .addParameter("userLimit", data.get("userLimit"))
                    .addParameter("productLimit", data.get("productLimit"))
                    .addParameter("projectLimit", data.get("projectLimit"))
                    .executeUpdate();
            con.createQuery("INSERT INTO AccountPayRecord (AccountId,EffectiveDate,ExpireDate,UserLimit,ProductLimit,ProjectLimit,CreateTime)" +
                            "VALUES(:accountId,(SELECT EffectiveDate from tbAccount WHERE AccountId = :accountId),:expireDate,:userLimit,:productLimit,:projectLimit,NOW())")
                    .addParameter("accountId", data.get("accountId"))
                    .addParameter("expireDate", data.get("expireDate"))
                    .addParameter("userLimit", data.get("userLimit"))
                    .addParameter("productLimit", data.get("productLimit"))
                    .addParameter("projectLimit", data.get("projectLimit"))
                    .executeUpdate();
            con.commit();
        }

    }

    public static Boolean checkUserLimit(String accountId) {
        val sql = "SELECT COUNT(U.id) >= IFNULL(A.UserLimit,0) value FROM tbAccount A " +
                  " LEFT JOIN tbUser U ON A.AccountId = U.AccountId " +
                  " WHERE A.AccountId = :accountId";
        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .executeAndFetchFirst(Boolean.class);
        }
    }

    public static AccountInfo checkInviteCode(String userId, String inviteCode) {
        val sql = "SELECT A.AccountId,IFNULL(A.UserLimit,0) UserLimit,COUNT(U.id) userNum FROM tbAccount A " +
                  " LEFT JOIN tbUser U ON A.AccountId = U.AccountId " +
                  " WHERE A.AccountId=(SELECT accountId FROM staff_join_record WHERE state = 0 AND userId = :userId AND code = :inviteCode)";
        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("userId", userId)
                    .addParameter("inviteCode", inviteCode)
                    .executeAndFetchFirst(AccountInfo.class);
        }
    }


    public static AccountInfo loadByName(String accountName) {

        val sql = " SELECT AccountId FROM tbAccount WHERE AccountName= :accountName";

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("accountName", accountName)
                    .executeAndFetchFirst(AccountInfo.class);
        }
    }

    public static AccountInfo loadByInviteCode(String inviteCode) {

        val sql = " SELECT AccountId ,AccountName FROM tbAccount WHERE InviteCode= :inviteCode";

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("inviteCode", inviteCode)
                    .executeAndFetchFirst(AccountInfo.class);
        }
    }

    public static void register(AccountInfo account) {

        val sql = "INSERT INTO `tbaccount`(`AccountId`, `AccountName`, `Remark`, `InviteCode`, `is_register`, `user_id`, `telephone`, `city_id`, `address`, `industry`, `project_type`, `employees_num`, `average_project_size`, `average_project_number`, CreateUserPosition, MainBrand, `create_time`) " +
                  "    VALUES (:account_id, :accountName, null, :invite_code, 0, :userId, :telephone, :cityId, :address, :industry, :projectType, :employeesNum, :averageProjectSize, :averageProjectNumber, :createUserPosition, :mainBrand,  NOW());";

        try (val con = db.sql2o.open()) {
            con.createQuery(sql).bind(account)
                    .addParameter("account_id", IdGenerator.NewId())
                    .addParameter("invite_code", IdGenerator.NewId())
                    .executeUpdate();
        }
    }

    public static List<AccountInfo> getCompanyList(Map conditions) {
        val sql = "SELECT DISTINCT C.`AccountId`, C.`AccountName`,C.is_register isRegister, C.`user_id` userId, " +
                  "CU.UserName userName, C.`telephone`, C.`city_id` cityId, C.`address`, C.`industry`, " +
                  "C.`project_type` projectType, C.`employees_num` employeesNum, C.`average_project_size` averageProjectSize, " +
                  "C.`average_project_number` averageProjectNumber, C.UserLimit, C.ProductLimit, C.ProjectLimit, " +
                  "C.EffectiveDate, C.ExpireDate, C.CreateUserPosition,C.MainBrand, C.IsPaid,C.`create_time` createTime " +
                  " FROM  `tbAccount` C LEFT JOIN tbUser U ON U.AccountId = C.AccountId " +
                  " LEFT JOIN tbUser CU ON CU.UserId = C.user_id " +
                  " WHERE C.`AccountId` <> 'Public' AND (C.is_register = :isRegister or :isRegister IS NULL) AND C.is_register IS NOT NULL " +
                  " AND (C.AccountName LIKE CONCAT('%',:accountName,'%') OR :accountName IS NULL) " +
                  " AND (U.UserId = :userId OR :userId IS NULL) " +
                  " GROUP BY C.`AccountId` " +
                  " ORDER BY C.create_time DESC " +
                  " LIMIT :PAGEOFFSET ,:PAGESIZE ";
        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("isRegister", conditions.get("isRegister") == null ? 1 : conditions.get("isRegister"))
                    .addParameter("PAGEOFFSET", conditions.get("PAGEOFFSET"))
                    .addParameter("PAGESIZE", conditions.get("PAGESIZE"))
                    .addParameter("accountName", conditions.get("accountName"))
                    .addParameter("userId", conditions.get("userId"))
                    .executeAndFetch(AccountInfo.class);
        }
    }

    public static Integer getCompanyCount(Map conditions) {
        val sql = "SELECT COUNT(DISTINCT C.`AccountId`) FROM `tbAccount` C LEFT JOIN tbUser U ON U.AccountId = C.AccountId " +
                  " WHERE C.is_register IS NOT NULL AND C.`AccountId` <> 'Public' " +
                  " AND (C.is_register = :isRegister or :isRegister IS NULL) " +
                  " AND (C.AccountName LIKE CONCAT('%',:accountName,'%') OR :accountName IS NULL) " +
                  " AND (U.UserId = :userId OR :userId IS NULL) ";
        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("isRegister", conditions.get("isRegister") == null ? 1 : conditions.get("isRegister"))
                    .addParameter("accountName", conditions.get("accountName"))
                    .addParameter("userId", conditions.get("userId"))
                    .executeScalar(Integer.class);
        }
    }

    public static List<AccountInfo> getPublicCompanyList(Map conditions) {
        val sql = "SELECT DISTINCT C.`AccountId`, C.`AccountName`,C.is_register isRegister, C.`user_id` userId, C.`telephone`, C.`city_id` cityId, C.`address`, C.`industry`, C.`project_type` projectType, C.`employees_num` employeesNum, C.`average_project_size` averageProjectSize, C.`average_project_number` averageProjectNumber, C.`create_time` createTime, " +
                  " F.isAuth " +
                  " FROM  `tbAccount` C INNER JOIN tbProduct P ON P.AccountId = C.AccountId AND P.isInExclusive = true " +
                  " left join tbFriend F on C.accountId = F.accountId and F.friendAccountId = :accountId" +
                  " WHERE C.`AccountId` <> 'Public' AND (C.is_register = 1) " +
                  " ORDER BY C.create_time DESC " +
                  " LIMIT :PAGEOFFSET ,:PAGESIZE ";
        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("accountId", conditions.get("accountId"))
                    .addParameter("PAGEOFFSET", conditions.get("PAGEOFFSET"))
                    .addParameter("PAGESIZE", conditions.get("PAGESIZE"))
                    .executeAndFetch(AccountInfo.class);
        }
    }

    public static Integer alreadyExist(String userId) {
        val sql = "SELECT IFNULL(COUNT(1),0) " +
                  " FROM  `tbaccount` C  WHERE C.is_register <> 2 AND C.user_id = :userId";
        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("userId", userId)
                    .executeScalar(Integer.class);
        }
    }

    public static void agreeToCreate(String userId, String accountId, Boolean isAgree) {
        String sql = isAgree ? "CALL CREATE_ACCOUNT(:accountId, :userId)"
                :
                " UPDATE tbaccount SET is_register = 2 WHERE user_id = :userId AND AccountId = :accountId AND is_register = 0 ; ";
        try (val con = db.sql2o.beginTransaction()) {
            con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .addParameter("userId", userId)
                    .executeUpdate();

            con.commit();
        }
    }


    public static List<AccountInfo> companyOrderByProductCount(Map conditions) {
        val sql = "SELECT A.AccountId,A.AccountName,COUNT(P.ProductId) productCount FROM tbAccount A " +
                  " LEFT JOIN tbProduct P ON A.AccountId = P.AccountId AND P.State = 1 " +
                  " WHERE A.is_register = 1 AND A.AccountId <> 'Public' " +
                  " AND (A.AccountName = :accountName OR :accountName IS NULL) " +
                  " GROUP BY P.AccountId " +
                  " ORDER BY COUNT(P.ProductId) DESC " +
                  " LIMIT :PAGEOFFSET ,:PAGESIZE ";
        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("PAGEOFFSET", conditions.get("PAGEOFFSET"))
                    .addParameter("PAGESIZE", conditions.get("PAGESIZE"))
                    .addParameter("accountName", conditions.get("accountName"))
                    .executeAndFetch(AccountInfo.class);
        }
    }

    public static Integer CompanyCount(Map conditions) {
        val sql = "SELECT COUNT(1) FROM `tbAccount` C " +
                  " WHERE C.`AccountId` <> 'Public' AND C.is_register = 1 " +
                  " AND (C.AccountName = :accountName OR :accountName IS NULL) ";
        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("accountName", conditions.get("accountName"))
                    .executeScalar(Integer.class);
        }
    }

    public static void update(String accountId, AccountInfo accountInfo) {

        val sql = "UPDATE tbAccount SET " +
                  "        zoom = :zoom ," +
                  "        lng = :lng ," +
                  "        address = :address ," +
                  "        industry = :industry ," +
                  "        mainBrand = :mainBrand ," +
                  "        provinceId = :provinceId ," +
                  "        city_id = :cityId ," +
                  "        countyId = :countyId ," +
                  "        adCode = :adCode ," +
                  "        employees_Num = :employees_Num ," +
                  "        average_project_size = :average_project_size ," +
                  "        average_project_number = :average_project_number ," +
                  "        IsPaid = :isPaid," +
                  "        lat = :lat " +
                  " WHERE AccountId= :accountId  ";

        try (val con = db.sql2o.open()) {
            con.createQuery(sql).bind(accountInfo)
                    .addParameter("accountId", accountInfo.getAccountId())
                    .addParameter("zoom", accountInfo.getZoom())
                    .addParameter("industry", accountInfo.getIndustry())
                    .addParameter("mainBrand", accountInfo.getMainBrand())
                    .addParameter("employees_Num", accountInfo.getEmployees_num())
                    .addParameter("provinceId", accountInfo.getProvinceId())
                    .addParameter("cityId", accountInfo.getCityId())
                    .addParameter("countyId", accountInfo.getCountyId())
                    .addParameter("adCode", accountInfo.getAdCode())
                    .addParameter("employees_Num", accountInfo.getEmployees_num())
                    .addParameter("average_project_size", accountInfo.getAverage_project_size())
                    .addParameter("average_project_number", accountInfo.getAverage_project_number())
                    .addParameter("lng", accountInfo.getLng())
                    .addParameter("address", accountInfo.getAddress())
                    .addParameter("lat", accountInfo.getLat())
                    .executeUpdate();
        }
    }

    public static void modifyIsPaid(){
        val sql = "UPDATE tbAccount SET IsPaid = 0 WHERE ExpireDate <= NOW() AND IsPaid = 1";
        try (val con = db.sql2o.open()) {
            con.createQuery(sql).executeUpdate();
        }
    }
}
