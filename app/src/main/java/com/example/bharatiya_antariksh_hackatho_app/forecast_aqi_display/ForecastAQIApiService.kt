package com.example.bharatiya_antariksh_hackatho_app.forecast_aqi_display

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ForecastAQIApiService {
    @Headers("Content-Type: application/json")
    @POST("/api/v1/aqi_data")
    fun getForecastAQIData(@Body request: ForecastAQIApiRequest): Call<ForecastAQIApiResponse>
}