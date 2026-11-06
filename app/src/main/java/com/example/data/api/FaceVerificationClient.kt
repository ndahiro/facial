package com.example.data.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object FaceVerificationClient {

  private val moshi: Moshi = Moshi.Builder()
    .addLast(KotlinJsonAdapterFactory())
    .build()

  fun createService(baseUrl: String): FaceVerificationApi {
    val loggingInterceptor = HttpLoggingInterceptor().apply {
      level = HttpLoggingInterceptor.Level.BODY
    }

    val okHttpClient = OkHttpClient.Builder()
      .connectTimeout(10, TimeUnit.SECONDS)
      .readTimeout(10, TimeUnit.SECONDS)
      .writeTimeout(10, TimeUnit.SECONDS)
      .addInterceptor(loggingInterceptor)
      .addInterceptor { chain ->
        val original = chain.request()
        val requestBuilder = original.newBuilder()
          .header("User-Agent", "FacePass-Biometric-Android/1.0")
          .header("Accept", "application/json")
        val request = requestBuilder.build()
        chain.proceed(request)
      }
      .build()

    val sanitizedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"

    val retrofit = Retrofit.Builder()
      .baseUrl(sanitizedUrl)
      .client(okHttpClient)
      .addConverterFactory(MoshiConverterFactory.create(moshi))
      .build()

    return retrofit.create(FaceVerificationApi::class.java)
  }
}
