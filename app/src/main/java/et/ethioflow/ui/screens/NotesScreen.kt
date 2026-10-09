package et.ethioflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import et.ethioflow.data.entity.Note
import et.ethioflow.data.entity.ParaType
import et.ethioflow.domain.Repository
import et.ethioflow.ui.components.DarkHeader
import et.ethioflow.ui.components.WhiteCard
import et.ethioflow.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun NotesScreen(repo: Repository, onBack: () -> Unit) {
    var selectedPara by remember { mutableStateOf(ParaType.INBOX) }
    val notes by repo.observeNotesByPara(selectedPara).collectAsState(initial = emptyList())
    var showAdd by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newBody by remember { mutableStateOf("") }
    var editing by remember { mutableStateOf<Note?>(null) }
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize().background(SurfaceLight)) {
        DarkHeader(
            title = "Second Brain",
            subtitle = "PARA · Inbox → Organize",
            onBack = onBack,
            actions = {
                IconButton(onClick = { showAdd = true; editing = null; newTitle = ""; newBody = "" }) {
                    Icon(Icons.Default.Add, null, tint = TextOnDark)
                }
            }
        )

        // PARA tabs
        LazyRow(
            modifier = Modifier.fillMaxWidth().background(NavyDark).padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(ParaType.entries) { para ->
                FilterChip(
                    selected = selectedPara == para,
                    onClick = { selectedPara = para },
                    label = { Text(para.name.lowercase().replaceFirstChar { it.uppercase() }, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AccentBlue,
                        selectedLabelColor = TextOnDark,
                        containerColor = NavySoft,
                        labelColor = TextOnDarkSecondary
                    )
                )
            }
        }

        LazyColumn(
            Modifier.fillMaxSize().padding(16.dp),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (notes.isEmpty()) {
                item {
                    WhiteCard {
                        Text(
                            when (selectedPara) {
                                ParaType.INBOX -> "Inbox is empty.\nQuick-capture ideas here, then move them."
                                else -> "No notes in ${selectedPara.name.lowercase()}."
                            },
                            color = TextSecondary,
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                }
            }
            items(notes, key = { it.id }) { note ->
                WhiteCard(modifier = Modifier.clickable {
                    editing = note; newTitle = note.title; newBody = note.body; showAdd = true
                }) {
                    Text(note.title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    if (note.body.isNotBlank()) {
                        Text(note.body.take(120), color = TextSecondary, fontSize = 13.sp, maxLines = 2)
                    }
                    if (note.tags.isNotBlank()) {
                        Text(note.tags, color = AccentBlue, fontSize = 11.sp)
                    }
                    // Move actions for Inbox
                    if (selectedPara == ParaType.INBOX) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(ParaType.PROJECTS, ParaType.AREAS, ParaType.RESOURCES, ParaType.ARCHIVE).forEach { p ->
                                TextButton(onClick = { scope.launch { repo.moveNote(note, p) } }) {
                                    Text(p.name.take(4), fontSize = 11.sp)
                                }
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
            title = { Text(if (editing != null) "Edit Note" else "Quick Capture") },
            text = {
                Column {
                    OutlinedTextField(value = newTitle, onValueChange = { newTitle = it }, label = { Text("Title") }, singleLine = true)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = newBody, onValueChange = { newBody = it }, label = { Text("Body") }, modifier = Modifier.height(120.dp))
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newTitle.isNotBlank()) scope.launch {
                        if (editing != null) repo.updateNote(editing!!.copy(title = newTitle.trim(), body = newBody.trim()))
                        else repo.addNote(newTitle.trim(), newBody.trim(), selectedPara)
                        showAdd = false
                    }
                }) { Text("Save") }
            },
            dismissButton = {
                Row {
                    if (editing != null) {
                        TextButton(onClick = { scope.launch { repo.deleteNote(editing!!); showAdd = false } }) {
                            Text("Delete", color = AccentRed)
                        }
                    }
                    TextButton(onClick = { showAdd = false }) { Text("Cancel") }
                }
            }
        )
    }
}
