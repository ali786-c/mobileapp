package com.cricketdraft.mobile.captain.presentation

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
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
import com.cricketdraft.mobile.captain.data.CaptainMatchPlayer
import com.cricketdraft.mobile.core.ui.CricketDraftColors
import com.cricketdraft.mobile.core.ui.CricketDraftSpacing
import com.cricketdraft.mobile.core.ui.LoadingState
import com.cricketdraft.mobile.core.ui.MetricCard
import com.cricketdraft.mobile.core.ui.PrimaryAction
import com.cricketdraft.mobile.core.ui.ScreenHeader
import com.cricketdraft.mobile.core.ui.SectionTitle
import com.cricketdraft.mobile.core.ui.StateCard

@Composable
fun PlayingXiScreen(matchId: Long, teamId: Long, players: List<CaptainMatchPlayer>, onBack: () -> Unit, viewModel: PlayingXiViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var selected by remember(players) { mutableStateOf(players.filter { it.selectionType == "playing_xi" }.map { it.id }.toSet()) }
    var showSubmitConfirmation by remember { mutableStateOf(false) }
    if (showSubmitConfirmation) {
        AlertDialog(
            onDismissRequest = { showSubmitConfirmation = false },
            title = { Text("Submit playing XI?") },
            text = { Text("You selected ${selected.size} player${if (selected.size == 1) "" else "s"}. The admin will review this lineup after submission.") },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { showSubmitConfirmation = false; viewModel.submit(matchId, teamId, selected.toList()) }) {
                    Text("Submit", color = CricketDraftColors.Green, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = { androidx.compose.material3.TextButton(onClick = { showSubmitConfirmation = false }) { Text("Cancel") } },
        )
    }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(CricketDraftSpacing.Screen), verticalArrangement = Arrangement.spacedBy(CricketDraftSpacing.Section)) {
        item { ScreenHeader("Playing XI", "Select the players who will start this match.", onBack = onBack) }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard("Selected", selected.size.toString(), "Starting players", Modifier.weight(1f))
                MetricCard("Squad", players.size.toString(), "Available players", Modifier.weight(1f))
            }
        }
        item { SectionTitle("Choose your players", "The admin will review and approve the final lineup.") }
        when (val value = state) {
            PlayingXiState.Saving -> item { LoadingState("Submitting your XI…") }
            is PlayingXiState.Error -> item { StateCard("Submission not completed", value.message) }
            is PlayingXiState.Saved -> item { StateCard("Playing XI submitted", "Your admin can now review and approve this lineup.") }
            PlayingXiState.Idle -> Unit
        }
        if (players.isEmpty()) item { StateCard("No squad available", "Your team squad will appear here once the draft is complete.") }
        items(players, key = { it.id }) { player ->
            val checked = player.id in selected
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = if (checked) CricketDraftColors.Green.copy(alpha = .08f) else Color.White)) {
                Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Checkbox(checked = checked, onCheckedChange = { selected = if (it) selected + player.id else selected - player.id })
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(player.name, color = CricketDraftColors.Ink, fontWeight = FontWeight.Bold)
                        Text(player.role ?: "Role not set", color = CricketDraftColors.Muted)
                    }
                }
            }
        }
        item { PrimaryAction("Review and submit XI", { showSubmitConfirmation = true }, Modifier.fillMaxWidth(), state !is PlayingXiState.Saving && selected.isNotEmpty()) }
    }
}
