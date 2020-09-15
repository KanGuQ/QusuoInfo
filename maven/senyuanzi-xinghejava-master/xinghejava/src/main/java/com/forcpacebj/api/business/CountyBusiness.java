package com.forcpacebj.api.business;

import com.forcpacebj.api.business.db;
import com.forcpacebj.api.entity.AccountInfo;
import com.forcpacebj.api.entity.County;
import lombok.val;
import lombok.var;

import java.util.List;
import java.util.Map;

/**
 * {@link CountyBusiness}
 * Author: ACL
 * Date:2020/02/18
 * Description:
 * Created by ACL on 2020/02/18.
 */
public class CountyBusiness {

    /**
     * 根据城市id 查询城市信息
     * @param conditions
     * @param accountId
     * @return
     */
    public static List<County> list(Map<String, Object> conditions, String accountId) {
        var sql="select countyId,countyName from tbCounty where cityId=:cityId or :cityId is null";

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("cityId", conditions.get( "cityId" ))
                    .executeAndFetch(County.class);
        }
        }

}
