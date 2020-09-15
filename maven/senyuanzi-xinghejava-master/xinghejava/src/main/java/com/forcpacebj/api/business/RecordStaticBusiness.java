package com.forcpacebj.api.business;

import com.forcpacebj.api.entity.RecordStaticInfo;
import com.forcpacebj.api.entity.UserInfo;
import lombok.val;

import java.util.List;
import java.util.Map;

/**
 * 日志统计业务逻辑
 */
public class RecordStaticBusiness {

    public static List<UserInfo> recordStaticByUser(Map conditions, String accountId) {

        val sql = " select U.AccountId ,U.UserId ,U.UserName ,U.Email ,U.UserRole 'userRoleEnum',U.IsAdmin ,U.PhoneNumber , " +
                "       COALESCE(R.RecordCount,0) RecordCount , COALESCE(R.RecordDateCount,0) RecordDateCount from tbUser U " +
                "   left join (" +
                "           select AccountId ,UserId , Count(1) RecordCount , Count(Distinct RecordDate) RecordDateCount" +
                "           from tbRecord  " +
                "           where AccountId= :accountId And (DATE_FORMAT(RecordDate,'%Y-%m')= :queryDate OR :queryDate is null) " +
                "           group by AccountId ,UserId " +
                " )R on U.AccountId=R.AccountId And U.UserId=R.UserId " +
                " Where U.UserId not like 'yabbaddabado%' " +
                "   AND U.AccountId= :accountId " +
                " order by R.RecordCount Desc";

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("accountId", accountId)
                    .addParameter("queryDate", conditions.get("queryDate"))
                    .executeAndFetch(UserInfo.class);
        }
    }

    public static List<RecordStaticInfo> recordDayStatic(Map conditions, String acountId) {
        val sql = " select cast(DAY(RecordDate) as SIGNED) RecordDay ,count(1) TotalCount " +
                "     from tbRecord " +
                "    where AccountId= :accountId" +
                "      and (UserId= :userId Or :userId is null)" +
                "      and (DATE_FORMAT(RecordDate,'%Y-%m')= :month OR :month is null)" +
                "    GROUP BY RecordDate " +
                "    ORDER BY RecordDate";

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("accountId", acountId)
                    .addParameter("userId", conditions.get("userId"))
                    .addParameter("month", conditions.get("month"))
                    .executeAndFetch(RecordStaticInfo.class);
        }
    }
}