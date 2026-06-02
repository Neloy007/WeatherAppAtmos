package com.example.weatherapp.ui.viewmodel

import android.content.Context
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.data.repository.WeatherRepository
import com.example.weatherapp.ui.state.WeatherState
import com.example.weatherapp.utils.LocationManager
import kotlinx.coroutines.launch

class WeatherViewModel(
    private val context: Context
) : ViewModel() {

    private val repository = WeatherRepository()
    private val locationManager = LocationManager(context)

    var state by mutableStateOf(WeatherState())
        private set

    private val apiKey = "6d669e53d8af4a71ad2120759260206"

    init {
        // Automatically load weather for current location when ViewModel is created
        loadWeatherForCurrentLocation()
    }

    fun getWeather(city: String) {
        viewModelScope.launch {
            state = state.copy(
                isLoading = true,
                error = null
            )

            try {
                val weather = repository.getWeather(city, apiKey)
                state = state.copy(
                    weather = weather,
                    isLoading = false
                )
            } catch (e: Exception) {
                state = state.copy(
                    error = e.message ?: "Failed to get weather data",
                    isLoading = false
                )
            }
        }
    }

    fun loadWeatherForCurrentLocation() {
        viewModelScope.launch {
            state = state.copy(
                isLoading = true,
                error = null
            )

            try {
                // First check if location is enabled
                if (!locationManager.isLocationEnabled()) {
                    state = state.copy(
                        error = "Location is disabled. Please enable location services in your device settings.",
                        isLoading = false
                    )
                    return@launch
                }

                // Check permissions
                if (!locationManager.hasLocationPermission()) {
                    state = state.copy(
                        error = "Location permission required. Please grant permission to use current location.",
                        isLoading = false
                    )
                    return@launch
                }

                val city = locationManager.getCurrentCity()
                if (city != null && city.isNotEmpty()) {
                    getWeather(city)
                } else {
                    state = state.copy(
                        error = "Unable to detect current location. Please:\n" +
                                "1. Make sure GPS/Location is turned ON\n" +
                                "2. Go outside for better GPS signal\n" +
                                "3. Or search for a city manually",
                        isLoading = false
                    )
                }
            } catch (e: SecurityException) {
                state = state.copy(
                    error = "Location permission required. Please grant permission to use current location.",
                    isLoading = false
                )
            } catch (e: Exception) {
                state = state.copy(
                    error = "Failed to get location: ${e.message}\nPlease try searching for a city.",
                    isLoading = false
                )
            }
        }
    }

    fun retryWithCurrentLocation() {
        loadWeatherForCurrentLocation()
    }

    // Factory class to provide ViewModel with context
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(WeatherViewModel::class.java)) {
                return WeatherViewModel(context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}