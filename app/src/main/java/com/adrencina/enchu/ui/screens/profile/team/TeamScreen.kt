package com.adrencina.enchu.ui.screens.profile.team

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.adrencina.enchu.data.model.UserProfile
import com.adrencina.enchu.domain.model.Obra

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamScreen(
    viewModel: TeamViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = { Text("MI EQUIPO", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    }
                )
                TabRow(selectedTabIndex = uiState.selectedTab) {
                    Tab(selected = uiState.selectedTab == 0, onClick = { viewModel.onTabSelected(0) }, text = { Text("MIEMBROS") })
                    Tab(selected = uiState.selectedTab == 1, onClick = { viewModel.onTabSelected(1) }, text = { Text("ESTADÍSTICAS") })
                }
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                when (uiState.selectedTab) {
                    0 -> {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            item {
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("Código de Invitación", style = MaterialTheme.typography.labelSmall)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(uiState.organizationId, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                            IconButton(onClick = { clipboardManager.setText(AnnotatedString(uiState.organizationId)) }) {
                                                Icon(Icons.Default.ContentCopy, contentDescription = null)
                                            }
                                        }
                                    }
                                }
                            }
                            if (uiState.pendingMembers.isNotEmpty()) {
                                item { Text("SOLICITUDES", fontWeight = FontWeight.Bold, color = Color.Red) }
                                items(uiState.pendingMembers) { member ->
                                    Card(modifier = Modifier.fillMaxWidth()) {
                                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Text(member.displayName, modifier = Modifier.weight(1f))
                                            IconButton(onClick = { viewModel.rejectMember(member.id) }) { Icon(Icons.Default.Close, contentDescription = null, tint = Color.Red) }
                                            IconButton(onClick = { viewModel.approveMember(member.id) }) { Icon(Icons.Default.Check, contentDescription = null, tint = Color.Green) }
                                        }
                                    }
                                }
                            }
                            item { Text("MIEMBROS ACTIVOS", fontWeight = FontWeight.Bold) }
                            items(uiState.activeMembers) { member ->
                                var menu by remember { mutableStateOf(false) }
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(member.displayName, fontWeight = FontWeight.Bold)
                                            Text(member.role, style = MaterialTheme.typography.bodySmall)
                                        }
                                        Box {
                                            IconButton(onClick = { menu = true }) { Icon(Icons.Default.MoreVert, contentDescription = null) }
                                            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                                                DropdownMenuItem(text = { Text("Hacer OWNER") }, onClick = { viewModel.updateRole(member.id, "OWNER"); menu = false })
                                                DropdownMenuItem(text = { Text("Hacer WORKER") }, onClick = { viewModel.updateRole(member.id, "WORKER"); menu = false })
                                                DropdownMenuItem(text = { Text("Eliminar", color = Color.Red) }, onClick = { viewModel.removeMember(member.id); menu = false })
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    1 -> {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            items(uiState.activeMembers) { member ->
                                val stats = uiState.stats[member.id] ?: MemberStats(member.id)
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(member.displayName, fontWeight = FontWeight.Bold)
                                        Text("Obras: ${stats.activeWorksCount}")
                                        Text("Tareas completadas: ${stats.completedTasksCount}")
                                        if (stats.assignedWorks.isNotEmpty()) {
                                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                                            stats.assignedWorks.forEach { obra ->
                                                Text("• ${obra.nombreObra} (${obra.tareasCompletadas}/${obra.tareasTotales})", style = MaterialTheme.typography.bodySmall)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
