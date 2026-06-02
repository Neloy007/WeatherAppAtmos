package com.example.weatherapp.data.repository

import com.example.weatherapp.data.remote.RetrofitInstance
import com.example.weatherapp.domain.model.Weather

class WeatherRepository {

    private val api = RetrofitInstance.api

    suspend fun getWeather(
        city: String,
        apiKey: String
    ): Weather {

        val response =
            api.getWeather(apiKey, city)

        return Weather(
            city = response.location.name,
            temperature = response.current.temp_c,
            condition = response.current.condition.text,
            icon = "https:${response.current.condition.icon}",
            humidity = response.current.humidity,
            windSpeed = response.current.wind_kph
        )
    }
}