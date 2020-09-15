package com.xinghe.stellaris.service

import com.xinghe.stellaris.entity.Product
import com.xinghe.stellaris.repository.ProductRepository
import org.springframework.stereotype.Service

@Service
class ProductService(val repository: ProductRepository) {

    fun findAllByAccountId(accountId: String): MutableList<Product> {
        return repository.findAllByAccountId(accountId)
    }

    fun save(product: Product) {
        repository.save(product)
    }

    fun deleteById(product: Product) {
        repository.deleteByProductId(product.productId)
    }

    fun countByOriginAccountId(accountId: String): Long {
        return repository.countByOriginAccountIdAndAccountIdNot(accountId, accountId)
    }
}