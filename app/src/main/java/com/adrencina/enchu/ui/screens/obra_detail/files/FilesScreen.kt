package com.adrencina.enchu.ui.screens.obra_detail.files

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.adrencina.enchu.ui.theme.Dimens

@Composable
fun FilesScreen(
    viewModel: FilesViewModel = hiltViewModel()
) {
    val lazyPagingItems = viewModel.files.collectAsLazyPagingItems()

    Box(modifier = Modifier.fillMaxSize()) {
        when (lazyPagingItems.loadState.refresh) {
            is LoadState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            is LoadState.Error -> {
                Text(
                    text = "Error al cargar los archivos.",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.error
                )
            }
            is LoadState.NotLoading -> {
                if (lazyPagingItems.itemCount == 0) {
                    Text(
                        text = "No hay archivos subidos.",
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(Dimens.PaddingSmall)
                    ) {
                        items(lazyPagingItems.itemCount, key = { index -> lazyPagingItems.peek(index)?.fileId ?: index }) {
                            val file = lazyPagingItems[it]
                            if (file != null) {
                                FileItem(file = file)
                                Divider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            }
                        }

                        if (lazyPagingItems.loadState.append is LoadState.Loading) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(Dimens.PaddingMedium)) {
                                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}