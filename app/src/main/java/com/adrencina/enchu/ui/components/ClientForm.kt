package com.adrencina.enchu.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.input.KeyboardType
import com.adrencina.enchu.ui.theme.Dimens

@Composable
fun ClientForm(
    name: String,
    onNameChange: (String) -> Unit,
    dni: String,
    onDniChange: (String) -> Unit,
    isAutoDni: Boolean,
    onAutoDniChange: (Boolean) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    address: String,
    onAddressChange: (String) -> Unit,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    showExpandButton: Boolean = true,
    onPickContact: (() -> Unit)? = null
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Dimens.PaddingSmall),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Basic Info (Always visible)
        FormSection(title = "Nombre Completo") {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppTextField(
                    value = name,
                    onValueChange = onNameChange,
                    placeholder = "Ej: Juan Pérez",
                    modifier = Modifier.weight(1f)
                )
                if (onPickContact != null) {
                    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()
                    val scale by androidx.compose.animation.core.animateFloatAsState(if (isPressed) 0.9f else 1f, label = "scale")

                    IconButton(
                        onClick = onPickContact,
                        interactionSource = interactionSource,
                        modifier = Modifier
                            .padding(start = Dimens.PaddingSmall)
                            .scale(scale)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Importar de Contactos",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        FormSection(title = "Identificación") {
            Column {
                AppTextField(
                    value = dni,
                    onValueChange = { onDniChange(it.filter { char -> char.isDigit() }) },
                    placeholder = if (isAutoDni) "DNI (Automático)" else "DNI / Cédula",
                    enabled = !isAutoDni,
                    keyboardType = KeyboardType.Number
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = Dimens.PaddingExtraSmall)
                ) {
                    Checkbox(
                        checked = isAutoDni,
                        onCheckedChange = onAutoDniChange
                    )
                    Text(
                        text = "Generar ID automáticamente",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // Expandable Section
        if (showExpandButton) {
            TextButton(
                onClick = onToggleExpand,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(if (isExpanded) "Ver menos detalles" else "Añadir más detalles")
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.padding(start = Dimens.PaddingSmall)
                )
            }
        }

        AnimatedVisibility(visible = isExpanded) {
            Column(
                verticalArrangement = Arrangement.spacedBy(Dimens.PaddingSmall)
            ) {
                FormSection(title = "Contacto (Opcional)") {
                    AppTextField(
                        value = phone,
                        onValueChange = { onPhoneChange(it.filter { char -> char.isDigit() }) },
                        placeholder = "Teléfono / Celular",
                        keyboardType = KeyboardType.Phone
                    )
                }

                FormSection(title = "Email (Opcional)") {
                    AppTextField(
                        value = email,
                        onValueChange = onEmailChange,
                        placeholder = "correo@ejemplo.com",
                        keyboardType = KeyboardType.Email
                    )
                }

                FormSection(title = "Dirección (Opcional)") {
                    AppTextField(
                        value = address,
                        onValueChange = onAddressChange,
                        placeholder = "Calle 123, Ciudad"
                    )
                }
            }
        }
    }
}
