package com.luna.secretnotes.ui.notes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.luna.secretnotes.data.EncryptedNotesDatabase
import com.luna.secretnotes.data.FolderRecord
import com.luna.secretnotes.data.NoteRecord
import com.luna.secretnotes.ui.theme.CosmicTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import org.json.JSONArray

private enum class NotesMode { NOTES, ARCHIVE, TRASH }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesHomeScreen(
    database: EncryptedNotesDatabase,
    onLock: () -> Unit,
    onActivity: () -> Unit
) {
    var mode by remember { mutableStateOf(NotesMode.NOTES) }
    var search by remember { mutableStateOf("") }
    var selectedFolder by remember { mutableStateOf<String?>(null) }
    var notes by remember { mutableStateOf(emptyList<NoteRecord>()) }
    var folders by remember { mutableStateOf(emptyList<FolderRecord>()) }
    var editorOpen by remember { mutableStateOf(false) }
    var folderDialogOpen by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<NoteRecord?>(null) }
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var newFolderName by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun reload() {
        scope.launch {
            loading = true
            try {
                val result = withContext(Dispatchers.IO) {
                    val list = if (search.isBlank()) {
                        when (mode) {
                            NotesMode.NOTES -> database.list(archived = false, folderId = selectedFolder)
                            NotesMode.ARCHIVE -> database.list(archived = true, folderId = selectedFolder)
                            NotesMode.TRASH -> database.list(includeTrash = true, archived = null)
                        }
                    } else {
                        database.search(
                            query = search,
                            includeTrash = mode == NotesMode.TRASH,
                            archived = when (mode) {
                                NotesMode.NOTES -> false
                                NotesMode.ARCHIVE -> true
                                NotesMode.TRASH -> null
                            }
                        )
                    }
                    list to database.listFolders()
                }
                notes = result.first
                folders = result.second
                error = null
            } catch (_: Exception) {
                error = "Не удалось загрузить заметки из зашифрованной базы."
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(mode, search, selectedFolder, database) { reload() }

    fun startNewNote() {
        editing = null
        title = ""
        body = ""
        tags = ""
        editorOpen = true
    }

    fun startEditNote(note: NoteRecord) {
        editing = note
        title = note.title
        body = note.body
        tags = runCatching {
            val json = JSONArray(note.tagsJson)
            (0 until json.length()).joinToString(", ") { json.getString(it) }
        }.getOrDefault("")
        editorOpen = true
    }

    Scaffold(
        containerColor = CosmicTheme.Background,
        floatingActionButton = {
            if (mode == NotesMode.NOTES || mode == NotesMode.ARCHIVE) {
                FloatingActionButton(
                    onClick = { onActivity(); startNewNote() },
                    containerColor = CosmicTheme.Starlight,
                    contentColor = CosmicTheme.Background
                ) { Text("+", style = MaterialTheme.typography.headlineMedium) }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("SECRET NOTES", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    Text("PRIVATE UNIVERSE", style = MaterialTheme.typography.labelSmall, color = CosmicTheme.PulsarBlue)
                }
                TextButton(onClick = { onActivity(); onLock() }) { Text("Lock") }
            }

            OutlinedTextField(
                value = search,
                onValueChange = { search = it; onActivity() },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Search encrypted notes") }
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = mode == NotesMode.NOTES, onClick = { mode = NotesMode.NOTES }, label = { Text("Notes") })
                FilterChip(selected = mode == NotesMode.ARCHIVE, onClick = { mode = NotesMode.ARCHIVE }, label = { Text("Archive") })
                FilterChip(selected = mode == NotesMode.TRASH, onClick = { mode = NotesMode.TRASH }, label = { Text("Trash") })
                Spacer(Modifier.weight(1f))
                TextButton(onClick = { onActivity(); folderDialogOpen = true }) { Text("Folders") }
            }

            if (selectedFolder != null) {
                val selectedName = folders.firstOrNull { it.id == selectedFolder }?.name ?: "Folder"
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Folder: $selectedName", color = CosmicTheme.TextSecondary)
                    TextButton(onClick = { selectedFolder = null }) { Text("Clear") }
                }
            }

            if (mode == NotesMode.TRASH && notes.isNotEmpty()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = {
                        scope.launch {
                            withContext(Dispatchers.IO) { database.emptyTrash() }
                            reload()
                        }
                    }) { Text("Empty trash permanently") }
                }
            }

            when {
                loading -> Text("Opening encrypted index…", color = CosmicTheme.TextSecondary)
                error != null -> Text(error.orEmpty(), color = MaterialTheme.colorScheme.error)
                notes.isEmpty() -> Column(
                    Modifier.fillMaxWidth().weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("✦", style = MaterialTheme.typography.displaySmall, color = CosmicTheme.PulsarBlue)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        when (mode) {
                            NotesMode.NOTES -> "Your universe is empty"
                            NotesMode.ARCHIVE -> "Nothing in the archive"
                            NotesMode.TRASH -> "Trash is empty"
                        },
                        color = CosmicTheme.TextPrimary,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        if (mode == NotesMode.NOTES) "Create your first private note." else "Items will appear here when moved.",
                        color = CosmicTheme.TextMuted
                    )
                }
                else -> LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(notes, key = { it.id }) { note ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable {
                                onActivity()
                                if (mode != NotesMode.TRASH) startEditNote(note)
                            },
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = CosmicTheme.Card)
                        ) {
                            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        note.title.ifBlank { "Untitled note" },
                                        modifier = Modifier.weight(1f),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = CosmicTheme.TextPrimary,
                                        fontWeight = FontWeight.Medium
                                    )
                                    if (note.pinned) Text("✦", color = CosmicTheme.PulsarBlue)
                                }
                                if (note.body.isNotBlank()) {
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        note.body,
                                        maxLines = 3,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = CosmicTheme.TextSecondary
                                    )
                                }
                                if (note.tagsJson != "[]") {
                                    Spacer(Modifier.height(7.dp))
                                    Text(note.tagsJson, style = MaterialTheme.typography.labelSmall, color = CosmicTheme.TextMuted)
                                }
                                Spacer(Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    if (mode == NotesMode.TRASH) {
                                        TextButton(onClick = {
                                            scope.launch {
                                                withContext(Dispatchers.IO) { database.restoreFromTrash(note.id, System.currentTimeMillis()) }
                                                reload()
                                            }
                                        }) { Text("Restore") }
                                        TextButton(onClick = {
                                            scope.launch {
                                                withContext(Dispatchers.IO) { database.permanentlyDelete(note.id) }
                                                reload()
                                            }
                                        }) { Text("Delete forever") }
                                    } else {
                                        TextButton(onClick = {
                                            scope.launch {
                                                withContext(Dispatchers.IO) { database.setPinned(note.id, !note.pinned, System.currentTimeMillis()) }
                                                reload()
                                            }
                                        }) { Text(if (note.pinned) "Unpin" else "Pin") }
                                        TextButton(onClick = {
                                            scope.launch {
                                                withContext(Dispatchers.IO) { database.setArchived(note.id, !note.archived, System.currentTimeMillis()) }
                                                reload()
                                            }
                                        }) { Text(if (note.archived) "Unarchive" else "Archive") }
                                        TextButton(onClick = {
                                            scope.launch {
                                                withContext(Dispatchers.IO) { database.moveToTrash(note.id, System.currentTimeMillis()) }
                                                reload()
                                            }
                                        }) { Text("Trash") }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (editorOpen) {
        AlertDialog(
            onDismissRequest = { editorOpen = false },
            title = { Text(if (editing == null) "New note" else "Edit note") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it; onActivity() },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = body,
                        onValueChange = { body = it; onActivity() },
                        label = { Text("Note") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 5
                    )
                    OutlinedTextField(
                        value = tags,
                        onValueChange = { tags = it; onActivity() },
                        label = { Text("Tags, separated by commas") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val now = System.currentTimeMillis()
                    val old = editing
                    val encodedTags = JSONArray(
                        tags.split(",").map { it.trim() }.filter { it.isNotBlank() }
                    ).toString()
                    scope.launch {
                        withContext(Dispatchers.IO) {
                            database.save(
                                NoteRecord(
                                    id = old?.id ?: UUID.randomUUID().toString(),
                                    title = title.trim(),
                                    body = body,
                                    createdAt = old?.createdAt ?: now,
                                    updatedAt = now,
                                    folderId = old?.folderId ?: selectedFolder,
                                    tagsJson = encodedTags,
                                    pinned = old?.pinned ?: false,
                                    archived = old?.archived ?: false
                                )
                            )
                        }
                        editorOpen = false
                        reload()
                    }
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { editorOpen = false }) { Text("Cancel") }
            }
        )
    }

    if (folderDialogOpen) {
        AlertDialog(
            onDismissRequest = { folderDialogOpen = false },
            title = { Text("Folders") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newFolderName,
                        onValueChange = { newFolderName = it; onActivity() },
                        label = { Text("New folder name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    folders.forEach { folder ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = {
                                selectedFolder = folder.id
                                folderDialogOpen = false
                            }) { Text(folder.name) }
                            Spacer(Modifier.weight(1f))
                            TextButton(onClick = {
                                scope.launch {
                                    withContext(Dispatchers.IO) { database.deleteFolder(folder.id) }
                                    if (selectedFolder == folder.id) selectedFolder = null
                                    reload()
                                }
                            }) { Text("Remove") }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    val name = newFolderName.trim()
                    if (name.isNotEmpty()) {
                        val now = System.currentTimeMillis()
                        scope.launch {
                            withContext(Dispatchers.IO) {
                                database.saveFolder(FolderRecord(UUID.randomUUID().toString(), name, now, now))
                            }
                            newFolderName = ""
                            reload()
                        }
                    }
                }) { Text("Create") }
            },
            dismissButton = { TextButton(onClick = { folderDialogOpen = false }) { Text("Close") } }
        )
    }
}
