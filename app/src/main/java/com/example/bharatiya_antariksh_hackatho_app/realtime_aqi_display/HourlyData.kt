package com.example.bharatiya_antariksh_hackatho_app.realtime_aqi_display

data class HourlyData(
    val time: List<String>,
    val pm10: List<Double>,
    val pm2_5: List<Double>,
    val carbon_monoxide: List<Double>,
    val nitrogen_dioxide: List<Double>,
    val sulphur_dioxide: List<Double>,
    val ozone: List<Double>
)
