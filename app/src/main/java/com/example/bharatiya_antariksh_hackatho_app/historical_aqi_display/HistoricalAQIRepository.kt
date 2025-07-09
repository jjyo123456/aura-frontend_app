package com.example.bharatiya_antariksh_hackatho_app.historical_aqi_display


class HistoricalAQIRepository {
    suspend fun fetchHistoricalAQI(
        lat: Double,
        lon: Double,
        startDate: String,
        endDate: String
    ): HistoricalAirQualityResponse {
        val response = HistoricalRetrofitClient.api.getHistoricalAQI(
            latitude = lat,
            longitude = lon,
            startDate = startDate,
            endDate = endDate
        )

        if (response.isSuccessful) {
            return response.body()!!
        } else {
            throw Exception("API Error: ${response.message()}")
        }
    }
}
