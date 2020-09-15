package com.xinghe.stellaris.service

import com.xinghe.stellaris.entity.WholesalerData
import com.xinghe.stellaris.repository.ProductRepository
import com.xinghe.stellaris.repository.QuotationBillProductRepository
import com.xinghe.stellaris.repository.WholesalerDataRepository
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class WholesalerDataService(val repository: WholesalerDataRepository, val productRepository: ProductRepository,
                            val quotationBillProductRepository: QuotationBillProductRepository) {

    fun findAll(): MutableList<WholesalerData> {
        return repository.findAll()
    }


    fun findData(companyId: String): WholesalerData? {
        val data = repository.findByCompanyId(companyId)
        data?.apply {
            products = null
            publicProducts = productRepository.findAllByAccountIdAndStatusIs(companyId, 5)
            publicProducts?.forEach {
                it.downloadCount = productRepository.countByOriginProductId(it.productId.toString()) - 1
                it.quotationCount = quotationBillProductRepository.countDistinctByProductId(it.productId.toString())
            }
        }

        return data
    }

    @Transactional
    fun initData(): Unit {
        val data = WholesalerData()
        data.companyId = "1230483527246544896"
        data.products = null
//        data.publicProducts = null
//        data.publicProducts = productRepository.findAllByAccountIdAndStatusIs(data.companyId, 5)
//        data.publicProducts.forEach {
//            it.downloadCount = productRepository.countByOriginProductId(it.productId.toString()) - 1
//            it.quotationCount = quotationBillProductRepository.countDistinctByProductId(it.productId.toString())
//        }

        repository.save(data)
    }
}