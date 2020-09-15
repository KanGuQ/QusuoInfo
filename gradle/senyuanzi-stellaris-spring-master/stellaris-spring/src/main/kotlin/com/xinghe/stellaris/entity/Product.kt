package com.xinghe.stellaris.entity

import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "tbproduct", schema = "stellaris")
data class Product(@Id var productId: Long = 0) {
    var accountId: String? = null
    var id: String? = null
    var productName: String? = null
    var shortName: String? = null
    var productCategoryId: String? = null
    var publicCategoryId: String? = null
    var catalogId: String? = null
    var brand: String? = null
    var productArea: String? = null
    var color: String? = null
    var guidePrice: java.math.BigDecimal? = null
    var purchPrice: java.math.BigDecimal? = null
    var salePrice: java.math.BigDecimal? = null
    var unit: String? = null
    var role: String? = null
    var remark: String? = null
    var brief: String? = null
    var feature: String? = null
    var productDetail: String? = null
    var productDetail2: String? = null
    var isShow: Boolean? = null
    var sortNumber: Int? = null
    var pictureUrl: String? = null
    var pictureScale: Int? = null
    var productType: String? = null
    var minDistance: java.math.BigDecimal? = null
    var maxDistance: java.math.BigDecimal? = null
    var curtainSize: java.math.BigDecimal? = null
    var curtainProportion: String? = null
    var isSharedProduct: Boolean? = null
    var fromAccountId: String? = null
    var fromProductId: String? = null
    var originAccountId: String? = null
    var originProductId: String? = null
    var state: Boolean? = null
    var updateTime: java.sql.Timestamp? = null
    var createTime: java.sql.Timestamp? = null
    var status: Int? = null
    var isDelete: Boolean? = null
    var deleteTime: java.sql.Timestamp? = null


    // constant value returned to avoid entity inequality to itself before and after it's update/merge
//    override fun hashCode(): Int = 42

    @Transient
    var downloadCount: Long = 0//下载量
    @Transient
    var quotationCount: Long = 0//配单量


}

