package com.samarth.courseapp.di

import android.content.Context
import android.content.res.AssetManager
import com.samarth.courseapp.core.network.AndroidNetworkMonitor
import com.samarth.courseapp.core.network.NetworkMonitor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        // Tolerate a backend that adds fields; dropping a payload over an unknown key is a
        // self-inflicted outage.
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Provides
    fun provideAssetManager(@ApplicationContext context: Context): AssetManager = context.assets

    @Provides
    @Singleton
    fun provideNetworkMonitor(@ApplicationContext context: Context): NetworkMonitor =
        AndroidNetworkMonitor(context)
}
