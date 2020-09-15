package com.forcpacebj.api.business;

import com.forcpacebj.api.entity.CityInfo;
import lombok.val;

import java.util.List;
import java.util.Map;

public class CityBusiness {

    public static List<CityInfo> find(Map conditions) {

        val sql = " Select CityId,CityName from tbCity  " +
                " WHERE (ProvinceID = :provinceId OR :provinceId is null) ";

        try (val con = db.sql2o.open()) {
            return con.createQuery(sql)
                    .addParameter("provinceId", conditions.get("provinceId"))
                    .executeAndFetch(CityInfo.class);
        }
    }
}