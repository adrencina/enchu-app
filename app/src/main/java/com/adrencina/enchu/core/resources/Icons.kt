package com.adrencina.enchu.core.resources

import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector
import com.adrencina.enchu.R

object AppIcons {
    // Drawables
    @DrawableRes val GoogleLogo = R.drawable.ic_google_logo

    // Material Icons
    val Add: ImageVector = Icons.Default.Add
    val Search: ImageVector = Icons.Default.Search
    val MoreVert: ImageVector = Icons.Default.MoreVert
    val ExpandLess: ImageVector = Icons.Default.ExpandLess
    val ExpandMore: ImageVector = Icons.Default.ExpandMore

    val ArrowBack = Icons.AutoMirrored.Filled.ArrowBack
    val Close = Icons.Default.Close

    @DrawableRes
    val Gallery = R.drawable.ic_gallery
}