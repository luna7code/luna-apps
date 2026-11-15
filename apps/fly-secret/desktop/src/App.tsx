export function App() {
  return (
    <main style={{ minHeight: "100vh", background: "#171113", color: "#f5f0f0", fontFamily: "system-ui, sans-serif", padding: "8vh 8vw" }}>
      <div style={{ color: "#bd7889", fontSize: 11, letterSpacing: ".2em", fontWeight: 700 }}>FLY SECRET · DESKTOP</div>
      <h1 style={{ fontSize: "clamp(42px, 6vw, 76px)", lineHeight: 1.02, letterSpacing: "-.05em", fontWeight: 550, marginTop: 28 }}>Your conversations.<br /><span style={{ color: "#bd7889" }}>Your control.</span></h1>
      <p style={{ maxWidth: 460, color: "#a99da1", lineHeight: 1.8 }}>Desktop application shell for Windows and macOS. The native Tauri host and hardened capability model are added in the desktop integration phase.</p>
      <section style={{ marginTop: 56, maxWidth: 560, padding: 28, background: "#21191c", border: "1px solid #382a30", borderRadius: 14 }}>
        <strong>Foundation build</strong>
        <p style={{ color: "#a99da1", lineHeight: 1.7 }}>The frontend shell is scaffolded. Authentication, messaging, transport, and cryptographic features are not implemented in this build.</p>
      </section>
    </main>
  );
}