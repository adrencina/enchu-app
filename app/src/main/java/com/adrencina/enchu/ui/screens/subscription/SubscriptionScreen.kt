package com.adrencina.enchu.ui.screens.subscription

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adrencina.enchu.ui.theme.AzulCorporativo
import com.adrencina.enchu.ui.theme.NaranjaAccion
import com.adrencina.enchu.ui.theme.TextoGris

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    onBack: () -> Unit
) {
    var selectedPlan by remember { mutableStateOf("ANNUAL") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Hero
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(NaranjaAccion, Color(0xFFFFD54F))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.ElectricBolt,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Potencia tu trabajo con Enchu PRO",
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
                color = AzulCorporativo
            )

            Text(
                text = "Sin límites, sin interrupciones. Diseñado para electricistas profesionales.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = TextoGris,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Beneficios
            BenefitItem(
                icon = Icons.Default.AllInclusive,
                title = "Obras Ilimitadas",
                subtitle = "Gestiona todos tus proyectos sin restricciones."
            )
            BenefitItem(
                icon = Icons.Default.CloudDone,
                title = "50GB Almacenamiento Cloud",
                subtitle = "Guarda fotos y planos de alta resolución."
            )
            BenefitItem(
                icon = Icons.Default.PictureAsPdf,
                title = "Informes Personalizados",
                subtitle = "Genera PDFs con tu logo y firma profesional."
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Selector de Planes
            PlanCard(
                title = "Plan Mensual",
                price = "$4.99",
                period = "al mes",
                isSelected = selectedPlan == "MONTHLY",
                onClick = { selectedPlan = "MONTHLY" }
            )

            Spacer(modifier = Modifier.height(16.dp))

            PlanCard(
                title = "Plan Anual",
                price = "$3.99",
                period = "al mes (pago anual)",
                savings = "AHORRA 20%",
                isSelected = selectedPlan == "ANNUAL",
                onClick = { selectedPlan = "ANNUAL" }
            )

            Spacer(modifier = Modifier.height(40.dp))

            // CTA
            Button(
                onClick = { /* Implementación futura */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NaranjaAccion)
            ) {
                Text(
                    text = if (selectedPlan == "ANNUAL") "Suscribirme ahora ($47.90/año)" else "Suscribirme ahora ($4.99/mes)",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Puedes cancelar en cualquier momento desde Google Play.",
                style = MaterialTheme.typography.bodySmall,
                color = TextoGris,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun BenefitItem(icon: ImageVector, title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(AzulCorporativo.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = AzulCorporativo, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = AzulCorporativo)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = TextoGris)
        }
    }
}

@Composable
fun PlanCard(
    title: String,
    price: String,
    period: String,
    savings: String? = null,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) NaranjaAccion else Color.LightGray.copy(alpha = 0.5f)
    val background = if (isSelected) NaranjaAccion.copy(alpha = 0.05f) else Color.Transparent

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(background)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                if (savings != null) {
                    Text(
                        text = savings,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        color = NaranjaAccion,
                        modifier = Modifier
                            .background(NaranjaAccion.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Text(text = title, style = MaterialTheme.typography.titleLarge, color = AzulCorporativo)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = price,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = AzulCorporativo
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = period, style = MaterialTheme.typography.bodySmall, color = TextoGris)
                }
            }
            
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = NaranjaAccion)
            )
        }
    }
}
