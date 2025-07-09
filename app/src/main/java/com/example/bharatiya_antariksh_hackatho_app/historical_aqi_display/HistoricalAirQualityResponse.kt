package com.example.bharatiya_antariksh_hackatho_app.historical_aqi_display

data class HistoricalAirQualityResponse(
    val latitude: Double,
    val longitude: Double,
    val generationtime_ms: Double,
    val utc_offset_seconds: Int,
    val timezone: String,
    val timezone_abbreviation: String,
    val elevation: Double,
    val hourly_units: HistoricalHourlyUnits,
    val hourly: HistoricalHourlyData
)



