package com.example.taller2.ui.components

import android.location.Location
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.taller2.R
import com.example.taller2.data.RoutePhoto
import com.mapbox.geojson.Point
import com.mapbox.maps.ViewAnnotationAnchor
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.ViewAnnotation
import com.mapbox.maps.extension.compose.annotation.generated.PolylineAnnotationGroup
import com.mapbox.maps.extension.style.layers.properties.generated.LineCap
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.viewport.data.FollowPuckViewportStateOptions
import com.mapbox.maps.viewannotation.annotationAnchor
import com.mapbox.maps.viewannotation.geometry
import com.mapbox.maps.viewannotation.viewAnnotationOptions

@Composable
fun MapComponent(
    modifier: Modifier = Modifier,
    defaultZoom: Double = 16.0,
    defaultLineColor: Color = MaterialTheme.colorScheme.primary,
    route: List<Location> = emptyList(),
    photos: List<RoutePhoto> = emptyList(),
    isRecording: Boolean = false,
    onToggleRecording: () -> Unit = {},
    onClearRoute: () -> Unit = {},
) {
    val mapViewportState = rememberMapViewportState()

    Box(modifier = modifier) {
        MapboxMap(
            Modifier.fillMaxSize(),
            mapViewportState = mapViewportState,
            scaleBar = { }, // Disables/hides the scale bar ornament
        ) {
            // Muestra la ubicación actual del usuario y la cámara del mapa la sigue
            MapEffect(Unit) { mapView ->
                mapView.location.updateSettings {
                    locationPuck = createDefault2DPuck(withBearing = true)
                    puckBearing = PuckBearing.COURSE
                    puckBearingEnabled = true
                    pulsingEnabled = true
                    enabled = true
                }
                mapViewportState.transitionToFollowPuckState(
                    FollowPuckViewportStateOptions.Builder().zoom(defaultZoom).pitch(0.0).build()
                )
            }
            //Polyline del recorrido
            if (route.size > 1) {
                PolylineAnnotationGroup(
                    annotations = listOf(
                        PolylineAnnotationOptions().withPoints(route.map {
                            Point.fromLngLat(
                                it.longitude,
                                it.latitude
                            )
                        })
                    )
                ) {
                    lineCap = LineCap.ROUND
                    lineWidth = 8.0
                    lineColor = defaultLineColor
                    lineBorderWidth = 2.0
                    lineBorderColor = Color.White
                    lineJoin = LineJoin.ROUND
                }
            }
            //Marcadores con la miniatura de cada foto en el lugar donde fue tomada
            photos.forEach { photo ->
                photo.location?.let { location ->
                    key(photo.uri.toString()) {
                        ViewAnnotation(
                            options = viewAnnotationOptions {
                                geometry(Point.fromLngLat(location.longitude, location.latitude))
                                annotationAnchor {
                                    anchor(ViewAnnotationAnchor.BOTTOM)
                                }
                                allowOverlap(true)
                            }
                        ) {
                            PhotoMarker(name = photo.name, uri = photo.uri)
                        }
                    }
                }
            }
        }

        // Botones del recorrido: borrar e iniciar / detener
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledIconButton(
                onClick = onClearRoute,
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.DeleteSweep,
                    contentDescription = stringResource(R.string.clear_route)
                )
            }
            BadgedBox(
                badge = {
                    if (route.isNotEmpty()) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = MaterialTheme.colorScheme.onTertiary
                        ) {
                            Text("${route.size}")
                        }
                    }
                }
            ) {
                FilledIconButton(
                    onClick = onToggleRecording,
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor =
                            if (isRecording) MaterialTheme.colorScheme.tertiary
                            else MaterialTheme.colorScheme.primary,
                        contentColor =
                            if (isRecording) MaterialTheme.colorScheme.onTertiary
                            else MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector =
                            if (isRecording) Icons.Rounded.Stop
                            else Icons.Rounded.PlayArrow,
                        contentDescription =
                            if (isRecording) stringResource(R.string.stop_route)
                            else stringResource(R.string.start_route)
                    )
                }
            }
        }
    }
}
