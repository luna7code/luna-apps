package com.luna.secretnotes.security.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.luna.secretnotes.security.CredentialVault
import com.luna.secretnotes.security.InvalidCredentialException
import com.luna.secretnotes.security.TooManyAttemptsException
import com.luna.secretnotes.security.VaultKeyMaterial
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun CredentialGate(vault: CredentialVault) {
    var credentialExists by remember(vault) { mutableStateOf(vault.hasCredential()) }
    var secret by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    var session by remember { mutableStateOf<VaultKeyMaterial?>(null) }
    var busy by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    DisposableEffect(session) {
        onDispose { session?.close() }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "SECRET NOTES",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (session == null) "PRIVATE BY DESIGN" else "VAULT UNLOCKED",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(Modifier.height(28.dp))

            if (session == null) {
                Text(
                    text = if (credentialExists) "Enter your PIN or password" else "Create your vault credential",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = secret,
                    onValueChange = {
                        secret = it
                        errorMessage = null
                    },
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
                        onValueChange = {
                            confirmation = it
                            errorMessage = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !busy,
                        singleLine = true,
                        label = { Text("Confirm PIN / password") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "PIN: 8+ digits. Password: 10+ letters and digits.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (errorMessage != null) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = errorMessage.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
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
                                } catch (locked: TooManyAttemptsException) {
                                    val seconds = (locked.retryAfterMs + 999L) / 1_000L
                                    errorMessage = "Too many attempts. Try again in $seconds seconds."
                                } catch (_: InvalidCredentialException) {
                                    errorMessage = "Incorrect PIN or password."
                                } catch (invalid: IllegalArgumentException) {
                                    errorMessage = invalid.message ?: "Credential does not meet the security policy."
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
                    if (busy) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(if (credentialExists) "Unlock vault" else "Create secure vault")
                    }
                }
            } else {
                Text(
                    text = "Your credential unlocked the local key vault.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Encrypted notes storage is the next implementation stage.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        session?.close()
                        session = null
                    }
                ) {
                    Text("Lock vault")
                }
            }
        }
    }
}
