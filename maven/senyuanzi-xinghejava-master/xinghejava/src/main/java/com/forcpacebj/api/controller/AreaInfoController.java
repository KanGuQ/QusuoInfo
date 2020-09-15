package com.forcpacebj.api.controller;

import com.forcpacebj.api.business.AreaInfoBusiness;
import com.forcpacebj.api.business.CatalogBusiness;
import com.forcpacebj.api.entity.AreaInfo;
import com.forcpacebj.api.utils.JSONUtil;
import lombok.val;
import spark.Route;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link AreaInfoController}
 * Author: ACL
 * Date:2020/02/26
 * Description:
 * Created by ACL on 2020/02/26.
 */
public class AreaInfoController extends BaseController {


    public static Route findAll = (request, response) -> {

        val conditions = JSONUtil.toMap( request.body() );

        val list = AreaInfoBusiness.findAll( conditions );
        return toJson( list );
    };

    /**
     * 获取所有的设备信息
     */
    public static Route findList = (request, response) -> {

        val conditions = JSONUtil.toMap( request.body() );

        val list = AreaInfoBusiness.findList( conditions );
        Map<String, String> map = new HashMap<>();
        list.forEach( e -> {
            map.put( e.getId().toString(), e.getName() );
        } );
        return toJson( map );
    };
    //根据街道id 查询城市 省市区id
    public static Route findByAdCode = (request, response) -> {
        Map<String, Object> map = new HashMap<>();
        String adCode = request.queryParams( "adCode" );
        AreaInfo areaInfo = AreaInfoBusiness.findByAdCode( adCode );// 城市id
        if (areaInfo.getLevel_type() == 3) { //说明是城市
            map.put( "cityId", areaInfo.getParent_id().toString() );
            AreaInfo a = AreaInfoBusiness.findByAdCode( areaInfo.getParent_id().toString() );
            if (a.getLevel_type() == 2) {
                map.put( "provinceId", a.getParent_id().toString() );
            }
            map.put( "countyId", adCode );
        } else if (areaInfo.getLevel_type() == 2) { //说明是省份
            map.put( "provinceId", areaInfo.getParent_id().toString() );
            map.put( "cityId", adCode.toString() );
            map.put( "countyId", "0" );
        }
//        Integer provinceId = AreaInfoBusiness.findByAdCode( cityId.toString() );//省份id
//        map.put( "provinceId", provinceId );
//        map.put( "cityId", cityId );
        return toJson( map );
    };
}
