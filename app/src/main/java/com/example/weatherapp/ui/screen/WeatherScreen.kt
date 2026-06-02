package com.example.weatherapp.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherapp.ui.state.WeatherState

@Composable
fun WeatherScreen(
    state: WeatherState,
    onSearch: (String) -> Unit
) {

    var city by remember {
        mutableStateOf("")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),

        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        OutlinedTextField(
            value = city,
            onValueChange = {
                city = it
            },
            label = {
                Text("City")
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                onSearch(city)
            }
        ) {
            Text("Search")
        }

        Spacer(modifier = Modifier.height(24.dp))

        state.weather?.let {

            Text(
                text = it.city,
                fontSize = 30.sp
            )

            Text(
                text = "${it.temperature}°C",
                fontSize = 48.sp
            )

            Text(it.condition)

            Text("Humidity: ${it.humidity}%")

            Text("Wind: ${it.windSpeed} km/h")
        }

        state.error?.let {
            Text(it)
        }
    }
}