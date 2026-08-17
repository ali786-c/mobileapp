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
import androidx.compose.material3.HorizontalDivider
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
import com.cricketdraft.mobile.core.ui.PrimaryAction
import com.cricketdraft.mobile.core.ui.SectionTitle
import com.cricketdraft.mobile.core.ui.StateCard
import com.cricketdraft.mobile.core.ui.StatusChip

@Composable
fun TournamentDetailScreen(slug: String, onBack: () -> Unit, onReports: () -> Unit = {}, onMatch: (Long) -> Unit = {}, viewModel: TournamentDetailViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(slug) { viewModel.load(slug) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { TextButton(onClick = onBack) { Text("← Back to tournaments", color = CricketDraftColors.Green) } }
        when (val value = state) {
            TournamentDetailState.Loading -> item { LoadingState("Loading tournament details…") }
            is TournamentDetailState.Error -> item { StateCard("Tournament unavailable", value.message) }
            is TournamentDetailState.Ready -> {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = CricketDraftColors.DeepGreen), shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)) {
                        Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(value.tournament.name, color = Color.White, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                                StatusChip(value.tournament.status)
                            }
                            value.tournament.description?.let { Text(it, color = Color.White.copy(alpha = .8f)) }
                            Text(value.tournament.city ?: value.tournament.location ?: "Venue to be announced", color = CricketDraftColors.Lime)
                            value.tournament.defaultOversPerInnings?.let { Text("$it overs per innings", color = Color.White.copy(alpha = .8f)) }
                        }
                    }
                }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        PrimaryAction("Open reports", onReports, Modifier.weight(1f))
                        TextButton(onClick = onBack) { Text("Back", color = CricketDraftColors.Green) }
                    }
                }
                item { SectionTitle("Fixtures", "See when teams play and follow live matches from here.") }
                if (value.fixtures.isEmpty()) item { StateCard("No fixtures published yet", "The admin will publish match times and venues here.") }
                items(value.fixtures, key = { it.id }) { fixture ->
                    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${fixture.homeTeam.name} vs ${fixture.awayTeam.name}", color = CricketDraftColors.Ink, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            StatusChip(fixture.status)
                        }
                        Text(fixture.scheduledAt ?: "Time to be announced", color = CricketDraftColors.Green)
                        Text(fixture.venue ?: fixture.city ?: "Venue to be announced", color = CricketDraftColors.Muted)
                        fixture.matchId?.let { matchId ->
                            TextButton(onClick = { onMatch(matchId) }) { Text("Open scorecard", color = CricketDraftColors.Green, fontWeight = FontWeight.Bold) }
                        }
                        HorizontalDivider(color = CricketDraftColors.Border)
                    }
                }
                item { SectionTitle("Standings", "Points and net run rate after approved results.") }
                if (value.standings.isEmpty()) item { StateCard("Standings are not ready yet", "The table will appear after approved match results.") }
                items(value.standings, key = { it.team.id }) { row ->
                    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${row.position ?: "—"}. ${row.team.shortName ?: row.team.name}", color = CricketDraftColors.Ink, fontWeight = FontWeight.Bold)
                            Text("${row.points} pts · ${row.wins}W ${row.losses}L", color = CricketDraftColors.Green)
                        }
                    }
                }
            }
        }
    }
}
