package com.forcpacebj.api;

import com.forcpacebj.api.business.*;
import com.forcpacebj.api.config.CorsConfig;
import com.forcpacebj.api.config.RouteConfig;
import com.forcpacebj.api.entity.ProductInfo;
import com.forcpacebj.api.newModule.business.NewProductBusiness;
import com.forcpacebj.api.utils.JedisUtil;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static spark.Spark.port;

@Slf4j
public class Program {

    private static int port = 23008;
    public static int redisDB = 1, redisPreset = redisDB;
    public static String mysqlDB = "stellaris";
//    public static String mysqlDB = "stellaris-prod";
    public static String fileUploadDirectory = "D:\\IdeaProjects\\forcpace2\\forcpace-bj-api\\upload\\";
    public static String templateServer = "http://www.xinghecrm.com/template-server/generate/";
    public static String host = "http://47.103.58.94:";

    public static void main(String[] args) {

        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
            redisDB = Integer.parseInt(args[1]);
            mysqlDB = args[2];
            fileUploadDirectory = args[3];
        }
        host += port;
        log.info("{port:" + port +
                ",redisDB:" + redisDB +
                ",fileUploadDirectory:" + fileUploadDirectory +
                ",mysqlDB:" + mysqlDB + "}");

        port(port);

        RouteConfig.config();

        CorsConfig.enableCORS();

        JedisUtil.init("47.103.58.94", 63790, redisDB);

        startJob();
        RefreshOffLineTimeOutMessage();
    }

    private static void startJob() {
        ScheduledExecutorService service = Executors.newScheduledThreadPool(10);

        long oneDay = 24 * 60 * 60 * 1000;
        long initDelay = getTimeMillis("2:00:00") - System.currentTimeMillis();
        initDelay = initDelay > 0 ? initDelay : oneDay + initDelay;

        service.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                //遍历状态9和10的产品，到更新时间了，就更新旧产品

                syncStatus9Product();
                syncStatus10Product();

                timedDelete();
                AccountBusiness.modifyIsPaid();
            }

        }, initDelay, oneDay, TimeUnit.MILLISECONDS);
    }

    private static void RefreshOffLineTimeOutMessage(){
        ScheduledExecutorService service = Executors.newScheduledThreadPool(1);

        service.scheduleAtFixedRate(GoEasyMessageBusiness::offLineTimeOut, 0, 30, TimeUnit.MINUTES);
    }

    /**
     * 同步更新 任务
     */
    private static void syncStatus9Product() {
        val condition1 = new HashMap<String, Object>();
        condition1.put("status", 9);
        int count9 = NewProductBusiness.findProductCountByCondition(condition1);
        int allPage = count9 / 100 + 1;
        for (int i = 1; i <= allPage; i++) {
            val condition = new HashMap<String, Object>();
            condition.put("status", 9);
            condition.put("PAGEOFFSET", (i - 1) * 100);
            condition.put("PAGESIZE", 100);

            List<ProductInfo> list = NewProductBusiness.findProductByCondition(condition);
            list.forEach(productInfo -> {
                long diff = new Date().getTime() - productInfo.getCreateTime().getTime();
                long day = diff / (1000 * 60 * 60 * 24);
                if (day >= 10) {//大于等于10天，触发同步更新
                    ProductInfo product = ProductBusiness.load(productInfo.getProductId());
                    product.setProductId(productInfo.getOriginProductId());
                    product.setStatus(5);//上架中
                    ProductBusiness.update(productInfo.getAccountId(), product);//临时产品覆盖原产品
                    ProductBusiness.delete(productInfo.getAccountId(), productInfo.getProductId());
                }
            });
        }
    }

    /**
     * 同步下架 任务
     */
    private static void syncStatus10Product() {
        val condition1 = new HashMap<String, Object>();
        condition1.put("status", 10);
        int count10 = NewProductBusiness.findProductCountByCondition(condition1);
        int allPage = count10 / 100 + 1;
        for (int i = 1; i <= allPage; i++) {
            val condition = new HashMap<String, Object>();
            condition.put("status", 10);
            condition.put("PAGEOFFSET", (i - 1) * 100);
            condition.put("PAGESIZE", 100);

            List<ProductInfo> list = NewProductBusiness.findProductByCondition(condition);
            list.forEach(productInfo -> {
                long diff = new Date().getTime() - productInfo.getCreateTime().getTime();
                long day = diff / (1000 * 60 * 60 * 24);
                if (day >= 10) {//大于等于10天，触发同步更新
                    ProductInfo product = ProductBusiness.load(productInfo.getOriginProductId());//获取原产品
                    product.setStatus(2);//待上架
                    ProductBusiness.update(productInfo.getAccountId(), product);//更新原产品
                    ProductBusiness.delete(productInfo.getAccountId(), productInfo.getProductId());//删除临时更新产品
                    // 同步删除零售商无配单产品，复制有配单产品

                }
            });
        }
    }

    private static void timedDelete() {
        ProductBusiness.getToDelList().forEach(p -> ProductBusiness.delete(p.getAccountId(), p.getProductId()));
        ProjectBusiness.getToDelList().forEach(p -> ProjectBusiness.delete(p.getAccountId(), p.getProjectId()));
        PeopleBusiness.getToDelList().forEach(p -> PeopleBusiness.delete(p.getAccountId(), p.getPeopleId()));
    }

    /**
     * 获取指定时间对应的毫秒数
     *
     * @param time "HH:mm:ss"
     * @return
     */
    private static long getTimeMillis(String time) {
        try {
            DateFormat dateFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
            DateFormat dayFormat = new SimpleDateFormat("yy-MM-dd");
            Date curDate = dateFormat.parse(dayFormat.format(new Date()) + " " + time);
            return curDate.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }
}

