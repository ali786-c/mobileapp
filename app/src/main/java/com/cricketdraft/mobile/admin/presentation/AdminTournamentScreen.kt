package com.cricketdraft.mobile.admin.presentation

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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cricketdraft.mobile.core.ui.CricketDraftColors
import com.cricketdraft.mobile.core.ui.CricketDraftSpacing
import com.cricketdraft.mobile.core.ui.LoadingState
import com.cricketdraft.mobile.core.ui.MetricCard
import com.cricketdraft.mobile.core.ui.PrimaryAction
import com.cricketdraft.mobile.core.ui.ScreenHeader
import com.cricketdraft.mobile.core.ui.SectionTitle
import com.cricketdraft.mobile.core.ui.StateCard
import com.cricketdraft.mobile.core.ui.StatusChip

private val Statuses = listOf("draft", "registration", "ready", "live", "completed", "cancelled")

@Composable
fun AdminTournamentScreen(onBack: () -> Unit, viewModel: AdminTournamentViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LazyColumn(Modifier.fillMaxSize(), contentPadding = androidx.compose.foundation.layout.PaddingValues(CricketDraftSpacing.Screen), verticalArrangement = Arrangement.spacedBy(CricketDraftSpacing.Section)) {
        item { ScreenHeader("Tournament operations", "Manage lifecycle, squads, fixtures, draft, and reports.", onBack = onBack) }
        item { StateCard("Server-controlled workflow", "Status changes are validated by Laravel. If a transition is invalid, the app will keep the current status and show the server response.") }
        when (val value = state) {
            AdminTournamentState.Loading -> item { LoadingState("Loading your tournaments…") }
            is AdminTournamentState.Error -> item { StateCard("Could not load tournaments", value.message, actionText = "Try again", onAction = viewModel::load) }
            is AdminTournamentState.Ready -> {
                if (value.tournaments.isEmpty()) item { StateCard("No tournaments found", "Create a tournament from the web admin workspace first.") }
                items(value.tournaments, key = { it.id }) { tournament ->
                    var expanded by remember { mutableStateOf(false) }
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = CricketDraftColors.Surface), border = androidx.compose.foundation.BorderStroke(1.dp, CricketDraftColors.Border)) {
                        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(tournament.name, color = CricketDraftColors.Ink, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("Operational overview", color = CricketDraftColors.Muted, style = MaterialTheme.typography.bodySmall)
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                MetricCard("Teams", tournament.teamsCount.toString(), modifier = Modifier.weight(1f))
                                MetricCard("Players", tournament.playersCount.toString(), modifier = Modifier.weight(1f))
                                MetricCard("Fixtures", tournament.fixturesCount.toString(), modifier = Modifier.weight(1f))
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Current status", color = Color.Gray)
                                StatusChip(tournament.status)
                            }
                            Text("${tournament.matchesCount} matches connected to this event", color = CricketDraftColors.Muted)
                            PrimaryAction("Change tournament status", { expanded = true }, Modifier.fillMaxWidth())
                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                Statuses.forEach { status ->
                                    DropdownMenuItem(
                                        text = { Text("Move to ${status.replaceFirstChar { it.uppercase() }}") },
                                        onClick = { expanded = false; viewModel.changeStatus(tournament.slug, status) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
