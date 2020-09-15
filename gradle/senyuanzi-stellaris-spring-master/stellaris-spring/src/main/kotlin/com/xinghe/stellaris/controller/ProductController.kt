package com.xinghe.stellaris.controller

import com.xinghe.stellaris.entity.Product
import com.xinghe.stellaris.http.ResponseWrapper
import com.xinghe.stellaris.service.ProductService
import org.springframework.web.bind.annotation.*

@CrossOrigin
@RestController
@RequestMapping("api/v2/Product/")
class ProductController(val service: ProductService) {

    @GetMapping("findAll")
    fun findAllByAccountId(@RequestParam accountId: String): ResponseWrapper<List<Product>> {
        return ResponseWrapper.ok(service.findAllByAccountId(accountId))
    }

    @GetMapping("count")
    fun countByOriginAccountId(@RequestParam accountId: String): ResponseWrapper<Long> {
        return ResponseWrapper.ok(service.countByOriginAccountId(accountId))
    }

    @PostMapping("save")
    fun save(@RequestBody product: Product): ResponseWrapper<Boolean> {
        service.save(product)
        return ResponseWrapper.ok(true)
    }


}