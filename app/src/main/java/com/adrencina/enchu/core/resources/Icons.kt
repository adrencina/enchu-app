package com.adrencina.enchu.core.resources

import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
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

    val ArrowBack = Icons.AutoMirrored.Filled.ArrowBack
    val Close = Icons.Default.Close

    val Gallery: ImageVector = Icons.Default.Home
}