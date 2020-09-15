package com.forcpacebj.api.business;

import com.forcpacebj.api.entity.Message;
import lombok.val;
import org.sql2o.Connection;

import java.util.List;
import java.util.Map;

public class MessageBusiness {

    public static int count(Map conditions) {

        val sql = " SELECT count(1)  FROM message " +
                " WHERE (TargetAccountId = :accountId) ";

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("accountId", conditions.get("accountId"))
                    .executeScalar(int.class);
        }
    }


    public static List<Message> find(Map conditions) {
        val sql = " SELECT * " +
                " FROM message  " +
                " WHERE (TargetAccountId = :accountId) " +
                " Order By Create_Time DESC" +
                " LIMIT :PAGEOFFSET ,:PAGESIZE";


        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("accountId", conditions.get("accountId"))
                    .addParameter("PAGEOFFSET", conditions.get("PAGEOFFSET"))
                    .addParameter("PAGESIZE", conditions.get("PAGESIZE"))
                    .executeAndFetch(Message.class);
        }
    }

    public static void createMessage(Connection connection, Message message) {
        val sql2 = " insert into message " +
                " (targetAccountId,targetUserID,originAccountId,originUserId,type,message) " +
                " values " +
                " (:targetAccountId,:targetUserID,:originAccountId,:originUserId,:type,:message) ";


        try {
            connection.createQuery(sql2)
                    .addParameter("targetAccountId", message.getTargetAccountId())
                    .addParameter("targetUserID", message.getTargetUserId())
                    .addParameter("originAccountId", message.getOriginAccountId())
                    .addParameter("originUserId", message.getOriginUserId())
                    .addParameter("type", message.getType())
                    .addParameter("message", message.getMessage())
                    .executeUpdate();
        } catch (Exception e) {

        }
    }

}
