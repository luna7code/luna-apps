package com.luna.secretnotes

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.luna.secretnotes.security.CredentialVault
import com.luna.secretnotes.security.ui.CredentialGate
import com.luna.secretnotes.ui.theme.SecretNotesTheme

class MainActivity : ComponentActivity() {
    private val credentialVault by lazy { CredentialVault(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        setContent {
            SecretNotesTheme {
                CredentialGate(credentialVault)
            }
        }
    }
}
