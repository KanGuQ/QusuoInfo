package com.forcpacebj.api.business;

import com.forcpacebj.api.entity.ProvinceInfo;
import lombok.val;

import java.util.List;

public class ProvinceBusiness {

    public static List<ProvinceInfo> list() {

        try (val con = db.sql2o.open()) {
            return con.createQuery(" SELECT ProvinceId,ProvinceName FROM tbProvince ")
                    .executeAndFetch(ProvinceInfo.class);
        }
    }
}