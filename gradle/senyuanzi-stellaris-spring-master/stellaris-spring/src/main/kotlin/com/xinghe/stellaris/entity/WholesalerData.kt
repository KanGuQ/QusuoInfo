package com.xinghe.stellaris.entity

import org.hibernate.annotations.Cascade
import org.hibernate.annotations.CascadeType
import javax.persistence.Entity
import javax.persistence.JoinColumn
import javax.persistence.OneToMany

@Entity
data class WholesalerData(

        var companyId: String = "",//企业ID，对应老系统的accountID
        /**
         * 上架产品 下载量
         */
        var productDownloadCount: Long = 0,
        /**
         * 上架产品 配单量
         */
        var productQuotationCount: Long = 0,

        @OneToMany
        @JoinColumn(name = "accountId", referencedColumnName = "companyId")
        @Cascade(CascadeType.ALL)
        var products: List<Product>? = listOf(),

        /**
         * 已上架产品列表
         */
        @Transient
        var publicProducts: List<Product>? = listOf()

) : BaseData()