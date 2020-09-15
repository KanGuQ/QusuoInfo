package com.forcpacebj.api.utils;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.forcpacebj.api.business.ProductBusiness;
import com.forcpacebj.api.config.StaticParam;
import com.forcpacebj.api.entity.ProductInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by pc on 2019/12/27.
 */
public class ProductInfoListener extends AnalysisEventListener<ProductInfo> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductInfoListener.class);
    private static final int BATCH_COUNT = 3000;
    private List<ProductInfo> list = new ArrayList<ProductInfo>();
    private static String accountId;

    public static void setAccountId(String accountId) {
        ProductInfoListener.accountId = accountId;
    }

    @Override
    public void invoke(ProductInfo data, AnalysisContext context) {
        if (data.getShortName() != null && !"产品型号".equals(data.getShortName().trim())) {
//        if (data.getShortName() != null) {
            if (data.getGuidePrice() == null) data.setGuidePrice(new BigDecimal(0));
            if (data.getStatus() != null && data.getStatus() == StaticParam.PASSED && !"xh".equals(accountId)) data.setStatus(null);
            list.add(data);
        }

        if (list.size() >= BATCH_COUNT) {
            saveData();
            list.clear();
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        saveData();
        LOGGER.info("所有数据解析完成！");
    }

    private void saveData() {
        LOGGER.info("{}条数据，开始存储数据库！", list.size());
        Collections.reverse(list);
        ProductBusiness.importExcel(accountId, list);
        LOGGER.info("存储数据库成功！");
    }



}
