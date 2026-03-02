package com.realtime.price.tracker.feature.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.realtime.price.tracker.feature.data.StockDetailsResult
import com.realtime.price.tracker.feature.data.dto.StockDetailModel
import com.realtime.price.tracker.feature.domain.StockPriceDetailsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StockPriceTrackerListScreenViewModel(private val stockPriceDetailsUseCase: StockPriceDetailsUseCase) :
    ViewModel() {
    private val _state = MutableStateFlow<List<StockDetailModel>>(emptyList())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            stockPriceDetailsUseCase.observeStockPriceDetails()
                .collect { result ->
                    if (result is StockDetailsResult.Success) {
                        _state.value = result.stocks
                    }
                }
        }
    }
}
