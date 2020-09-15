package com.xinghe.stellaris.entity

import javax.persistence.*

@Entity
data class QuotationPage(
        @ManyToOne
//        @JoinColumn(name = "quotationBookId", insertable = false, updatable = false)
        var quotationBook: QuotationBook?,
//        var quotationBookId: Long,
        var sortNumber: Int,
        var quotationBillId: Long?,
        var title: String = "",
        var type: Short = 1 // 6种：0、标题 1、封面 2、企业宣传 3、合计 4、报价 5、产品详情 6、尾页 7、自定义
) : BaseData()