package com.cricketdraft.mobile.superadmin.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cricketdraft.mobile.core.ui.CricketDraftColors
import com.cricketdraft.mobile.core.ui.LoadingState
import com.cricketdraft.mobile.core.ui.SectionTitle
import com.cricketdraft.mobile.core.ui.StateCard
import com.cricketdraft.mobile.core.ui.StatusChip

@Composable
fun SuperAdminScreen(onBack: () -> Unit, viewModel: SuperAdminViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LazyColumn(Modifier.fillMaxSize(), contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { TextButton(onClick = onBack) { Text("← Back", color = CricketDraftColors.Green) } }
        item { SectionTitle("Platform governance", "Monitor the health of every tournament, account, session, and API client.") }
        when (val value = state) {
            SuperAdminState.Loading -> item { LoadingState("Checking platform health…") }
            is SuperAdminState.Error -> item { StateCard("Governance data unavailable", value.message, actionText = "Try again", onAction = viewModel::load) }
            is SuperAdminState.Ready -> {
                val dashboard = value.dashboard
                val metrics = listOf("Users" to dashboard.users, "Tournaments" to dashboard.tournaments, "Live matches" to dashboard.liveMatches, "Pending registrations" to dashboard.pendingRegistrations, "API clients" to dashboard.apiClients, "Active sessions" to dashboard.activeSessions)
                item { SectionTitle("At a glance", "A quick view of platform activity.") }
                items(metrics.chunked(2)) { pair ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        pair.forEach { (label, count) ->
                            Card(Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Color.White)) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) { Text(count.toString(), color = CricketDraftColors.Ink, style = androidx.compose.material3.MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold); Text(label, color = CricketDraftColors.Muted) } }
                        }
                        if (pair.size == 1) Column(Modifier.weight(1f)) {}
                    }
                }
                item { SectionTitle("System health", "These checks describe whether the platform is ready for users.") }
                item {
                    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Application", color = CricketDraftColors.Ink); StatusChip(value.health.application) }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Database", color = CricketDraftColors.Ink); StatusChip(value.health.database) }
                            Text("Database response: ${value.health.databaseResponseMs ?: "—"} ms", color = CricketDraftColors.Muted)
                            Text("API: ${value.health.api} · Storage: ${value.health.storage}", color = CricketDraftColors.Muted)
                            Text("Scheduler: ${value.health.scheduler}", color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}
