package com.adrencina.enchu.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.adrencina.enchu.core.resources.AppStrings
import com.adrencina.enchu.ui.theme.Dimens
import com.adrencina.enchu.ui.theme.EnchuTheme
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EstadoObraChips(
    selectedState: String,
    onStateSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val states = listOf(
        AppStrings.stateBudgeted,
        AppStrings.stateFinished,
        AppStrings.statePaused,
        AppStrings.stateInProgress
    )

    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.PaddingSmall),
        verticalArrangement = Arrangement.spacedBy(Dimens.PaddingSmall)
    ) {
        states.forEach { state ->
            FilterChip(
                selected = state == selectedState,
                onClick = { onStateSelected(state) },
                label = { Text(state) },
                shape = MaterialTheme.shapes.small,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EstadoObraChipsPreview() {
    EnchuTheme {
        EstadoObraChips(selectedState = AppStrings.stateBudgeted, onStateSelected = {})
    }
}