const test = require("node:test");
const assert = require("node:assert/strict");
const {
  createInitialState,
  addContact,
  startConversation,
  appendLocalMessage,
} = require("../src/domain/chatState");

test("starts with no fabricated contacts or conversations", () => {
  assert.deepEqual(createInitialState(), { contacts: [], conversations: [] });
});

test("adds a contact after trimming input", () => {
  const result = addContact(createInitialState(), " Alice ", " alice#123 ", "c1");
  assert.equal(result.ok, true);
  assert.equal(result.contact.name, "Alice");
  assert.equal(result.contact.flyId, "alice#123");
});

test("rejects blank and duplicate FLY IDs case-insensitively", () => {
  const initial = createInitialState();
  assert.equal(addContact(initial, " ", "id").ok, false);
  const first = addContact(initial, "Alice", "Alice#123", "c1");
  assert.equal(addContact(first.state, "Alice 2", "alice#123", "c2").ok, false);
});

test("creates one conversation per contact", () => {
  const contact = addContact(createInitialState(), "Alice", "alice#123", "c1");
  const first = startConversation(contact.state, "c1");
  const second = startConversation(first.state, "c1");
  assert.equal(first.conversation.id, second.conversation.id);
  assert.equal(second.state.conversations.length, 1);
});

test("stores only non-empty local-only message records", () => {
  const contact = addContact(createInitialState(), "Alice", "alice#123", "c1");
  const conversation = startConversation(contact.state, "c1");
  const unchanged = appendLocalMessage(conversation.state, conversation.conversation.id, "   ");
  assert.equal(unchanged.conversations[0].messages.length, 0);
  const next = appendLocalMessage(conversation.state, conversation.conversation.id, " Hello ", "m1", 123);
  assert.deepEqual(next.conversations[0].messages[0], {
    id: "m1",
    text: "Hello",
    createdAt: 123,
    delivery: "local-only",
  });
});
