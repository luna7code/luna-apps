function createInitialState() {
  return { contacts: [], conversations: [] };
}

function addContact(state, rawName, rawFlyId, id) {
  const name = String(rawName || "").trim();
  const flyId = String(rawFlyId || "").trim();
  if (!name || !flyId) return { ok: false, error: "Укажите имя и FLY ID." };
  const duplicate = state.contacts.some(
    (contact) => contact.flyId.toLocaleLowerCase() === flyId.toLocaleLowerCase(),
  );
  if (duplicate) return { ok: false, error: "Этот FLY ID уже есть в контактах." };
  const contact = { id: id || "contact-" + Date.now(), name, flyId };
  return { ok: true, state: { ...state, contacts: [...state.contacts, contact] }, contact };
}

function startConversation(state, contactId) {
  const existing = state.conversations.find((item) => item.contactId === contactId);
  if (existing) return { state, conversation: existing };
  const conversation = {
    id: "conversation-" + contactId,
    contactId,
    messages: [],
  };
  return {
    state: { ...state, conversations: [...state.conversations, conversation] },
    conversation,
  };
}

function appendLocalMessage(state, conversationId, rawText, id, createdAt) {
  const text = String(rawText || "").trim();
  if (!text) return state;
  return {
    ...state,
    conversations: state.conversations.map((conversation) =>
      conversation.id !== conversationId
        ? conversation
        : {
            ...conversation,
            messages: [
              ...conversation.messages,
              {
                id: id || "local-" + Date.now() + "-" + conversation.messages.length,
                text,
                createdAt: createdAt || Date.now(),
                delivery: "local-only",
              },
            ],
          },
    ),
  };
}

module.exports = {
  createInitialState,
  addContact,
  startConversation,
  appendLocalMessage,
};
