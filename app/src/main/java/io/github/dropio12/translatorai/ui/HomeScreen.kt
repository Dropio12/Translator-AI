package io.github.dropio12.translatorai.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.dropio12.translatorai.R
import io.github.dropio12.translatorai.domain.LanguageCodes
import io.github.dropio12.translatorai.domain.Patch

class HomeActions(
    val onCamera: () -> Unit,
    val onLive: () -> Unit,
    val onGallery: () -> Unit,
    val onSample: () -> Unit,
    val onSource: (String?) -> Unit,
    val onTarget: (String) -> Unit,
    val onShowOriginal: (Boolean) -> Unit,
    val onRetry: () -> Unit,
)

@Composable
fun HomeScreen(state: UiState, actions: HomeActions) {
    Scaffold(
        topBar = {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            )
        },
        bottomBar = { ActionBar(actions) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { LanguageRow(state, actions) }
            item {
                Column {
                    StatusLine(state.status)
                    if (state.status == Status.DownloadFailed) {
                        TextButton(onClick = actions.onRetry) { Text(stringResource(R.string.try_again)) }
                    }
                }
            }

            val image = state.image
            if (image == null) {
                if (!state.status.isBusy) {
                    item { Text(stringResource(R.string.home_empty), style = MaterialTheme.typography.bodyLarge) }
                }
            } else {
                item { TranslatedImage(image, state.patches, state.showOriginal) }
            }

            if (state.patches.isNotEmpty()) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.show_original), modifier = Modifier.weight(1f))
                        Switch(checked = state.showOriginal, onCheckedChange = actions.onShowOriginal)
                    }
                }
                item { Text(stringResource(R.string.list_title), style = MaterialTheme.typography.titleMedium) }
                items(state.patches) { patch -> PatchRow(patch) }
            }
        }
    }
}

@Composable
private fun LanguageRow(state: UiState, actions: HomeActions) {
    val autoDetect = stringResource(R.string.auto_detect)
    val sourceOptions = remember(state.languages, autoDetect) {
        listOf<Pair<String?, String>>(null to autoDetect) + state.languages.map { it to LanguageCodes.displayName(it) }
    }
    val targetOptions = remember(state.languages) {
        state.languages.map { it to LanguageCodes.displayName(it) }
    }
    val sourceName = state.source?.let { LanguageCodes.displayName(it) } ?: autoDetect
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        LanguageMenu(
            label = stringResource(R.string.from_label, sourceName),
            options = sourceOptions,
            onSelect = actions.onSource,
            modifier = Modifier.weight(1f),
        )
        LanguageMenu(
            label = stringResource(R.string.to_label, LanguageCodes.displayName(state.target)),
            options = targetOptions,
            onSelect = actions.onTarget,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
fun StatusLine(status: Status, modifier: Modifier = Modifier) {
    val message = statusMessage(status) ?: return
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (status.isBusy) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        Text(message, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun statusMessage(status: Status): String? = when (status) {
    Status.Idle -> null
    Status.Reading -> stringResource(R.string.status_reading)
    Status.Detecting -> stringResource(R.string.status_detecting)
    is Status.Downloading -> stringResource(
        R.string.status_downloading,
        LanguageCodes.displayName(status.source),
        LanguageCodes.displayName(status.target),
    )
    is Status.Translating -> stringResource(R.string.status_translating, status.done, status.total)
    is Status.Done -> when {
        status.source == status.target ->
            stringResource(R.string.status_same_language, LanguageCodes.displayName(status.source))
        status.detected -> stringResource(
            R.string.status_done_detected,
            LanguageCodes.displayName(status.source),
            LanguageCodes.displayName(status.target),
        )
        else -> stringResource(
            R.string.status_done_chosen,
            LanguageCodes.displayName(status.source),
            LanguageCodes.displayName(status.target),
        )
    }
    Status.NoText -> stringResource(R.string.status_no_text)
    Status.UnknownLanguage -> stringResource(R.string.status_unknown_language)
    Status.DownloadFailed -> stringResource(R.string.status_download_failed)
    Status.Failed -> stringResource(R.string.status_failed)
    Status.CameraDenied -> stringResource(R.string.status_camera_denied)
}

@Composable
private fun TranslatedImage(image: Bitmap, patches: List<Patch>, showOriginal: Boolean) {
    val bitmap = remember(image) { image.asImageBitmap() }
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .heightIn(max = 520.dp)
                .aspectRatio(image.width.toFloat() / image.height)
                .clip(RoundedCornerShape(12.dp)),
        ) {
            Image(
                bitmap = bitmap,
                contentDescription = stringResource(R.string.image_description),
                contentScale = ContentScale.Fit,
                modifier = Modifier.matchParentSize(),
            )
            if (!showOriginal) {
                TranslationOverlay(
                    patches = patches,
                    modifier = Modifier.matchParentSize(),
                    imageWidth = image.width,
                    imageHeight = image.height,
                )
            }
        }
    }
}

@Composable
private fun PatchRow(patch: Patch) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = patch.original,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(text = patch.translation, style = MaterialTheme.typography.bodyLarge)
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
private fun ActionBar(actions: HomeActions) {
    Surface(tonalElevation = 3.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ActionButton(stringResource(R.string.action_camera), actions.onCamera, Modifier.weight(1f))
            ActionButton(stringResource(R.string.action_live), actions.onLive, Modifier.weight(1f))
            ActionButton(stringResource(R.string.action_gallery), actions.onGallery, Modifier.weight(1f))
            ActionButton(stringResource(R.string.action_sample), actions.onSample, Modifier.weight(1f))
        }
    }
}

@Composable
private fun ActionButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
    ) {
        Text(text, maxLines = 1)
    }
}
