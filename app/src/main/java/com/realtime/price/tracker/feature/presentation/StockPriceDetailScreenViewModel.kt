package com.realtime.price.tracker.feature.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.realtime.price.tracker.feature.domain.StockPriceDetailsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StockPriceDetailUiState(
    val symbol: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val currency: String = "",
    val description: String = "",
    val priceChange: PriceChange = PriceChange.NONE,
    val isConnected: Boolean = false,
    val isLoading: Boolean = true
)

class StockPriceDetailScreenViewModel(
    private val symbol: String,
    private val stockPriceDetailsUseCase: StockPriceDetailsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(StockPriceDetailUiState())
    val state: StateFlow<StockPriceDetailUiState> = _state.asStateFlow()

    private var previousPrice: Double? = null

    init {
        // Listen to connection status updates
        viewModelScope.launch {
            stockPriceDetailsUseCase.isConnected.collect { isConnected ->
                _state.value = _state.value.copy(isConnected = isConnected)
            }
        }

        // Observe specific stock details
        viewModelScope.launch {
            stockPriceDetailsUseCase.observeStockPriceDetails(symbol)
                .collect { stock ->
                    if (stock != null) {
                        val change = when {
                            previousPrice == null -> PriceChange.NONE
                            stock.price > previousPrice!! -> PriceChange.UP
                            stock.price < previousPrice!! -> PriceChange.DOWN
                            else -> PriceChange.NONE
                        }

                        _state.value = StockPriceDetailUiState(
                            symbol = stock.symbol,
                            name = stock.name,
                            price = stock.price,
                            currency = stock.currency,
                            description = stock.description,
                            priceChange = change,
                            isConnected = _state.value.isConnected,
                            isLoading = false
                        )

                        previousPrice = stock.price
                    } else {
                        _state.value = _state.value.copy(isLoading = true)
                    }
                }
        }
    }
}
