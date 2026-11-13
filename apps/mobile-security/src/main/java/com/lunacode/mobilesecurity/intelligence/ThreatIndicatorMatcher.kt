package com.lunacode.mobilesecurity.intelligence

/**
 * Matches a package against a verified offline feed without unnecessary APK I/O.
 * Package and signer constraints are checked first. The digest provider is called
 * at most once and only when a surviving indicator requires an APK SHA-256 match.
 */
object ThreatIndicatorMatcher {
    fun match(
        packageName: String,
        signerDigests: Set<String>,
        indicators: List<ThreatIndicator>,
        apkSha256Provider: () -> String?
    ): List<ThreatIndicator> {
        val packageMatches = indicators.filter { it.packageName == packageName }
        if (packageMatches.isEmpty()) return emptyList()

        val signerMatches = packageMatches.filter { indicator ->
            indicator.signerSha256 == null || signerDigests.any {
                it.equals(indicator.signerSha256, ignoreCase = true)
            }
        }
        if (signerMatches.isEmpty()) return emptyList()

        val apkDigest = if (signerMatches.any { it.apkSha256 != null }) {
            runCatching(apkSha256Provider).getOrNull()
        } else {
            null
        }
        return signerMatches.filter { indicator ->
            indicator.apkSha256 == null ||
                apkDigest?.equals(indicator.apkSha256, ignoreCase = true) == true
        }
    }
}
