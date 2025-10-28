package com.adrencina.enchu.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.adrencina.enchu.ui.theme.EnchuTheme

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    // MODIFIED START: La 'label' ya no es necesaria aquí, la gestiona FormSection.
    // Se mantiene solo 'placeholder'. ¡Esto es un cambio en la firma del componente!
    placeholder: String,
    // MODIFIED END
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    // MODIFIED START: Se ajustan los colores para que el fondo sea blanco y el borde sutil.
    // Ya no se necesita una 'label' dentro del OutlinedTextField.
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .testTag("app_text_field_$placeholder"),
        placeholder = { Text(placeholder) },
        shape = MaterialTheme.shapes.medium,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction,
            capitalization = KeyboardCapitalization.Sentences
        ),
        singleLine = singleLine,
        minLines = minLines,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        )
    )
    // MODIFIED END
}

@Preview(name = "Light Mode", showBackground = true)
@Composable
private fun AppTextFieldPreview() {
    EnchuTheme {
        AppTextField(
            value = "",
            onValueChange = {},
            placeholder = "Ej: Juan Pérez"
        )
    }
}