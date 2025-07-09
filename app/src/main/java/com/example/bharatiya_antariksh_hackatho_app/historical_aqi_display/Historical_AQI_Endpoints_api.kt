package com.example.bharatiya_antariksh_hackatho_app.historical_aqi


import com.example.bharatiya_antariksh_hackatho_app.historical_aqi_display.HistoricalAirQualityResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface Historical_AQI_Endpoints_api {
    @GET("v1/air-quality")
    suspend fun getHistoricalAQI(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("hourly") hourly: String = "pm10,pm2_5,nitrogen_dioxide",
        @Query("start_date") startDate: String,   // Format: yyyy-MM-dd
        @Query("end_date") endDate: String,       // Format: yyyy-MM-dd
        @Query("timezone") timezone: String = "auto"
    ): Response<HistoricalAirQualityResponse>
}