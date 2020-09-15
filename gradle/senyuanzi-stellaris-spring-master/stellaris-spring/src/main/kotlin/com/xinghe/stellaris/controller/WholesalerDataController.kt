package com.xinghe.stellaris.controller

import com.xinghe.stellaris.entity.QuotationBook
import com.xinghe.stellaris.entity.WholesalerData
import com.xinghe.stellaris.http.ResponseWrapper
import com.xinghe.stellaris.service.WholesalerDataService
import org.springframework.web.bind.annotation.*

@CrossOrigin
@RestController
@RequestMapping("api/v2/WholesalerData/")
class WholesalerDataController(val service: WholesalerDataService) {


    @GetMapping("findAll")
    fun findAll(): ResponseWrapper<List<WholesalerData>> {
        return ResponseWrapper.ok(service.findAll())
    }

    @GetMapping("findData")
    fun findData(@RequestParam companyId: String): ResponseWrapper<WholesalerData?> {
        return ResponseWrapper.ok(service.findData(companyId))
    }

}