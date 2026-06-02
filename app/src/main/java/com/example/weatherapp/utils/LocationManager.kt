package com.example.weatherapp.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.util.Locale

class LocationManager(private val context: Context) {

    suspend fun getCurrentCity(): String? = withContext(Dispatchers.IO) {
        try {
            // Double-check permissions before any location operation
            if (!hasLocationPermission()) {
                return@withContext null
            }

            // Try to get active location (not just last known)
            val location = getActiveLocation()

            if (location != null) {
                val city = getCityFromLocation(location)
                if (!city.isNullOrEmpty()) {
                    return@withContext city
                }
            }

            // Fallback to last known location
            val lastKnownLocation = getLastKnownLocation()
            if (lastKnownLocation != null) {
                val city = getCityFromLocation(lastKnownLocation)
                if (!city.isNullOrEmpty()) {
                    return@withContext city
                }
            }

            null
        } catch (e: SecurityException) {
            // Permission was revoked during execution
            e.printStackTrace()
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun getActiveLocation(): Location? {
        // Check permissions again before accessing location
        if (!hasLocationPermission()) {
            return null
        }

        return try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

            // Request current location with timeout
            withTimeout(5000) {
                // Use a try-catch specifically for the location request
                try {
                    fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        null
                    ).await()
                } catch (e: SecurityException) {
                    println("Security exception in getCurrentLocation: ${e.message}")
                    null
                }
            }
        } catch (e: TimeoutCancellationException) {
            println("Location request timeout")
            null
        } catch (e: Exception) {
            println("Error getting active location: ${e.message}")
            null
        }
    }

    private fun getLastKnownLocation(): Location? {
        // Check permissions before accessing location
        if (!hasLocationPermission()) {
            return null
        }

        return try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            val providers = listOf(
                LocationManager.GPS_PROVIDER,
                LocationManager.NETWORK_PROVIDER,
                LocationManager.PASSIVE_PROVIDER
            )

            providers.firstNotNullOfOrNull { provider ->
                try {
                    if (locationManager.isProviderEnabled(provider)) {
                        locationManager.getLastKnownLocation(provider)
                    } else null
                } catch (e: SecurityException) {
                    println("Security exception for provider $provider: ${e.message}")
                    null
                }
            }
        } catch (e: SecurityException) {
            println("Security exception in getLastKnownLocation: ${e.message}")
            null
        } catch (e: Exception) {
            println("Error getting last known location: ${e.message}")
            null
        }
    }

    private fun getCityFromLocation(location: Location): String? {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(
                location.latitude,
                location.longitude,
                1
            )

            if (addresses?.isNotEmpty() == true) {
                addresses[0].locality ?:
                addresses[0].subAdminArea ?:
                addresses[0].adminArea ?:
                addresses[0].countryName
            } else null
        } catch (e: Exception) {
            println("Error getting city from location: ${e.message}")
            null
        }
    }

    fun hasLocationPermission(): Boolean {
        return try {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            false
        }
    }

    fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return try {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (e: Exception) {
            false
        }
    }
}