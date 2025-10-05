package com.adrencina.enchu.ui.components

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
    // MODIFIED: Adaptado para no usar 'label' y en su lugar un placeholder.
    placeholder: String,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = { isExpanded = it },
        modifier = modifier.testTag("app_client_selector")
    ) {
        OutlinedTextField(
            value = selectedCliente?.nombre ?: "",
            onValueChange = {},
            readOnly = true,
            // MODIFIED: Usando placeholder en lugar de label
            placeholder = { Text(placeholder) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
            shape = MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
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
@Composable
private fun AppClientSelectorPreview() {
    EnchuTheme {
        val clientes = listOf(
            Cliente(id = "1", nombre = "Constructora Acme"),
            Cliente(id = "2", nombre = "Inversiones Beta"),
        )
        AppClientSelector(
            clientes = clientes,
            selectedCliente = null,
            onClienteSelected = {},
            placeholder = "Ej: Juan Pérez"
        )
    }
}