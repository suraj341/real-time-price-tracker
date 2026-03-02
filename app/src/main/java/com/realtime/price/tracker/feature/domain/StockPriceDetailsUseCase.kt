package com.realtime.price.tracker.feature.domain

import com.realtime.price.tracker.feature.data.StockPriceDetailsRepository

class StockPriceDetailsUseCase(private val stockPriceRepository: StockPriceDetailsRepository) {
    fun observeStockPriceDetails() = stockPriceRepository.observeStockPriceDetails()

    fun observeStockPriceDetails(symbol: String) = stockPriceRepository.observeStockPriceDetails()
}