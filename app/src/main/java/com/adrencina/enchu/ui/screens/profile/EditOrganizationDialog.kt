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
import com.adrencina.enchu.domain.model.Organization

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.adrencina.enchu.ui.components.AppTextField
import com.adrencina.enchu.ui.components.EnchuButton
import com.adrencina.enchu.ui.components.EnchuDialog

@Composable
fun EditOrganizationDialog(
    organization: Organization,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String, String, String) -> Unit,
    onLogoSelected: (android.net.Uri) -> Unit
) {
    var name by remember { mutableStateOf(organization.name) }
    var phone by remember { mutableStateOf(organization.businessPhone) }
    var email by remember { mutableStateOf(organization.businessEmail) }
    var address by remember { mutableStateOf(organization.businessAddress) }
    var web by remember { mutableStateOf(organization.businessWeb) }
    var cuit by remember { mutableStateOf(organization.cuit) }
    var taxCondition by remember { mutableStateOf(organization.taxCondition) }

    val logoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> uri?.let { onLogoSelected(it) } }
    )

    EnchuDialog(
        onDismiss = onDismiss,
        title = "Datos de Empresa",
        confirmButton = {
            EnchuButton(
                onClick = { onConfirm(name, phone, email, address, web, cuit, taxCondition) },
                text = "Guardar",
                enabled = name.isNotBlank()
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.height(56.dp)) {
                Text("Cancelar", fontWeight = FontWeight.Bold)
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Logo Upload Section Premium
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    onClick = { logoPickerLauncher.launch("image/*") },
                    modifier = Modifier.size(100.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (organization.logoUrl.isNotBlank()) {
                            AsyncImage(
                                model = organization.logoUrl,
                                contentDescription = "Logo",
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Business,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                            )
                        }
                        
                        // Badge de cámara
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(32.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            shadowElevation = 4.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.PhotoCamera,
                                    null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }
            }

            Text(
                text = "INFORMACIÓN FISCAL",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            AppTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = "Nombre de Fantasía"
            )
            
            AppTextField(
                value = cuit,
                onValueChange = { cuit = it },
                placeholder = "CUIT / ID Fiscal",
                keyboardType = KeyboardType.Number
            )
            
            AppTextField(
                value = taxCondition,
                onValueChange = { taxCondition = it },
                placeholder = "Condición Fiscal (ej: Monotributo)"
            )

            Spacer(Modifier.height(8.dp))
            
            Text(
                text = "CONTACTO COMERCIAL",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            AppTextField(
                value = phone,
                onValueChange = { phone = it },
                placeholder = "Teléfono Comercial",
                keyboardType = KeyboardType.Phone
            )
            
            AppTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "Email Comercial",
                keyboardType = KeyboardType.Email
            )
            
            AppTextField(
                value = address,
                onValueChange = { address = it },
                placeholder = "Dirección Comercial"
            )
            
            AppTextField(
                value = web,
                onValueChange = { web = it },
                placeholder = "Sitio Web (Opcional)",
                keyboardType = KeyboardType.Uri
            )
            
            Spacer(Modifier.height(8.dp))
        }
    }
}