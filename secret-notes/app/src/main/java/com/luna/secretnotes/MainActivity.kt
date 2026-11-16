package com.luna.secretnotes

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.luna.secretnotes.security.CredentialVault
import com.luna.secretnotes.security.ui.CredentialGate
import com.luna.secretnotes.ui.theme.SecretNotesTheme
import java.io.File

class MainActivity : ComponentActivity() {
    private val credentialVault by lazy { CredentialVault(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        cleanupExpiredExternalExports()
        setContent {
            SecretNotesTheme {
                CredentialGate(credentialVault)
            }
        }
    }

    /**
     * Best-effort cleanup of temporary plaintext copies created only after the
     * external-view warning. Receiving apps may retain their own copies.
     */
    private fun cleanupExpiredExternalExports() {
        val directory = File(cacheDir, EXTERNAL_EXPORT_DIRECTORY)
        val cutoff = System.currentTimeMillis() - EXTERNAL_EXPORT_TTL_MS
        directory.listFiles().orEmpty().forEach { file ->
            if (file.lastModified() < cutoff) {
                if (file.isDirectory) file.deleteRecursively() else file.delete()
            }
        }
    }

    private companion object {
        const val EXTERNAL_EXPORT_DIRECTORY = "secret_notes_external"
        const val EXTERNAL_EXPORT_TTL_MS = 24L * 60L * 60L * 1_000L
    }
}
