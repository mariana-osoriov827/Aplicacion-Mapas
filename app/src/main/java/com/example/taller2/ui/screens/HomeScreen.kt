package com.example.taller2.ui.screens

import android.Manifest
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.taller2.R
import com.example.taller2.ui.components.CameraPreviewContent
import com.example.taller2.ui.components.MapComponent
import com.example.taller2.ui.components.NoPermissionCard
import com.example.taller2.ui.components.PhotoListComponent
import com.example.taller2.viewmodels.RouteState
import com.example.taller2.viewmodels.RouteViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    routeViewModel: RouteViewModel = viewModel()
) {
    val context = LocalContext.current
    val permissionsState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    val uiState by routeViewModel.state.collectAsState()

    val cameraGranted = permissionsState.permissions.any {
        it.permission == Manifest.permission.CAMERA && it.status.isGranted
    }
    val locationGranted = permissionsState.permissions.any {
        it.permission != Manifest.permission.CAMERA && it.status.isGranted
    }

    LaunchedEffect(Unit) {
        permissionsState.launchMultiplePermissionRequest()
    }
    LaunchedEffect(locationGranted) {
        if (locationGranted)
            routeViewModel.setup(context)
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (maxWidth > maxHeight) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CameraRegion(
                    cameraGranted = cameraGranted,
                    uiState = uiState,
                    routeViewModel = routeViewModel,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
                MapRegion(
                    locationGranted = locationGranted,
                    uiState = uiState,
                    routeViewModel = routeViewModel,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CameraRegion(
                    cameraGranted = cameraGranted,
                    uiState = uiState,
                    routeViewModel = routeViewModel,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
                MapRegion(
                    locationGranted = locationGranted,
                    uiState = uiState,
                    routeViewModel = routeViewModel,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun CameraRegion(
    cameraGranted: Boolean,
    uiState: RouteState,
    routeViewModel: RouteViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        RegionCard(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (cameraGranted) {
                CameraPreviewContent(
                    modifier = Modifier.fillMaxSize(),
                    onPhotoTaken = { uri, name ->
                        routeViewModel.addPhoto(uri, name)
                    }
                )
            } else {
                NoPermissionCard(message = stringResource(R.string.camera_permission_denied))
            }
        }
        if (cameraGranted) {
            RegionCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                PhotoListComponent(photos = uiState.photos)
            }
        }
    }
}

// Región del mapa: ubicación actual, recorrido, marcadores y botones
@Composable
private fun MapRegion(
    locationGranted: Boolean,
    uiState: RouteState,
    routeViewModel: RouteViewModel,
    modifier: Modifier = Modifier
) {
    RegionCard(modifier = modifier) {
        if (locationGranted) {
            MapComponent(
                modifier = Modifier.fillMaxSize(),
                route = uiState.route,
                currentLocation = uiState.currentLocation,
                photos = uiState.photos,
                isRecording = uiState.isRecording,
                onToggleRecording = {
                    if (uiState.isRecording) routeViewModel.stopLocationUpdates()
                    else routeViewModel.startLocationUpdates()
                },
                onClearRoute = { routeViewModel.clearRoute() }
            )
        } else {
            NoPermissionCard(message = stringResource(R.string.location_permission_denied))
        }
    }
}

@Composable
private fun RegionCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    OutlinedCard(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primaryContainer),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        content()
    }
}
