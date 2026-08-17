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
import com.cricketdraft.mobile.core.ui.LoadingState
import com.cricketdraft.mobile.core.ui.PrimaryAction
import com.cricketdraft.mobile.core.ui.SectionTitle
import com.cricketdraft.mobile.core.ui.StateCard

@Composable
fun PlayingXiScreen(matchId: Long, teamId: Long, players: List<CaptainMatchPlayer>, onBack: () -> Unit, viewModel: PlayingXiViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var selected by remember(players) { mutableStateOf(players.filter { it.selectionType == "playing_xi" }.map { it.id }.toSet()) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { TextButton(onClick = onBack) { Text("← Back", color = CricketDraftColors.Green) } }
        item { SectionTitle("Choose your playing XI", "Tap the players who will start. The admin will approve the final lineup.") }
        item { Text("${selected.size} player${if (selected.size == 1) "" else "s"} selected", color = CricketDraftColors.Green, fontWeight = FontWeight.Bold) }
        when (val value = state) {
            PlayingXiState.Saving -> item { LoadingState("Submitting your XI…") }
            is PlayingXiState.Error -> item { StateCard("Submission not completed", value.message) }
            is PlayingXiState.Saved -> item { StateCard("Playing XI submitted", "Your admin can now review and approve this lineup.") }
            PlayingXiState.Idle -> Unit
        }
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
        item { PrimaryAction("Submit playing XI", { viewModel.submit(matchId, teamId, selected.toList()) }, Modifier.fillMaxWidth(), state !is PlayingXiState.Saving && selected.isNotEmpty()) }
    }
}
