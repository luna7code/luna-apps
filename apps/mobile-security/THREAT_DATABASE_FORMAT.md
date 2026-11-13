# PRO SEC — signed offline threat database

## Trust model

PRO SEC never downloads threat intelligence and never accepts an unsigned feed. The matching RSA private key must remain offline with the publisher. The application stores only the publisher's RSA public key.

Before pinning a publisher key in the app, compare its displayed SHA-256 fingerprint with the fingerprint published through a separate trusted channel. Do not trust a public key merely because it arrived next to a database file.

The current implementation supports RSA public keys encoded as X.509 SubjectPublicKeyInfo PEM and requires RSA 2048 bits or stronger. Signatures use SHA256withRSA.

## File framing

The input is UTF-8 text with LF line endings and no trailing newline before the signature record.

```text
PROSEC-THREAT-DB<TAB>1
version<TAB>positive-integer
issued_at<TAB>epoch-milliseconds
key_id<TAB>publisher-key-id
indicator<TAB>id<TAB>package.name<TAB>apk-sha256-or--<TAB>signer-sha256-or--<TAB>HIGH|MEDIUM|LOW<TAB>confidence-0-to-100<TAB>base64url-title<TAB>base64url-detail
signature<TAB>SHA256withRSA<TAB>standard-base64-signature
```

- The header is exactly `PROSEC-THREAT-DB<TAB>1`.
- Metadata fields have exactly two tab-separated columns.
- Each indicator has exactly nine tab-separated columns.
- Package names use the reverse-domain Android package format.
- SHA-256 values are exactly 64 hexadecimal characters; use `-` to omit a hash.
- Title and detail are UTF-8 text encoded as Base64URL.
- Indicator IDs must be unique inside a feed.
- Severity is HIGH, MEDIUM or LOW. Confidence is an explanatory feed value, not a statistical malware probability.
- The final signature record uses standard Base64, not Base64URL.

## Signature input

The RSA signature covers the exact UTF-8 bytes of every line before the final `signature` record, joined with a single LF between lines. There is no final LF after the last indicator line. The signature record itself is excluded.

Any edit to metadata or indicator content invalidates the signature.

## Limits and rollback policy

- Maximum feed size: 4 MiB.
- Maximum indicators: 20,000.
- The version must be strictly greater than the last imported version.
- The imported signed file is encrypted locally with the PRO SEC AES-GCM key and written atomically.
- A malformed, untrusted, tampered or older feed is rejected and does not replace the active database.

The saved version counter is local application state. This protects normal import flows, but it cannot defeat an attacker who can restore an earlier complete application-data image or compromise the device/Keystore.

## Detection semantics

A feed entry can match a package name and optionally require APK SHA-256 and/or signer SHA-256. PRO SEC computes an APK digest only for package entries that require it. Matching a package name alone is a weak indicator; use signer or APK hashes for stronger exact matches.

A match requires review, not proof that the code executed or that the app is malicious. False positives, legitimate repackaging and signing-key rotation must be assessed in context.

## Private-key handling

Never commit a publisher private key, keystore file, password or signing token to this repository or the APK. Keep the signing key offline and produce signed database files in a controlled publisher process. A public key may be imported and pinned after independent fingerprint verification.
