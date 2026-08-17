package com.cricketdraft.mobile

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberSaveable
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
import com.cricketdraft.mobile.core.ui.ActionCard
import com.cricketdraft.mobile.core.ui.CricketDraftColors
import com.cricketdraft.mobile.core.ui.CricketDraftSpacing
import com.cricketdraft.mobile.core.ui.CricketDraftTheme
import com.cricketdraft.mobile.core.ui.LiveIndicator
import com.cricketdraft.mobile.core.ui.LoadingState
import com.cricketdraft.mobile.core.ui.MetricCard
import com.cricketdraft.mobile.core.ui.PrimaryAction
import com.cricketdraft.mobile.core.ui.ScreenHeader
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
            composable(AppDestination.Home.route) { HomeScreen(session, navController, onLogout, isExplore = false) }
            composable(AppDestination.Tournaments.route) { HomeScreen(session, navController, onLogout, isExplore = true) }
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
                onClick = {
                    navController.navigate(destination) {
                        launchSingleTop = true
                        restoreState = true
                        popUpTo(AppDestination.Home.route) { saveState = true }
                    }
                },
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label, style = MaterialTheme.typography.labelMedium) },
            )
        }
    }
}

@Composable
private fun HomeScreen(
    session: SessionState.SignedIn,
    navController: NavHostController,
    onLogout: () -> Unit,
    isExplore: Boolean,
    viewModel: TournamentViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var statusFilter by rememberSaveable { mutableStateOf("All") }
    LazyColumn(contentPadding = PaddingValues(CricketDraftSpacing.Screen), verticalArrangement = Arrangement.spacedBy(CricketDraftSpacing.Section)) {
        item {
            if (isExplore) {
                ScreenHeader("Explore tournaments", "Browse public events, fixtures, standings, and live updates.")
            } else {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("CRICKET DRAFT OS", color = CricketDraftColors.Green, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Hi, ${session.user.name ?: "there"}", color = CricketDraftColors.Ink, style = MaterialTheme.typography.headlineMedium)
                        Text("Your tournaments and live cricket, in one place.", color = CricketDraftColors.Muted)
                    }
                    TextButton(onClick = onLogout) { Text("Log out", color = CricketDraftColors.Green) }
                }
            }
        }
        if (!isExplore) item {
            val role = session.user.roles.firstOrNull()?.replace('_', ' ')?.replaceFirstChar { it.uppercase() } ?: "Viewer"
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard("Role", role, modifier = Modifier.weight(1f))
                MetricCard("Session", "Secure", "Laravel connected", modifier = Modifier.weight(1f))
            }
        }
        if (!isExplore) item {
            val role = session.user.roles.firstOrNull()
            ActionCard(
                title = "Your workspace",
                message = roleGreeting(session.user.roles),
                actionText = when (role) {
                    "captain" -> "Open my tournament"
                    "admin" -> "Manage tournaments"
                    "super_admin" -> "Open governance"
                    else -> "Explore live cricket"
                },
                onAction = {
                    when (role) {
                        "captain" -> navController.navigate(AppDestination.Tournaments.route)
                        "admin" -> navController.navigate(AppDestination.AdminWorkspace.route)
                        "super_admin" -> navController.navigate(AppDestination.SuperAdmin.route)
                        else -> navController.navigate(AppDestination.MatchCenter.route)
                    }
                },
            )
        }
        if (!isExplore) item { SectionTitle("Public tournaments", "Browse events, fixtures, standings, and live updates.") }
        if (isExplore) item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search tournaments") },
                singleLine = true,
            )
        }
        if (isExplore) item {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("All", "Live", "Registration", "Completed").forEach { filter ->
                    FilterChip(
                        selected = statusFilter == filter,
                        onClick = { statusFilter = filter },
                        label = { Text(filter) },
                    )
                }
            }
        }
        when (val value = state) {
            TournamentListState.Loading -> item { LoadingState() }
            is TournamentListState.Error -> item { StateCard("Could not load tournaments", value.message, actionText = "Try again", onAction = { viewModel.load() }) }
            is TournamentListState.Ready -> {
                val visibleTournaments = value.tournaments.filter { tournament ->
                    val matchesQuery = searchQuery.isBlank() ||
                        tournament.name.contains(searchQuery, ignoreCase = true) ||
                        (tournament.city ?: tournament.location.orEmpty()).contains(searchQuery, ignoreCase = true)
                    val matchesStatus = statusFilter == "All" || tournament.status.equals(statusFilter, ignoreCase = true)
                    matchesQuery && matchesStatus
                }
                if (visibleTournaments.isEmpty()) item {
                    StateCard(
                        if (value.tournaments.isEmpty()) "No tournaments yet" else "No matching tournaments",
                        if (value.tournaments.isEmpty()) "When an event becomes public, it will appear here." else "Try another search term or clear the status filter.",
                    )
                }
                items(visibleTournaments, key = { it.id }) { tournament ->
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
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Live updates", color = CricketDraftColors.Muted, style = MaterialTheme.typography.bodySmall)
                LiveIndicator(isLive = true)
            }
            Spacer(Modifier.height(60.dp))
        }
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
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    val canSubmit = email.isNotBlank() && password.isNotBlank()
    val submit = { if (canSubmit) onLogin(email.trim(), password, "cricket-draft-android") }
    Column(
        Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Card(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = CricketDraftColors.Surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, CricketDraftColors.Border),
        ) {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(color = CricketDraftColors.Lime, shape = RoundedCornerShape(14.dp)) {
                    Text("CD", color = CricketDraftColors.DeepGreen, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp))
                }
                Text("CRICKET DRAFT OS", color = CricketDraftColors.Green, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("Welcome back", color = CricketDraftColors.Ink, style = MaterialTheme.typography.headlineLarge)
                Text("Sign in to manage tournaments, squads, and live cricket.", color = CricketDraftColors.Muted)
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Email address") },
                    singleLine = true,
                    isError = error != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Password") },
                    singleLine = true,
                    isError = error != null,
                    visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { submit() }),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            )
                        }
                    },
                )
                if (error != null) {
                    Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                PrimaryAction("Sign in", submit, Modifier.fillMaxWidth(), canSubmit)
                Text("Your role determines which workspace you see after sign in.", color = CricketDraftColors.Muted, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

private fun roleGreeting(roles: List<String>): String = when {
    "super_admin" in roles -> "Keep the whole platform healthy."
    "admin" in roles -> "Run your tournament with confidence."
    "captain" in roles -> "Your squad and draft room are ready."
    "scorer" in roles -> "Capture every delivery with confidence."
    else -> "Follow the action and stay up to date."
}
