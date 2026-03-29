package com.emul8r.bizap.ui.components.classic

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun ClassicPhotoAttachmentPicker(
    photos: List<String>,
    onPhotosChange: (List<String>) -> Unit,
    onAddPhotoClicked: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Attachments",
            style = MaterialTheme.typography.labelMedium
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(photos.size) { index ->
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .border(1.dp, Color.Gray, shape = RoundedCornerShape(4.dp))
                        .background(Color.LightGray, shape = RoundedCornerShape(4.dp))
                ) {
                    AsyncImage(
                        model = photos[index],
                        contentDescription = "Attachment $index",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    IconButton(
                        onClick = {
                            onPhotosChange(photos.filterIndexed { i, _ -> i != index })
                        },
                        modifier = Modifier
                            .align(androidx.compose.ui.Alignment.TopEnd)
                            .size(32.dp)
                            .background(Color.Red, shape = RoundedCornerShape(50))
                    ) {
                        Icon(
                            Icons.Default.Close,
                            "Remove",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        Button(
            onClick = { onAddPhotoClicked?.invoke() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Photo")
        }
    }
}

