package com.example.taller2.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.taller2.R
import com.example.taller2.data.RoutePhoto
import com.example.taller2.ui.theme.AppTheme

// Lista horizontal con las fotos tomadas durante el recorrido
@Composable
fun PhotoListComponent(
    photos: List<RoutePhoto>,
    modifier: Modifier = Modifier
) {
    if (photos.isEmpty()) {
        Row(
            modifier = modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.PhotoLibrary,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.no_photos),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        val listState = rememberLazyListState()

        // Muestra siempre la última foto tomada
        LaunchedEffect(photos.size) {
            listState.animateScrollToItem(photos.lastIndex)
        }

        LazyRow(
            state = listState,
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items = photos, key = { it.uri.toString() }) { photo ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .animateItem()
                        .fillMaxHeight()
                        .aspectRatio(0.75f)
                ) {
                    AsyncImage(
                        model = photo.uri,
                        contentDescription = photo.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@PreviewLightDark
@Preview(showBackground = true)
@Composable
fun PhotoListComponentPreview() {
    AppTheme {
        PhotoListComponent(photos = emptyList(), modifier = Modifier.height(120.dp))
    }
}
