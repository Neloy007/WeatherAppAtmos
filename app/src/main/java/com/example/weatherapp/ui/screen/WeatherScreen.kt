package com.example.weatherapp.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.weatherapp.ui.state.WeatherState
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.math.PI

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    state: WeatherState,
    onSearch: (String) -> Unit
) {
    var city by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    val gradientColors = remember(state.weather?.condition) {
        weatherGradient(state.weather?.condition ?: "")
    }

    // Animated gradient background
    val infiniteTransition = rememberInfiniteTransition(label = "background")
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )

    // Weather icon animation
    val iconTransition = rememberInfiniteTransition(label = "icon")
    val iconScale by iconTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val iconRotation by iconTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )

    // Floating particles animation
    val particleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "particle"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = gradientColors,
                    startY = animatedOffset,
                    endY = animatedOffset + 1000f
                )
            )
    ) {
        // Animated floating particles
        FloatingParticles(particleAlpha)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with search
            AnimatedVisibility(
                visible = !isSearchActive,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                WeatherHeader(
                    onSearchClick = { isSearchActive = true }
                )
            }

            // Search bar
            AnimatedVisibility(
                visible = isSearchActive,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                SearchBar(
                    city = city,
                    onCityChange = { city = it },
                    onSearch = {
                        if (city.isNotBlank()) {
                            onSearch(city)
                            isSearchActive = false
                        }
                    },
                    onDismiss = { isSearchActive = false }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Main weather content with animation
            AnimatedContent(
                targetState = state.weather,
                transitionSpec = {
                    fadeIn(animationSpec = tween(500)) +
                            slideInVertically(initialOffsetY = { it / 2 }) togetherWith
                            fadeOut(animationSpec = tween(300)) +
                            slideOutVertically(targetOffsetY = { -it / 2 })
                },
                label = "weather"
            ) { weather ->
                weather?.let {
                    WeatherContent(
                        weather = it,
                        iconScale = iconScale,
                        iconRotation = iconRotation
                    )
                }
            }

            // Loading indicator
            AnimatedVisibility(
                visible = state.isLoading,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Box(
                    modifier = Modifier
                        .padding(32.dp)
                        .size(60.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.fillMaxSize(),
                        color = Color.White.copy(alpha = 0.8f),
                        strokeWidth = 3.dp
                    )
                }
            }

            // Error message
            state.error?.let { error ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    ErrorCard(
                        error = error,
                        onRetry = {
                            if (city.isNotBlank()) onSearch(city)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun WeatherHeader(onSearchClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Weather App",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.shadow(4.dp, CircleShape)
        )

        IconButton(
            onClick = onSearchClick,
            modifier = Modifier
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f))
        ) {
            Icon(
                Icons.Default.Search,
                contentDescription = "Search",
                tint = Color.White
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    city: String,
    onCityChange: (String) -> Unit,
    onSearch: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onDismiss) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            // Fixed: Proper weight modifier syntax
            TextField(
                value = city,
                onValueChange = onCityChange,
                placeholder = {
                    Text(
                        "Enter city name...",
                        color = Color.White.copy(alpha = 0.7f)
                    )
                },
                modifier = Modifier
                    .weight(1f),  // Fixed: Added Modifier. before weight
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White,
                    focusedIndicatorColor = Color.White,
                    unfocusedIndicatorColor = Color.White.copy(alpha = 0.5f),
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedPlaceholderColor = Color.White.copy(alpha = 0.7f),
                    unfocusedPlaceholderColor = Color.White.copy(alpha = 0.5f)
                ),
                singleLine = true
            )

            IconButton(onClick = onSearch) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = "Search",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun WeatherContent(
    weather: com.example.weatherapp.domain.model.Weather,
    iconScale: Float,
    iconRotation: Float
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // City name with animation
        AnimatedContent(
            targetState = weather.city,
            transitionSpec = {
                fadeIn(animationSpec = tween(400)) +
                        slideInHorizontally() togetherWith
                        fadeOut(animationSpec = tween(300)) +
                        slideOutHorizontally()
            },
            label = "city"
        ) { cityName ->
            Text(
                text = cityName,
                fontSize = 36.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Current date and time
        val currentDateTime by remember { mutableStateOf(getCurrentDateTime()) }
        Text(
            text = currentDateTime,
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.7f),
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Weather icon with enhanced animation
        Box(
            modifier = Modifier
                .size(140.dp)
                .graphicsLayer(
                    scaleX = iconScale,
                    scaleY = iconScale,
                    rotationZ = iconRotation
                )
        ) {
            AsyncImage(
                model = weather.icon,
                contentDescription = weather.condition,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Temperature with staggered animation
        StaggeredTemperature(weather.temperature.toInt())

        // Condition text
        AnimatedContent(
            targetState = weather.condition,
            transitionSpec = {
                fadeIn(animationSpec = tween(600)) togetherWith
                        fadeOut(animationSpec = tween(400))
            },
            label = "condition"
        ) { condition ->
            Text(
                text = condition,
                fontSize = 22.sp,
                color = Color.White.copy(alpha = 0.9f),
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Main weather info cards
        MainWeatherInfoCard(weather)

        Spacer(modifier = Modifier.height(16.dp))

        // Hourly forecast
        HourlyForecastSection()

        Spacer(modifier = Modifier.height(16.dp))

        // Weather details
        WeatherDetailsSection(weather)
    }
}

@Composable
fun StaggeredTemperature(temperature: Int) {
    val tempString = temperature.toString()

    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.Center
    ) {
        tempString.forEachIndexed { index, digit ->
            AnimatedContent(
                targetState = digit.toString(),
                transitionSpec = {
                    fadeIn(
                        animationSpec = tween(
                            durationMillis = 300,
                            delayMillis = index * 150
                        )
                    ) + slideInVertically(
                        animationSpec = tween(
                            durationMillis = 400,
                            delayMillis = index * 150
                        )
                    ) { it } togetherWith
                            fadeOut(animationSpec = tween(200)) +
                            slideOutVertically(animationSpec = tween(200)) { -it }
                },
                label = "temp_digit_$index"
            ) { digitValue ->
                Text(
                    text = digitValue,
                    fontSize = 92.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Light
                )
            }
        }
        Text(
            text = "°",
            fontSize = 72.sp,
            color = Color.White.copy(alpha = 0.8f),
            fontWeight = FontWeight.Light
        )
    }
}

@Composable
fun MainWeatherInfoCard(weather: com.example.weatherapp.domain.model.Weather) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Each card will take equal width without using weight
        WeatherInfoCard(
            title = "Humidity",
            value = "${weather.humidity}%",
            icon = Icons.Default.WaterDrop,
            color = Color(0xFF4FC3F7),
            modifier = Modifier.weight(1f)
        )

        WeatherInfoCard(
            title = "Wind Speed",
            value = "${weather.windSpeed.toInt()} km/h",
            icon = Icons.Default.Air,
            color = Color(0xFF81C784),
            modifier = Modifier.weight(1f)
        )

        WeatherInfoCard(
            title = "Feels Like",
            value = "${(weather.temperature + 2).toInt()}°",
            icon = Icons.Default.Thermostat,
            color = Color(0xFFFFB74D),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun WeatherInfoCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(100.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.12f)
        ),
        border = BorderStroke(
            1.dp,
            Color.White.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Icon(
                icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(28.dp)
            )

            Text(
                text = value,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Text(
                text = title,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
        }
    }
}
@Composable
fun HourlyForecastSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        border = BorderStroke(
            1.dp,
            Color.White.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "24-Hour Forecast",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(generateHourlyForecast()) { hour ->
                    HourlyForecastItem(
                        time = hour.time,
                        icon = hour.icon,
                        temperature = hour.temp,
                        isCurrent = hour.time == "Now"
                    )
                }
            }
        }
    }
}

data class HourlyData(
    val time: String,
    val icon: String,
    val temp: String
)

@Composable
fun HourlyForecastItem(
    time: String,
    icon: String,
    temperature: String,
    isCurrent: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(
                if (isCurrent) Color.White.copy(alpha = 0.15f)
                else Color.Transparent,
                RoundedCornerShape(16.dp)
            )
            .padding(12.dp)
    ) {
        Text(
            text = time,
            color = if (isCurrent) Color(0xFFFFD54F) else Color.White,
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = icon,
            fontSize = 28.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = temperature,
            color = Color.White,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp
        )
    }
}

@Composable
fun WeatherDetailsSection(weather: com.example.weatherapp.domain.model.Weather) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        border = BorderStroke(
            1.dp,
            Color.White.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Weather Details",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            WeatherDetailItem(
                label = "Temperature",
                value = "${weather.temperature.toInt()}°C",
                icon = Icons.Default.Thermostat
            )

            WeatherDetailItem(
                label = "Feels Like",
                value = "${(weather.temperature + 2).toInt()}°C",
                icon = Icons.Default.Thermostat
            )

            WeatherDetailItem(
                label = "Humidity",
                value = "${weather.humidity}%",
                icon = Icons.Default.WaterDrop
            )

            WeatherDetailItem(
                label = "Wind Speed",
                value = "${weather.windSpeed.toInt()} km/h",
                icon = Icons.Default.Air
            )

            WeatherDetailItem(
                label = "Condition",
                value = weather.condition,
                icon = Icons.Default.WbSunny
            )

            WeatherDetailItem(
                label = "Visibility",
                value = "10 km",
                icon = Icons.Default.Visibility
            )

            WeatherDetailItem(
                label = "UV Index",
                value = "5",
                icon = Icons.Default.BrightnessMedium
            )
        }
    }
}

@Composable
fun WeatherDetailItem(
    label: String,
    value: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )

            Text(
                text = label,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
        }

        Text(
            text = value,
            color = Color.White,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )
    }
}

@Composable
fun ErrorCard(error: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE53935).copy(alpha = 0.9f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = "Error",
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = error,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onRetry,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFFE53935)
                )
            ) {
                Text("Retry")
            }
        }
    }
}

@Composable
fun FloatingParticles(alpha: Float) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    repeat(8) { index ->
        val delay = (index * 500).toLong()
        val animationState = remember { Animatable(0f) }

        LaunchedEffect(Unit) {
            delay(delay)
            animationState.animateTo(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(4000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
        }

        val xOffset = (index * 50).dp
        val yOffset = (index * 100).dp

        Box(
            modifier = Modifier
                .offset(
                    x = xOffset + (sin(animationState.value * PI.toFloat() * 2) * 30).dp,
                    y = yOffset + (animationState.value * screenHeight.value * 0.8f).dp
                )
                .size(4.dp)
                .alpha(alpha)
                .background(
                    Color.White,
                    CircleShape
                )
        )
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(
            1.dp,
            Color.White.copy(alpha = 0.2f)
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.08f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            content()
        }
    }
}

@Composable
fun WeatherInfo(
    title: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 14.sp
        )

        Text(
            text = value,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
    }
}

@Composable
fun HourItem(
    time: String,
    icon: String,
    temp: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = time,
            color = Color.White,
            fontSize = 12.sp
        )

        Text(
            text = icon,
            fontSize = 24.sp
        )

        Text(
            text = temp,
            color = Color.White,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )
    }
}

@Composable
fun DetailRow(
    title: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 14.sp
        )

        Text(
            text = value,
            color = Color.White,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )
    }

    Spacer(modifier = Modifier.height(10.dp))
}

fun weatherGradient(condition: String): List<Color> {
    return when {
        condition.contains("Sunny", true) || condition.contains("Clear", true) ->
            listOf(
                Color(0xFF43C6AC),
                Color(0xFF191654)
            )

        condition.contains("Cloud", true) || condition.contains("Overcast", true) ->
            listOf(
                Color(0xFF757F9A),
                Color(0xFF2C3E50)
            )

        condition.contains("Rain", true) || condition.contains("Drizzle", true) ->
            listOf(
                Color(0xFF1A2980),
                Color(0xFF26A0DA)
            )

        condition.contains("Snow", true) ->
            listOf(
                Color(0xFF83A4D4),
                Color(0xFFB6FBFF)
            )

        condition.contains("Thunder", true) || condition.contains("Storm", true) ->
            listOf(
                Color(0xFF232526),
                Color(0xFF414345)
            )

        condition.contains("Mist", true) || condition.contains("Fog", true) ->
            listOf(
                Color(0xFF606c88),
                Color(0xFF3f4c6b)
            )

        else ->
            listOf(
                Color(0xFF4FACFE),
                Color(0xFF00F2FE)
            )
    }
}

fun getCurrentDateTime(): String {
    val calendar = java.util.Calendar.getInstance()
    val dateFormat = java.text.SimpleDateFormat("EEEE, MMM d • h:mm a", java.util.Locale.getDefault())
    return dateFormat.format(calendar.time)
}

private fun generateHourlyForecast(): List<HourlyData> {
    val times = listOf("Now", "1 PM", "2 PM", "3 PM", "4 PM", "5 PM", "6 PM", "7 PM")
    val icons = listOf("☀️", "🌤️", "⛅", "☁️", "🌧️", "🌦️", "☁️", "🌙")
    val temps = listOf("24°", "25°", "26°", "25°", "24°", "23°", "22°", "21°")

    return times.indices.map { index ->
        HourlyData(
            time = times[index],
            icon = icons[index % icons.size],
            temp = temps[index % temps.size]
        )
    }
}