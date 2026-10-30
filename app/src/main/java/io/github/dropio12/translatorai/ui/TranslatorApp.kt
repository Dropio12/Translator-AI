package io.github.dropio12.translatorai.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** Shows the home screen or the camera, and asks for the camera permission when needed. */
@Composable
fun TranslatorApp(viewModel: TranslatorViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var liveRequested by rememberSaveable { mutableStateOf(false) }
    val askCamera = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) viewModel.openCamera(live = liveRequested) else viewModel.onCameraDenied()
    }
    val openCamera = { live: Boolean ->
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        if (granted) {
            viewModel.openCamera(live)
        } else {
            liveRequested = live
            askCamera.launch(Manifest.permission.CAMERA)
        }
    }
    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let(viewModel::onImagePicked)
    }

    when (state.screen) {
        Screen.Camera -> CameraScreen(
            state = state,
            onClose = viewModel::closeCamera,
            onLiveChange = viewModel::setLive,
            onPhoto = viewModel::onPhotoCaptured,
            onPhotoFailed = viewModel::onPhotoFailed,
            onLiveBlocks = viewModel::onLiveBlocks,
        )
        Screen.Home -> HomeScreen(
            state = state,
            actions = HomeActions(
                onCamera = { openCamera(false) },
                onLive = { openCamera(true) },
                onGallery = {
                    pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                onSample = viewModel::onSampleRequested,
                onSource = viewModel::setSource,
                onTarget = viewModel::setTarget,
                onShowOriginal = viewModel::setShowOriginal,
                onRetry = viewModel::retry,
            ),
        )
    }
}
