export type LocalMessage = {
  id: string;
  text: string;
  createdAt: number;
  delivery: "local-only";
};

export type Contact = {
  id: string;
  name: string;
  flyId: string;
};

export type Conversation = {
  id: string;
  contactId: string;
  messages: LocalMessage[];
};

export type ChatState = {
  contacts: Contact[];
  conversations: Conversation[];
};

export function createInitialState(): ChatState;
export function addContact(
  state: ChatState,
  name: string,
  flyId: string,
  id?: string,
):
  | { ok: true; state: ChatState; contact: Contact }
  | { ok: false; error: string };
export function startConversation(
  state: ChatState,
  contactId: string,
): { state: ChatState; conversation: Conversation };
export function appendLocalMessage(
  state: ChatState,
  conversationId: string,
  text: string,
  id?: string,
  createdAt?: number,
): ChatState;
