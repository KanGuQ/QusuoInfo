package com.forcpacebj.api.business;

import com.forcpacebj.api.config.StaticParam;
import com.forcpacebj.api.entity.UserInfo;
import com.forcpacebj.api.utils.JSONUtil;
import lombok.val;
import lombok.var;

import java.util.*;

public class UserBusiness {

    public static List<UserInfo> find(Map conditions, String accountId) {

        val sql = " SELECT U.UserId ,U.UserName ,U.Email ,U.Title ,U.IsAdmin ,U.PhoneNumber ," +
                  " U.RoleId 'userRole.roleId' ,R.RoleName 'userRole.roleName',U.LastLogin,U.LastAccessed," +
                  " U.DepartmentId 'department.id', D.Name 'department.name' " +
                  "   FROM tbUser U" +
                  "   LEFT JOIN tbUserRole R ON (U.AccountId=R.AccountId OR R.AccountId = 'Default') " +
                  "         AND U.RoleId=R.RoleId " +
                  "   LEFT JOIN Department D ON D.Id = U.DepartmentId " +
                  " WHERE U.AccountId= :accountId " +
                  "      AND (U.UserName LIKE CONCAT('%',:userName,'%') OR :userName is null) " +
                  "      AND (U.RoleId = :roleId OR :roleId is null) " +
                  "      AND (U.Email LIKE CONCAT('%',:email,'%') OR :email is null)" +
                  "      AND (U.PhoneNumber LIKE CONCAT('%',:phoneNumber,'%') OR :phoneNumber is null)" +
                  "      AND (U.UserName LIKE CONCAT('%',:all,'%') OR U.UserId LIKE CONCAT('%',:all,'%') OR U.Email LIKE CONCAT('%',:all,'%') OR U.PhoneNumber LIKE CONCAT('%',:all,'%') OR :all is null)" +
                  " ORDER BY U.Id LIMIT :PAGEOFFSET ,:PAGESIZE";

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .addParameter("PAGEOFFSET", conditions.get("PAGEOFFSET"))
                    .addParameter("PAGESIZE", conditions.get("PAGESIZE"))
                    .addParameter("userName", conditions.get("userName"))
                    .addParameter("email", conditions.get("email"))
                    .addParameter("phoneNumber", conditions.get("phoneNumber"))
                    .addParameter("all", conditions.get("all"))
                    .addParameter("roleId", conditions.get("role"))
                    .executeAndFetch(UserInfo.class);
        }
    }

    public static int count(Map conditions, String accountId) {

        val sql = " SELECT count(1) xcount FROM tbUser " +
                  " WHERE (AccountId= :accountId OR :accountId IS NULL)" +
                  "      AND (UserName LIKE CONCAT('%',:userName,'%') OR :userName is null) " +
                  "      AND (RoleId = :roleId OR :roleId is null) " +
                  "      AND (Email LIKE CONCAT('%',:email,'%') OR :email is null)" +
                  "      AND (PhoneNumber LIKE CONCAT('%',:phoneNumber,'%') OR :phoneNumber is null)" +
                  "      AND ( UserName LIKE CONCAT('%',:all,'%') OR UserId LIKE CONCAT('%',:all,'%') OR Email LIKE CONCAT('%',:all,'%') OR PhoneNumber LIKE CONCAT('%',:all,'%') OR :all is null)";

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .addParameter("userName", conditions.get("userName"))
                    .addParameter("email", conditions.get("email"))
                    .addParameter("phoneNumber", conditions.get("phoneNumber"))
                    .addParameter("all", conditions.get("all"))
                    .addParameter("roleId", conditions.get("role"))
                    .executeScalar(int.class);
        }
    }

    public static List<UserInfo> find(Map conditions) {

        val sql = " SELECT U.UserId ,U.UserName ,A.AccountName ,U.Email ,U.Title ,U.IsAdmin ,U.PhoneNumber ," +
                  " U.RoleId 'userRole.roleId' ,R.RoleName 'userRole.roleName',U.LastLogin,U.LastAccessed," +
                  " U.DepartmentId 'department.id', D.Name 'department.name' " +
                  "   FROM tbUser U" +
                  "   LEFT JOIN tbUserRole R ON (U.AccountId=R.AccountId OR R.AccountId = 'Default') " +
                  "         AND U.RoleId=R.RoleId " +
                  "   LEFT JOIN tbAccount A ON U.AccountId = A.AccountId " +
                  "   LEFT JOIN Department D ON D.Id = U.DepartmentId " +
                  " WHERE (U.UserName LIKE CONCAT('%',:userName,'%') OR :userName is null) " +
                  "      AND (A.AccountName LIKE CONCAT('%',:accountName,'%') OR :accountName is null)"+
                  "      AND (U.RoleId = :roleId OR :roleId is null) " +
                  "      AND (U.UserId = :userId OR :userId is null) " +
                  "      AND (U.Email LIKE CONCAT('%',:email,'%') OR :email is null)" +
                  "      AND (U.PhoneNumber LIKE CONCAT('%',:phoneNumber,'%') OR :phoneNumber is null)" +
                  "      AND (U.UserName LIKE CONCAT('%',:all,'%') OR U.UserId LIKE CONCAT('%',:all,'%') OR U.Email LIKE CONCAT('%',:all,'%') OR U.PhoneNumber LIKE CONCAT('%',:all,'%') OR :all is null)" +
                  " ORDER BY U.LastLogin DESC LIMIT :PAGEOFFSET ,:PAGESIZE";

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("PAGEOFFSET", conditions.get("PAGEOFFSET"))
                    .addParameter("PAGESIZE", conditions.get("PAGESIZE"))
                    .addParameter("userName", conditions.get("userName"))
                    .addParameter("accountName", conditions.get("accountName"))
                    .addParameter("userId", conditions.get("userId"))
                    .addParameter("email", conditions.get("email"))
                    .addParameter("phoneNumber", conditions.get("phoneNumber"))
                    .addParameter("all", conditions.get("all"))
                    .addParameter("roleId", conditions.get("role"))
                    .executeAndFetch(UserInfo.class);
        }
    }

    public static int count(Map conditions) {

        val sql = " SELECT count(1) xcount FROM tbUser " +
                  " WHERE (UserName LIKE CONCAT('%',:userName,'%') OR :userName is null) " +
                  "      AND (RoleId = :roleId OR :roleId is null) " +
                  "      AND (Email LIKE CONCAT('%',:email,'%') OR :email is null)" +
                  "      AND (PhoneNumber LIKE CONCAT('%',:phoneNumber,'%') OR :phoneNumber is null)" +
                  "      AND ( UserName LIKE CONCAT('%',:all,'%') OR UserId LIKE CONCAT('%',:all,'%') OR Email LIKE CONCAT('%',:all,'%') OR PhoneNumber LIKE CONCAT('%',:all,'%') OR :all is null)";

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("userName", conditions.get("userName"))
                    .addParameter("email", conditions.get("email"))
                    .addParameter("phoneNumber", conditions.get("phoneNumber"))
                    .addParameter("all", conditions.get("all"))
                    .addParameter("roleId", conditions.get("role"))
                    .executeScalar(int.class);
        }
    }

    public static List<UserInfo> list(String accountId) {

        val sql = " SELECT U.UserId ,U.DepartmentId 'department.id' ,U.UserName ,U.Email ,U.Title ,U.IsAdmin ,U.PhoneNumber ,U.RoleId 'userRole.roleId' ,R.RoleName 'userRole.roleName'" +
                  "   FROM tbUser U " +
                  "   LEFT JOIN tbUserRole R ON U.AccountId=R.AccountId " +
                  "         AND U.RoleId=R.RoleId " +
                  " where U.UserId not like 'yabbaddabado%' AND U.AccountId= :accountId ";

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .executeAndFetch(UserInfo.class);
        }
    }

    public static List<String> userIdList() {

        val sql = " SELECT U.UserId FROM tbUser U ";

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .executeAndFetch(String.class);
        }
    }

    public static UserInfo load(String accountId, String id) {

        val sql = " SELECT U.UserId ,U.UserName ,U.Title ,U.Email ,U.IsAdmin ,U.PhoneNumber," +
                  " U.RoleId 'userRole.roleId' , U.AccountId,R.RoleName 'userRole.roleName'," +
                  " U.DepartmentId 'department.id', D.Name 'department.name'" +
                  "   FROM tbUser U " +
                  "   LEFT JOIN tbUserRole R ON U.AccountId=R.AccountId " +
                  "         AND U.RoleId=R.RoleId " +
                  "   LEFT JOIN Department D ON D.Id = U.DepartmentId " +
                  "  WHERE (U.AccountId= :accountId OR :accountId IS NULL) AND U.UserId = :id";

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .addParameter("id", id)
                    .executeAndFetchFirst(UserInfo.class);
        }
    }

    public static void insert(UserInfo user) {

        val sql = "INSERT INTO tbUser(UserId ,UserName , RoleId ,Title ,Email ,IsAdmin ,Pwd ,PhoneNumber) " +
                  " values (:userId ,:userName ,:roleId ,:title ,:email ,:isAdmin ,:pwd ,:phoneNumber)";

        try (val con = db.sql2o.beginTransaction()) {
            con.createQuery(sql).bind(user)
                    .addParameter("roleId", user.getIsAdmin() || user.getUserRole() == null ? null : user.getUserRole().getRoleId())
                    .executeUpdate();
            con.commit();
        }
    }

    public static void update(String accountId, UserInfo user) {

        val sql = "UPDATE tbUser SET " +
                  "        UserName = :userName ," +
                  "        RoleId = :roleId ," +
                  "        Email = :email ," +
                  "        Title = :title ," +
                  "        DepartmentId = :departmentId," +
                  "        IsAdmin = :isAdmin ," +
                  "        PhoneNumber = :phoneNumber " +
                  " WHERE (AccountId= :accountId OR :accountId IS NULL) AND UserId = :userId ";

        try (val con = db.sql2o.open()) {
            con.createQuery(sql).bind(user)
                    .addParameter("accountId", accountId)
                    .addParameter("roleId", user.getIsAdmin() ? null : user.getUserRole().getRoleId())
                    .addParameter("departmentId", user.getDepartment().getId())
                    .executeUpdate();
        }
    }

    public static void delete(String accountId, String id) {

        val sql = " DELETE FROM tbUser WHERE AccountId= :accountId AND UserId = :id";

        try (val con = db.sql2o.open()) {
            con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .addParameter("id", id)
                    .executeUpdate();
        }
    }

    public static void expel(Map condition) {
        try (val con = db.sql2o.beginTransaction()) {
            Arrays.asList(
                    "UPDATE tbUser SET AccountId = NULL,DepartmentId = NULL,RoleId = NULL,isAdmin = 0 " +
                    " WHERE UserId = :userId AND :handoverUserId=:handoverUserId AND :handoverUserName=:handoverUserName",
                    "UPDATE tbProject SET HandoverUserId=:handoverUserId,HandoverUserName=:handoverUserName " +
                    " WHERE CreateUserId=:userId",
                    "UPDATE tbPeople SET HandoverUserId=:handoverUserId,HandoverUserName=:handoverUserName " +
                    " WHERE CreateUserId=:userId").forEach(s ->
                    con.createQuery(s)
                            .addParameter("userId", condition.get("userId"))
                            .addParameter("handoverUserId", condition.get("handoverUserId"))
                            .addParameter("handoverUserName", condition.get("handoverUserName"))
                            .executeUpdate());
            con.commit();
        }
    }

    public static void changeDepartment(Map condition) {
        try (val con = db.sql2o.beginTransaction()) {
            val handoverUser = JSONUtil.toBean(condition.get("handoverUser").toString(), UserInfo.class);
            Arrays.asList(
                    "UPDATE tbUser SET DepartmentId = :departmentId " +
                    " WHERE UserId = :userId AND :handoverUserId=:handoverUserId AND :handoverUserName=:handoverUserName",
                    "UPDATE tbProject SET HandoverUserId=:handoverUserId,HandoverUserName=:handoverUserName " +
                    " WHERE CreateUserId=:userId AND :departmentId = :departmentId",
                    "UPDATE tbPeople SET HandoverUserId=:handoverUserId,HandoverUserName=:handoverUserName " +
                    " WHERE CreateUserId=:userId AND :departmentId = :departmentId").forEach(s ->
                    con.createQuery(s)
                            .addParameter("userId", condition.get("userId"))
                            .addParameter("departmentId", condition.get("departmentId"))
                            .addParameter("handoverUserId", handoverUser.getUserId())
                            .addParameter("handoverUserName", handoverUser.getUserName())
                            .executeUpdate());
            con.commit();
        }
    }

    public static void lastLogin(UserInfo user) {
        val sql = "UPDATE tbUser SET LastLogin = Now(),LastAccessed = Now() " +
                  " WHERE UserId = :userId ";

        val recordSql = "INSERT INTO UserLoginRecord(`UserId`, `AccountId`, `LoginTime`) " +
                        "VALUES (:userId, :accountId, NOW())";

        try (val con = db.sql2o.beginTransaction()) {
            con.createQuery(sql).bind(user)
                    .executeUpdate();
            con.createQuery(recordSql).bind(user)
                    .executeUpdate();
            con.commit();
        }
    }

    public static void lastAccessed(UserInfo user) {
        val sql = "UPDATE tbUser SET LastAccessed = Now() " +
                  " WHERE UserId = :userId ";

        try (val con = db.sql2o.open()) {
            con.createQuery(sql).bind(user)
                    .executeUpdate();
        }
    }

    public static List<UserInfo> findAllByAccountId(String accountId, Integer pageSize, Integer pageOffset) {
        val sql = " SELECT U.UserId ,U.UserName ,U.Email ,U.Title ,U.IsAdmin ,U.PhoneNumber," +
                  " U.RoleId 'userRole.roleId' ,R.RoleName 'userRole.roleName'," +
                  " U.DepartmentId 'department.id', D.Name 'department.name'" +
                  "   FROM tbUser U " +
                  "   LEFT JOIN tbUserRole R ON U.AccountId=R.AccountId " +
                  "         AND U.RoleId=R.RoleId " +
                  "   LEFT JOIN Department D ON D.Id = U.DepartmentId " +
                  " where U.UserId not like 'yabbaddabado%' AND U.AccountId= :accountId " +
                  " LIMIT :PAGEOFFSET ,:PAGESIZE";

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .addParameter("PAGEOFFSET", pageOffset)
                    .addParameter("PAGESIZE", pageSize)
                    .executeAndFetch(UserInfo.class);
        }
    }

    public static List<UserInfo> loginRecordByDate(Map conditions) {
        val sql = "SELECT DISTINCT DATE_FORMAT(LoginTime,'%Y-%m-%d') title,count(1) recordDateCount FROM UserLoginRecord " +
                  " WHERE UserId = :userId AND DATE_FORMAT(LoginTime,'%Y') = :year " +
                  " GROUP BY DATE_FORMAT(LoginTime,'%Y-%m-%d')";
        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("userId", conditions.get("userId"))
                    .addParameter("year", conditions.get("year"))
                    .executeAndFetch(UserInfo.class);
        }
    }

    public static UserInfo chooseDepartment(String userId, Integer departmentId) {

        val sql = " SELECT D.Id 'department.id', D.Name 'department.name', D.Manager 'department.manager.userId', IFNULL(D.Power,'[]') 'department.power'," +
                  " U.AccountId ,U.UserId ,U.UserName , UR.RoleName 'userRole.roleName'," +
                  " U.RoleId 'userRole.roleId', IFNULL(UR.Power,'[]') 'userRole.power'," +
                  " U.Email ,U.IsAdmin ,U.MultipleLogin, A.AccountName " +
                  " FROM tbUser U " +
                  " LEFT JOIN tbAccount A ON U.AccountId = A.AccountId " +
                  " LEFT JOIN Department D ON D.Id = :departmentId" +
                  " LEFT JOIN tbUserRole UR ON U.RoleId = UR.RoleId AND (U.AccountId = UR.AccountId OR UR.AccountId = 'Default')" +
                  " WHERE U.UserId = :userId ";

        try (val con = db.sql2o.open()) {
            var user = con.createQuery(sql)
                    .addParameter("userId", userId)
                    .addParameter("departmentId", departmentId)
                    .executeAndFetchFirst(UserInfo.class);
            if (user.getUserId().equals(user.getDepartment().getManager().getUserId())) {
                var power = user.getUserRole().getPower();
                power.add(StaticParam.DEPARTMENT_MANAGER);
                user.getUserRole().setPower(power.toString());
            }
            con.createQuery("UPDATE tbUser SET CurrentDepartmentId = :id WHERE userId = :userId")
                    .addParameter("id", user.getDepartment().getId())
                    .addParameter("userId", user.getUserId())
                    .executeUpdate();
            return user;
        }
    }

}