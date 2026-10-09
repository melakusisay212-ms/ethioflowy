package et.ethioflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import et.ethioflow.calendar.EthiopianDate
import et.ethioflow.data.entity.TxType
import et.ethioflow.domain.Repository
import et.ethioflow.ui.components.DarkHeader
import et.ethioflow.ui.components.WhiteCard
import et.ethioflow.ui.theme.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@Composable
fun JournalScreen(repo: Repository, onBack: () -> Unit) {
    val today = remember { EthiopianDate.now() }
    var mood by remember { mutableIntStateOf(3) }
    var body by remember { mutableStateOf("") }
    var highlights by remember { mutableStateOf("") }
    var review by remember { mutableStateOf<Repository.WeeklyReview?>(null) }
    var tab by remember { mutableIntStateOf(0) } // 0 = journal, 1 = weekly
    val scope = rememberCoroutineScope()
    val fmt = remember { NumberFormat.getNumberInstance(Locale.US) }

    LaunchedEffect(Unit) {
        val existing = repo.getJournalFor(today)
        if (existing != null) {
            mood = existing.mood
            body = existing.body
            highlights = existing.highlights
        }
        review = repo.buildWeeklyReview(today)
    }

    Column(Modifier.fillMaxSize().background(SurfaceLight)) {
        DarkHeader(
            title = if (tab == 0) "Journal" else "Weekly Review",
            subtitle = if (tab == 0) "${today.dayOfWeekName()}, ${today.monthName()} ${today.day}" else "Last 7 Ethiopian days",
            onBack = onBack,
            actions = {
                if (tab == 1) {
                    IconButton(onClick = { scope.launch { review = repo.buildWeeklyReview() } }) {
                        Icon(Icons.Default.Refresh, null, tint = TextOnDark)
                    }
                }
            }
        )

        // Tab switcher
        Row(Modifier.fillMaxWidth().background(NavyDark).padding(horizontal = 16.dp, vertical = 8.dp)) {
            FilterChip(selected = tab == 0, onClick = { tab = 0 }, label = { Text("Today") },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AccentBlue, selectedLabelColor = TextOnDark, containerColor = NavySoft, labelColor = TextOnDarkSecondary))
            Spacer(Modifier.width(8.dp))
            FilterChip(selected = tab == 1, onClick = { tab = 1 }, label = { Text("Weekly Review") },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AccentBlue, selectedLabelColor = TextOnDark, containerColor = NavySoft, labelColor = TextOnDarkSecondary))
        }

        if (tab == 0) {
            Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("How are you feeling?", fontWeight = FontWeight.Medium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("😞" to 1, "😐" to 2, "🙂" to 3, "😊" to 4, "🤩" to 5).forEach { (emoji, v) ->
                        FilterChip(selected = mood == v, onClick = { mood = v }, label = { Text(emoji) })
                    }
                }
                OutlinedTextField(
                    value = body, onValueChange = { body = it },
                    label = { Text("Journal entry") },
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = highlights, onValueChange = { highlights = it },
                    label = { Text("Highlights / wins") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Button(
                    onClick = { scope.launch { repo.saveJournal(today, mood, body, highlights) } },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyDark)
                ) { Text("Save Entry") }
            }
        } else {
            val r = review
            LazyColumn(Modifier.fillMaxSize().padding(16.dp), contentPadding = PaddingValues(bottom = 100.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (r == null) {
                    item { CircularProgressIndicator() }
                } else {
                    item {
                        WhiteCard {
                            Text("${r.from.monthName()} ${r.from.day} → ${r.to.monthName()} ${r.to.day}, ${r.to.year}", fontWeight = FontWeight.SemiBold)
                            Text("Ethiopian week", color = TextSecondary, fontSize = 12.sp)
                        }
                    }
                    item {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatBox("Tasks done", r.tasksCompleted.size.toString(), AccentBlue, Modifier.weight(1f))
                            StatBox("Habit checks", r.habitLogs.size.toString(), AccentGreen, Modifier.weight(1f))
                        }
                    }
                    item {
                        val exp = r.transactions.filter { it.type == TxType.EXPENSE }.sumOf { it.amount }
                        val inc = r.transactions.filter { it.type == TxType.INCOME }.sumOf { it.amount }
                        WhiteCard {
                            Text("Finance this week", fontWeight = FontWeight.SemiBold)
                            Text("Income +${fmt.format(inc)} · Expense -${fmt.format(exp)}", color = TextSecondary)
                        }
                    }
                    if (r.tasksCompleted.isNotEmpty()) {
                        item {
                            Text("Completed tasks", fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(6.dp))
                            r.tasksCompleted.forEach { t ->
                                WhiteCard { Text("✓ ${t.title}", fontSize = 14.sp) }
                                Spacer(Modifier.height(6.dp))
                            }
                        }
                    }
                    if (r.journalEntries.isNotEmpty()) {
                        item {
                            Text("Journal entries", fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(6.dp))
                            r.journalEntries.forEach { j ->
                                WhiteCard {
                                    Text(j.ethDate, color = TextSecondary, fontSize = 12.sp)
                                    if (j.body.isNotBlank()) Text(j.body.take(100), fontSize = 14.sp)
                                }
                                Spacer(Modifier.height(6.dp))
                            }
                        }
                    }
                    if (r.tasksCompleted.isEmpty() && r.habitLogs.isEmpty() && r.transactions.isEmpty()) {
                        item { WhiteCard { Text("Quiet week — nothing logged yet.", color = TextSecondary, modifier = Modifier.padding(16.dp)) } }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatBox(label: String, value: String, color: androidx.compose.ui.graphics.Color, modifier: Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
        Column(Modifier.padding(14.dp)) {
            Text(label, color = TextSecondary, fontSize = 12.sp)
            Text(value, color = color, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }
}
