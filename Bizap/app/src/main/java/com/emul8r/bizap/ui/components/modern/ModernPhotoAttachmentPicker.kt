package com.emul8r.bizap.ui.components.modern

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import coil.compose.AsyncImage

@Composable
fun ModernPhotoAttachmentPicker(
    photos: List<String>,
    onPhotosChange: (List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Attachments",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(photos.size) { index ->
                Card(
                    modifier = Modifier.size(120.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
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
                                .size(40.dp)
                                .padding(4.dp)
                                .background(Color.Red, shape = RoundedCornerShape(50))
                        ) {
                            Icon(
                                Icons.Default.Close,
                                "Remove",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        Button(
            onClick = { /* Open camera/gallery - to be implemented */ },
            modifier = Modifier
                .align(androidx.compose.ui.Alignment.CenterHorizontally)
                .padding(top = 12.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("+ Add Photo")
        }
    }
}

