package com.example.weatherapp.ui.viewmodel


import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.data.repository.WeatherRepository
import com.example.weatherapp.ui.state.WeatherState
import kotlinx.coroutines.launch

class WeatherViewModel : ViewModel() {

    private val repository =
        WeatherRepository()

    var state by mutableStateOf(
        WeatherState()
    )
        private set

    private val apiKey =
        "6d669e53d8af4a71ad2120759260206"

    fun getWeather(city: String) {

        viewModelScope.launch {

            state = state.copy(
                isLoading = true,
                error = null
            )

            try {

                val weather =
                    repository.getWeather(
                        city,
                        apiKey
                    )

                state = state.copy(
                    weather = weather,
                    isLoading = false
                )

            } catch (e: Exception) {

                state = state.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }
}