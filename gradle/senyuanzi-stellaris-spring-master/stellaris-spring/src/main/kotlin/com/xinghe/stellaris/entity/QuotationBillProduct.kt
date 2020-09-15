package com.xinghe.stellaris.entity

import javax.persistence.*

@Entity
@Table(name = "tbquotationbillproduct", schema = "stellaris")
@IdClass(QuotationBillProductEntityPK::class)
data class QuotationBillProduct(@Id var accountId: String) {
    @Id
    var billId: String? = null

    @Id
    var sheetId: Int? = null

    @Id
    var sectionId: Int? = null

    @Id
    var sortNumber: Int? = null

    @Id
    var productId: String? = null
    var productName: String? = null
    var role: String? = null
    var salesPrice: java.math.BigDecimal? = null
    var quantity: java.math.BigDecimal? = null
    var unit: String? = null
    var purchCost: java.math.BigDecimal? = null
    var salesAmount: java.math.BigDecimal? = null
    var isManual: Boolean? = null
}

class QuotationBillProductEntityPK : java.io.Serializable {
    var accountId: String? = null
    var billId: String? = null
    var sheetId: Int? = null
    var sectionId: Int? = null
    var sortNumber: Int? = null
    var productId: String? = null
}
