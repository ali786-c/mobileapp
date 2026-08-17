package com.cricketdraft.mobile

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cricketdraft.mobile.auth.presentation.AuthViewModel
import com.cricketdraft.mobile.auth.presentation.ProfileScreen
import com.cricketdraft.mobile.auth.presentation.RegistrationScreen
import com.cricketdraft.mobile.auth.presentation.SessionState
import com.cricketdraft.mobile.admin.presentation.AdminTournamentScreen
import com.cricketdraft.mobile.core.ui.CricketDraftColors
import com.cricketdraft.mobile.core.ui.CricketDraftTheme
import com.cricketdraft.mobile.core.ui.LoadingState
import com.cricketdraft.mobile.core.ui.PrimaryAction
import com.cricketdraft.mobile.core.ui.SectionTitle
import com.cricketdraft.mobile.core.ui.StateCard
import com.cricketdraft.mobile.core.ui.StatusChip
import com.cricketdraft.mobile.draft.presentation.DraftScreen
import com.cricketdraft.mobile.navigation.AppDestination
import com.cricketdraft.mobile.audience.presentation.MatchStateScreen
import com.cricketdraft.mobile.audience.presentation.TournamentDetailScreen
import com.cricketdraft.mobile.audience.presentation.TournamentListState
import com.cricketdraft.mobile.audience.presentation.TournamentViewModel
import com.cricketdraft.mobile.reports.presentation.ReportsScreen
import com.cricketdraft.mobile.scoring.presentation.ScoringScreen
import com.cricketdraft.mobile.superadmin.presentation.SuperAdminScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { CricketDraftApp() }
    }
}

@Composable
fun CricketDraftApp(viewModel: AuthViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CricketDraftTheme {
        when (val session = state) {
            SessionState.Loading -> LoadingState("Checking your secure session…")
            SessionState.SignedOut -> LoginScreen(onLogin = viewModel::login)
            is SessionState.Error -> LoginScreen(error = session.message, onLogin = viewModel::login)
            is SessionState.SignedIn -> MainShell(session, viewModel::logout)
        }
    }
}

@Composable
private fun MainShell(session: SessionState.SignedIn, onLogout: () -> Unit) {
    val navController = rememberNavController()
    val start = AppDestination.Home.route
    Scaffold(
        containerColor = CricketDraftColors.Canvas,
        bottomBar = { BottomNavigation(navController) },
    ) { padding ->
        NavHost(navController, startDestination = start, modifier = Modifier.padding(padding)) {
            composable(AppDestination.Home.route) { HomeScreen(session, navController, onLogout) }
            composable(AppDestination.Tournaments.route) { HomeScreen(session, navController, onLogout) }
            composable(AppDestination.Profile.route) { ProfileScreen(onBack = { navController.popBackStack() }) }
            composable(AppDestination.AdminWorkspace.route) { AdminTournamentScreen(onBack = { navController.popBackStack() }) }
            composable(AppDestination.SuperAdmin.route) { SuperAdminScreen(onBack = { navController.popBackStack() }) }
            composable(AppDestination.CaptainDraft.route) { DraftEntryScreen(onBrowse = { navController.navigate(AppDestination.Tournaments.route) }, onBack = { navController.popBackStack() }) }
            composable(AppDestination.ScorerRoom.route) { ScorerEntryScreen(onBack = { navController.popBackStack() }) }
            composable(AppDestination.MatchCenter.route) { LiveEntryScreen(onBrowse = { navController.navigate(AppDestination.Tournaments.route) }) }
            composable("matches/{matchId}", arguments = listOf(navArgument("matchId") { type = NavType.LongType })) { entry ->
                MatchStateScreen(entry.arguments?.getLong("matchId") ?: 0L, onBack = { navController.popBackStack() })
            }
            composable(AppDestination.TournamentDetail.route, arguments = listOf(navArgument("slug") { type = NavType.StringType })) { entry ->
                val slug = entry.arguments?.getString("slug").orEmpty()
                TournamentDetailScreen(slug, onBack = { navController.popBackStack() }, onReports = { navController.navigate("reports/$slug/public") }, onMatch = { matchId -> navController.navigate("matches/$matchId") })
            }
            composable("reports/{slug}/{audience}", arguments = listOf(navArgument("slug") { type = NavType.StringType }, navArgument("audience") { type = NavType.StringType })) { entry ->
                ReportsScreen(entry.arguments?.getString("slug").orEmpty(), entry.arguments?.getString("audience").orEmpty(), onBack = { navController.popBackStack() })
            }
            composable("register/{slug}/{name}", arguments = listOf(navArgument("slug") { type = NavType.StringType }, navArgument("name") { type = NavType.StringType })) { entry ->
                RegistrationScreen(entry.arguments?.getString("slug").orEmpty(), entry.arguments?.getString("name").orEmpty(), onBack = { navController.popBackStack() })
            }
        }
    }
}

@Composable
private fun BottomNavigation(navController: NavHostController) {
    val current by navController.currentBackStackEntryAsState()
    val route = current?.destination?.route
    NavigationBar(Modifier.navigationBarsPadding(), containerColor = Color.White) {
        listOf(
            Triple(AppDestination.Home.route, "Home", Icons.Default.Home),
            Triple(AppDestination.MatchCenter.route, "Live", Icons.Default.LiveTv),
            Triple(AppDestination.Tournaments.route, "Tournaments", Icons.Default.SportsCricket),
            Triple(AppDestination.Profile.route, "Profile", Icons.Default.AccountCircle),
        ).forEach { (destination, label, icon) ->
            NavigationBarItem(
                selected = route == destination,
                onClick = { navController.navigate(destination) { launchSingleTop = true } },
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label) },
            )
        }
    }
}

@Composable
private fun HomeScreen(session: SessionState.SignedIn, navController: NavHostController, onLogout: () -> Unit, viewModel: TournamentViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("CRICKET DRAFT OS", color = CricketDraftColors.Green, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Hi, ${session.user.name ?: "there"}", color = CricketDraftColors.Ink, style = MaterialTheme.typography.headlineMedium)
                    Text("Everything happening in your tournaments, in one place.", color = CricketDraftColors.Muted)
                }
                TextButton(onClick = onLogout) { Text("Log out", color = CricketDraftColors.Green) }
            }
        }
        item {
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = CricketDraftColors.DeepGreen)) {
                Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("YOUR WORKSPACE", color = CricketDraftColors.Lime, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(roleGreeting(session.user.roles), color = Color.White, style = MaterialTheme.typography.titleLarge)
                    Text("Choose an action below. You can always come back here from the Home tab.", color = Color.White.copy(alpha = .75f))
                    val role = session.user.roles.firstOrNull()
                    if (role == "captain") PrimaryAction("Find my tournament", { navController.navigate(AppDestination.Tournaments.route) })
                    if (role == "admin") PrimaryAction("Manage tournaments", { navController.navigate(AppDestination.AdminWorkspace.route) })
                    if (role == "super_admin") PrimaryAction("Open governance", { navController.navigate(AppDestination.SuperAdmin.route) })
                }
            }
        }
        item { SectionTitle("Public tournaments", "Browse events, fixtures, standings, and live updates.") }
        when (val value = state) {
            TournamentListState.Loading -> item { LoadingState() }
            is TournamentListState.Error -> item { StateCard("Could not load tournaments", value.message, actionText = "Try again", onAction = { viewModel.load() }) }
            is TournamentListState.Ready -> {
                if (value.tournaments.isEmpty()) item { StateCard("No tournaments yet", "When an event becomes public, it will appear here.") }
                items(value.tournaments, key = { it.id }) { tournament ->
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(tournament.name, color = CricketDraftColors.Ink, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                StatusChip(tournament.status)
                            }
                            Text(tournament.description ?: "Follow this tournament for live updates and results.", color = CricketDraftColors.Muted, maxLines = 2)
                            Text(tournament.city ?: tournament.location ?: "Venue to be announced", color = CricketDraftColors.Green, fontSize = 13.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                PrimaryAction("View tournament", { navController.navigate(AppDestination.TournamentDetail.create(tournament.slug)) }, modifier = Modifier.weight(1f))
                                if (tournament.status == "registration" || tournament.status == "ready") {
                                    TextButton(onClick = { navController.navigate("register/${tournament.slug}/${Uri.encode(tournament.name)}") }) { Text("Register", color = CricketDraftColors.Green) }
                                }
                            }
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun LiveEntryScreen(onBrowse: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
        SectionTitle("Live match center", "Open a match from its tournament page to see the live scorecard.")
        Spacer(Modifier.height(18.dp))
        StateCard("No match selected", "Choose a public tournament first. Its fixtures and live matches will be linked from the detail page.", actionText = "Browse tournaments", onAction = onBrowse)
    }
}

@Composable
private fun ScorerEntryScreen(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
        TextButton(onClick = onBack) { Text("← Back", color = CricketDraftColors.Green) }
        SectionTitle("Scorer room", "Open this screen from an assigned live match. Your scorer permissions and player lineup will be loaded from Laravel.")
        Spacer(Modifier.height(18.dp))
        StateCard("Match selection required", "No match has been selected yet. This prevents recording a delivery against the wrong match.")
    }
}

@Composable
private fun DraftEntryScreen(onBrowse: () -> Unit, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
        TextButton(onClick = onBack) { Text("← Back", color = CricketDraftColors.Green) }
        SectionTitle("Draft room", "Open a tournament-specific draft from its live tournament page. The server will confirm whether it is your team's turn.")
        Spacer(Modifier.height(18.dp))
        StateCard("Choose a tournament", "Browse public tournaments first, then open the draft linked to your assigned event.", actionText = "Browse tournaments", onAction = onBrowse)
    }
}

@Composable
private fun LoginScreen(error: String? = null, onLogin: (String, String, String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val canSubmit = email.isNotBlank() && password.isNotBlank()
    val submit = { if (canSubmit) onLogin(email.trim(), password, "cricket-draft-android") }
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
        Text("CRICKET DRAFT OS", color = CricketDraftColors.Green, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        Text("Welcome back", color = CricketDraftColors.Ink, style = MaterialTheme.typography.headlineLarge)
        Text("Sign in to manage your tournaments or follow live cricket.", color = CricketDraftColors.Muted, modifier = Modifier.padding(top = 6.dp, bottom = 24.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Email address") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { submit() }),
        )
        if (error != null) {
            Text(error, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 10.dp))
        }
        Spacer(Modifier.height(18.dp))
        PrimaryAction("Sign in", submit, Modifier.fillMaxWidth(), canSubmit)
        Text("Your account access determines which workspace you see.", color = CricketDraftColors.Muted, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 14.dp))
    }
}

private fun roleGreeting(roles: List<String>): String = when {
    "super_admin" in roles -> "Keep the whole platform healthy."
    "admin" in roles -> "Run your tournament with confidence."
    "captain" in roles -> "Your squad and draft room are ready."
    "scorer" in roles -> "Capture every delivery with confidence."
    else -> "Follow the action and stay up to date."
}
