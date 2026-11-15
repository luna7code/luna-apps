# FLY SECRET Desktop shell

Desktop target: Windows and macOS, using React in the frontend and Tauri 2 / Rust as the native host.

This directory is intentionally a frontend shell boundary. The Tauri host, capability manifest, CSP, and platform packaging are added in the dedicated desktop integration task after the workspace toolchain is reproducibly installable. The host must remain deny-by-default; no broad filesystem or shell permissions are permitted.