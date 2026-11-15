const navigation = ["Chats", "Calls", "Contacts", "Settings"] as const;

export function App() {
  return (
    <main className="shell">
      <aside className="sidebar" aria-label="Main navigation">
        <a className="wordmark" href="/" aria-label="FLY SECRET home">FLY<span>SECRET</span></a>
        <nav>
          {navigation.map((item, index) => (
            <button className={index === 0 ? "nav-item active" : "nav-item"} key={item} type="button">
              <span className="nav-marker" aria-hidden="true" />
              {item}
            </button>
          ))}
        </nav>
        <div className="sidebar-footer">PRIVATE BY DESIGN</div>
      </aside>
      <section className="workspace">
        <header className="topbar">
          <span className="eyebrow">PERSONAL SPACE</span>
          <span className="status"><i /> Local preview</span>
        </header>
        <div className="content">
          <p className="eyebrow">FLY SECRET / MESSAGING</p>
          <h1>Your conversations.<br /><em>Your control.</em></h1>
          <p className="intro">A private communication workspace. The application shell is ready for the next product modules.</p>
          <div className="empty-state">
            <div className="empty-icon" aria-hidden="true">＋</div>
            <h2>Your space starts here</h2>
            <p>Chats and account features will appear here as their approved modules are implemented.</p>
            <span className="state-label">FOUNDATION BUILD · 0.1.0</span>
          </div>
        </div>
      </section>
    </main>
  );
}