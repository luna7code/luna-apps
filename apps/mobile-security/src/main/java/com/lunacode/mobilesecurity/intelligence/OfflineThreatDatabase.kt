package com.lunacode.mobilesecurity.intelligence

import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import java.util.Base64

data class ThreatIndicator(
    val id: String,
    val packageName: String,
    val apkSha256: String?,
    val signerSha256: String?,
    val severity: String,
    val confidence: Int,
    val title: String,
    val detail: String
)

data class VerifiedThreatDatabase(
    val version: Long,
    val issuedAtEpochMillis: Long,
    val keyId: String,
    val indicators: List<ThreatIndicator>
) {
    val byPackageName: Map<String, List<ThreatIndicator>> by lazy {
        indicators.groupBy { it.packageName }
    }
}

/**
 * Strict, signed, offline threat-feed format.
 *
 * File format (UTF-8, LF line endings):
 * PROSEC-THREAT-DB<TAB>1
 * version<TAB>positive integer
 * issued_at<TAB>epoch milliseconds
 * key_id<TAB>trusted key id
 * indicator<TAB>id<TAB>package<TAB>apk_sha256 or -<TAB>signer_sha256 or -
 *           <TAB>HIGH|MEDIUM|LOW<TAB>confidence 0..100<TAB>base64url(title)<TAB>base64url(detail)
 * signature<TAB>SHA256withRSA<TAB>base64(signature)
 *
 * The signature covers every byte from the first header byte through the last
 * indicator field, excluding the final signature line. No canonicalization is
 * attempted: the exact signed bytes are verified.
 */
object OfflineThreatDatabaseVerifier {
    private const val HEADER = "PROSEC-THREAT-DB\t1"
    private const val MAX_BYTES = 4 * 1024 * 1024
    private const val MAX_ENTRIES = 20_000
    private val packagePattern = Regex("^[A-Za-z][A-Za-z0-9_]*(?:\\.[A-Za-z][A-Za-z0-9_]*)+$")
    private val hashPattern = Regex("^[A-Fa-f0-9]{64}$")
    private val idPattern = Regex("^[A-Za-z0-9._-]{1,80}$")

    fun verify(
        text: String,
        trustedPublicKeys: Map<String, String>,
        minimumVersionExclusive: Long = 0L
    ): VerifiedThreatDatabase {
        require(text.toByteArray(StandardCharsets.UTF_8).size <= MAX_BYTES) { "Файл базы индикаторов превышает 4 МБ." }
        require(!text.contains('\u0000')) { "В файле обнаружен нулевой байт." }
        require(!text.contains('\r')) { "Формат принимает только UTF-8 с LF-переносами строк." }
        val normalized = text.removeSuffix("\n")
        val lines = normalized.split('\n')
        require(lines.size >= 5) { "Файл базы индикаторов неполный." }
        require(lines.first() == HEADER) { "Неизвестный формат базы индикаторов." }
        val signatureFields = lines.last().split('\t')
        require(signatureFields.size == 3 && signatureFields[0] == "signature" &&
            signatureFields[1] == "SHA256withRSA"
        ) { "Подпись отсутствует или использует неподдерживаемый алгоритм." }

        fun field(lineIndex: Int, name: String): String {
            val parts = lines[lineIndex].split('\t')
            require(parts.size == 2 && parts[0] == name) { "Некорректное поле $name." }
            return parts[1]
        }
        val version = field(1, "version").toLongOrNull()
            ?: throw IllegalArgumentException("Версия базы не является числом.")
        val issuedAt = field(2, "issued_at").toLongOrNull()
            ?: throw IllegalArgumentException("Время выпуска базы не является числом.")
        val keyId = field(3, "key_id")
        require(version > minimumVersionExclusive) { "Версия базы устарела или уже импортирована." }
        require(issuedAt > 0L) { "Некорректное время выпуска базы." }
        require(idPattern.matches(keyId)) { "Некорректный идентификатор ключа подписи." }

        val encodedKey = trustedPublicKeys[keyId]
            ?: throw IllegalArgumentException("Ключ подписи '$keyId' не входит в список доверенных издателей.")
        val publicKeyBytes = Base64.getDecoder().decode(encodedKey)
        val publicKey = KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(publicKeyBytes))
        val signedPayload = lines.dropLast(1).joinToString("\n")
        val verifier = Signature.getInstance("SHA256withRSA")
        verifier.initVerify(publicKey)
        verifier.update(signedPayload.toByteArray(StandardCharsets.UTF_8))
        val signatureBytes = Base64.getDecoder().decode(signatureFields[2])
        require(verifier.verify(signatureBytes)) { "Криптографическая подпись базы недействительна." }

        val indicators = ArrayList<ThreatIndicator>()
        val ids = HashSet<String>()
        for (index in 4 until lines.lastIndex) {
            val parts = lines[index].split('\t')
            require(parts.size == 9 && parts[0] == "indicator") { "Некорректная запись индикатора в строке ${index + 1}." }
            require(indicators.size < MAX_ENTRIES) { "Слишком много индикаторов в одной базе." }
            val id = parts[1]
            val packageName = parts[2]
            val apkHash = parts[3].takeUnless { it == "-" }
            val signerHash = parts[4].takeUnless { it == "-" }
            val severity = parts[5]
            val confidence = parts[6].toIntOrNull()
                ?: throw IllegalArgumentException("Некорректная оценка confidence у индикатора $id.")
            require(idPattern.matches(id) && ids.add(id)) { "Повторяющийся или некорректный идентификатор индикатора $id." }
            require(packagePattern.matches(packageName)) { "Некорректное имя пакета у индикатора $id." }
            require(apkHash == null || hashPattern.matches(apkHash)) { "Некорректный SHA-256 APK у индикатора $id." }
            require(signerHash == null || hashPattern.matches(signerHash)) { "Некорректный SHA-256 сертификата у индикатора $id." }
            require(apkHash != null || signerHash != null || packageName.isNotBlank()) { "Индикатор $id не содержит признаков сопоставления." }
            require(severity in setOf("HIGH", "MEDIUM", "LOW")) { "Некорректный уровень индикатора $id." }
            require(confidence in 0..100) { "Confidence индикатора $id должен быть от 0 до 100." }
            val title = decodeText(parts[7], "название", id)
            val detail = decodeText(parts[8], "описание", id)
            require(title.isNotBlank() && detail.isNotBlank()) { "Пустое название или описание индикатора $id." }
            indicators += ThreatIndicator(id, packageName, apkHash, signerHash, severity, confidence, title, detail)
        }
        require(indicators.isNotEmpty()) { "Подписанная база не содержит индикаторов." }
        return VerifiedThreatDatabase(version, issuedAt, keyId, indicators)
    }

    private fun decodeText(value: String, field: String, id: String): String {
        val bytes = try { Base64.getUrlDecoder().decode(value) }
        catch (_: IllegalArgumentException) { throw IllegalArgumentException("Некорректное Base64-поле $field у индикатора $id.") }
        require(bytes.size <= 8_192) { "Поле $field у индикатора $id слишком длинное." }
        return String(bytes, StandardCharsets.UTF_8)
    }
}

/**
 * Trust roots are deliberately provisioned as public keys only. Keep the matching
 * private signing key offline and never place it in this repository or the APK.
 * The empty default registry fails closed until the product owner provisions a key.
 */
object TrustedThreatKeys {
    val publicKeysBase64: Map<String, String> = emptyMap()
}
