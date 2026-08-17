package com.cricketdraft.mobile.draft.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.cricketdraft.mobile.core.ui.LoadingState
import com.cricketdraft.mobile.core.ui.PrimaryAction
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

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { TextButton(onClick = onBack) { Text("← Back to workspace", color = CricketDraftColors.Green) } }
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
                item { SectionTitle("Available players", "Choose a player only when it is your team's turn.") }
                if (draft.availablePlayers.isEmpty()) item { StateCard("No players available", "The list will update when approved players are available.") }
                items(draft.availablePlayers, key = { it.id }) { player ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(player.fullName ?: "Unnamed player", color = CricketDraftColors.Ink, fontWeight = FontWeight.Bold)
                            Text(player.playingRole ?: "Playing role not set", color = CricketDraftColors.Muted, style = MaterialTheme.typography.bodySmall)
                        }
                        Button(onClick = { selectedPlayerId = player.id; selectedPlayerName = player.fullName ?: "this player" }, enabled = draft.captainCanPick) { Text("Pick") }
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
