package ca.kallistesoft.hunt.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ImageNotSupported
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ZoomOutMap
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ca.kallistesoft.hunt.core.designsystem.theme.Sizes
import ca.kallistesoft.hunt.core.designsystem.theme.Spacing
import ca.kallistesoft.hunt.core.model.Question
import coil3.SingletonImageLoader
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest

/** Coil model for a bundled photo (`wildlife/abc.jpg` in the assets). */
fun assetPhoto(path: String): String = "file:///android_asset/$path"

/**
 * Photo of a "What is it?" question with the credit its free licence requires. The box has the photo's exact shape
 * before it loads (no jump), capped at [maxHeightFraction] of the screen so the choices stay visible.
 */
@Composable
fun QuestionPhoto(question: Question, modifier: Modifier = Modifier, maxHeightFraction: Float = 0.42f) {
    val image = question.image ?: return
    val links = rememberLinkOpener()
    val description = question.text(currentLang()).q
    var zoomed by rememberSaveable { mutableStateOf(false) }
    val screenHeight = with(LocalDensity.current) { LocalWindowInfo.current.containerSize.height.toDp() }

    Column(modifier.fillMaxWidth()) {
        BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            val aspect = question.imageAspect ?: (4f / 3f)
            // The window size can be unknown (0) in previews: then only the width limits the photo.
            val maxHeight = if (screenHeight.value > 0f) screenHeight * maxHeightFraction else Dp.Infinity
            val height = min(maxWidth / aspect, maxHeight)
            Box(
                Modifier
                    .size(width = height * aspect, height = height)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    .clickable(onClickLabel = stringResource(R.string.zoom_photo)) { zoomed = true }
                    .testTag("question-photo"),
            ) {
                // Previews and screenshot tests draw a fixed placeholder instead of loading the photo.
                if (LocalInspectionMode.current) {
                    Icon(Icons.Outlined.Photo, contentDescription = null, Modifier.size(Sizes.iconXl).align(Alignment.Center))
                } else {
                    SubcomposeAsyncImage(
                        model = assetPhoto(image),
                        contentDescription = description,
                        // Never crop: heads and tails must stay visible.
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize(),
                        loading = {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(Modifier.size(Sizes.loader), strokeWidth = Sizes.loaderStroke)
                            }
                        },
                        error = {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(Icons.Outlined.ImageNotSupported, contentDescription = null, Modifier.size(Sizes.iconXl))
                            }
                        },
                    )
                }
                Icon(
                    Icons.Rounded.ZoomOutMap,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(Spacing.s)
                        .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                        .padding(Sizes.badgeInsetS)
                        .size(Sizes.iconM),
                )
            }
        }
        question.imageCredit?.let { credit ->
            TextButton(
                onClick = { links.open(credit.page) },
                modifier = Modifier.align(Alignment.End),
            ) {
                Text(
                    stringResource(R.string.photo_by, credit.author, credit.license),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
    if (zoomed) FullPhoto(image, description, onDismiss = { zoomed = false })
}

/** Full-screen photo with pinch-to-zoom, drawn edge to edge; closing resets the zoom. */
@Composable
private fun FullPhoto(image: String, description: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        var scale by remember { mutableFloatStateOf(1f) }
        var offset by remember { mutableStateOf(Offset.Zero) }
        val state = rememberTransformableState { zoom, pan, _ ->
            scale = (scale * zoom).coerceIn(1f, 5f)
            offset = if (scale == 1f) Offset.Zero else offset + pan
        }
        Box(Modifier.fillMaxSize().background(Color.Black)) {
            SubcomposeAsyncImage(
                model = assetPhoto(image),
                contentDescription = description,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .transformable(state)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offset.x
                        translationY = offset.y
                    },
            )
            IconButton(onClick = onDismiss, modifier = Modifier.safeDrawingPadding().align(Alignment.TopStart)) {
                Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.back), tint = Color.White)
            }
        }
    }
}

/** Small thumbnail for lists (results screen); whole photo, never cropped. */
@Composable
fun PhotoThumb(image: String, modifier: Modifier = Modifier) {
    val box = modifier
        .size(Sizes.photoThumb)
        .clip(MaterialTheme.shapes.small)
        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
    if (LocalInspectionMode.current) {
        Box(box)
    } else {
        SubcomposeAsyncImage(
            model = assetPhoto(image),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = box,
        )
    }
}

/** Loads [image] ahead of time (the next question's photo), so it appears without a wait. */
@Composable
fun PrefetchPhoto(image: String?) {
    if (image == null || LocalInspectionMode.current) return
    val context = LocalContext.current
    LaunchedEffect(image) {
        SingletonImageLoader.get(context).enqueue(ImageRequest.Builder(context).data(assetPhoto(image)).build())
    }
}
