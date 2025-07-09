package com.example.bharatiya_antariksh_hackatho_app.realtime_aqi_display

open class UiState {
    object Loading : UiState()
    data class Success(val value : AirQualityResponse) : UiState()
    data class Failue(val value : String) : UiState()
}