package com.example.taller2.viewmodels

import android.content.Context
import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.example.taller2.R
import com.example.taller2.utils.ImageFileUtils
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Date

data class CameraPreviewState(
    val surfaceRequest: SurfaceRequest? = null,
    val useBackLens: Boolean = true,
)

class CameraPreviewViewModel : ViewModel() {
    // Used to set up a link between the Camera and your UI.
    private val _uiState = MutableStateFlow(CameraPreviewState())
    val uiState: StateFlow<CameraPreviewState> = _uiState.asStateFlow()

    private var lifecycleOwner: LifecycleOwner? = null
    private var processCameraProvider: ProcessCameraProvider? = null
    private var selector = CameraSelector.DEFAULT_BACK_CAMERA
    private val imageCapture = ImageCapture.Builder()
        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
        .build()

    private val cameraPreviewUseCase = Preview.Builder().build().apply {
        setSurfaceProvider { newSurfaceRequest ->
            _uiState.update { it.copy(surfaceRequest = newSurfaceRequest) }
        }
    }

    suspend fun bindToCamera(appContext: Context, lifecycleOwner: LifecycleOwner) {
        this.lifecycleOwner = lifecycleOwner
        processCameraProvider = ProcessCameraProvider.awaitInstance(appContext)
        processCameraProvider?.unbindAll()
        processCameraProvider?.bindToLifecycle(
            lifecycleOwner, selector, cameraPreviewUseCase, imageCapture
        )

        try {
            awaitCancellation()
        } finally {
            processCameraProvider?.unbindAll()
        }
    }

    fun toggleCameraLens() {
        val nextUseBackLens = !_uiState.value.useBackLens
        val nextSelector = if (nextUseBackLens) {
            CameraSelector.DEFAULT_BACK_CAMERA
        } else {
            CameraSelector.DEFAULT_FRONT_CAMERA
        }

        val owner = lifecycleOwner
        val provider = processCameraProvider
        // Si el dispositivo no tiene esa cámara (p. ej. tablet sin cámara trasera) no se cambia
        if (provider != null && !provider.hasCamera(nextSelector)) return

        _uiState.update { it.copy(useBackLens = nextUseBackLens) }
        selector = nextSelector
        if (owner != null && provider != null) {
            provider.unbindAll()
            provider.bindToLifecycle(owner, selector, cameraPreviewUseCase, imageCapture)
        }
    }

    // Toma la foto y la guarda en la galería. Devuelve la uri y el nombre del archivo (null si falla)
    fun takePhoto(context: Context, onPhotoCaptured: (Uri?, String) -> Unit = { _, _ -> }) {
        val name = "${context.getString(R.string.photo_prefix)}${Date().time}"
        val outputOptions = ImageFileUtils.createGalleryOutputOptions(context, name)

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    onPhotoCaptured(outputFileResults.savedUri, "$name.jpg")
                }

                override fun onError(exception: ImageCaptureException) {
                    onPhotoCaptured(null, name)
                }
            }
        )
    }
}
