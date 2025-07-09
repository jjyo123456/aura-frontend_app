package com.example.bharatiya_antariksh_hackatho_app.historical_aqi_display

data class HistoricalHourlyData(
val time: List<String>,
val pm10: List<Double>,
val pm2_5: List<Double>,
val nitrogen_dioxide: List<Double>,
)
