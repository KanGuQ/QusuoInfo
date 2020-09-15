package com.forcpacebj.api.business;

import com.forcpacebj.api.entity.UserRoleInfo;
import lombok.val;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class UserRoleBusiness {

    public static List<UserRoleInfo> find(Map conditions, String accountId) {

        val sql = " SELECT RoleId ,RoleName ,ifNull(case when Power = '' then null else Power end,'[]') Power FROM tbUserRole " +
                " WHERE AccountId= :accountId or AccountId = 'Default'" +
                " Order By RoleId " +
                " LIMIT :PAGEOFFSET ,:PAGESIZE";

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .addParameter("PAGEOFFSET", conditions.get("PAGEOFFSET"))
                    .addParameter("PAGESIZE", conditions.get("PAGESIZE"))
                    .executeAndFetch(UserRoleInfo.class);
        }
    }

    public static int count(String accountId) {

        val sql = " SELECT count(1) xcount FROM tbUserRole " +
                " WHERE AccountId= :accountId ";

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .executeScalar(int.class);
        }
    }

    public static List<UserRoleInfo> list(String accountId) {

        val sql = " SELECT RoleId ,RoleName ,Power FROM tbUserRole " +
                " where AccountId= :accountId or AccountId = 'Default' Order By RoleId ";

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .executeAndFetch(UserRoleInfo.class);
        }
    }

    public static UserRoleInfo load(String accountId, String roleId) {

        val sql = " SELECT RoleId ,RoleName ,Power FROM tbUserRole WHERE (AccountId= :accountId or AccountId = 'Default') AND RoleId = :roleId";

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .addParameter("roleId", roleId)
                    .executeAndFetchFirst(UserRoleInfo.class);
        }
    }

    public static void insert(String accountId, UserRoleInfo userRole) {

        val sql = "INSERT INTO tbUserRole(AccountId ,Power ,RoleName) " +
                " values (:accountId ,:power ,:roleName)";

        try (val con = db.sql2o.open()) {
            con.createQuery(sql).bind(userRole)
                    .addParameter("accountId", accountId)
                    .addParameter("power", userRole.getPower().toString())
                    .executeUpdate();
        }
    }

    public static void update(String accountId, UserRoleInfo userRole) {

        val sql = "UPDATE tbUserRole SET " +
                "        RoleName = :roleName ," +
                "        Power = :power" +
                " WHERE AccountId= :accountId AND RoleId = :roleId ";

        try (val con = db.sql2o.open()) {
            con.createQuery(sql).bind(userRole)
                    .addParameter("accountId", accountId)
                    .addParameter("power", userRole.getPower().toString())
                    .executeUpdate();
        }
    }

    public static void delete(String accountId, String roleId) {

        try (val con = db.sql2o.beginTransaction()) {
            Arrays.asList(
                    "DELETE FROM tbUserRole WHERE AccountId= :accountId AND RoleId = :roleId",
                    "UPDATE tbUser SET roleId = null WHERE AccountId= :accountId AND RoleId = :roleId")
                    .forEach(s ->
                            con.createQuery(s)

                                    .addParameter("accountId", accountId)
                                    .addParameter("roleId", roleId)
                                    .executeUpdate()
                    );
            con.commit();
        }
    }
}