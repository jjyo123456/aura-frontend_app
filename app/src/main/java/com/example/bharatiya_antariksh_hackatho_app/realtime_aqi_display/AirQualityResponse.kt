package com.example.bharatiya_antariksh_hackatho_app.realtime_aqi_display

data class AirQualityResponse(
    val latitude: Double,
    val longitude: Double,
    val generationtime_ms: Double,
    val utc_offset_seconds: Int,
    val timezone: String,
    val timezone_abbreviation: String,
    val elevation: Double,
    val hourly_units: HourlyUnits,
    val hourly: HourlyData
)



