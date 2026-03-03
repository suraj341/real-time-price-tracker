package com.realtime.price.tracker.di

import android.content.Context
import com.realtime.price.tracker.feature.data.MockDataGenerator
import com.realtime.price.tracker.feature.data.MockDataGeneratorDataSource
import com.realtime.price.tracker.feature.data.StockPriceDetailsDataSource
import com.realtime.price.tracker.feature.data.StockPriceDetailsRepository
import com.realtime.price.tracker.feature.data.StockPriceDetailsWebSocketDataSource
import com.realtime.price.tracker.feature.domain.StockPriceDetailsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
object StockPriceDetailsRepositoryModule {

    @Provides
    @ActivityRetainedScoped
    fun provideMockDataGenerator(@ApplicationContext context: Context): MockDataGeneratorDataSource {
        return MockDataGenerator(context)
    }

    @Provides
    @ActivityRetainedScoped
    fun provideStockPriceDetailsDataSource(
        mockDataGenerator: MockDataGeneratorDataSource
    ): StockPriceDetailsDataSource {
        return StockPriceDetailsWebSocketDataSource(
            mockDataGenerator = mockDataGenerator
        )
    }

    @Provides
    @ActivityRetainedScoped
    fun provideRepository(
        dataSource: StockPriceDetailsDataSource
    ): StockPriceDetailsRepository {
        return StockPriceDetailsRepository(dataSource)
    }

    @Provides
    @ActivityRetainedScoped
    fun provideUseCase(
        repository: StockPriceDetailsRepository
    ): StockPriceDetailsUseCase {
        return StockPriceDetailsUseCase(repository)
    }
}

