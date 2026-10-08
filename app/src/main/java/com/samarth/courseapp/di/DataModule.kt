package com.samarth.courseapp.di

import android.content.res.AssetManager
import com.samarth.courseapp.core.network.NetworkMonitor
import com.samarth.courseapp.data.auth.AuthApi
import com.samarth.courseapp.data.auth.MockAuthApi
import com.samarth.courseapp.data.local.CourseDao
import com.samarth.courseapp.data.remote.AssetCourseApi
import com.samarth.courseapp.data.remote.CourseApi
import com.samarth.courseapp.data.remote.MockApiConfig
import com.samarth.courseapp.data.repository.AuthRepositoryImpl
import com.samarth.courseapp.data.repository.CourseRepositoryImpl
import com.samarth.courseapp.domain.repository.AuthRepository
import com.samarth.courseapp.domain.repository.CourseRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.serialization.json.Json

/**
 * The only place that knows the APIs are mocked. Swapping in Retrofit means editing these two
 * `@Provides` functions and nothing above them.
 */
@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideCourseApi(
        assets: AssetManager,
        json: Json,
        networkMonitor: NetworkMonitor,
        mockApiConfig: MockApiConfig,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): CourseApi = AssetCourseApi(assets, json, networkMonitor, mockApiConfig, ioDispatcher)

    @Provides
    @Singleton
    fun provideAuthApi(@IoDispatcher ioDispatcher: CoroutineDispatcher): AuthApi =
        MockAuthApi(ioDispatcher)

    @Provides
    @Singleton
    fun provideCourseRepository(courseApi: CourseApi, courseDao: CourseDao): CourseRepository =
        CourseRepositoryImpl(courseApi, courseDao)

    @Provides
    @Singleton
    fun provideAuthRepository(authApi: AuthApi): AuthRepository = AuthRepositoryImpl(authApi)
}
