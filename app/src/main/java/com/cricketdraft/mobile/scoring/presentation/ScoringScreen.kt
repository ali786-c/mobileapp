package com.cricketdraft.mobile.scoring.presentation

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
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
import com.cricketdraft.mobile.core.ui.CricketDraftColors
import com.cricketdraft.mobile.core.ui.CricketDraftSpacing
import com.cricketdraft.mobile.core.ui.LoadingState
import com.cricketdraft.mobile.core.ui.PrimaryAction
import com.cricketdraft.mobile.core.ui.ScreenHeader
import com.cricketdraft.mobile.core.ui.StateCard
import com.cricketdraft.mobile.scoring.data.DeliveryRequest

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
    var showUndoConfirmation by remember { mutableStateOf(false) }
    val isSaving = state is ScoringState.Saving

    if (showUndoConfirmation) {
        AlertDialog(
            onDismissRequest = { showUndoConfirmation = false },
            title = { Text("Undo latest delivery?") },
            text = { Text("This is a controlled correction and will be recorded in the match audit. Continue with the supplied reason?") },
            confirmButton = {
                TextButton(onClick = { showUndoConfirmation = false; viewModel.undo(matchId, undoReason.trim()) }) {
                    Text("Confirm undo", color = CricketDraftColors.Danger, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = { TextButton(onClick = { showUndoConfirmation = false }) { Text("Cancel") } },
        )
    }

    Column(
        Modifier.fillMaxSize().padding(CricketDraftSpacing.Screen),
        verticalArrangement = Arrangement.spacedBy(CricketDraftSpacing.Section),
    ) {
        ScreenHeader("Scorer room", "Record one server-confirmed delivery at a time.", onBack = onBack)
        Card(
            Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CricketDraftColors.DeepGreen),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
        ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("MATCH $matchId", color = CricketDraftColors.Lime, fontWeight = FontWeight.Bold)
                Text("Striker $strikerId · Non-striker $nonStrikerId", color = Color.White, fontWeight = FontWeight.Bold)
                Text("Bowler $bowlerId · Server-authoritative ball ledger", color = Color.White.copy(alpha = .78f))
            }
        }
        when (val value = state) {
            ScoringState.Saving -> LoadingState("Saving delivery to the live scorecard…")
            is ScoringState.Success -> StateCard("Score updated", value.message)
            is ScoringState.Error -> StateCard("Score not updated", value.message)
            ScoringState.Ready -> Unit
        }
        Text("Runs off bat", color = CricketDraftColors.Ink, fontWeight = FontWeight.Bold)
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            listOf(0, 1, 2, 3, 4, 6).forEach { value ->
                Button(onClick = { runs = value }, enabled = !isSaving) { Text(value.toString()) }
            }
        }
        Text("Extras", color = CricketDraftColors.Ink, fontWeight = FontWeight.Bold)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { wides = 0 }, enabled = !isSaving, modifier = Modifier.weight(1f)) { Text("Legal") }
            OutlinedButton(onClick = { wides = 1 }, enabled = !isSaving, modifier = Modifier.weight(1f)) { Text("Wide") }
            OutlinedButton(onClick = { wides = 2 }, enabled = !isSaving, modifier = Modifier.weight(1f)) { Text("2 wides") }
        }
        OutlinedTextField(
            value = commentary,
            onValueChange = { commentary = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Commentary (optional)") },
            minLines = 2,
            maxLines = 3,
            enabled = !isSaving,
        )
        PrimaryAction(
            "Record delivery",
            onClick = {
                viewModel.recordDelivery(
                    matchId,
                    DeliveryRequest(
                        strikerId,
                        nonStrikerId,
                        bowlerId,
                        runsOffBat = runs,
                        wides = wides,
                        commentary = commentary.ifBlank { null },
                    ),
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving,
        )
        OutlinedButton(onClick = { viewModel.startNextInnings(matchId) }, modifier = Modifier.fillMaxWidth(), enabled = !isSaving) {
            Text("Start next innings")
        }
        OutlinedTextField(
            value = undoReason,
            onValueChange = { undoReason = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Correction reason") },
            enabled = !isSaving,
        )
        OutlinedButton(
            onClick = { showUndoConfirmation = true },
            modifier = Modifier.fillMaxWidth(),
            enabled = undoReason.trim().length >= 5 && !isSaving,
        ) {
            Text("Review undo", color = CricketDraftColors.Danger)
        }
    }
}
