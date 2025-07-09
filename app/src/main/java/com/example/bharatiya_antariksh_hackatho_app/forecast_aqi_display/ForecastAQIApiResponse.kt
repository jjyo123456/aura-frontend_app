package com.example.bharatiya_antariksh_hackatho_app.forecast_aqi_display

import com.google.gson.annotations.SerializedName

data class ForecastAQIApiResponse(
    @SerializedName("type") val type: String,
    @SerializedName("location_name") val locationName: String,
    @SerializedName("forecast_hourly_pm25") val forecastHourlyPm25: List<Double>?,
    @SerializedName("estimated_current_pm25") val estimatedCurrentPm25: Double?
)