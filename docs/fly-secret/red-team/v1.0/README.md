# FLY SECRET — Red-Team Test Specification v1.0

This directory stores the security test planning documents for FLY SECRET.

## Documents
- FLY_RED_TEAM_ATTACK_CATALOG.md
- FLY_RED_TEAM_TEST_CASES.md
- FLY_PRIVACY_ATTACK_MODEL.md
- FLY_IDENTITY_ATTACK_MODEL.md
- FLY_RECOVERY_SECURITY_MODEL.md
- FLY_DECENTRALIZATION_THREAT_MODEL.md
- FLY_SECURITY_RELEASE_GATES.md

## Scope constraints
- TOR MODE is excluded from product scope, architecture, UI, configuration and test plans.
- Decentralization is a future architectural direction only; no decentralized implementation is implied.
- Every test case starts with status `NOT_RUN`.
- No test execution is claimed by this documentation commit.
- Documentation only; application implementation is not part of this change.

## Safe execution policy
Run only in isolated lab/staging environments owned by the project, using synthetic accounts, keys, devices and data. Never target production or third-party systems. Redact secrets from evidence.
