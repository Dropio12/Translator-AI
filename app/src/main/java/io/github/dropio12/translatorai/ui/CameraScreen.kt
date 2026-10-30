package io.github.dropio12.translatorai.ui

import androidx.activity.compose.BackHandler
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import io.github.dropio12.translatorai.R
import io.github.dropio12.translatorai.data.LiveTextAnalyzer
import io.github.dropio12.translatorai.domain.RecognizedBlock
import java.util.concurrent.Executors

/**
 * Camera preview with a shutter button. With "Live" on, every frame is read and the
 * translations are drawn over the preview as the camera moves.
 */
@Composable
fun CameraScreen(
    state: UiState,
    onClose: () -> Unit,
    onLiveChange: (Boolean) -> Unit,
    onPhoto: (ImageProxy) -> Unit,
    onPhotoFailed: () -> Unit,
    onLiveBlocks: (List<RecognizedBlock>) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val controller = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_CAPTURE or CameraController.IMAGE_ANALYSIS)
        }
    }
    // Frames for live mode and photo captures are handled off the main thread.
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val currentOnLiveBlocks by rememberUpdatedState(onLiveBlocks)
    val liveAnalyzer = remember { LiveTextAnalyzer(cameraExecutor) { blocks -> currentOnLiveBlocks(blocks) } }
    var capturing by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        controller.bindToLifecycle(lifecycleOwner)
        onDispose {
            controller.clearImageAnalysisAnalyzer()
            controller.unbind()
            liveAnalyzer.close()
            cameraExecutor.shutdown()
        }
    }
    LaunchedEffect(state.live) {
        if (state.live) {
            controller.setImageAnalysisAnalyzer(cameraExecutor, liveAnalyzer.analyzer)
        } else {
            controller.clearImageAnalysisAnalyzer()
        }
    }
    BackHandler(onBack = onClose)

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        AndroidView(
            factory = { viewContext ->
                PreviewView(viewContext).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    this.controller = controller
                }
            },
            modifier = Modifier.fillMaxSize(),
        )
        if (state.live) {
            TranslationOverlay(patches = state.livePatches, modifier = Modifier.fillMaxSize())
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onClose) { Text(stringResource(R.string.close), color = Color.White) }
                Spacer(Modifier.weight(1f))
                Text(stringResource(R.string.action_live), color = Color.White)
                Spacer(Modifier.width(8.dp))
                Switch(checked = state.live, onCheckedChange = onLiveChange)
            }
            if (state.live) {
                val hint = stringResource(R.string.live_hint)
                val message = if (state.liveStatus == Status.NoText) hint else statusMessage(state.liveStatus) ?: hint
                Text(
                    text = message,
                    color = Color.White,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                )
            }
        }

        val takePhoto = stringResource(R.string.take_photo)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            Box(
                Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(if (capturing) Color.Gray else Color.White)
                    .border(5.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                    .semantics { contentDescription = takePhoto }
                    .clickable(enabled = !capturing, role = Role.Button) {
                        capturing = true
                        controller.takePicture(
                            cameraExecutor,
                            object : ImageCapture.OnImageCapturedCallback() {
                                override fun onCaptureSuccess(image: ImageProxy) = onPhoto(image)

                                override fun onError(exception: ImageCaptureException) {
                                    capturing = false
                                    onPhotoFailed()
                                }
                            },
                        )
                    },
            )
        }
    }
}
