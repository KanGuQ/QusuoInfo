package com.xinghe.stellaris.repository

import com.xinghe.stellaris.entity.QuotationBillProduct
import com.xinghe.stellaris.entity.QuotationBillProductEntityPK
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface QuotationBillProductRepository : JpaRepository<QuotationBillProduct, QuotationBillProductEntityPK> {

    fun countDistinctByProductId(productId:String): Long
}