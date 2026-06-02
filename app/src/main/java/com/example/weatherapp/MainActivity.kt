package com.example.weatherapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherapp.ui.screen.WeatherScreen
import com.example.weatherapp.ui.viewmodel.WeatherViewModel
import com.example.weatherapp.ui.theme.WeatherAppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            WeatherAppTheme {

                val viewModel: WeatherViewModel = viewModel()

                WeatherScreen(
                    state = viewModel.state,
                    onSearch = {
                        viewModel.getWeather(it)
                    }
                )
            }
        }
    }
}