package com.cricketdraft.mobile.scoring.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cricketdraft.mobile.scoring.data.DeliveryRequest

private val PitchGreen = Color(0xFF13795B)
private val PitchDeep = Color(0xFF062C20)

@Composable
fun ScoringScreen(
    matchId: Long,
    strikerId: Long,
    nonStrikerId: Long,
    bowlerId: Long,
    onBack: () -> Unit,
    viewModel: ScoringViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var runs by remember { mutableIntStateOf(0) }
    var wides by remember { mutableIntStateOf(0) }
    var commentary by remember { mutableStateOf("") }
    var undoReason by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        TextButton(onClick = onBack) { Text("← Back", color = PitchGreen) }
        Text("Scorer room", color = PitchDeep, style = androidx.compose.material3.MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
        Text("Server-authoritative ball ledger", color = PitchGreen)
        Text("Runs off bat", color = PitchDeep, fontWeight = FontWeight.Bold)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(0, 1, 2, 3, 4, 6).forEach { value ->
                Button(onClick = { runs = value }) { Text(value.toString()) }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { wides = 0 }) { Text("Legal") }
            Button(onClick = { wides = 1 }) { Text("Wide") }
            Button(onClick = { wides = 2 }) { Text("2 wides") }
        }
        OutlinedTextField(commentary, { commentary = it }, Modifier.fillMaxWidth(), label = { Text("Commentary") })
        Button(
            onClick = { viewModel.recordDelivery(matchId, DeliveryRequest(strikerId, nonStrikerId, bowlerId, runsOffBat = runs, wides = wides, commentary = commentary.ifBlank { null })) },
            modifier = Modifier.fillMaxWidth(),
            enabled = state !is ScoringState.Saving,
        ) { Text("Record delivery") }
        Button(onClick = { viewModel.startNextInnings(matchId) }, modifier = Modifier.fillMaxWidth(), enabled = state !is ScoringState.Saving) { Text("Start next innings") }
        OutlinedTextField(undoReason, { undoReason = it }, Modifier.fillMaxWidth(), label = { Text("Undo reason") })
        Button(onClick = { viewModel.undo(matchId, undoReason) }, modifier = Modifier.fillMaxWidth(), enabled = undoReason.trim().length >= 5 && state !is ScoringState.Saving) { Text("Undo latest delivery") }
        when (val value = state) {
            ScoringState.Saving -> CircularProgressIndicator(color = PitchGreen)
            is ScoringState.Success -> Text(value.message, color = PitchGreen)
            is ScoringState.Error -> Text(value.message, color = androidx.compose.material3.MaterialTheme.colorScheme.error)
            ScoringState.Ready -> Unit
        }
    }
}
