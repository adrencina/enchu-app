package com.adrencina.enchu.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import com.adrencina.enchu.data.model.Cliente
import com.adrencina.enchu.ui.theme.EnchuTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppClientSelector(
    clientes: List<Cliente>,
    selectedCliente: Cliente?,
    onClienteSelected: (Cliente) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    // MODIFIED START: Corregido el typo de mutableState of a mutableStateOf
    var isExpanded by remember { mutableStateOf(false) }
    // MODIFIED END

    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = { isExpanded = it },
        modifier = modifier.testTag("app_client_selector")
    ) {
        OutlinedTextField(
            value = selectedCliente?.nombre ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
            shape = MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            ),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false }
        ) {
            clientes.forEach { cliente ->
                DropdownMenuItem(
                    text = { Text(cliente.nombre) },
                    onClick = {
                        onClienteSelected(cliente)
                        isExpanded = false
                    },
                    modifier = Modifier.testTag("client_option_${cliente.id}")
                )
            }
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun AppClientSelectorPreview() {
    EnchuTheme {
        val clientes = listOf(
            Cliente(id = "1", nombre = "Constructora Acme"),
            Cliente(id = "2", nombre = "Inversiones Beta"),
        )
        AppClientSelector(
            clientes = clientes,
            selectedCliente = clientes.first(),
            onClienteSelected = {},
            label = "Cliente"
        )
    }
}