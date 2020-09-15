package com.xinghe.stellaris.entity

import org.hibernate.annotations.Cascade
import org.hibernate.annotations.CascadeType
import javax.persistence.Entity
import javax.persistence.JoinColumn
import javax.persistence.OneToMany

//@Entity
data class ProductData(

        var companyId: String = "",
        /**
         * 上架产品 下载量
         */
        var downLoadCount: Long = 0,
        /**
         * 上架产品 配单量
         */
        var quotationCount: Long = 0,

        @OneToMany
        @JoinColumn(name = "accountId",referencedColumnName = "companyId")
        @Cascade(CascadeType.ALL)
        var products: List<Product> = listOf()

) : BaseData()