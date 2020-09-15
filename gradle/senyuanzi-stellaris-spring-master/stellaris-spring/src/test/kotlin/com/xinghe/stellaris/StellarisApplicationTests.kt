package com.xinghe.stellaris

import com.xinghe.stellaris.service.WholesalerDataService
import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest
class StellarisApplicationTests() {
    @Autowired
    lateinit var wholesalerDataService: WholesalerDataService

    @Test
    fun contextLoads() {
    }


    @Test
    fun initWholesalerData(): Unit {
        wholesalerDataService.initData()
    }

}
