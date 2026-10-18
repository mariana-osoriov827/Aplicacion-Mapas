package com.example.taller2.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.camera.compose.CameraXViewfinder
import androidx.camera.viewfinder.core.ImplementationMode
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Camera
import androidx.compose.material.icons.rounded.Cameraswitch
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.taller2.R
import com.example.taller2.viewmodels.CameraPreviewViewModel

@Composable
fun CameraPreviewContent(
    modifier: Modifier = Modifier,
    viewModel: CameraPreviewViewModel = viewModel(),
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onPhotoTaken: (Uri, String) -> Unit = { _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(lifecycleOwner) {
        viewModel.bindToCamera(context.applicationContext, lifecycleOwner)
    }

    // clipToBounds: la vista previa no se sale del recuadro de la cámara
    Box(modifier = modifier.clipToBounds()) {
        uiState.surfaceRequest?.let { request ->
            // EMBEDDED (TextureView) respeta el recorte y las esquinas redondeadas de la tarjeta;
            // el modo por defecto (SurfaceView) se dibuja por encima y se pasa al lado del mapa
            CameraXViewfinder(
                surfaceRequest = request,
                modifier = Modifier.fillMaxSize(),
                implementationMode = ImplementationMode.EMBEDDED,
                contentScale = ContentScale.Crop
            )
        }

        // Cambiar entre cámara frontal / trasera
        FilledIconButton(
            onClick = { viewModel.toggleCameraLens() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .size(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Icon(
                imageVector = Icons.Rounded.Cameraswitch,
                contentDescription = stringResource(R.string.switch_camera)
            )
        }

        // Tomar foto
        FilledIconButton(
            onClick = {
                viewModel.takePhoto(context = context, onPhotoCaptured = { uri, name ->
                    if (uri != null) {
                        onPhotoTaken(uri, name)
                        Toast.makeText(
                            context,
                            context.getString(R.string.photo_saved, name),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(context, R.string.photo_error, Toast.LENGTH_SHORT).show()
                    }
                })
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .size(72.dp),
            shape = CircleShape,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Icon(
                imageVector = Icons.Rounded.Camera,
                contentDescription = stringResource(R.string.take_photo),
                modifier = Modifier.size(40.dp)
            )
        }
    }
}
