package com.productivize.ui.journal.components

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.productivize.utils.rememberMediaPicker

@Composable
fun AttachmentGallery(uris: List<String>, onAdd: (List<String>) -> Unit) {
    val mediaPicker = rememberMediaPicker { uri ->
        uri?.let {
            onAdd(uris + it.toString())
        }
    }
    
    LazyRow(modifier = Modifier.padding(8.dp)) {
        items(uris) { uri ->
            AttachmentCard(uri = uri)
        }
        item {
            AddAttachmentCard(onClick = {
                mediaPicker.pickImage()
            })
        }
    }
}

@Composable
fun AttachmentCard(uri: String) {
    Card(modifier = Modifier.size(120.dp).padding(8.dp)) {
        AsyncImage(
            model = uri,
            contentDescription = "Attachment",
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun AddAttachmentCard(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .size(120.dp)
            .padding(8.dp)
    ) {
        Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
            Icon(Icons.Default.Add, contentDescription = "Add attachment")
        }
    }
} 