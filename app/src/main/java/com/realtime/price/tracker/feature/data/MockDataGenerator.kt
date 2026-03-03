package com.realtime.price.tracker.feature.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.realtime.price.tracker.feature.data.dto.StockDetailModel
import com.realtime.price.tracker.feature.data.dto.StockDetailApiResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlin.random.Random

private const val UPDATE_INTERVAL_MS = 2000L
private const val PRICE_CHANGE_PERCENTAGE = 0.02 // 2% max change

class MockDataGenerator(private val context: Context) {

    private var stockList: MutableList<StockDetailModel>? = null
    private val gson = Gson()

    fun generateMockStockPrices(): Flow<String> = flow {
        if (stockList == null) {
            stockList = loadStockDataFromJson(context).toMutableList()
        }

        while (true) {
            val responseModel = StockDetailApiResponse(
                status = "success",
                stocks = stockList!!.toList()
            )

            emit(gson.toJson(responseModel))

            delay(UPDATE_INTERVAL_MS)

            updatePricesRandomly()
        }
    }.flowOn(Dispatchers.IO)

    private fun loadStockDataFromJson(context: Context): List<StockDetailModel> {
        val jsonString = context.assets.open("Sample.json").bufferedReader().use { it.readText() }
        val listType = object : TypeToken<List<StockDetailModel>>() {}.type
        return gson.fromJson(jsonString, listType)
    }

    private fun updatePricesRandomly() {
        stockList?.let { list ->
            for (i in list.indices) {
                val currentStock = list[i]
                val changeFactor =
                    1 + (Random.nextDouble(-PRICE_CHANGE_PERCENTAGE, PRICE_CHANGE_PERCENTAGE))
                val newPrice = currentStock.price * changeFactor

                // Update in place to avoid creating new objects
                list[i] = currentStock.copy(price = newPrice)
            }
        }
    }
}
