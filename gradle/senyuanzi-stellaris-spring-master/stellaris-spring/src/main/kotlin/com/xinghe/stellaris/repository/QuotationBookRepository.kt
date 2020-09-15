package com.xinghe.stellaris.repository

import com.xinghe.stellaris.entity.QuotationBook
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
//@RepositoryRestResource(collectionResourceRel = "quotationBook", path = "quotationBook")
interface QuotationBookRepository : JpaRepository<QuotationBook, Long> {
    fun findAllByAccountId(accountId: String): List<QuotationBook>
    fun findAllByAccountIdAndIsTemplate(accountId: String, isTemplate: Boolean): List<QuotationBook>
}