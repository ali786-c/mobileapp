package com.cricketdraft.mobile.auth.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cricketdraft.mobile.auth.data.ProfileUpdateRequest
import com.cricketdraft.mobile.core.ui.CricketDraftColors
import com.cricketdraft.mobile.core.ui.LoadingState
import com.cricketdraft.mobile.core.ui.PrimaryAction
import com.cricketdraft.mobile.core.ui.SectionTitle
import com.cricketdraft.mobile.core.ui.StateCard

private val Roles = listOf("Batter", "Bowler", "All-rounder", "Wicketkeeper")

@Composable
fun ProfileScreen(onBack: () -> Unit, viewModel: ProfileViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(Roles.first()) }
    var bio by remember { mutableStateOf("") }
    var roleMenuOpen by remember { mutableStateOf(false) }

    LaunchedEffect(state) {
        val profile = (state as? ProfileState.Ready)?.profile ?: (state as? ProfileState.Saved)?.profile
        if (profile != null) { fullName = profile.fullName; phone = profile.phone.orEmpty(); city = profile.city.orEmpty(); role = profile.playingRole; bio = profile.bio.orEmpty() }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TextButton(onClick = onBack) { Text("← Back", color = CricketDraftColors.Green) }
        SectionTitle("Your player profile", "Keep these details accurate so captains and admins can understand your playing role.")
        when (val value = state) {
            ProfileState.Loading -> LoadingState("Loading your profile…")
            is ProfileState.Error -> StateCard("We could not save your profile", value.message)
            else -> Unit
        }
        OutlinedTextField(fullName, { fullName = it }, Modifier.fillMaxWidth(), label = { Text("Full name *") }, supportingText = { Text("Required") }, singleLine = true)
        OutlinedTextField(phone, { phone = it }, Modifier.fillMaxWidth(), label = { Text("Phone number") }, singleLine = true)
        OutlinedTextField(city, { city = it }, Modifier.fillMaxWidth(), label = { Text("City") }, singleLine = true)
        TextButton(onClick = { roleMenuOpen = true }) { Text("Playing role: $role", color = CricketDraftColors.Green) }
        DropdownMenu(expanded = roleMenuOpen, onDismissRequest = { roleMenuOpen = false }) { Roles.forEach { option -> DropdownMenuItem(text = { Text(option) }, onClick = { role = option; roleMenuOpen = false }) } }
        OutlinedTextField(bio, { bio = it }, Modifier.fillMaxWidth(), label = { Text("Short bio") }, minLines = 4)
        PrimaryAction("Save profile", {
            viewModel.save(ProfileUpdateRequest(fullName.trim(), phone.trim().ifBlank { null }, city.trim().ifBlank { null }, role, bio = bio.trim().ifBlank { null }))
        }, Modifier.fillMaxWidth(), fullName.isNotBlank() && state !is ProfileState.Loading)
        if (state is ProfileState.Saved) Text("Profile saved successfully.", color = CricketDraftColors.Success, style = MaterialTheme.typography.bodyMedium)
        Text("Fields marked with * are required.", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
    }
}
