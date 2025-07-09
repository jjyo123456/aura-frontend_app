package com.example.bharatiya_antariksh_hackatho_app.realtime_aqi_display

import retrofit2.HttpException
import java.io.IOException

class AQIRepository {

    suspend fun fetchAirQuality(lat: Double, lon: Double): AirQualityResponse {
        val response = RetrofitClient.api.getAirQuality(latitude = lat, longitude = lon)

        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                return body
            } else {
                throw IOException("Response body is null")
            }
        } else {
            throw HttpException(response)
        }
    }
}
