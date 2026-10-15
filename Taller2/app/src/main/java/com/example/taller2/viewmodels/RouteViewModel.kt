package com.example.taller2.viewmodels

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.net.Uri
import android.os.Looper
import androidx.lifecycle.ViewModel
import com.example.taller2.data.RoutePhoto
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class RouteState(
    val isRecording: Boolean = false,
    val route: List<Location> = emptyList(),
    val photos: List<RoutePhoto> = emptyList()
)

class RouteViewModel : ViewModel() {
    private val interval = 5000L // 5 seconds
    private val minDistance = 5F // 5 meters
    private val fastestInterval = 1000L // 1 second

    private val _uiState = MutableStateFlow(RouteState())
    val state = _uiState.asStateFlow()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
    }

    fun setup(context: Context) {
        // Al abrir/cerrar el plegable la pantalla se recompone: no se vuelve a configurar
        if (::fusedLocationClient.isInitialized) return

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, interval).apply {
            setMinUpdateDistanceMeters(minDistance)
            setMinUpdateIntervalMillis(fastestInterval)
            setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
            setWaitForAccurateLocation(true)
        }.build()
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                _uiState.update { currentState ->
                    currentState.copy(route = currentState.route.plus(locationResult.locations))
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        if (!::fusedLocationClient.isInitialized) return
        _uiState.update {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            it.copy(isRecording = true)
        }
    }

    fun stopLocationUpdates() {
        if (!::fusedLocationClient.isInitialized) return
        _uiState.update {
            fusedLocationClient.removeLocationUpdates(locationCallback)
            it.copy(isRecording = false)
        }
    }

    // Agrega la foto con la ubicación actual del dispositivo (donde fue tomada)
    @SuppressLint("MissingPermission")
    fun addPhoto(uri: Uri, name: String) {
        if (!::fusedLocationClient.isInitialized) {
            _uiState.update { it.copy(photos = it.photos.plus(RoutePhoto(uri, name, null))) }
            return
        }
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnCompleteListener { task ->
                val current = if (task.isSuccessful) task.result else null
                val location = current ?: _uiState.value.route.lastOrNull()
                _uiState.update { it.copy(photos = it.photos.plus(RoutePhoto(uri, name, location))) }
            }
    }

    // Borra el recorrido: puntos de la ruta y fotos (los archivos siguen en la galería)
    fun clearRoute() {
        _uiState.update {
            it.copy(route = emptyList(), photos = emptyList())
        }
    }
}
