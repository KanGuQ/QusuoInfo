package com.xinghe.stellaris.repository

import com.xinghe.stellaris.entity.WholesalerData
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface WholesalerDataRepository : JpaRepository<WholesalerData, String> {

    fun findByCompanyId(companyId: String): WholesalerData?
}