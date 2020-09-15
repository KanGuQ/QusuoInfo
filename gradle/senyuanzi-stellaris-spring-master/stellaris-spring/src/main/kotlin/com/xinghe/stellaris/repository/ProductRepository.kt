package com.xinghe.stellaris.repository

import com.xinghe.stellaris.entity.Product
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Service

@Service
interface ProductRepository : JpaRepository<Product, Int> {
    fun findAllByAccountId(accountId: String): MutableList<Product>
    fun deleteByProductId(productId: Long): MutableList<Product>

    fun findAllByAccountIdAndStatusIs(accountId: String, status:Int): MutableList<Product>

    fun countByOriginAccountIdAndAccountIdNot(accountId: String, accountId2: String): Long
    fun countByOriginProductId(productId: String): Long

}