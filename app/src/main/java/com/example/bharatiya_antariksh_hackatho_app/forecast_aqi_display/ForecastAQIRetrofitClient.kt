package com.example.bharatiya_antariksh_hackatho_app.forecast_aqi_display

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ForecastAQIRetrofitClient {
    private const val BASE_URL = "https://36dc5d205b64.ngrok-free.app"

    val instance: ForecastAQIApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ForecastAQIApiService::class.java)
    }
}
