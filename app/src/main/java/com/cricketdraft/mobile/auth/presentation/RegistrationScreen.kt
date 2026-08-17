package com.cricketdraft.mobile.auth.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
fun RegistrationScreen(tournamentSlug: String, tournamentName: String, onBack: () -> Unit, viewModel: RegistrationViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(tournamentSlug) { viewModel.load(tournamentSlug) }
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        TextButton(onClick = onBack) { Text("← Back", color = CricketDraftColors.Green) }
        SectionTitle("Join $tournamentName", "Submit your player profile for the admin to review.")
        when (val value = state) {
            RegistrationState.Loading -> LoadingState("Checking registration…")
            RegistrationState.NotRegistered -> {
                StateCard("Ready to register", "Your profile will be sent to the tournament admin. You will see the result here once it is reviewed.")
                PrimaryAction("Submit registration", { viewModel.submit(tournamentSlug) }, Modifier.fillMaxWidth())
            }
            is RegistrationState.Registered -> {
                StateCard("Your registration", "We will keep this status updated when the admin reviews your request.")
                StatusChip(value.registration.status)
                Text(value.registration.submittedAt?.let { "Submitted on $it" } ?: "Registration submitted", color = Color.Gray)
            }
            is RegistrationState.Error -> StateCard("Registration could not be completed", value.message, actionText = "Try again", onAction = { viewModel.load(tournamentSlug) })
        }
    }
}
