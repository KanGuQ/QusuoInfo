package com.forcpacebj.api.business;

import com.forcpacebj.api.config.StaticParam;
import com.forcpacebj.api.entity.UserInfo;
import lombok.val;
import lombok.var;
import org.sql2o.Connection;

import java.util.Arrays;
import java.util.List;

import static spark.Spark.halt;

public class LoginBusiness {

    public static UserInfo checkLogin(String userId, String pwd) /*throws Exception*/ {

        val sql = " SELECT U.DepartmentId 'department.id', D.Name 'department.name', D.Manager 'department.manager.userId', IFNULL(D.Power,'[]') 'department.power'," +
                " U.AccountId ,U.UserId ,U.UserName , UR.RoleName 'userRole.roleName'," +
                " U.RoleId 'userRole.roleId', IFNULL(UR.Power,'[]') 'userRole.power'," +
                " U.Email ,U.IsAdmin ,U.MultipleLogin, A.AccountName ,U.CurrentDepartmentId,U.DepartmentId" +
                " FROM tbUser U " +
                " LEFT JOIN tbAccount A ON U.AccountId = A.AccountId " +
                " LEFT JOIN Department D ON U.DepartmentId = D.Id " +
                " LEFT JOIN tbUserRole UR ON U.RoleId = UR.RoleId AND (U.AccountId = UR.AccountId OR UR.AccountId = 'Default')" +
                " WHERE U.UserId = :userId AND U.PWD = :pwd ";

        try (val con = db.sql2o.open()) {
            var user = con.createQuery(sql)
                    .addParameter("userId", userId)
                    .addParameter("pwd", pwd)
                    .executeAndFetchFirst(UserInfo.class);
            //MD5验证
//          MD5.verify2(pwd,user.getPwd());
            if (user == null) halt(401, "用户名或者密码错误");
            if (user.getAccountId() == null) return user;
            return departmentHandle(con, user, pwd);
        }
    }

    private static UserInfo departmentHandle(Connection con, UserInfo user, String pwd) /*throws Exception*/ {

        if (!DepartmentBusiness.checkDepartment(user.getUserId(), user.getDepartment().getId(), user.getAccountId(), user.getIsAdmin() || user.getUserRole().getPower().contains(StaticParam.SUPER))) {
            val default_department_id = DepartmentBusiness.getDefaultDepartment(user.getAccountId());
            if (default_department_id == null) halt(401, "用户部门信息异常");
            con.createQuery("UPDATE tbUser SET DepartmentId = :id WHERE userId = :userId")
                    .addParameter("id", default_department_id)
                    .addParameter("userId", user.getUserId())
                    .executeUpdate();
            user = checkLogin(user.getUserId(), pwd);
        }

        if (!user.getDepartment().getId().equals(user.getCurrentDepartmentId())) {

            if (!DepartmentBusiness.checkDepartment(user.getUserId(), user.getCurrentDepartmentId(), user.getAccountId(), user.getIsAdmin() || user.getUserRole().getPower().contains(StaticParam.SUPER))) {//不可以用这个部门
                con.createQuery("UPDATE tbUser SET CurrentDepartmentId = :id WHERE userId = :userId")
                        .addParameter("id", user.getDepartment().getId())
                        .addParameter("userId", user.getUserId())
                        .executeUpdate();
                user = checkLogin(user.getUserId(), pwd);
            } else {
                return UserBusiness.chooseDepartment(user.getUserId(), user.getCurrentDepartmentId());
            }
        }
        if (user.getUserId().equals(user.getDepartment().getManager().getUserId())) {
            var power = user.getUserRole().getPower();
            power.add(StaticParam.DEPARTMENT_MANAGER);
            user.getUserRole().setPower(power.toString());
        }
        return user;
    }

    public static void modifyPwd(String userId, String pwd) {

        val sql = "UPDATE tbUser SET Pwd = :pwd WHERE UserId = :userId ";

        try (val con = db.sql2o.open()) {
            con.createQuery(sql)
                    .addParameter("userId", userId)
                    .addParameter("pwd", pwd)
                    .executeUpdate();
        }
    }

    public static List<String> updateContent() {
        val sql = "SELECT update_content FROM version_update_content ORDER BY update_time DESC LIMIT 1";
        try (val con = db.sql2o.open()) {
            return Arrays.asList(con.createQuery(sql).executeScalar(String.class).split("\r\n"));
        }
    }
}