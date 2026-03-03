package com.realtime.price.tracker.feature.data

import com.google.gson.Gson
import com.realtime.price.tracker.BuildConfig
import com.realtime.price.tracker.feature.data.dto.StockDetailModel
import com.realtime.price.tracker.feature.data.dto.StockDetailApiResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit
import kotlin.math.min
import kotlin.math.pow

sealed class StockDetailsResult {
    data class Success(
        val stocks: List<StockDetailModel>
    ) : StockDetailsResult()

    data class Error(
        val exception: Throwable,
        val message: String = exception.message ?: "Unknown error"
    ) : StockDetailsResult()
}

class StockPriceDetailsWebSocketDataSource(
    private val mockDataGenerator: MockDataGenerator,
    private val retryConfig: RetryConfig = RetryConfig()
) {
    data class RetryConfig(
        val maxRetries: Int = 5,
        val initialDelayMillis: Long = 1000L,
        val maxDelayMillis: Long = 30000L,
        val backoffMultiplier: Double = 2.0
    )

    companion object {
        private const val WEBSOCKET_URL = BuildConfig.WEBSOCKET_URL
        private const val STATUS_ERROR = "error"

        private const val CLOSE_NORMAL = 1000
        private const val APPLICATION_ERROR = 4001
    }

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.SECONDS)
        .pingInterval(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private var webSocket: WebSocket? = null

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _stockUpdates = MutableSharedFlow<List<StockDetailModel>>(
        replay = 1,
        extraBufferCapacity = 10
    )

    private var retryAttempt = 0

    init {
        startMockUpdates()
    }

    fun startMockUpdates() {
        coroutineScope.launch {
            mockDataGenerator.generateMockStockPrices()
                .collect {
                    webSocket?.send(it)
                }
        }
    }

    fun observeStockPriceDetails(): Flow<StockDetailsResult> {
        return flow<StockDetailsResult> {
            ensureConnected()

            _stockUpdates.collect { stocks ->
                emit(StockDetailsResult.Success(stocks))
            }
        }.flowOn(Dispatchers.IO)
    }

    fun disconnect() {
        webSocket?.close(CLOSE_NORMAL, "Client disconnecting")
        webSocket = null
        _isConnected.value = false
    }

    private fun ensureConnected() {
        if (webSocket == null || !_isConnected.value) {
            connect()
        }
    }

    private fun connect() {
        val request = Request.Builder()
            .url(WEBSOCKET_URL)
            .header("X-Client-Version", "1.0")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(webSocket: WebSocket, response: Response) {
                _isConnected.value = true
                retryAttempt = 0
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val response = gson.fromJson(text, StockDetailApiResponse::class.java)

                    if (response.status == STATUS_ERROR) {
                        webSocket.close(APPLICATION_ERROR, "Error status received from server")
                        return
                    }

                    _stockUpdates.tryEmit(response.stocks)

                } catch (_: Exception) {
                    // If the BE returns any app specific errors then those will be handled here
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                _isConnected.value = false
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                _isConnected.value = false
                this@StockPriceDetailsWebSocketDataSource.webSocket = null
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                _isConnected.value = false
                this@StockPriceDetailsWebSocketDataSource.webSocket = null

                if (isRetryableError(t) && retryAttempt < retryConfig.maxRetries) {
                    val delayMillis = calculateBackoffDelay(retryAttempt)
                    retryAttempt++

                    coroutineScope.launch {
                        delay(delayMillis)
                        ensureConnected()
                    }
                }
            }
        })
    }

    private fun isRetryableError(throwable: Throwable): Boolean {
        return when (throwable) {
            is java.net.SocketException -> true
            is java.net.SocketTimeoutException -> true
            is java.net.UnknownHostException -> true
            else -> false
        }
    }

    private fun calculateBackoffDelay(attempt: Int): Long {
        val exponentialDelay = (retryConfig.initialDelayMillis *
                retryConfig.backoffMultiplier.pow(attempt.toDouble())).toLong()
        return min(exponentialDelay, retryConfig.maxDelayMillis)
    }
}
