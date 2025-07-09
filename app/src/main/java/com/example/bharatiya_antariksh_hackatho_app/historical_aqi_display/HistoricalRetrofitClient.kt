package com.example.bharatiya_antariksh_hackatho_app.historical_aqi_display

import com.example.bharatiya_antariksh_hackatho_app.historical_aqi.Historical_AQI_Endpoints_api
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object HistoricalRetrofitClient {
    private const val BASE_URL = "https://air-quality-api.open-meteo.com/"

    val api: Historical_AQI_Endpoints_api by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(Historical_AQI_Endpoints_api::class.java)
    }
}