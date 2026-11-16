package com.luna.secretnotes.ui.notes

import android.content.ClipData
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.luna.secretnotes.data.AttachmentRecord
import com.luna.secretnotes.data.EncryptedNotesDatabase
import com.luna.secretnotes.ui.theme.CosmicTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.UUID

@Composable
fun NoteAttachmentsSection(
    database: EncryptedNotesDatabase,
    noteId: String,
    onActivity: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var attachments by remember(noteId) { mutableStateOf(emptyList<AttachmentRecord>()) }
    var preview by remember { mutableStateOf<AttachmentRecord?>(null) }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    fun refresh() {
        scope.launch {
            attachments = withContext(Dispatchers.IO) { database.listAttachments(noteId) }
        }
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            onActivity()
            scope.launch {
                busy = true
                message = null
                var key: ByteArray? = null
                try {
                    val details = withContext(Dispatchers.IO) {
                        val displayName = queryDisplayName(context, uri)
                        val mime = context.contentResolver.getType(uri) ?: "application/octet-stream"
                        val input = context.contentResolver.openInputStream(uri)
                            ?: error("The selected file cannot be opened")
                        val id = UUID.randomUUID().toString()
                        key = database.copyVaultKeyForAttachment()
                        val encrypted = input.use {
                            database.newAttachmentCipher().encrypt(it, id, key!!)
                        }
                        Triple(displayName, mime, encrypted)
                    }
                    withContext(Dispatchers.IO) {
                        database.saveAttachment(
                            AttachmentRecord(
                                id = details.third.id,
                                noteId = noteId,
                                encryptedFileName = details.third.encryptedFileName,
                                displayName = details.first,
                                mimeType = details.second,
                                sizeBytes = details.third.sizeBytes,
                                createdAt = System.currentTimeMillis()
                            )
                        )
                    }
                    refresh()
                    message = "File encrypted and attached."
                } catch (_: Exception) {
                    message = "The file could not be encrypted or attached."
                } finally {
                    key?.fill(0)
                    busy = false
                }
            }
        }
    }

    LaunchedEffect(noteId) {
        attachments = withContext(Dispatchers.IO) { database.listAttachments(noteId) }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                enabled = !busy,
                onClick = {
                    onActivity()
                    picker.launch(arrayOf("*/*"))
                }
            ) {
                if (busy) CircularProgressIndicator(Modifier.height(16.dp), strokeWidth = 2.dp)
                else Text("Attach file")
            }
            if (attachments.isNotEmpty()) {
                Text(
                    "${attachments.size} encrypted file(s)",
                    style = MaterialTheme.typography.labelMedium,
                    color = CosmicTheme.TextMuted,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
        attachments.forEach { attachment ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = {
                    onActivity()
                    preview = attachment
                }) {
                    Text(
                        "${attachment.displayName} · ${formatBytes(attachment.sizeBytes)}",
                        color = CosmicTheme.PulsarBlue
                    )
                }
                TextButton(onClick = {
                    onActivity()
                    scope.launch {
                        withContext(Dispatchers.IO) { database.deleteAttachment(attachment.id) }
                        refresh()
                    }
                }) { Text("Remove") }
            }
        }
        message?.let {
            Text(it, style = MaterialTheme.typography.bodySmall, color = CosmicTheme.TextMuted)
        }
    }

    preview?.let { attachment ->
        AttachmentPreviewDialog(
            database = database,
            attachment = attachment,
            onActivity = onActivity,
            onDismiss = { preview = null }
        )
    }
}

@Composable
private fun AttachmentPreviewDialog(
    database: EncryptedNotesDatabase,
    attachment: AttachmentRecord,
    onActivity: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var decryptedFile by remember(attachment.id) { mutableStateOf<File?>(null) }
    var previewText by remember(attachment.id) { mutableStateOf<String?>(null) }
    var bitmap by remember(attachment.id) { mutableStateOf<Bitmap?>(null) }
    var pageCount by remember(attachment.id) { mutableStateOf<Int?>(null) }
    var loading by remember(attachment.id) { mutableStateOf(true) }
    var error by remember(attachment.id) { mutableStateOf<String?>(null) }
    var showExternalWarning by remember { mutableStateOf(false) }
    var externalBusy by remember { mutableStateOf(false) }
    var externalError by remember { mutableStateOf<String?>(null) }

    DisposableEffect(decryptedFile) {
        val file = decryptedFile
        onDispose { file?.delete() }
    }

    LaunchedEffect(attachment.id) {
        loading = true
        try {
            val file = withContext(Dispatchers.IO) {
                val directory = File(context.cacheDir, "secret_notes_preview").apply { mkdirs() }
                val destination = File(directory, UUID.randomUUID().toString() + safeExtension(attachment.displayName))
                val key = database.copyVaultKeyForAttachment()
                try {
                    database.newAttachmentCipher().decryptToFile(
                        attachment.encryptedFileName, attachment.id, key, destination
                    )
                } finally {
                    key.fill(0)
                }
                destination
            }
            decryptedFile = file
            when {
                attachment.mimeType.startsWith("text/") ||
                    attachment.mimeType == "application/json" ||
                    attachment.displayName.endsWith(".md", ignoreCase = true) ||
                    attachment.displayName.endsWith(".csv", ignoreCase = true) -> {
                    previewText = withContext(Dispatchers.IO) { readTextPreview(file) }
                }
                attachment.mimeType.startsWith("image/") -> {
                    bitmap = withContext(Dispatchers.IO) { decodePreviewImage(file) }
                    if (bitmap == null) error = "This image format cannot be previewed on this device."
                }
                attachment.mimeType == "application/pdf" ||
                    attachment.displayName.endsWith(".pdf", ignoreCase = true) -> {
                    val result = withContext(Dispatchers.IO) { renderPdfFirstPage(file) }
                    bitmap = result.first
                    pageCount = result.second
                    if (bitmap == null) error = "The PDF could not be rendered."
                }
                else -> error = "No built-in preview is available for this file type."
            }
        } catch (_: Exception) {
            error = "The encrypted file could not be verified or decrypted."
        } finally {
            loading = false
        }
    }

    fun openExternally() {
        val currentFile = decryptedFile
        if (currentFile == null) {
            externalError = "The file is not ready."
            return
        }
        scope.launch {
            externalBusy = true
            try {
                val exportFile = withContext(Dispatchers.IO) {
                    val directory = File(context.cacheDir, "secret_notes_external").apply {
                        if (!exists()) mkdirs()
                    }
                    val target = File(directory, attachment.id + safeExtension(attachment.displayName))
                    currentFile.copyTo(target, overwrite = true)
                    target
                }
                val uri = FileProvider.getUriForFile(
                    context,
                    context.packageName + ".fileprovider",
                    exportFile
                )
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, attachment.mimeType)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    clipData = ClipData.newUri(context.contentResolver, attachment.displayName, uri)
                }
                context.startActivity(Intent.createChooser(intent, "Open decrypted file"))
                onActivity()
                onDismiss()
            } catch (_: Exception) {
                externalError = "No compatible application could open this file."
            } finally {
                externalBusy = false
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(attachment.displayName) },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 460.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "${attachment.mimeType} · ${formatBytes(attachment.sizeBytes)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = CosmicTheme.TextMuted
                )
                when {
                    loading -> CircularProgressIndicator(color = CosmicTheme.PulsarBlue)
                    previewText != null -> Text(
                        previewText.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = CosmicTheme.TextPrimary
                    )
                    bitmap != null -> Image(
                        bitmap = bitmap!!.asImageBitmap(),
                        contentDescription = attachment.displayName,
                        modifier = Modifier.fillMaxWidth().heightIn(max = 360.dp),
                        contentScale = ContentScale.Fit
                    )
                    else -> Text(error.orEmpty(), color = CosmicTheme.TextSecondary)
                }
                pageCount?.let {
                    Text("PDF · $it page(s), first page preview", color = CosmicTheme.TextMuted)
                }
                externalError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                if (showExternalWarning) {
                    Text(
                        "The file will be decrypted into a temporary private cache file and shared with another app. That app may copy or retain it. Continue only if you trust the receiving app.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            if (showExternalWarning) {
                Button(enabled = !externalBusy, onClick = { openExternally() }) {
                    Text(if (externalBusy) "Preparing…" else "Continue")
                }
            } else {
                TextButton(onClick = onDismiss) { Text("Close") }
            }
        },
        dismissButton = {
            if (!showExternalWarning && !loading) {
                TextButton(onClick = { showExternalWarning = true }) {
                    Text("Open in another app")
                }
            } else if (showExternalWarning) {
                TextButton(onClick = { showExternalWarning = false }) { Text("Cancel") }
            }
        }
    )
}

private fun queryDisplayName(context: android.content.Context, uri: Uri): String {
    val fromProvider = context.contentResolver.query(
        uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null
    )?.use { cursor ->
        if (cursor.moveToFirst()) cursor.getString(0) else null
    }
    return fromProvider?.takeIf { it.isNotBlank() } ?: "attachment-${UUID.randomUUID()}"
}

private fun safeExtension(name: String): String {
    val extension = name.substringAfterLast('.', "").takeIf { it.matches(Regex("[A-Za-z0-9]{1,10}")) }
    return extension?.let { ".$it" } ?: ".bin"
}

private fun formatBytes(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "${bytes / 1024} KB"
    else -> "${bytes / (1024 * 1024)} MB"
}

private fun readTextPreview(file: File): String {
    val bytes = ByteArray(MAX_TEXT_PREVIEW_BYTES)
    val count = FileInputStream(file).use { input ->
        var total = 0
        while (total < bytes.size) {
            val read = input.read(bytes, total, bytes.size - total)
            if (read < 0) break
            if (read > 0) total += read
        }
        total
    }
    val text = bytes.copyOf(count).toString(Charsets.UTF_8)
    bytes.fill(0)
    return if (file.length() > count) "$text

[Preview truncated]" else text
}

private fun decodePreviewImage(file: File): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(file.absolutePath, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
    var sample = 1
    while (bounds.outWidth / sample > MAX_IMAGE_DIMENSION ||
        bounds.outHeight / sample > MAX_IMAGE_DIMENSION
    ) sample *= 2
    val options = BitmapFactory.Options().apply { inSampleSize = sample }
    return BitmapFactory.decodeFile(file.absolutePath, options)
}

private fun renderPdfFirstPage(file: File): Pair<Bitmap?, Int> {
    val descriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    descriptor.use { pfd ->
        PdfRenderer(pfd).use { renderer ->
            if (renderer.pageCount <= 0) return null to 0
            renderer.openPage(0).use { page ->
                val scale = minOf(
                    1f,
                    MAX_IMAGE_DIMENSION.toFloat() / page.width,
                    MAX_IMAGE_DIMENSION.toFloat() / page.height
                )
                val width = (page.width * scale).toInt().coerceAtLeast(1)
                val height = (page.height * scale).toInt().coerceAtLeast(1)
                val image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                page.render(image, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                return image to renderer.pageCount
            }
        }
    }
}

private const val MAX_TEXT_PREVIEW_BYTES = 512 * 1024
private const val MAX_IMAGE_DIMENSION = 1600
