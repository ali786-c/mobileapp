package com.cricketdraft.mobile.reports.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.runtime.LaunchedEffect
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
fun ReportsScreen(tournamentSlug: String, audience: String, onBack: () -> Unit, viewModel: ReportsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(tournamentSlug, audience) { viewModel.load(tournamentSlug, audience) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { TextButton(onClick = onBack) { Text("← Back", color = CricketDraftColors.Green) } }
        when (val value = state) {
            ReportsState.Loading -> item { LoadingState("Preparing report…") }
            is ReportsState.Error -> item { StateCard("Report unavailable", value.message, actionText = "Try again", onAction = viewModel::retry) }
            is ReportsState.Ready -> {
                val report = value.report
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        SectionTitle(report.tournament.name, "${report.tournament.city ?: report.tournament.venue ?: "Venue to be announced"} · ${report.audience.replaceFirstChar { it.uppercase() }} view")
                        StatusChip(report.tournament.status)
                    }
                }
                item { SectionTitle("Tournament summary", "A quick overview of this event.") }
                item {
                    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            MetricRow("Teams", report.summary.teams.toString())
                            MetricRow("Registered players", report.summary.registeredPlayers.toString())
                            MetricRow("Approved players", report.summary.approvedPlayers.toString())
                            MetricRow("Selected players", report.summary.selectedPlayers.toString())
                            MetricRow("Draft status", report.summary.draftStatus.replaceFirstChar { it.uppercase() })
                        }
                    }
                }
                item { SectionTitle("Team squads", "Confirmed players grouped by team.") }
                if (report.teamSquads.isEmpty()) item { StateCard("No squads yet", "Squads will appear after draft selections are confirmed.") }
                items(report.teamSquads, key = { it.teamId }) { squad ->
                    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(squad.shortName ?: squad.team, color = CricketDraftColors.Ink, fontWeight = FontWeight.Bold); Text("${squad.selectedCount} players", color = CricketDraftColors.Green) }
                            squad.players.forEach { Text("• ${it.player ?: "Player"} · ${it.playingRole ?: "Role not set"}", color = CricketDraftColors.Muted) }
                        }
                    }
                }
                item { SectionTitle("Draft activity", "Selection history and timer activity.") }
                items(report.history.take(30), key = { "${it.pickNumber}-${it.team}-${it.player}" }) { pick -> Text("Pick ${pick.pickNumber ?: "—"} · ${pick.team ?: "Team"} · ${pick.player ?: pick.status ?: "Update"}", color = CricketDraftColors.Ink) }
                item { StateCard("Timer activity", "${report.timer.extensionCount} extensions · ${report.timer.extendedSeconds} seconds added · ${report.timer.expiredPicks} expired picks") }
            }
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(label, color = CricketDraftColors.Muted); Text(value, color = CricketDraftColors.Ink, fontWeight = FontWeight.Bold) }
}
