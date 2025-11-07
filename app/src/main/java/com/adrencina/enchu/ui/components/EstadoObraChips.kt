package com.adrencina.enchu.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.adrencina.enchu.core.resources.AppStrings
import com.adrencina.enchu.ui.theme.Dimens
import com.adrencina.enchu.ui.theme.EnchuTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EstadoObraChips(
    selectedState: String,
    onStateSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true // opcional: exponer enabled si querés deshabilitar todo el grupo
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
            val isSelected = state == selectedState

            FilterChip(
                selected = isSelected,                // obligatorio
                onClick = { onStateSelected(state) },
                enabled = enabled,
                label = { Text(state) },
                shape = MaterialTheme.shapes.medium,
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = if (isSelected)
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    else
                        MaterialTheme.colorScheme.surface,
                    labelColor = if (isSelected)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = enabled,                  // <- obligatorio
                    selected = isSelected,              // <- obligatorio
                    borderColor = MaterialTheme.colorScheme.outline,
                    selectedBorderColor = MaterialTheme.colorScheme.primary,
                    borderWidth = 1.dp,
                    selectedBorderWidth = 1.dp
                )
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun EstadoObraChipsPreview() {
    EnchuTheme {
        Column {
            EstadoObraChips(selectedState = AppStrings.stateBudgeted, onStateSelected = {})
            Spacer(modifier = Modifier.height(Dimens.PaddingMedium))
            EstadoObraChips(selectedState = "", onStateSelected = {})
        }
    }
}