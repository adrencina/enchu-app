package com.adrencina.enchu.ui.screens.obra_detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.adrencina.enchu.core.resources.AppIcons
import com.adrencina.enchu.core.resources.AppStrings
import com.adrencina.enchu.ui.theme.Dimens
import com.adrencina.enchu.ui.theme.EnchuTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObraDetailScreen(
    obraId: String,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = { ObraDetailTopAppBar(onNavigateBack = onNavigateBack) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = Dimens.PaddingMedium),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Detalles de la Obra ID: $obraId")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ObraDetailTopAppBar(onNavigateBack: () -> Unit) {
    SmallTopAppBar(
        modifier = Modifier.height(Dimens.TopBarHeight),
        title = { /* No title */ },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = AppIcons.ArrowBack,
                    contentDescription = AppStrings.back
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun ObraDetailScreenPreview() {
    EnchuTheme {
        ObraDetailScreen(obraId = "preview-123", onNavigateBack = {})
    }
}
