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
    val currentLocation: Location? = null,
    val photos: List<RoutePhoto> = emptyList()
)

class RouteViewModel : ViewModel() {
    private val interval = 2000L // 2 seconds
    private val minDistance = 2F // 2 meters
    private val fastestInterval = 1000L // 1 second

    private val _uiState = MutableStateFlow(RouteState())
    val state = _uiState.asStateFlow()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    override fun onCleared() {
        super.onCleared()
        if (::fusedLocationClient.isInitialized)
            fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    fun setup(context: Context) {
        // Al abrir/cerrar el plegable la pantalla se recompone: no se vuelve a configurar
        if (::fusedLocationClient.isInitialized) return

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context.applicationContext)
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, interval).apply {
            setMinUpdateDistanceMeters(minDistance)
            setMinUpdateIntervalMillis(fastestInterval)
            setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
            setWaitForAccurateLocation(false)
        }.build()
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                // La ubicación actual siempre se actualiza; la ruta solo mientras se graba
                _uiState.update { currentState ->
                    currentState.copy(
                        route = if (currentState.isRecording)
                            currentState.route.plus(locationResult.locations)
                        else currentState.route,
                        currentLocation = locationResult.lastLocation ?: currentState.currentLocation
                    )
                }
            }
        }
        fetchCurrentLocation()
        requestLocationUpdates()
    }

    // Escucha el GPS desde que se dan los permisos, para mostrar la ubicación actual en el mapa
    @SuppressLint("MissingPermission")
    private fun requestLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    // Ubicación actual al abrir la app, para centrar el mapa aunque no se haya iniciado el recorrido
    @SuppressLint("MissingPermission")
    fun fetchCurrentLocation() {
        if (!::fusedLocationClient.isInitialized) return
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    _uiState.update { it.copy(currentLocation = location) }
                } else {
                    fusedLocationClient.lastLocation.addOnSuccessListener { last ->
                        if (last != null) _uiState.update { it.copy(currentLocation = last) }
                    }
                }
            }
    }

    // Inicia el recorrido: el primer punto es la ubicación actual
    fun startLocationUpdates() {
        if (!::fusedLocationClient.isInitialized) return
        _uiState.update {
            it.copy(
                isRecording = true,
                route = it.route.plus(listOfNotNull(it.currentLocation))
            )
        }
    }

    fun stopLocationUpdates() {
        _uiState.update { it.copy(isRecording = false) }
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
                val location = current ?: _uiState.value.currentLocation
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
