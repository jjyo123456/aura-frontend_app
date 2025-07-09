package com.example.bharatiya_antariksh_hackatho_app.realtime_aqi_display

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface Real_time_AQI_Url_endpoints {
    @GET("v1/air-quality")
    suspend fun getAirQuality(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("hourly") hourly: String = "pm10,pm2_5,carbon_monoxide,nitrogen_dioxide,sulphur_dioxide,ozone",
        @Query("timezone") timezone: String = "auto"
    ): Response<AirQualityResponse>
}
