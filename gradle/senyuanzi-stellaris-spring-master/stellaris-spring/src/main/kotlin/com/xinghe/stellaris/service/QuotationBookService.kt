package com.xinghe.stellaris.service

import com.xinghe.stellaris.entity.QuotationBook
import com.xinghe.stellaris.repository.QuotationBookRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class QuotationBookService(val repository: QuotationBookRepository) {


    fun findAll(): List<QuotationBook> {
        return repository.findAll()
    }

    fun findAllByAccountId(accountId: String): List<QuotationBook> {
        return repository.findAllByAccountId(accountId)
    }

    fun findAllTemplatesByAccountId(accountId: String): List<QuotationBook> {
        return repository.findAllByAccountIdAndIsTemplate(accountId, true).apply {
            map { it.pages.forEach { page -> page.quotationBook = null } }
        }
    }

    fun findById(quotationBookId: Long): QuotationBook? {
        return repository.findById(quotationBookId).apply {
            ifPresent { it.pages.forEach { page -> page.quotationBook = null } }
        }.orElse(null)
    }

    fun save(quotationBook: QuotationBook) {
        quotationBook.pages.forEach { it.quotationBook = quotationBook }
        repository.save(quotationBook)
    }

    fun deleteById(quotationBookId: Long) {
        repository.deleteById(quotationBookId)
    }
}