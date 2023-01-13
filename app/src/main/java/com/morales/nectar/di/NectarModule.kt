package com.morales.nectar.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.morales.nectar.data.remote.CareLogApi
import com.morales.nectar.data.remote.PlantsApi
import com.morales.nectar.data.remote.UserApi
import com.morales.nectar.repository.CareLogRepository
import com.morales.nectar.repository.PlantsRepository
import com.morales.nectar.repository.UserRepository
import com.morales.nectar.util.Constants.BASE_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NectarModule {

    @Provides
    fun providesAuthentication(): FirebaseAuth = Firebase.auth

    @Provides
    fun providesStorage(): FirebaseStorage = Firebase.storage

    @Singleton
    @Provides
    fun providePlantRepository(
        api: PlantsApi
    ) = PlantsRepository(api)

    @Singleton
    @Provides
    fun providePlantsApi(): PlantsApi {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .build()
            .create(PlantsApi::class.java)
    }

    @Singleton
    @Provides
    fun provideUserRepository(
        api: UserApi
    ) = UserRepository(api)

    @Singleton
    @Provides
    fun provideUserApi(): UserApi {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .build()
            .create(UserApi::class.java)
    }

    @Singleton
    @Provides
    fun provideCareLogRepository(
        api: CareLogApi
    ) = CareLogRepository(api)

    @Singleton
    @Provides
    fun provideCareLogApi(): CareLogApi {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .build()
            .create(CareLogApi::class.java)
    }
}