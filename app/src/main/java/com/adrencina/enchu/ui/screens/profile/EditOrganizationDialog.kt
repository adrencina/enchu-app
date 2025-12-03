package com.adrencina.enchu.ui.screens.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.adrencina.enchu.data.model.Organization

@Composable
fun EditOrganizationDialog(
    organization: Organization,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String) -> Unit,
    onLogoSelected: (android.net.Uri) -> Unit
) {
    var name by remember { mutableStateOf(organization.name) }
    var phone by remember { mutableStateOf(organization.businessPhone) }
    var email by remember { mutableStateOf(organization.businessEmail) }
    var address by remember { mutableStateOf(organization.businessAddress) }
    var web by remember { mutableStateOf(organization.businessWeb) }

    val logoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> uri?.let { onLogoSelected(it) } }
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Datos de Empresa") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Logo Upload
                Button(
                    onClick = { logoPickerLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (organization.logoUrl.isBlank()) "Subir Logo" else "Cambiar Logo")
                }
                
                if (organization.logoUrl.isNotBlank()) {
                    AsyncImage(
                        model = organization.logoUrl,
                        contentDescription = "Logo",
                        modifier = Modifier.size(80.dp).padding(8.dp)
                    )
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre de Fantasía") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Teléfono Comercial") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Comercial") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Dirección Comercial") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = web,
                    onValueChange = { web = it },
                    label = { Text("Sitio Web") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(name, phone, email, address, web)
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
