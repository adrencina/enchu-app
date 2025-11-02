package com.adrencina.enchu.ui.screens.obra_detail.files

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems

@Composable
fun FilesScreen(
    viewModel: FilesViewModel = hiltViewModel()
) {
    val files = viewModel.files.collectAsLazyPagingItems()

    Box(modifier = Modifier.fillMaxSize()) {
        if (files.itemCount == 0) {
            Text(text = "No files yet", modifier = Modifier.align(Alignment.Center))
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(files.itemCount) { index ->
                    val file = files[index]
                    file?.let {
                        Text(text = it.fileName)
                    }
                }
            }
        }
    }
}
