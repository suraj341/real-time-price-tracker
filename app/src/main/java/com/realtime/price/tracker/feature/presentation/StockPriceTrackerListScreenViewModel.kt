package com.realtime.price.tracker.feature.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.realtime.price.tracker.feature.data.StockDetailsResult
import com.realtime.price.tracker.feature.data.dto.StockDetailModel
import com.realtime.price.tracker.feature.domain.StockPriceDetailsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class PriceChange {
    UP, DOWN, NONE
}

data class StockItemUiModel(
    val stock: StockDetailModel,
    val priceChange: PriceChange
)

data class StockPriceTrackerUiState(
    val stocks: List<StockItemUiModel> = emptyList(),
    val isLoading: Boolean = true
)

class StockPriceTrackerListScreenViewModel(private val stockPriceDetailsUseCase: StockPriceDetailsUseCase) :
    ViewModel() {
    private val _state = MutableStateFlow(StockPriceTrackerUiState())
    val state = _state.asStateFlow()

    private var previousPrices: Map<String, Double> = emptyMap()

    init {
        viewModelScope.launch {
            stockPriceDetailsUseCase.observeStockPriceDetails()
                .collect { result ->
                    if (result is StockDetailsResult.Success) {
                        val currentPrices = result.stocks.associate { it.symbol to it.price }
                        
                        val uiModels = result.stocks.map { stock ->
                            val previousPrice = previousPrices[stock.symbol]
                            val change = when {
                                previousPrice == null -> PriceChange.NONE
                                stock.price > previousPrice -> PriceChange.UP
                                stock.price < previousPrice -> PriceChange.DOWN
                                else -> PriceChange.NONE
                            }
                            StockItemUiModel(stock, change)
                        }
                        
                        _state.value = StockPriceTrackerUiState(
                            stocks = uiModels,
                            isLoading = false
                        )
                        previousPrices = currentPrices
                    }
                }
        }
    }
}
