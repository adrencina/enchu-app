package com.adrencina.enchu.ui.screens.obra_detail.files

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.adrencina.enchu.core.resources.AppIcons
import com.adrencina.enchu.data.model.FileEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.ln
import kotlin.math.pow

@Composable
fun FileItem(
    file: FileEntity, 
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = {
            Text(
                text = file.fileName,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Normal)
            )
        },
        supportingContent = {
            val formattedSize = formatFileSize(file.size)
            val formattedDate = formatDate(file.createdAt)
            Text(text = "$formattedSize • $formattedDate")
        },
        leadingContent = {
            FileIcon(fileExtension = file.fileName.substringAfterLast('.', ""))
        },
        trailingContent = {
            IconButton(onClick = { /* TODO: Handle more options */ }) {
                Icon(
                    imageVector = AppIcons.MoreVert,
                    contentDescription = "Más opciones",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    )
}

@Composable
private fun FileIcon(fileExtension: String) {
    Surface(
        modifier = Modifier.size(48.dp),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Article,
                contentDescription = "Icono de archivo",
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = fileExtension.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private fun formatFileSize(sizeInBytes: Long): String {
    if (sizeInBytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (ln(sizeInBytes.toDouble()) / ln(1024.0)).toInt()
    return String.format("%.1f %s", sizeInBytes / 1024.0.pow(digitGroups.toDouble()), units[digitGroups])
}

private fun formatDate(date: Date): String {
    val pattern = "dd MMM yyyy"
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    return sdf.format(date)
}