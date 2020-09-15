package com.forcpacebj.api.business;

import com.forcpacebj.api.config.StaticParam;
import com.forcpacebj.api.core.ResponseWrapper;
import com.forcpacebj.api.entity.*;
import com.forcpacebj.api.utils.CollectionUtil;
import io.goeasy.GoEasy;
import lombok.extern.log4j.Log4j;
import lombok.val;
import org.apache.commons.codec.binary.Base64;
import org.sql2o.Query;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by 树 on 2020/4/26.
 */
@Log4j
public class GoEasyMessageBusiness {

    public static ResponseWrapper find(Map conditions) {
        val list_sql = "SELECT Id,Content,IsRead,CreateTime" +
                " FROM GoEasyMessage WHERE Channel = :channel ORDER BY CreateTime DESC" +
                " LIMIT :PAGEOFFSET ,:PAGESIZE ";

        val count_sql = "SELECT COUNT(1)" +
                " FROM GoEasyMessage WHERE Channel = :channel";

        try (val con = db.sql2o.open()) {
            List<GoEasyMessageInfo> list = con.createQuery(list_sql)
                    .addParameter("channel", conditions.get("channel"))
                    .addParameter("PAGEOFFSET", conditions.get("PAGEOFFSET"))
                    .addParameter("PAGESIZE", conditions.get("PAGESIZE"))
                    .executeAndFetch(GoEasyMessageInfo.class);
            Integer msgCount = con.createQuery(count_sql)
                    .addParameter("channel", conditions.get("channel"))
                    .executeScalar(Integer.class);
            return ResponseWrapper.page(msgCount, list);
        }
    }

    public static void insert(GoEasyMessageInfo msg) {
        val sql = "INSERT INTO GoEasyMessage (Channel,Content,IsPush,IsRead)" +
                " VALUES (:channel,:content,1,0) ";
        try (val con = db.sql2o.open()) {
            con.createQuery(sql).bind(msg).executeUpdate();
        }
    }

    public static void insert(List<GoEasyMessageInfo> msgs) {
        val sql = "INSERT INTO GoEasyMessage (Channel,Content,IsPush,IsRead)" +
                " VALUES (:channel,:content,1,0) ";
        try (val con = db.sql2o.beginTransaction()) {
            Query query = con.createQuery(sql);
            for (GoEasyMessageInfo geMsg : msgs) {
                query.bind(geMsg).addToBatch();
            }
            query.executeBatch();
            con.commit();
        }
    }

    public static void alreadyRead(Integer id) {
        val sql = "UPDATE GoEasyMessage SET IsRead=1" +
                " WHERE Id = :id ";
        try (val con = db.sql2o.open()) {
            con.createQuery(sql).addParameter("id", id).executeUpdate();
        }
    }

    public static void alreadyRead(String channel) {
        val sql = "UPDATE GoEasyMessage SET IsRead=1" +
                " WHERE Channel = :channel ";
        try (val con = db.sql2o.open()) {
            con.createQuery(sql).addParameter("channel", channel).executeUpdate();
        }
    }

    public static void offLineTimeOut() {
        val sql = "UPDATE GoEasyMessage GE INNER JOIN tbUser U ON GE.Channel = U.UserId " +
                "SET GE.IsPush=0 WHERE TIMESTAMPDIFF(HOUR,U.LastAccessed,NOW()) >= 24";
        try (val con = db.sql2o.open()) {
            log.info("---处理超时离线消息---");
            con.createQuery(sql).executeUpdate();
        }
    }

    public static void pushOffLineTimeOutMessage(String userId) {
        val sql = "SELECT GE.Id,GE.Content,GE.IsRead,GE.CreateTime FROM GoEasyMessage GE " +
                " INNER JOIN tbUser U ON GE.Channel = U.UserId " +
                " WHERE GE.IsPush = 0 AND Channel = :channel";
        try (val con = db.sql2o.beginTransaction()) {
            val list = con.createQuery(sql)
                    .addParameter("channel", userId)
                    .executeAndFetch(GoEasyMessageInfo.class);

            if (CollectionUtil.isEmpty(list)) return;
            publishAgain(list);

            con.createQuery("UPDATE GoEasyMessage SET IsPush=1 WHERE Channel=:channel AND IsPush=0")
                    .addParameter("channel", userId)
                    .executeUpdate();
            con.commit();
        }
    }


    public static void publish(List<String> channels, String content) {
        GoEasy goEasy = new GoEasy(StaticParam.GOEASY_REGION_HOST, StaticParam.GOEASY_COMMON_KEY);
        List<GoEasyMessageInfo> ge = new ArrayList<>();
        channels.forEach(channel -> {
            if (channel == null) return;
            goEasy.publish(channel, content);
            ge.add(new GoEasyMessageInfo(channel, content, true));
        });
        GoEasyMessageBusiness.insert(ge);
    }

    public static void publish(String channel, String content) {
        if (channel == null) return;
        GoEasy goEasy = new GoEasy(StaticParam.GOEASY_REGION_HOST, StaticParam.GOEASY_COMMON_KEY);
        goEasy.publish(channel, content);
        GoEasyMessageBusiness.insert(new GoEasyMessageInfo(channel, content, true));
    }

    public static void publishAgain(List<GoEasyMessageInfo> list) {
        GoEasy goEasy = new GoEasy(StaticParam.GOEASY_REGION_HOST, StaticParam.GOEASY_COMMON_KEY);
        for (GoEasyMessageInfo msg : list) {
            goEasy.publish(msg.getChannel(), msg.getContent());
        }
    }

    public static String goEasyOTP(String secretKey) {
        try {
            String otp = "000" + System.currentTimeMillis();
            SecretKeySpec key = new SecretKeySpec(secretKey.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            byte[] otpBytes = otp.getBytes();
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encryptedOTP = cipher.doFinal(otpBytes);
            otp = new Base64().encodeToString(encryptedOTP);
            return otp;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate GoEasy-OTP.", e);
        }
    }

}
