package com.lunacode.mobilesecurity.scanner

data class AuditFinding(
    val id: String,
    val title: String,
    val detail: String,
    val severity: String,
    val source: String
)
