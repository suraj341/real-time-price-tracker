package com.realtime.price.tracker.feature.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.realtime.price.tracker.feature.data.StockDetailsResult
import com.realtime.price.tracker.feature.data.dto.StockDetailModel
import com.realtime.price.tracker.feature.domain.StockPriceDetailsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PriceChange {
    UP, DOWN, NONE
}

data class StockItemUiModel(
    val stock: StockDetailModel,
    val priceChange: PriceChange
)

data class StockPriceTrackerUiState(
    val stocks: List<StockItemUiModel> = emptyList(),
    val isLoading: Boolean = true,
    val isConnected: Boolean = false
)

@HiltViewModel
class StockPriceTrackerListScreenViewModel @Inject constructor(private val stockPriceDetailsUseCase: StockPriceDetailsUseCase) :
    ViewModel() {
    private val _state = MutableStateFlow(StockPriceTrackerUiState())
    val state: StateFlow<StockPriceTrackerUiState> = _state.asStateFlow()

    private var previousPrices: Map<String, Double> = emptyMap()
    private var collectionJob: Job? = null

    init {
        viewModelScope.launch {
            stockPriceDetailsUseCase.isConnected.collect { isConnected ->
                _state.value = _state.value.copy(isConnected = isConnected)
            }
        }
        startCollecting()
    }

    private fun startCollecting() {
        collectionJob?.cancel()

        collectionJob = viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

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

                        _state.value = _state.value.copy(
                            stocks = uiModels,
                            isLoading = false
                        )
                        previousPrices = currentPrices
                    }
                }
        }
    }

    fun toggleConnection() {
        if (_state.value.isConnected) {
            collectionJob?.cancel()
            collectionJob = null
            stockPriceDetailsUseCase.disconnect()
            _state.value = _state.value.copy(
                isConnected = false,
                isLoading = false
            )
        } else {
            startCollecting()
        }
    }
}
