package com.example.taller2.ui.components

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.taller2.ui.theme.AppTheme

// Marcador del mapa: título (nombre de la foto) + miniatura de la foto + punta que señala la ubicación
@Composable
fun PhotoMarker(
    name: String,
    uri: Uri?,
    modifier: Modifier = Modifier
) {
    val color = MaterialTheme.colorScheme.primary

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .background(color, RoundedCornerShape(8.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        )
        Spacer(Modifier.height(4.dp))
        Card(
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(3.dp, color),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            AsyncImage(
                model = uri,
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(64.dp)
            )
        }
        Canvas(
            modifier = Modifier
                .width(16.dp)
                .height(10.dp)
        ) {
            val tip = Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width, 0f)
                lineTo(size.width / 2, size.height)
                close()
            }
            drawPath(path = tip, color = color)
        }
    }
}

@PreviewLightDark
@Preview(showBackground = true)
@Composable
fun PhotoMarkerPreview() {
    AppTheme {
        PhotoMarker(name = "RUTA_IMG_1774883431126.jpg", uri = null)
    }
}
