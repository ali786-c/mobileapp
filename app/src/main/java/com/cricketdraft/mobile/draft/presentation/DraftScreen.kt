package com.cricketdraft.mobile.draft.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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

@Composable
fun DraftScreen(
    tournamentSlug: String,
    onBack: () -> Unit,
    viewModel: DraftViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var selectedPlayerId by remember { mutableStateOf<Long?>(null) }
    var selectedPlayerName by remember { mutableStateOf("") }
    var playerQuery by rememberSaveable { mutableStateOf("") }
    var roleFilter by rememberSaveable { mutableStateOf("All") }
    LaunchedEffect(tournamentSlug) { viewModel.start(tournamentSlug) }

    selectedPlayerId?.let { playerId ->
        AlertDialog(
            onDismissRequest = { selectedPlayerId = null },
            title = { Text("Confirm player pick") },
            text = { Text("Pick $selectedPlayerName for your team? This action is recorded by the tournament server.") },
            confirmButton = { TextButton(onClick = { viewModel.pick(playerId); selectedPlayerId = null }) { Text("Confirm pick", color = CricketDraftColors.Green, fontWeight = FontWeight.Bold) } },
            dismissButton = { TextButton(onClick = { selectedPlayerId = null }) { Text("Cancel") } },
        )
    }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(CricketDraftSpacing.Screen), verticalArrangement = Arrangement.spacedBy(CricketDraftSpacing.Section)) {
        item { ScreenHeader("Captain draft room", "Make your next pick when the tournament server gives your team the turn.", onBack = onBack) }
        when (val value = state) {
            DraftScreenState.Loading -> item { LoadingState("Connecting to the live draft…") }
            is DraftScreenState.Error -> item { StateCard("Draft unavailable", value.message, actionText = "Try again", onAction = viewModel::retry) }
            is DraftScreenState.Ready -> {
                val draft = value.draft
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = CricketDraftColors.DeepGreen), shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)) {
                        Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("LIVE DRAFT", color = CricketDraftColors.Lime, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                StatusChip(if (value.connection == ConnectionState.Live) "Live" else "Reconnecting")
                            }
                            Text(draft.currentTeam?.name ?: "Waiting for the administrator", color = Color.White, style = MaterialTheme.typography.headlineMedium)
                            Text(if (draft.currentTeam != null) "This team is on the clock" else "The next pick will appear here when the admin starts it", color = Color.White.copy(alpha = .78f))
                            Text(timerLabel(draft.timer.remainingSeconds), color = CricketDraftColors.Lime, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.ExtraBold)
                            Text("${draft.summary.selected} picked · ${draft.summary.pending} waiting", color = Color.White.copy(alpha = .78f))
                        }
                    }
                }
                if (value.connection == ConnectionState.Reconnecting) item { StateCard("Connection is unstable", "We are showing the last update and will keep trying automatically.") }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        MetricCard("Picked", draft.summary.selected.toString(), "Confirmed selections", Modifier.weight(1f))
                        MetricCard("Waiting", draft.summary.pending.toString(), "Pending picks", Modifier.weight(1f))
                    }
                }
                if (!draft.captainCanPick) item {
                    StateCard("Waiting for your turn", "The administrator controls the next pick. Player selection will become available when your team is on the clock.")
                }
                item { SectionTitle("Available players", "Search by name and filter by playing role.") }
                item {
                    OutlinedTextField(
                        value = playerQuery,
                        onValueChange = { playerQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Search player") },
                        singleLine = true,
                    )
                }
                item {
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("All", "Batsman", "Bowler", "All-rounder", "Wicketkeeper").forEach { role ->
                            FilterChip(selected = roleFilter == role, onClick = { roleFilter = role }, label = { Text(role) })
                        }
                    }
                }
                val visiblePlayers = draft.availablePlayers.filter { player ->
                    val matchesQuery = playerQuery.isBlank() || player.fullName.orEmpty().contains(playerQuery, ignoreCase = true)
                    val matchesRole = roleFilter == "All" || player.playingRole.orEmpty().contains(roleFilter, ignoreCase = true)
                    matchesQuery && matchesRole
                }
                if (visiblePlayers.isEmpty()) item { StateCard("No matching players", "Try another name or role filter. Approved players will appear here when available.") }
                items(visiblePlayers, key = { it.id }) { player ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(player.fullName ?: "Unnamed player", color = CricketDraftColors.Ink, fontWeight = FontWeight.Bold)
                            Text(player.playingRole ?: "Playing role not set", color = CricketDraftColors.Muted, style = MaterialTheme.typography.bodySmall)
                        }
                        PrimaryAction("Pick", { selectedPlayerId = player.id; selectedPlayerName = player.fullName ?: "this player" }, enabled = draft.captainCanPick)
                    }
                    HorizontalDivider(color = CricketDraftColors.Border)
                }
                item { SectionTitle("Team squads", "Every confirmed pick appears here for everyone with access.") }
                items(draft.teamSquads, key = { it.id }) { squad ->
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(squad.name, color = CricketDraftColors.Ink, fontWeight = FontWeight.Bold)
                                Text("${squad.selectedCount} players", color = CricketDraftColors.Green)
                            }
                            squad.selectedPlayers.forEach { Text("• ${it.fullName ?: "Player"} · ${it.playingRole ?: "Role not set"}", color = CricketDraftColors.Muted) }
                        }
                    }
                }
                item { SectionTitle("Pick history", "A simple record of every completed selection.") }
                items(draft.picks, key = { it.pickNumber }) { pick ->
                    Text("Pick ${pick.pickNumber} · ${pick.team?.shortName ?: pick.team?.name ?: "Team"} · ${pick.player?.fullName ?: pick.status}", color = CricketDraftColors.Ink)
                }
            }
        }
    }
}

private fun timerLabel(seconds: Int?): String = seconds?.let { "%02d:%02d".format(it / 60, it % 60) } ?: "--:--"
