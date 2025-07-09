package com.example.bharatiya_antariksh_hackatho_app.forecast_aqi_display

import retrofit2.Call

class ForecastAQIRepository {
    private val api = ForecastAQIRetrofitClient.instance

    fun fetchForecastData(lat: Double, lon: Double): Call<ForecastAQIApiResponse> {
        val body = ForecastAQIApiRequest(lat = lat, lon = lon)
        return api.getForecastAQIData(body)
    }
}
