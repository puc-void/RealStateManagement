package com.example.realstate.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://real-estate-app-backend-six.vercel.app/api/v1/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val propertyApi: PropertyApiService by lazy {
        retrofit.create(PropertyApiService::class.java)
    }

    val reviewApi: ReviewApiService by lazy {
        retrofit.create(ReviewApiService::class.java)
    }
    
    val wishlistApi: WishlistApiService by lazy {
        retrofit.create(WishlistApiService::class.java)
    }

    val bookedPropertyApi: BookedPropertyApiService by lazy {
        retrofit.create(BookedPropertyApiService::class.java)
    }

    val userApi: UserApiService by lazy {
        retrofit.create(UserApiService::class.java)
    }

    val agentApi: AgentApiService by lazy {
        retrofit.create(AgentApiService::class.java)
    }

    val soldPropertyApi: SoldPropertyApiService by lazy {
        retrofit.create(SoldPropertyApiService::class.java)
    }
}
