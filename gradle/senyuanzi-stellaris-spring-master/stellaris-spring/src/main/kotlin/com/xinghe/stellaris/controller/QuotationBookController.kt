package com.xinghe.stellaris.controller

import com.xinghe.stellaris.dto.QuotationBookDTO
import com.xinghe.stellaris.entity.QuotationBook
import com.xinghe.stellaris.http.ResponseWrapper
import com.xinghe.stellaris.service.QuotationBookService
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.web.bind.annotation.*

@CrossOrigin
@RestController
@RequestMapping("api/v2/QuotationBook/")
class QuotationBookController(val service: QuotationBookService) {

    val log: Log = LogFactory.getLog(this.javaClass)

    @GetMapping("findAll")
    fun findAllTemplatesByAccountId(@RequestParam accountId: String): ResponseWrapper<List<QuotationBook>> {
        return ResponseWrapper.ok(service.findAllTemplatesByAccountId(accountId))
    }

    @PostMapping("save")
    fun save(@RequestBody quotationBook: QuotationBook): ResponseWrapper<Boolean> {
        service.save(quotationBook)
        return ResponseWrapper.ok(true)
    }

    @PostMapping("delete")
    fun deleteById(@RequestBody quotationBookDTO: QuotationBookDTO): ResponseWrapper<Boolean> {
        service.deleteById(quotationBookDTO.quotationBookId)
        return ResponseWrapper.ok(true)
    }

    @GetMapping("findById")
    fun findById(@RequestParam quotationBookId: Long): ResponseWrapper<QuotationBook> {
        val book = service.findById(quotationBookId)
        return if (book != null) ResponseWrapper.ok(book) else ResponseWrapper.notFound()
    }
}