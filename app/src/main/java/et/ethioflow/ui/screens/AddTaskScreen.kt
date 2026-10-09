package et.ethioflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import et.ethioflow.calendar.EthiopianDate
import et.ethioflow.data.entity.Priority
import et.ethioflow.domain.Repository
import et.ethioflow.domain.ReminderScheduler
import et.ethioflow.ui.components.DarkHeader
import et.ethioflow.ui.theme.*
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
    repo: Repository,
    initialDate: EthiopianDate? = null,
    onDone: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var ethDate by remember { mutableStateOf(initialDate ?: EthiopianDate.now()) }
    var priority by remember { mutableStateOf(Priority.MEDIUM) }
    var hasReminder by remember { mutableStateOf(false) }
    var reminderHour by remember { mutableStateOf(9) }
    var reminderMinute by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().background(SurfaceLight)) {
        DarkHeader(
            title = "New Task",
            onBack = onDone,
            actions = {
                IconButton(onClick = onDone) {
                    Icon(Icons.Default.Close, null, tint = TextOnDark)
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                shape = RoundedCornerShape(12.dp)
            )

            // Date display
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Due date (Ethiopian)", fontWeight = FontWeight.Medium)
                    Text(
                        "${ethDate.dayOfWeekName()}, ${ethDate.monthName()} ${ethDate.day}, ${ethDate.year}",
                        color = TextSecondary
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { ethDate = ethDate.minusDays(1) }) { Text("−1 day") }
                        TextButton(onClick = { ethDate = EthiopianDate.now() }) { Text("Today") }
                        TextButton(onClick = { ethDate = ethDate.plusDays(1) }) { Text("+1 day") }
                    }
                }
            }

            // Priority
            Text("Priority", fontWeight = FontWeight.Medium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Priority.entries.forEach { p ->
                    FilterChip(
                        selected = priority == p,
                        onClick = { priority = p },
                        label = { Text(p.name) }
                    )
                }
            }

            // Reminder
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Set reminder", modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                Switch(checked = hasReminder, onCheckedChange = { hasReminder = it })
            }
            if (hasReminder) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = reminderHour.toString(),
                        onValueChange = { it.toIntOrNull()?.let { h -> if (h in 0..23) reminderHour = h } },
                        label = { Text("Hour") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = reminderMinute.toString(),
                        onValueChange = { it.toIntOrNull()?.let { m -> if (m in 0..59) reminderMinute = m } },
                        label = { Text("Min") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    if (title.isBlank()) return@Button
                    scope.launch {
                        val id = repo.addTask(
                            title = title.trim(),
                            notes = notes.trim(),
                            ethDate = ethDate,
                            priority = priority,
                            reminderHour = if (hasReminder) reminderHour else null,
                            reminderMinute = if (hasReminder) reminderMinute else null
                        )
                        onDone()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavyDark)
            ) {
                Text("Save Task", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
