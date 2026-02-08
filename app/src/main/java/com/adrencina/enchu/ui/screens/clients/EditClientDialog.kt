package com.adrencina.enchu.ui.screens.clients

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.adrencina.enchu.data.model.Cliente

import androidx.compose.ui.text.font.FontWeight
import com.adrencina.enchu.ui.components.AppTextField
import com.adrencina.enchu.ui.components.EnchuButton
import com.adrencina.enchu.ui.components.EnchuDialog

@Composable
fun EditClientDialog(
    cliente: Cliente,
    onDismiss: () -> Unit,
    onConfirm: (Cliente) -> Unit
) {
    var nombre by remember { mutableStateOf(cliente.nombre) }
    var dni by remember { mutableStateOf(cliente.dni) }
    var telefono by remember { mutableStateOf(cliente.telefono) }
    var email by remember { mutableStateOf(cliente.email) }
    var direccion by remember { mutableStateOf(cliente.direccion) }

    EnchuDialog(
        onDismiss = onDismiss,
        title = "Editar Cliente",
        confirmButton = {
            EnchuButton(
                onClick = {
                    if (nombre.isNotBlank()) {
                        onConfirm(
                            cliente.copy(
                                nombre = nombre,
                                dni = dni,
                                telefono = telefono,
                                email = email,
                                direccion = direccion
                            )
                        )
                    }
                },
                text = "Guardar",
                enabled = nombre.isNotBlank()
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.height(56.dp)) {
                Text("Cancelar", fontWeight = FontWeight.Bold)
            }
        }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            AppTextField(
                value = nombre,
                onValueChange = { nombre = it },
                placeholder = "Nombre Completo"
            )
            AppTextField(
                value = dni,
                onValueChange = { dni = it },
                placeholder = "DNI / ID",
                keyboardType = KeyboardType.Number
            )
            AppTextField(
                value = telefono,
                onValueChange = { telefono = it },
                placeholder = "Teléfono",
                keyboardType = KeyboardType.Phone
            )
            AppTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "Email",
                keyboardType = KeyboardType.Email
            )
            AppTextField(
                value = direccion,
                onValueChange = { direccion = it },
                placeholder = "Dirección"
            )
        }
    }
}
