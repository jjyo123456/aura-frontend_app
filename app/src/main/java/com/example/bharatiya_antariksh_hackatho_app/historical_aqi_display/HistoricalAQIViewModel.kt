package com.example.bharatiya_antariksh_hackatho_app.historical_aqi_display


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.*

class HistoricalAQIViewModel : ViewModel() {

    private val repository = HistoricalAQIRepository()

    var historicalAQIData by mutableStateOf<HistoricalAirQualityResponse?>(null)
    var errorMessage by mutableStateOf("")

    fun loadHistoricalAQI(lat: Double, lon: Double, start: String, end: String) {
        viewModelScope.launch {
            try {
                val result = repository.fetchHistoricalAQI(lat, lon, start, end)
                historicalAQIData = result
                errorMessage = ""
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Error fetching historical AQI"
            }
        }
    }
}
