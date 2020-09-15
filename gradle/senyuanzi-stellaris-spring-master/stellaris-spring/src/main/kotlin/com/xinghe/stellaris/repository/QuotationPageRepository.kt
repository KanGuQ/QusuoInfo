package com.xinghe.stellaris.repository

import com.xinghe.stellaris.entity.QuotationPage
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface QuotationPageRepository : JpaRepository<QuotationPage, Long> {
}