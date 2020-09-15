package com.xinghe.stellaris.entity

import org.hibernate.annotations.Cascade
import org.hibernate.annotations.CascadeType
import org.hibernate.annotations.Where
import javax.persistence.*

@Entity
//@Where(clause = "delete_time is null")
data class QuotationBook(
        var accountId: String,
        var sortNumber: Int = 0,
        var quotationBillId: Long?,
        var title: String,
        var isTemplate: Boolean = false,//是否是报价书模板
        var creatorId: String,//创建者UserId


        //标题页logo
        var topLogo: String = "",
        var bottomLogo: String = "",


        @OneToMany
        @JoinColumn(name = "quotation_book_id")
        @Cascade(CascadeType.ALL)
//        @JoinColumn(name = "quotationBookId")
        var pages: List<QuotationPage> = listOf(),

        @Lob var coverHtml: String? = "",//封面内容
        @Lob var endHtml: String? = "",//尾页内容
        @Lob var companyIntroHtml: String? = "",//企业宣传内容

        //报价书模板配置
        var hideDesignPrice: Boolean = true,
        var hideProjectPrice: Boolean = true,
        var hideInstallationPrice: Boolean = true,
        var hideMediaPrice: Boolean = true,
        var hideSoftwarePrice: Boolean = true,
        var hideRemotePrice: Boolean = true,

        var hideProductPrice: Boolean = true,

        var orientation: Short = 0//0竖版，1横版

) : BaseData()