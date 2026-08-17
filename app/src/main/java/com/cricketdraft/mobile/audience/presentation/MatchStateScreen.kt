package com.cricketdraft.mobile.audience.presentation

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
import androidx.compose.material3.MaterialTheme
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
fun MatchStateScreen(matchId: Long, onBack: () -> Unit, viewModel: MatchStateViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(matchId) { viewModel.start(matchId) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { TextButton(onClick = onBack) { Text("← Back", color = CricketDraftColors.Green) } }
        when (val value = state) {
            MatchStateScreenState.Loading -> item { LoadingState("Loading live scorecard…") }
            is MatchStateScreenState.Error -> item { StateCard("Scorecard unavailable", value.message, actionText = "Try again", onAction = viewModel::retry) }
            is MatchStateScreenState.Ready -> {
                val match = value.match
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        SectionTitle("Match center", "Updates automatically every few seconds.")
                        StatusChip(if (value.connection == ConnectionState.Live) "Live" else "Reconnecting")
                    }
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(match.status.replaceFirstChar { it.uppercase() }, color = CricketDraftColors.Ink, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                        match.resultSummary?.let { Text(it, color = CricketDraftColors.Green, fontWeight = FontWeight.Bold) }
                        Text("Score update ${match.revision}", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                    }
                }
                if (value.connection == ConnectionState.Reconnecting) item { StateCard("Connection is unstable", "The score shown is the latest update we received. We are trying again automatically.") }
                if (match.innings.isEmpty()) item { StateCard("Match has not started", "The scorecard will appear as soon as the first innings begins.") }
                items(match.innings, key = { it.id }) { innings ->
                    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text("Innings ${innings.number} · ${innings.battingTeam.name}", color = CricketDraftColors.Ink, fontWeight = FontWeight.Bold)
                            Text("${innings.runs}/${innings.wickets} (${innings.overs})", color = CricketDraftColors.Ink, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                            Text("Target ${innings.target ?: "—"} · Maximum ${innings.maximumOvers} overs", color = CricketDraftColors.Green)
                        }
                    }
                }
            }
        }
    }
}
