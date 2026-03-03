package com.realtime.price.tracker.feature.domain

import com.realtime.price.tracker.feature.data.StockDetailsResult
import com.realtime.price.tracker.feature.data.StockPriceDetailsRepository
import com.realtime.price.tracker.feature.data.dto.StockDetailModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest

class StockPriceDetailsUseCase(private val stockPriceRepository: StockPriceDetailsRepository) {
    val isConnected: StateFlow<Boolean> = stockPriceRepository.isConnected

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeStockPriceDetails(): Flow<StockDetailsResult> {
        return stockPriceRepository.observeStockPriceDetails()
            .mapLatest { result ->
                (if (result is StockDetailsResult.Success) {
                    result.copy(stocks = result.stocks.sortedByDescending { it.price })
                } else {
                    result
                })
            }.flowOn(Dispatchers.Default)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeStockPriceDetails(symbol: String): Flow<StockDetailModel?> {
        return stockPriceRepository.observeStockPriceDetails().mapLatest { result ->
            if (result is StockDetailsResult.Success) {
                result.stocks.find { it.symbol == symbol }
            } else {
                null
            }
        }.flowOn(Dispatchers.Default)
    }

    fun disconnect() {
        stockPriceRepository.disconnect()
    }
}