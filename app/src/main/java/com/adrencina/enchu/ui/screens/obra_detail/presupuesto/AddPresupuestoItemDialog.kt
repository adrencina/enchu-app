package com.adrencina.enchu.ui.screens.obra_detail.presupuesto

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.adrencina.enchu.data.model.PresupuestoItem

@Composable
fun AddPresupuestoItemDialog(
    onDismiss: () -> Unit,
    onConfirm: (PresupuestoItem) -> Unit
) {
    var descripcion by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("1") }
    var precioUnitario by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("MATERIAL") } // "MATERIAL" o "MANO_DE_OBRA"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar Ítem al Presupuesto") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = cantidad,
                        onValueChange = { cantidad = it },
                        label = { Text("Cant.") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = precioUnitario,
                        onValueChange = { precioUnitario = it },
                        label = { Text("Precio Unit.") },
                        modifier = Modifier.weight(1.5f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                Text(text = "Tipo:", style = MaterialTheme.typography.bodyMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = tipo == "MATERIAL",
                        onClick = { tipo = "MATERIAL" }
                    )
                    Text("Material")
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(
                        selected = tipo == "MANO_DE_OBRA",
                        onClick = { tipo = "MANO_DE_OBRA" }
                    )
                    Text("Mano de Obra")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val qty = cantidad.toDoubleOrNull() ?: 0.0
                    val price = precioUnitario.toDoubleOrNull() ?: 0.0
                    if (descripcion.isNotBlank() && qty > 0) {
                        onConfirm(
                            PresupuestoItem(
                                descripcion = descripcion,
                                cantidad = qty,
                                precioUnitario = price,
                                tipo = tipo
                            )
                        )
                    }
                }
            ) {
                Text("Agregar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
