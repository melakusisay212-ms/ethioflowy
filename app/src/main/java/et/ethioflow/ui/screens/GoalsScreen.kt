package et.ethioflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import et.ethioflow.calendar.EthiopianDate
import et.ethioflow.data.entity.Goal
import et.ethioflow.domain.Repository
import et.ethioflow.ui.components.DarkHeader
import et.ethioflow.ui.components.WhiteCard
import et.ethioflow.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun GoalsScreen(repo: Repository, onBack: () -> Unit) {
    val goals by repo.observeGoals().collectAsState(initial = emptyList())
    var showAdd by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newDesc by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize().background(SurfaceLight)) {
        DarkHeader(
            title = "Goals",
            subtitle = "Link tasks & habits to stay on track",
            onBack = onBack,
            actions = {
                IconButton(onClick = { showAdd = true }) {
                    Icon(Icons.Default.Add, null, tint = TextOnDark)
                }
            }
        )

        LazyColumn(
            Modifier.fillMaxSize().padding(16.dp),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (goals.isEmpty()) {
                item {
                    WhiteCard {
                        Text("No goals yet.\nCreate one and link tasks or habits to it.", color = TextSecondary, modifier = Modifier.padding(24.dp))
                    }
                }
            }
            items(goals, key = { it.id }) { goal ->
                WhiteCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(12.dp).background(
                                Color(android.graphics.Color.parseColor(goal.colorHex)),
                                RoundedCornerShape(50)
                            )
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(goal.title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                            if (goal.description.isNotBlank()) Text(goal.description, color = TextSecondary, fontSize = 13.sp)
                            if (goal.targetDateEth != null) Text("Target: ${goal.targetDateEth}", color = TextSecondary, fontSize = 12.sp)
                        }
                        if (goal.isCompleted) {
                            Text("✓", color = AccentGreen, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { goal.progress.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        color = AccentBlue,
                        trackColor = Color(0xFFE2E5EF),
                    )
                    Text("${(goal.progress * 100).toInt()}%", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                    Row {
                        TextButton(onClick = { scope.launch { repo.refreshGoalProgress(goal.id) } }) {
                            Text("Refresh", fontSize = 12.sp)
                        }
                        TextButton(onClick = { scope.launch { repo.deleteGoal(goal) } }) {
                            Text("Delete", fontSize = 12.sp, color = AccentRed)
                        }
                    }
                }
            }
        }
    }

    if (showAdd) {
        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("New Goal") },
            text = {
                Column {
                    OutlinedTextField(value = newTitle, onValueChange = { newTitle = it }, label = { Text("Goal title") }, singleLine = true)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = newDesc, onValueChange = { newDesc = it }, label = { Text("Description") })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newTitle.isNotBlank()) scope.launch {
                        repo.addGoal(newTitle.trim(), newDesc.trim(), EthiopianDate.now().plusDays(30).toString())
                        newTitle = ""; newDesc = ""; showAdd = false
                    }
                }) { Text("Create") }
            },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text("Cancel") } }
        )
    }
}
