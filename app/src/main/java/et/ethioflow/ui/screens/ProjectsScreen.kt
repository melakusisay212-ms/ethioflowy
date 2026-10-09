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
import et.ethioflow.data.entity.Project
import et.ethioflow.domain.Repository
import et.ethioflow.ui.components.DarkHeader
import et.ethioflow.ui.components.WhiteCard
import et.ethioflow.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ProjectsScreen(
    repo: Repository,
    onBack: () -> Unit
) {
    val projects by repo.observeProjects().collectAsState(initial = emptyList())
    var showAdd by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().background(SurfaceLight)) {
        DarkHeader(
            title = "Projects",
            subtitle = "Organize your work",
            onBack = onBack,
            actions = {
                IconButton(onClick = { showAdd = true }) {
                    Icon(Icons.Default.Add, null, tint = TextOnDark)
                }
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (projects.isEmpty()) {
                item {
                    WhiteCard {
                        Text(
                            "No projects yet.\nCreate one to group your tasks.",
                            color = TextSecondary,
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                }
            }
            items(projects, key = { it.id }) { project ->
                WhiteCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(
                                    Color(android.graphics.Color.parseColor(project.colorHex)),
                                    shape = RoundedCornerShape(50)
                                )
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(project.title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                            if (project.description.isNotBlank()) {
                                Text(project.description, color = TextSecondary, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAdd) {
        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("New Project") },
            text = {
                OutlinedTextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    label = { Text("Project name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newTitle.isNotBlank()) {
                        scope.launch {
                            repo.addProject(newTitle.trim())
                            newTitle = ""
                            showAdd = false
                        }
                    }
                }) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showAdd = false }) { Text("Cancel") }
            }
        )
    }
}
