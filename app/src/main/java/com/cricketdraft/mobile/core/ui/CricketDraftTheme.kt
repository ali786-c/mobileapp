package com.cricketdraft.mobile.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object CricketDraftColors {
    val Green = Color(0xFF13795B)
    val DeepGreen = Color(0xFF062C20)
    val Lime = Color(0xFFD7F36B)
    val Canvas = Color(0xFFF4F7F3)
    val Surface = Color(0xFFFFFFFF)
    val Ink = Color(0xFF14332A)
    val Muted = Color(0xFF61756D)
    val Border = Color(0xFFDCE7E1)
    val Success = Color(0xFF16794F)
    val Warning = Color(0xFF986B00)
    val Danger = Color(0xFFB42318)
}

object CricketDraftSpacing {
    val Screen = 20.dp
    val Section = 16.dp
    val Card = 18.dp
    val Compact = 8.dp
}

@Composable
fun CricketDraftTheme(content: @Composable () -> Unit) {
    val baseTypography = Typography()
    MaterialTheme(
        colorScheme = androidx.compose.material3.lightColorScheme(
            primary = CricketDraftColors.Green,
            onPrimary = Color.White,
            secondary = CricketDraftColors.DeepGreen,
            background = CricketDraftColors.Canvas,
            surface = CricketDraftColors.Surface,
            onSurface = CricketDraftColors.Ink,
            error = CricketDraftColors.Danger,
        ),
        typography = androidx.compose.material3.Typography(
            headlineLarge = baseTypography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold, lineHeight = 40.sp),
            headlineMedium = baseTypography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold, lineHeight = 34.sp),
            titleLarge = baseTypography.titleLarge.copy(fontWeight = FontWeight.Bold, lineHeight = 28.sp),
            bodyLarge = baseTypography.bodyLarge.copy(lineHeight = 24.sp),
            bodyMedium = baseTypography.bodyMedium.copy(lineHeight = 20.sp),
        ),
        content = content,
    )
}

@Composable
fun PrimaryAction(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = CricketDraftColors.Green),
    ) { Text(text, fontWeight = FontWeight.Bold) }
}

@Composable
fun SecondaryAction(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 18.dp, vertical = 13.dp),
    ) { Text(text, fontWeight = FontWeight.Bold, color = CricketDraftColors.Green) }
}

@Composable
fun ScreenHeader(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Go back", tint = CricketDraftColors.Ink)
            }
        }
        SectionTitle(title, subtitle, Modifier.weight(1f))
    }
}

@Composable
fun SectionTitle(title: String, subtitle: String? = null, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = CricketDraftColors.Ink)
        subtitle?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = CricketDraftColors.Muted) }
    }
}

@Composable
fun MetricCard(label: String, value: String, supportingText: String? = null, modifier: Modifier = Modifier) {
    Card(
        modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CricketDraftColors.Surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, CricketDraftColors.Border),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label.uppercase(), color = CricketDraftColors.Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(value, color = CricketDraftColors.Ink, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
            supportingText?.let { Text(it, color = CricketDraftColors.Muted, style = MaterialTheme.typography.bodySmall) }
        }
    }
}

@Composable
fun LiveIndicator(isLive: Boolean, modifier: Modifier = Modifier) {
    val color = if (isLive) CricketDraftColors.Success else CricketDraftColors.Warning
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Icon(Icons.Default.Circle, contentDescription = null, tint = color, modifier = Modifier.size(8.dp))
        Text(if (isLive) "Live" else "Updating", color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ActionCard(title: String, message: String, actionText: String, onAction: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CricketDraftColors.DeepGreen),
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, color = CricketDraftColors.Lime, fontWeight = FontWeight.Bold)
            Text(message, color = Color.White.copy(alpha = .82f), style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(2.dp))
            PrimaryAction(actionText, onAction)
        }
    }
}

@Composable
fun StateCard(title: String, message: String, modifier: Modifier = Modifier, actionText: String? = null, onAction: (() -> Unit)? = null) {
    Card(modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = CricketDraftColors.Surface), border = androidx.compose.foundation.BorderStroke(1.dp, CricketDraftColors.Border)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, color = CricketDraftColors.Ink, fontWeight = FontWeight.Bold)
            Text(message, color = CricketDraftColors.Muted, style = MaterialTheme.typography.bodyMedium)
            if (actionText != null && onAction != null) TextButton(onClick = onAction) { Text(actionText, color = CricketDraftColors.Green, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
fun EmptyState(title: String, message: String, modifier: Modifier = Modifier, actionText: String? = null, onAction: (() -> Unit)? = null) {
    StateCard(title, message, modifier, actionText, onAction)
}

@Composable
fun ErrorState(title: String = "Something went wrong", message: String, modifier: Modifier = Modifier, actionText: String? = "Try again", onAction: (() -> Unit)? = null) {
    StateCard(title, message, modifier, actionText, onAction)
}

@Composable
fun OfflineBanner(message: String = "You are offline. Showing the latest saved information.", modifier: Modifier = Modifier) {
    Surface(modifier.fillMaxWidth(), color = CricketDraftColors.Warning.copy(alpha = .12f), shape = RoundedCornerShape(12.dp)) {
        Text(message, color = CricketDraftColors.Warning, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(12.dp))
    }
}

@Composable
fun LoadingState(message: String = "Loading your tournament data…") {
    Row(Modifier.fillMaxWidth().padding(vertical = 28.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 3.dp, color = CricketDraftColors.Green)
        Text(message, Modifier.padding(start = 12.dp), color = CricketDraftColors.Muted)
    }
}

@Composable
fun StatusChip(status: String) {
    val normalized = status.lowercase()
    val background = when (normalized) { "live", "approved", "selected", "healthy" -> CricketDraftColors.Success.copy(alpha = .12f); "pending", "paused", "registration", "ready" -> CricketDraftColors.Warning.copy(alpha = .14f); "cancelled", "expired", "error" -> CricketDraftColors.Danger.copy(alpha = .12f); else -> CricketDraftColors.Border }
    val foreground = when (normalized) { "live", "approved", "selected", "healthy" -> CricketDraftColors.Success; "pending", "paused", "registration", "ready" -> CricketDraftColors.Warning; "cancelled", "expired", "error" -> CricketDraftColors.Danger; else -> CricketDraftColors.Muted }
    Surface(color = background, shape = RoundedCornerShape(50)) { Text(status.replaceFirstChar { it.uppercase() }, color = foreground, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) }
}
