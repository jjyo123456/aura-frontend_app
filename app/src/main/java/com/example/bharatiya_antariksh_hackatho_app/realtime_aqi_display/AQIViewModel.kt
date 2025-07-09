package com.example.bharatiya_antariksh_hackatho_app.realtime_aqi_display

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class AQIViewModel : ViewModel() {

    private val repository = AQIRepository()

    // LiveData to expose AQI data to UI
    public val _aqiData = MutableLiveData<AirQualityResponse>()
    val aqiData: LiveData<AirQualityResponse> = _aqiData

    // LiveData to expose error messages to UI
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun loadAqidata(lat: Double, lon: Double) {
        viewModelScope.launch {
            try {
                val result = repository.fetchAirQuality(lat, lon)
                _aqiData.postValue(result)
                _errorMessage.postValue("") // clear previous errors
            } catch (e: Exception) {
                _errorMessage.postValue(e.localizedMessage ?: "Failed to fetch AQI data.")
            }
        }
    }
}
