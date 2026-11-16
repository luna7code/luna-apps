package com.luna.secretnotes.security.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.luna.secretnotes.data.EncryptedNotesDatabase
import com.luna.secretnotes.data.VaultBackupRestoreCoordinator
import com.luna.secretnotes.security.*
import com.luna.secretnotes.ui.notes.NotesHomeScreen
import com.luna.secretnotes.ui.theme.CosmicTheme
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun CredentialGate(vault: CredentialVault) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var credentialExists by remember(vault) { mutableStateOf(vault.hasCredential()) }
    var secret by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    var session by remember { mutableStateOf<VaultKeyMaterial?>(null) }
    var encryptedDatabase by remember { mutableStateOf<EncryptedNotesDatabase?>(null) }
    var databaseBusy by remember { mutableStateOf(false) }
    var databaseError by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var lastActivity by remember { mutableStateOf(SystemClock.elapsedRealtime()) }
    var backgroundStartedAt by remember { mutableStateOf<Long?>(null) }
    val scope = rememberCoroutineScope()

    fun touch() {
        lastActivity = SystemClock.elapsedRealtime()
    }

    fun lockVault() {
        encryptedDatabase?.close()
        encryptedDatabase = null
        session?.close()
        session = null
        secret = ""
        confirmation = ""
        touch()
    }

    val currentLockAction by rememberUpdatedState(newValue = { lockVault() })
    val latestLastActivity by rememberUpdatedState(lastActivity)

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    backgroundStartedAt = SystemClock.elapsedRealtime()
                }
                Lifecycle.Event.ON_START -> {
                    val inactiveFor = SystemClock.elapsedRealtime() - latestLastActivity
                    if (inactiveFor >= AUTO_LOCK_MS) {
                        currentLockAction()
                    }
                    backgroundStartedAt = null
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    DisposableEffect(context) {
        val screenReceiver = object : BroadcastReceiver() {
            override fun onReceive(receiverContext: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_SCREEN_OFF) currentLockAction()
            }
        }
        ContextCompat.registerReceiver(
            context,
            screenReceiver,
            IntentFilter(Intent.ACTION_SCREEN_OFF),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        onDispose { context.unregisterReceiver(screenReceiver) }
    }

    DisposableEffect(session) {
        val active = session
        onDispose { active?.close() }
    }
    DisposableEffect(encryptedDatabase) {
        val active = encryptedDatabase
        onDispose { active?.close() }
    }

    LaunchedEffect(session) {
        val active = session ?: return@LaunchedEffect
        databaseBusy = true
        databaseError = false
        try {
            encryptedDatabase = withContext(Dispatchers.IO) {
                EncryptedNotesDatabase(context) { active.copyForUse() }.open()
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            databaseError = true
        } finally {
            databaseBusy = false
        }
    }

    LaunchedEffect(session, lastActivity) {
        if (session != null) {
            delay(AUTO_LOCK_MS)
            if (SystemClock.elapsedRealtime() - lastActivity >= AUTO_LOCK_MS) {
                lockVault()
            }
        }
    }

    LaunchedEffect(session, backgroundStartedAt, lastActivity) {
        if (session != null && backgroundStartedAt != null) {
            val inactiveFor = SystemClock.elapsedRealtime() - lastActivity
            val remaining = (AUTO_LOCK_MS - inactiveFor).coerceAtLeast(0L)
            delay(remaining)
            if (backgroundStartedAt != null &&
                SystemClock.elapsedRealtime() - lastActivity >= AUTO_LOCK_MS
            ) {
                lockVault()
            }
        }
    }

    Surface(Modifier.fillMaxSize(), color = CosmicTheme.Background) {
        Column(
            Modifier.fillMaxSize()
                .background(CosmicTheme.Background)
                .pointerInput(session) {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        touch()
                    }
                }
                .drawBehind {
                    val stars = listOf(
                        0.08f to 0.12f, 0.21f to 0.08f, 0.39f to 0.16f, 0.73f to 0.11f,
                        0.91f to 0.19f, 0.14f to 0.34f, 0.84f to 0.37f, 0.06f to 0.61f,
                        0.94f to 0.67f, 0.24f to 0.82f, 0.68f to 0.87f, 0.51f to 0.06f
                    )
                    stars.forEachIndexed { index, point ->
                        drawCircle(
                            CosmicTheme.Starlight.copy(alpha = if (index % 4 == 0) 0.38f else 0.16f),
                            radius = if (index % 4 == 0) 1.5.dp.toPx() else 0.8.dp.toPx(),
                            center = Offset(size.width * point.first, size.height * point.second)
                        )
                    }
                }
        ) {
            if (session != null && encryptedDatabase != null) {
                NotesHomeScreen(
                    database = encryptedDatabase!!,
                    vault = vault,
                    onLock = { lockVault() },
                    onActivity = { touch() },
                    onRestoreBackup = { restored, currentPassword ->
                        val existingSession = session ?: error("Vault is locked")
                        encryptedDatabase?.close()
                        encryptedDatabase = null
                        try {
                            val newSession = withContext(Dispatchers.IO) {
                                VaultBackupRestoreCoordinator(context, vault).install(restored, currentPassword)
                            }
                            existingSession.close()
                            session = newSession
                            touch()
                        } catch (failure: Exception) {
                            encryptedDatabase = withContext(Dispatchers.IO) {
                                EncryptedNotesDatabase(context) { existingSession.copyForUse() }.open()
                            }
                            throw failure
                        }
                    }
                )
            } else if (session != null) {
                Column(
                    Modifier.fillMaxSize().padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("SECRET NOTES", style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(12.dp))
                    if (databaseBusy) {
                        CircularProgressIndicator(color = CosmicTheme.PulsarBlue)
                        Spacer(Modifier.height(12.dp))
                        Text("Opening encrypted space…", color = CosmicTheme.TextSecondary)
                    } else {
                        Text(
                            if (databaseError) "Encrypted database could not be opened."
                            else "Preparing encrypted storage…",
                            color = if (databaseError) MaterialTheme.colorScheme.error else CosmicTheme.TextSecondary
                        )
                        Spacer(Modifier.height(16.dp))
                        TextButton(onClick = { lockVault() }) { Text("Lock vault") }
                    }
                }
            } else {
                Column(
                    Modifier.fillMaxSize().padding(horizontal = 28.dp, vertical = 36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "SECRET NOTES",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "PRIVATE UNIVERSE",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(Modifier.height(28.dp))
                    Text(
                        if (credentialExists) "Enter your PIN or password" else "Create your vault credential",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = secret,
                        onValueChange = { secret = it; errorMessage = null; touch() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !busy,
                        singleLine = true,
                        label = { Text(if (credentialExists) "PIN / password" else "New PIN / password") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )
                    if (!credentialExists) {
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = confirmation,
                            onValueChange = { confirmation = it; errorMessage = null; touch() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !busy,
                            singleLine = true,
                            label = { Text("Confirm PIN / password") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "PIN: 8+ digits. Password: 10+ letters and digits.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    errorMessage?.let {
                        Spacer(Modifier.height(12.dp))
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                    Spacer(Modifier.height(20.dp))
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !busy && secret.isNotEmpty() &&
                            (credentialExists || confirmation.isNotEmpty()),
                        onClick = {
                            val isUnlock = credentialExists
                            if (!isUnlock && secret != confirmation) {
                                errorMessage = "PIN / password confirmation does not match."
                            } else {
                                val supplied = secret.toCharArray()
                                secret = ""
                                confirmation = ""
                                errorMessage = null
                                busy = true
                                scope.launch {
                                    try {
                                        val opened = withContext(Dispatchers.IO) {
                                            if (isUnlock) vault.unlock(supplied)
                                            else vault.createCredential(supplied)
                                        }
                                        session = opened
                                        credentialExists = true
                                        touch()
                                    } catch (_: TooManyAttemptsException) {
                                        errorMessage = "Too many attempts. Wait before trying again."
                                    } catch (_: InvalidCredentialException) {
                                        errorMessage = "Incorrect PIN or password."
                                    } catch (invalid: IllegalArgumentException) {
                                        errorMessage = invalid.message ?: "Credential does not meet policy."
                                    } catch (_: SecurityException) {
                                        errorMessage = "Vault security state could not be verified. Access is blocked."
                                    } catch (cancelled: CancellationException) {
                                        throw cancelled
                                    } catch (_: Exception) {
                                        errorMessage = "The secure vault could not be opened."
                                    } finally {
                                        supplied.fill('\u0000')
                                        busy = false
                                    }
                                }
                            }
                        }
                    ) {
                        if (busy) CircularProgressIndicator(Modifier.height(18.dp), strokeWidth = 2.dp)
                        else Text(if (credentialExists) "Unlock vault" else "Create secure vault")
                    }
                }
            }
        }
    }
}

private const val AUTO_LOCK_MS = 30_000L
