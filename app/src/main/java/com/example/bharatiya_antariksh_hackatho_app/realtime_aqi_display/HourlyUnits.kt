package com.example.bharatiya_antariksh_hackatho_app.realtime_aqi_display

data class HourlyUnits(
    val time: String,
    val pm10: String,
    val pm2_5: String,
    val carbon_monoxide: String,
    val nitrogen_dioxide: String,
    val sulphur_dioxide: String,
    val ozone: String
)
