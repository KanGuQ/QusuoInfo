package com.forcpacebj.api.business;

import com.forcpacebj.api.entity.DepartmentInfo;
import com.forcpacebj.api.entity.UserInfo;
import lombok.val;
import lombok.var;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by tree on 2020/3/24.
 */
public class DepartmentBusiness {

    public static List<DepartmentInfo> list(String accountId) {
        val sql = "SELECT D.Id,D.Name,D.AccountId,D.Manager 'manager.userId',U.UserName 'manager.userName',D.Power,D.SortNum From Department D" +
                  " LEFT JOIN tbUser U ON U.UserId = D.Manager " +
                  " WHERE D.AccountId = :accountId ORDER BY D.SortNum,D.Id DESC";
        try (val con = db.sql2o.open()) {
            return con.createQuery(sql).addParameter("accountId", accountId)
                    .executeAndFetch(DepartmentInfo.class);
        }
    }

    public static DepartmentInfo load(Object id) {
        val sql = "SELECT D.Id,D.Name,D.AccountId,D.Manager 'manager.userId',U.UserName 'manager.userName',D.Power,D.SortNum From Department D" +
                  " LEFT JOIN tbUser U ON U.UserId = D.Manager " +
                  " WHERE D.Id = :id";
        try (val con = db.sql2o.open()) {
            val department = con.createQuery(sql).addParameter("id", id)
                    .executeAndFetchFirst(DepartmentInfo.class);

            val user = con.createQuery("SELECT userId,UserName FROM tbUser WHERE DepartmentId = :id")
                    .addParameter("id", id)
                    .executeAndFetch(UserInfo.class);
            department.setUsers(user);
            return department;
        }
    }

    public static void insert(DepartmentInfo department) {
        val sql = "INSERT INTO Department(Name,AccountId,Manager,Power,SortNum) " +
                  " VALUES(:name,:accountId,:userId,:power,:sortNum)";
        try (val con = db.sql2o.beginTransaction()) {
            var key = con.createQuery(sql)
                    .bind(department)
                    .addParameter("userId", department.getManager() == null ? null : department.getManager().getUserId())
                    .executeUpdate().getKey(Integer.class);
            if (department.getUsers() != null) {
                con.createQuery("UPDATE tbUser SET DepartmentId = :departmentId WHERE UserId IN " + department.getUsers().toString().replace("[", "('").replace("]", "')").replace(",", "','").replace(" ", ""))
                        .addParameter("departmentId", key)
                        .executeUpdate();
            }
            con.commit();
        }
    }

    public static void update(DepartmentInfo department) {
        val sql = "UPDATE Department SET Name=:name," +
                  " Manager=:userId," +
                  " Power=:power," +
                  " SortNum=:sortNum" +
                  " WHERE Id = :id";
        try (val con = db.sql2o.beginTransaction()) {
            con.createQuery(sql).bind(department)
                    .addParameter("userId", department.getManager() == null ? null : department.getManager().getUserId())
                    .executeUpdate();
            if (department.getUsers() != null)
                con.createQuery("UPDATE tbUser SET DepartmentId = :departmentId WHERE UserId IN " + department.getUsers().toString().replace("[", "('").replace("]", "')").replace(",", "','").replace(" ", ""))
                        .addParameter("departmentId", department.getId())
                        .executeUpdate();
            con.commit();
        }
    }

    public static void update(List<DepartmentInfo> departments) {
        val sql = "UPDATE Department SET Name=:name," +
                  " Manager=:userId," +
                  " Power=:power," +
                  " SortNum=:sortNum" +
                  " WHERE Id = :id";
        try (val con = db.sql2o.beginTransaction()) {
            val query = con.createQuery(sql);
            for (DepartmentInfo department : departments) {
                query.bind(department)
                        .addParameter("userId", department.getManager() == null ? null : department.getManager().getUserId())
                        .addToBatch();
                if (department.getUsers() != null)
                    con.createQuery("UPDATE tbUser SET DepartmentId = :departmentId WHERE UserId IN " + department.getUsers().toString().replace("[", "('").replace("]", "')").replace(",", "','").replace(" ", ""))
                            .addParameter("departmentId", department.getId())
                            .executeUpdate();
            }
            query.executeBatch();
            con.commit();
        }
    }

    public static void delete(Integer id, String accountId) {

        val defaultDepartmentId = getDefaultDepartment(accountId);
        val sql = "DELETE FROM Department WHERE id = :id";
        try (val con = db.sql2o.beginTransaction()) {
            con.createQuery(sql)
                    .addParameter("id", id)
                    .executeUpdate();
            Arrays.asList(
                    "UPDATE tbUser SET DepartmentId = :defaultId WHERE DepartmentId = :id ;",
                    "UPDATE tbProject SET DepartmentId = :defaultId WHERE DepartmentId = :id ;",
                    "UPDATE tbPeople SET DepartmentId = :defaultId WHERE DepartmentId = :id ;",
                    "UPDATE tbQuotationBill SET DepartmentId = :defaultId WHERE DepartmentId = :id ;",
                    "UPDATE tbRecord SET DepartmentId = :defaultId WHERE DepartmentId = :id ;",
                    "UPDATE staff_join_record SET DepartmentId = :defaultId WHERE DepartmentId = :id AND state = 0;",
                    "DELETE FROM Department WHERE id = :id AND :defaultId = :defaultId ; ")
                    .forEach(s ->
                            con.createQuery(s)
                                    .addParameter("defaultId", defaultDepartmentId)
                                    .addParameter("id", id)
                                    .executeUpdate()
                    );
            con.commit();
        }
    }

    public static List<DepartmentInfo> optionalDepartment(String userId) {
        val sql = "SELECT Id,Name,Manager 'manager.userId' FROM Department WHERE manager = :userId " +
                  " or Id = (select departmentId from tbUser where UserId = :userId)";
        try (val con = db.sql2o.open()) {
            var departments = con.createQuery(sql).addParameter("userId", userId)
                    .executeAndFetch(DepartmentInfo.class);
            if (departments.stream().anyMatch(departmentInfo -> !Objects.equals(departmentInfo.getName(), "无部门") && !Objects.equals(userId, departmentInfo.getManager().getUserId())))
                departments = departments.stream().filter(departmentInfo -> !Objects.equals(departmentInfo.getName(), "无部门")).collect(Collectors.toList());
            return departments;
        }
    }

    public static Boolean checkDepartment(String userId, Integer departmentId, String accountId, Boolean isAdmin) {
        val sql = isAdmin ?
                "SELECT IFNull(:departmentId,0) IN (SELECT Id FROM Department WHERE AccountId = :accountId " +
                " AND :userId=:userId)" :
                "SELECT IFNull(:departmentId,0) IN (SELECT Id FROM Department WHERE AccountId = :accountId AND manager = :userId " +
                " or Id = (select U.DepartmentId from tbUser U INNER JOIN Department D ON U.DepartmentId=D.Id AND D.AccountId=:accountId where U.UserId = :userId))";
        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("userId", userId)
                    .addParameter("accountId", accountId)
                    .addParameter("departmentId", departmentId)
                    .executeScalar(Boolean.class);
        }
    }

    public static Integer getDefaultDepartment(Object accountId) {

        try (val con = db.sql2o.open()) {
            return con.createQuery("SELECT Id FROM Department WHERE AccountId = :accountId AND `Name` = '无部门'")
                    .addParameter("accountId", accountId)
                    .executeScalar(Integer.class);
        }
    }

}
