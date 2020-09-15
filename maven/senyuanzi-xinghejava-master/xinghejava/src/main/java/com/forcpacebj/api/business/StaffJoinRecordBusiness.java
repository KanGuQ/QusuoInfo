package com.forcpacebj.api.business;

import com.alibaba.fastjson.JSONObject;
import com.forcpacebj.api.config.StaticParam;
import com.forcpacebj.api.entity.StaffJoinRecordInfo;
import com.forcpacebj.api.entity.UserInfo;
import com.forcpacebj.api.utils.PhoneCode;
import lombok.val;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * Created by pc on 2019/9/25.
 */
public class StaffJoinRecordBusiness {

    public static void insert(String accountId, Integer departmentId, List<String> userIds) {
        val sql = "INSERT INTO staff_join_record (userId,departmentId,accountId,state,code,create_time) " +
                " VALUES (:userId,:departmentId,:accountId, 0, :code, NOW())";

        try (val con = db.sql2o.beginTransaction()) {
            userIds.forEach(phoneNum -> {
                //循环发邀请码
                JSONObject jsonObject = null;
                if (departmentId != null) {//注册不发
                    jsonObject = new JSONObject();
                    jsonObject.put("code", PhoneCode.vcode());
                    jsonObject.put("enterprise", AccountBusiness.load(accountId).getAccountName());
                    PhoneCode.getPhonemsg(phoneNum, StaticParam.SIGN_NAME, StaticParam.INVITATION_CODE, jsonObject);
                }
                con.createQuery(sql)
                        .addParameter("accountId", accountId)
                        .addParameter("departmentId", departmentId)
                        .addParameter("code", jsonObject == null ? null : jsonObject.get("code").toString())
                        .addParameter("userId", phoneNum)
                        .executeUpdate();
            });
            con.commit();
        }
    }


    public static List<StaffJoinRecordInfo> getJoinRecordList(Map conditions, String accountId) {
        val sql = " SELECT u.userName,s.userId,s.accountId,s.state,s.code,s.update_time updateTime,s.create_time createTime FROM staff_join_record s" +
                " LEFT JOIN tbuser u ON u.UserId = s.userId" +
                " WHERE s.AccountId = :accountId AND s.code IS " + conditions.get("code") +
                " AND ( s.state = :state OR :state IS NULL) " +
                " AND ( s.userId = :userId OR :userId IS NULL) " +
                " ORDER BY s.create_time DESC LIMIT :PAGEOFFSET ,:PAGESIZE ";
        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .addParameter("state", conditions.get("state"))
                    .addParameter("userId", conditions.get("userId"))
                    .addParameter("PAGEOFFSET", conditions.get("PAGEOFFSET"))
                    .addParameter("PAGESIZE", conditions.get("PAGESIZE"))
                    .executeAndFetch(StaffJoinRecordInfo.class);
        }
    }

    public static Integer getJoinRecordCount(Map conditions, String accountId) {
        val sql = " SELECT COUNT(1) FROM staff_join_record s" +
                " LEFT JOIN tbuser u ON u.UserId = s.userId" +
                " WHERE s.AccountId = :accountId AND s.code IS " + conditions.get("code") +
                " AND ( s.state = :state OR :state IS NULL) " +
                " AND ( s.userId = :userId OR :userId IS NULL) ";
        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .addParameter("state", conditions.get("state"))
                    .addParameter("userId", conditions.get("userId"))
                    .executeScalar(Integer.class);
        }
    }

    public static void isAcceptJoinCompany(String accountId, Integer departmentId, String userId, Boolean isAccept) {
        String[] sql = isAccept ?
                new String[]{" UPDATE staff_join_record SET state = 1,update_time = NOW() WHERE UserId = :userId AND AccountId = :accountId AND :departmentId=:departmentId AND state = 0; ",
                        " UPDATE tbuser SET AccountId = :accountId, DepartmentId=:departmentId, RoleId = 1 WHERE UserId = :userId AND (AccountId IS NULL OR AccountId = '') ; "}
                :
                new String[]{" UPDATE staff_join_record SET state = 2,update_time = NOW() WHERE UserId = :userId AND AccountId = :accountId AND :departmentId=:departmentId AND state = 0 AND code IS NULL; "};

        try (val con = db.sql2o.beginTransaction()) {

            Arrays.asList(sql).forEach(s ->
                    con.createQuery(s)
                            .addParameter("accountId", accountId)
                            .addParameter("departmentId", departmentId)
                            .addParameter("userId", userId)
                            .executeUpdate()
            );

            con.commit();
        }
    }

    public static Boolean joinFromInviteCode(String userId, String inviteCode) {
        val sql = "UPDATE tbUser U INNER JOIN staff_join_record SJ ON SJ.userId = U.userId AND SJ.state = 0 AND SJ.userId = :userId AND SJ.code = :inviteCode " +
                " SET U.accountId = SJ.accountId, U.DepartmentId = SJ.departmentId, U.RoleId = 1 " +
                " WHERE EXISTS (SELECT accountId FROM staff_join_record WHERE state = 0 AND userId = :userId AND code = :inviteCode ORDER BY create_time LIMIT 1)";
        try (val con = db.sql2o.beginTransaction()) {
            int res = con.createQuery(sql)
                    .addParameter("userId", userId)
                    .addParameter("inviteCode", inviteCode)
                    .executeUpdate().getResult();
            if (res == 0) {
                return false;
            }
            //更新处理邀请信息
            con.createQuery("UPDATE staff_join_record SET state = 1 WHERE state = 0 AND userId = :userId AND accountId = " +
                    " (SELECT T.accountId FROM (SELECT accountId FROM staff_join_record WHERE state = 0 AND userId = :userId AND code = :inviteCode) T) ")
                    .addParameter("userId", userId)
                    .addParameter("inviteCode", inviteCode)
                    .executeUpdate();
            con.commit();
        }
        return true;
    }

    public static Boolean registerFromInviteCode(UserInfo user, String inviteCode) {
        val sql = "INSERT INTO tbuser (userId,departmentId,accountId,UserName,RoleId,Email,IsAdmin,pwd,phoneNumber) " +
                " SELECT :userId," +
                " (SELECT departmentId FROM staff_join_record WHERE state = 0 AND userId = :userId AND code = :inviteCode) ," +
                " (SELECT accountId FROM staff_join_record WHERE state = 0 AND userId = :userId AND code = :inviteCode) ," +
                " :userName ,:userRole ,:email , 0, :pwd, :userId" +
                " WHERE EXISTS (SELECT 1 FROM staff_join_record WHERE state = 0 AND userId = :userId AND code = :inviteCode ORDER BY create_time LIMIT 1)";
        try (val con = db.sql2o.beginTransaction()) {
            int res = con.createQuery(sql)
                    .bind(user)
                    .addParameter("userRole", user.getUserRole().getRoleId())
                    .addParameter("inviteCode", inviteCode)
                    .executeUpdate().getResult();
            if (res == 0) {
                return false;
            }
            //更新处理邀请信息
            con.createQuery("UPDATE staff_join_record SET state = 1 WHERE state = 0 AND userId = :userId AND accountId = " +
                    " (SELECT T.accountId FROM (SELECT accountId FROM staff_join_record WHERE state = 0 AND userId = :userId AND code = :inviteCode) T) ")
                    .addParameter("userId", user.getUserId())
                    .addParameter("inviteCode", inviteCode)
                    .executeUpdate();
            con.commit();
        }
        return true;
    }
}
