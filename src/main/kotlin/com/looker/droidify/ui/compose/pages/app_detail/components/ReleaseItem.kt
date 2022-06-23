package com.looker.droidify.ui.compose.pages.app_detail.components

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.looker.droidify.R
import com.looker.droidify.RELEASE_STATE_INSTALLED
import com.looker.droidify.RELEASE_STATE_NONE
import com.looker.droidify.RELEASE_STATE_SUGGESTED
import com.looker.droidify.database.entity.Release
import com.looker.droidify.database.entity.Repository
import com.looker.droidify.utility.extension.android.Android
import com.looker.droidify.utility.extension.text.formatSize
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

@Composable
fun ReleaseItem(
    modifier: Modifier = Modifier,
    release: Release,
    repository: Repository,
    releaseState: Int = RELEASE_STATE_NONE,
    onDownloadClick: (Release) -> Unit = {},
    onLongClick: (Release) -> Unit = {},
) {
    val currentRelease by remember { mutableStateOf(release) }
    val isInstalled = releaseState == RELEASE_STATE_INSTALLED
    val isSuggested = releaseState == RELEASE_STATE_SUGGESTED
    val highlight by animateDpAsState(targetValue = if (isSuggested or isInstalled) 8.dp else 0.dp)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = MaterialTheme.shapes.large,
        tonalElevation = highlight
    ) {
        ReleaseItemContent(
            release = currentRelease,
            repository = repository,
            isSuggested = isSuggested,
            isInstalled = isInstalled,
            onDownloadClick = onDownloadClick,
            onLongClick = onLongClick
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReleaseItemContent(
    modifier: Modifier = Modifier,
    release: Release,
    repository: Repository,
    isSuggested: Boolean = false,
    isInstalled: Boolean = false,
    onDownloadClick: (Release) -> Unit = {},
    onLongClick: (Release) -> Unit = {}
) {
    Row(
        modifier = Modifier
            .combinedClickable(onClick = {}, onLongClick = { onLongClick(release) })
            .padding(end = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        IconButton(onClick = { onDownloadClick(release) }) {
            Icon(
                imageVector = Icons.Rounded.Download,
                contentDescription = "Download this version"
            )
        }
        Column(
            modifier = modifier
                .height(76.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            ReleaseTitleWithBadge(
                modifier = Modifier.weight(1f, true),
                version = release.version,
                added = if (Android.sdk(Build.VERSION_CODES.O)) {
                    LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(release.added),
                        TimeZone.getDefault().toZoneId()
                    ).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
                } else ""
            ) {
                AnimatedVisibility(
                    visible = isSuggested or isInstalled,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    val badgeText = remember { mutableStateOf(R.string.suggested) }
                    LaunchedEffect(isInstalled, isSuggested) {
                        badgeText.value =
                            if (isInstalled) R.string.installed else R.string.suggested
                    }
                    ReleaseBadge(
                        modifier = Modifier.padding(top = 8.dp),
                        text = stringResource(id = badgeText.value)
                    )
                }
            }
            ReleaseItemBottomText(
                modifier = Modifier.weight(1.2f, true),
                repository = repository.name,
                size = release.size.formatSize()
            )
            Spacer(modifier = Modifier.width(Dp.Hairline))
        }
    }
}

@Composable
fun ReleaseTitleWithBadge(
    modifier: Modifier = Modifier,
    version: String,
    added: String,
    badges: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        Text(text = version, style = MaterialTheme.typography.titleMedium)
        badges()
        Spacer(Modifier.weight(1f))
        Text(text = added, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun ReleaseItemBottomText(
    modifier: Modifier = Modifier,
    repository: String,
    size: String
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(id = R.string.provided_by_FORMAT, repository),
            style = MaterialTheme.typography.labelMedium
        )
        Spacer(Modifier.weight(1f))
        Text(text = size, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun ReleaseBadge(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = MaterialTheme.colorScheme.secondaryContainer,
    onColor: Color = MaterialTheme.colorScheme.onSecondaryContainer
) {
    Surface(
        modifier = modifier
            .background(color, Shapes.Full)
            .padding(6.dp, 2.dp),
        color = color
    ) {
        Text(text = text, color = onColor, style = MaterialTheme.typography.labelMedium)
    }
}