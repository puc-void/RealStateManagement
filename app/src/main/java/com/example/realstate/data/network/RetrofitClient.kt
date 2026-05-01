package com.example.realstate.data.network

import com.example.realstate.RealStateApp
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://real-estate-app-backend-six.vercel.app/api/v1/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val token = RealStateApp.preferenceManager.getToken()
        
        val requestBuilder = original.newBuilder()
        if (token != null) {
            requestBuilder.header("Authorization", "Bearer $token")
        }
        
        chain.proceed(requestBuilder.build())
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(authInterceptor)
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

    val authApi: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }

    val verificationApi: VerificationApiService by lazy {
        retrofit.create(VerificationApiService::class.java)
    }
}
