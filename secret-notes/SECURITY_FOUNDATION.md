# SECRET NOTES — Security Foundation

Status: implementation foundation added; device validation and full storage integration remain open.

## Credential policy

- Unlock method: numeric PIN or alphanumeric password; no biometrics.
- Default enrollment minimums: 8 decimal digits for numeric PIN; 10 letters/digits for alphanumeric password. These are implementation defaults and can be revised before release.
- Credentials are accepted as `CharArray` and cleared best-effort after operations. JVM memory zeroization cannot guarantee removal of all copies.
- No recovery path: losing the credential means losing access to the vault.

## Key hierarchy

1. Generate a random 256-bit Data Encryption Key (DEK).
2. Generate a per-vault random 128-bit salt.
3. Derive 256 bits from the credential using Argon2id v1.3, 64 MiB memory, 3 passes, 1 lane.
4. Pass the derived bytes through a non-exportable Android Keystore HMAC-SHA256 key. This device-binding operation produces the local wrapping key.
5. Wrap the DEK with AES-256-GCM, a fresh 96-bit nonce, and a 128-bit authentication tag.
6. Persist only KDF metadata, salt, nonce, and wrapped DEK. Never persist the PIN/password or plaintext DEK.

The device-bound Keystore factor intentionally means this local vault record is not portable by copying app files. The separately approved manual export/import flow must derive an independent portable archive key from the app PIN using Argon2id; it must not depend on this device Keystore alias.

## Attempt throttling

- Five consecutive incorrect credentials are allowed before the first delay.
- The fifth failure causes a 30-second delay.
- Subsequent failures increase the delay exponentially, capped at 15 minutes.
- A successful unlock resets the counter.
- The local attempt counter is authenticated with a separate Android Keystore HMAC key. Missing or modified state while the key remains present fails closed.
- This is an application-layer retry control, not a guarantee against offline attacks or a rooted device. Clearing app data/uninstalling may destroy local state depending on Android/Keystore lifecycle; do not claim it is tamper-proof against an owner with full device control.

## Storage and cryptography

- DEK wrapping uses AES/GCM/NoPadding.
- Nonces are freshly generated for every wrapping operation.
- Authenticated additional data binds the wrapped key to the current record format.
- KDF parameters are recorded and checked against the supported version before unwrapping.
- Credential operations must run off the main thread.
- The key returned by `VaultKeyMaterial` is an in-memory session key. Callers must close the object and clear any copies as soon as possible.

## Remaining security work

- Integrate the DEK with encrypted database and attachment storage.
- Add Android instrumentation tests for Keystore operations, invalidation, process death, and real-device lockout persistence.
- Verify actual Keystore security level (TEE/StrongBox where available) and report device capability; do not assume hardware backing.
- Add cryptographic test vectors and independent security review.
- Implement portable encrypted backup using a separate PIN-derived archive key and tamper tests.
- Verify merged manifest and APK contain no INTERNET permission or network stack dependencies.
