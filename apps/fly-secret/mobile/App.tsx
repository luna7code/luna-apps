import React, { useMemo, useState } from "react";
import {
  KeyboardAvoidingView,
  Platform,
  Pressable,
  SafeAreaView,
  ScrollView,
  StatusBar,
  StyleSheet,
  Text,
  TextInput,
  View,
} from "react-native";
import {
  addContact,
  appendLocalMessage,
  createInitialState,
  startConversation,
  type ChatState,
  type Contact,
  type Conversation,
} from "./src/domain/chatState";

type Screen = "chats" | "contacts" | "security" | "conversation";

const palette = {
  background: "#100D0F",
  surface: "#1B1518",
  surfaceRaised: "#241B1F",
  border: "#392A31",
  text: "#F7F1F3",
  muted: "#A9979E",
  faint: "#75636A",
  accent: "#C77A91",
  accentDark: "#6F3548",
  warning: "#E5B57A",
};

function ActionButton({
  label,
  onPress,
  secondary = false,
  disabled = false,
}: {
  label: string;
  onPress: () => void;
  secondary?: boolean;
  disabled?: boolean;
}) {
  return (
    <Pressable
      accessibilityRole="button"
      onPress={onPress}
      disabled={disabled}
      style={({ pressed }) => [
        styles.actionButton,
        secondary && styles.secondaryButton,
        disabled && styles.disabledButton,
        pressed && !disabled && styles.pressed,
      ]}
    >
      <Text style={[styles.actionText, secondary && styles.secondaryText]}>{label}</Text>
    </Pressable>
  );
}

function Field({
  label,
  value,
  onChangeText,
  placeholder,
  autoCapitalize = "none",
}: {
  label: string;
  value: string;
  onChangeText: (value: string) => void;
  placeholder: string;
  autoCapitalize?: "none" | "sentences" | "words";
}) {
  return (
    <View style={styles.fieldWrap}>
      <Text style={styles.fieldLabel}>{label}</Text>
      <TextInput
        accessibilityLabel={label}
        value={value}
        onChangeText={onChangeText}
        placeholder={placeholder}
        placeholderTextColor={palette.faint}
        autoCapitalize={autoCapitalize}
        autoCorrect={false}
        selectionColor={palette.accent}
        style={styles.input}
      />
    </View>
  );
}

function Notice({ children }: { children: string }) {
  return (
    <View style={styles.notice}>
      <Text style={styles.noticeMark}>!</Text>
      <Text style={styles.noticeText}>{children}</Text>
    </View>
  );
}

export default function App() {
  const [data, setData] = useState<ChatState>(createInitialState());
  const [screen, setScreen] = useState<Screen>("chats");
  const [activeConversationId, setActiveConversationId] = useState<string | null>(null);
  const [contactName, setContactName] = useState("");
  const [flyId, setFlyId] = useState("");
  const [contactError, setContactError] = useState("");
  const [draft, setDraft] = useState("");

  const activeConversation = useMemo(
    () => data.conversations.find((item) => item.id === activeConversationId) ?? null,
    [data.conversations, activeConversationId],
  );
  const activeContact = useMemo(
    () => data.contacts.find((item) => item.id === activeConversation?.contactId) ?? null,
    [data.contacts, activeConversation],
  );

  const openChat = (conversation: Conversation) => {
    setActiveConversationId(conversation.id);
    setDraft("");
    setScreen("conversation");
  };

  const beginChat = (contact: Contact) => {
    const next = startConversation(data, contact.id);
    setData(next.state);
    openChat(next.conversation);
  };

  const saveContact = () => {
    const result = addContact(data, contactName, flyId);
    if (!result.ok) {
      setContactError(result.error);
      return;
    }
    setData(result.state);
    setContactName("");
    setFlyId("");
    setContactError("");
    setScreen("contacts");
  };

  const addLocalMessage = () => {
    if (!activeConversation || !draft.trim()) return;
    setData((current) => appendLocalMessage(current, activeConversation.id, draft));
    setDraft("");
  };

  const renderChats = () => (
    <>
      <View style={styles.pageHeading}>
        <View>
          <Text style={styles.eyebrow}>ВАШЕ ПРОСТРАНСТВО</Text>
          <Text style={styles.pageTitle}>Сообщения</Text>
        </View>
        <Pressable accessibilityRole="button" onPress={() => setScreen("contacts")} style={styles.roundButton}>
          <Text style={styles.roundButtonText}>＋</Text>
        </Pressable>
      </View>
      <Notice>Демонстрационный режим: переписки пока не передаются по сети и не защищены E2EE. Не вводите личные или секретные данные.</Notice>
      {data.conversations.length === 0 ? (
        <View style={styles.emptyState}>
          <View style={styles.emptyIcon}><Text style={styles.emptyIconText}>✉</Text></View>
          <Text style={styles.emptyTitle}>Пока нет переписок</Text>
          <Text style={styles.emptyCopy}>Добавьте контакт по FLY ID, чтобы открыть локальную тестовую переписку.</Text>
          <ActionButton label="Добавить контакт" onPress={() => setScreen("contacts")} />
        </View>
      ) : (
        <View style={styles.list}>
          {data.conversations.map((conversation) => {
            const contact = data.contacts.find((item) => item.id === conversation.contactId);
            const lastMessage = conversation.messages[conversation.messages.length - 1];
            if (!contact) return null;
            return (
              <Pressable key={conversation.id} onPress={() => openChat(conversation)} style={styles.chatRow}>
                <View style={styles.avatar}><Text style={styles.avatarText}>{contact.name.slice(0, 1).toUpperCase()}</Text></View>
                <View style={styles.rowMain}>
                  <Text style={styles.rowTitle}>{contact.name}</Text>
                  <Text style={styles.rowSubtitle} numberOfLines={1}>{lastMessage?.text ?? "Переписка создана"}</Text>
                </View>
                <Text style={styles.rowChevron}>›</Text>
              </Pressable>
            );
          })}
        </View>
      )}
    </>
  );

  const renderContacts = () => (
    <>
      <View style={styles.pageHeading}>
        <View>
          <Text style={styles.eyebrow}>ЛЮДИ</Text>
          <Text style={styles.pageTitle}>Контакты</Text>
        </View>
      </View>
      <Notice>Контакты сохраняются только в памяти приложения и исчезнут после его закрытия. Проверка подлинности FLY ID ещё не реализована.</Notice>
      <View style={styles.formCard}>
        <Text style={styles.cardTitle}>Новый контакт</Text>
        <Field label="Имя" value={contactName} onChangeText={setContactName} placeholder="Как к нему обращаться" autoCapitalize="words" />
        <Field label="FLY ID" value={flyId} onChangeText={setFlyId} placeholder="Идентификатор собеседника" />
        {contactError ? <Text style={styles.errorText}>{contactError}</Text> : null}
        <ActionButton label="Добавить контакт" onPress={saveContact} disabled={!contactName.trim() || !flyId.trim()} />
      </View>
      {data.contacts.length > 0 ? (
        <View style={styles.list}>
          <Text style={styles.sectionLabel}>ДОБАВЛЕННЫЕ КОНТАКТЫ</Text>
          {data.contacts.map((contact) => (
            <View key={contact.id} style={styles.chatRow}>
              <View style={styles.avatar}><Text style={styles.avatarText}>{contact.name.slice(0, 1).toUpperCase()}</Text></View>
              <View style={styles.rowMain}>
                <Text style={styles.rowTitle}>{contact.name}</Text>
                <Text style={styles.rowSubtitle}>{contact.flyId}</Text>
              </View>
              <Pressable accessibilityRole="button" onPress={() => beginChat(contact)} style={styles.smallAction}>
                <Text style={styles.smallActionText}>Написать</Text>
              </Pressable>
            </View>
          ))}
        </View>
      ) : null}
    </>
  );

  const renderConversation = () => (
    <>
      <View style={styles.conversationHeader}>
        <Pressable accessibilityRole="button" onPress={() => setScreen("chats")} style={styles.backButton}>
          <Text style={styles.backButtonText}>‹</Text>
        </Pressable>
        <View style={styles.conversationIdentity}>
          <Text style={styles.rowTitle}>{activeContact?.name ?? "Переписка"}</Text>
          <Text style={styles.rowSubtitle}>{activeContact?.flyId ?? ""}</Text>
        </View>
        <View style={styles.localBadge}><Text style={styles.localBadgeText}>ЛОКАЛЬНО</Text></View>
      </View>
      <Notice>Это локальная запись на устройстве, а не отправленное сообщение. Шифрование и доставка ещё не подключены.</Notice>
      <ScrollView style={styles.messageList} contentContainerStyle={styles.messageContent} keyboardShouldPersistTaps="handled">
        {!activeConversation?.messages.length ? (
          <View style={styles.emptyConversation}>
            <Text style={styles.emptyTitle}>Начало переписки</Text>
            <Text style={styles.emptyCopy}>Можно проверить интерфейс ввода. Сообщения не покидают это состояние приложения.</Text>
          </View>
        ) : activeConversation.messages.map((message) => (
          <View key={message.id} style={styles.messageBubble}>
            <Text style={styles.messageText}>{message.text}</Text>
            <Text style={styles.messageMeta}>ЛОКАЛЬНАЯ ЗАПИСЬ · {new Date(message.createdAt).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })}</Text>
          </View>
        ))}
      </ScrollView>
      <View style={styles.composer}>
        <TextInput
          accessibilityLabel="Текст локального сообщения"
          value={draft}
          onChangeText={setDraft}
          placeholder="Введите тестовый текст"
          placeholderTextColor={palette.faint}
          multiline
          style={styles.composerInput}
        />
        <Pressable accessibilityRole="button" onPress={addLocalMessage} disabled={!draft.trim()} style={[styles.sendButton, !draft.trim() && styles.disabledButton]}>
          <Text style={styles.sendButtonText}>↑</Text>
        </Pressable>
      </View>
    </>
  );

  const renderSecurity = () => (
    <>
      <View style={styles.pageHeading}>
        <View>
          <Text style={styles.eyebrow}>КОНТРОЛЬ И ЗАЩИТА</Text>
          <Text style={styles.pageTitle}>Безопасность</Text>
        </View>
      </View>
      <View style={styles.securityCard}>
        <Text style={styles.securityLabel}>СТАТУС ПРОТОКОЛА</Text>
        <Text style={styles.securityValue}>Не активирован</Text>
        <Text style={styles.securityCopy}>В этой сборке нет криптографического протокола, управления ключами или сетевой доставки. Интерфейс не заявляет о наличии E2EE.</Text>
      </View>
      {[
        ["FLY ID", "Создание и подтверждение ещё не подключены"],
        ["Проверка ключей", "Ожидает утверждённого криптографического стека"],
        ["Несколько устройств", "Не реализовано"],
        ["Recovery Kit", "Не реализован"],
        ["Сетевой транспорт", "Не подключён"],
      ].map(([title, detail]) => (
        <View key={title} style={styles.statusRow}>
          <View style={styles.statusDot} />
          <View style={styles.rowMain}>
            <Text style={styles.rowTitle}>{title}</Text>
            <Text style={styles.rowSubtitle}>{detail}</Text>
          </View>
          <Text style={styles.pending}>ОЖИДАЕТ</Text>
        </View>
      ))}
      <Text style={styles.footnote}>TOR MODE отсутствует. Децентрализация не входит в текущий MVP.</Text>
    </>
  );

  return (
    <SafeAreaView style={styles.screen}>
      <StatusBar barStyle="light-content" backgroundColor={palette.background} />
      <View style={styles.header}>
        <View>
          <Text style={styles.brand}>FLY <Text style={styles.brandAccent}>SECRET</Text></Text>
          <Text style={styles.kicker}>PRIVATE COMMUNICATION</Text>
        </View>
        <View style={styles.headerStatus}><View style={styles.headerStatusDot} /><Text style={styles.headerStatusText}>MVP · LOCAL</Text></View>
      </View>
      <KeyboardAvoidingView style={styles.flex} behavior={Platform.OS === "ios" ? "padding" : undefined}>
        <ScrollView style={styles.flex} contentContainerStyle={styles.page} keyboardShouldPersistTaps="handled">
          {screen === "chats" ? renderChats() : null}
          {screen === "contacts" ? renderContacts() : null}
          {screen === "security" ? renderSecurity() : null}
          {screen === "conversation" ? renderConversation() : null}
        </ScrollView>
      </KeyboardAvoidingView>
      {screen !== "conversation" ? (
        <View style={styles.tabBar}>
          {([
            ["chats", "Сообщения", "◫"],
            ["contacts", "Контакты", "♙"],
            ["security", "Защита", "◇"],
          ] as const).map(([key, label, symbol]) => (
            <Pressable key={key} accessibilityRole="tab" accessibilityState={{ selected: screen === key }} onPress={() => setScreen(key)} style={styles.tab}>
              <Text style={[styles.tabSymbol, screen === key && styles.tabActive]}>{symbol}</Text>
              <Text style={[styles.tabText, screen === key && styles.tabActive]}>{label}</Text>
            </Pressable>
          ))}
        </View>
      ) : null}
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  flex: { flex: 1 },
  screen: { flex: 1, backgroundColor: palette.background },
  header: { paddingHorizontal: 22, paddingTop: 12, paddingBottom: 16, borderBottomWidth: 1, borderBottomColor: palette.border, flexDirection: "row", alignItems: "center", justifyContent: "space-between" },
  brand: { color: palette.text, fontSize: 17, fontWeight: "800", letterSpacing: 3 },
  brandAccent: { color: palette.accent },
  kicker: { color: palette.faint, fontSize: 9, letterSpacing: 2, marginTop: 6 },
  headerStatus: { flexDirection: "row", alignItems: "center", gap: 7, borderWidth: 1, borderColor: palette.border, borderRadius: 20, paddingHorizontal: 10, paddingVertical: 7 },
  headerStatusDot: { width: 6, height: 6, borderRadius: 3, backgroundColor: palette.warning },
  headerStatusText: { color: palette.muted, fontSize: 9, letterSpacing: 1 },
  page: { paddingHorizontal: 20, paddingTop: 25, paddingBottom: 28, flexGrow: 1 },
  pageHeading: { flexDirection: "row", justifyContent: "space-between", alignItems: "center", marginBottom: 20 },
  eyebrow: { color: palette.accent, fontSize: 9, letterSpacing: 2, fontWeight: "700" },
  pageTitle: { color: palette.text, fontSize: 32, fontWeight: "600", letterSpacing: -1, marginTop: 7 },
  roundButton: { width: 42, height: 42, borderRadius: 21, backgroundColor: palette.surfaceRaised, alignItems: "center", justifyContent: "center", borderWidth: 1, borderColor: palette.border },
  roundButtonText: { color: palette.accent, fontSize: 27, lineHeight: 30 },
  notice: { flexDirection: "row", gap: 10, padding: 13, borderRadius: 10, borderWidth: 1, borderColor: "#5A4437", backgroundColor: "#211B18", marginBottom: 20 },
  noticeMark: { color: palette.warning, fontWeight: "800", fontSize: 14 },
  noticeText: { flex: 1, color: "#D7C5B7", fontSize: 11, lineHeight: 17 },
  emptyState: { alignItems: "center", paddingHorizontal: 18, paddingTop: 42, paddingBottom: 36 },
  emptyIcon: { width: 64, height: 64, borderRadius: 32, backgroundColor: palette.surfaceRaised, alignItems: "center", justifyContent: "center", marginBottom: 18, borderWidth: 1, borderColor: palette.border },
  emptyIconText: { color: palette.accent, fontSize: 27 },
  emptyTitle: { color: palette.text, fontSize: 18, fontWeight: "600", textAlign: "center" },
  emptyCopy: { color: palette.muted, fontSize: 12, lineHeight: 19, textAlign: "center", marginTop: 9, marginBottom: 20, maxWidth: 290 },
  actionButton: { backgroundColor: palette.accentDark, borderRadius: 9, paddingHorizontal: 18, paddingVertical: 13, alignItems: "center", justifyContent: "center", marginTop: 8 },
  actionText: { color: "#FFF8FA", fontSize: 12, fontWeight: "700", letterSpacing: 0.2 },
  secondaryButton: { backgroundColor: "transparent", borderWidth: 1, borderColor: palette.border },
  secondaryText: { color: palette.text },
  disabledButton: { opacity: 0.35 },
  pressed: { opacity: 0.72 },
  list: { gap: 2 },
  chatRow: { flexDirection: "row", alignItems: "center", paddingVertical: 13, borderBottomWidth: 1, borderBottomColor: palette.border, gap: 12 },
  avatar: { width: 42, height: 42, borderRadius: 21, backgroundColor: palette.accentDark, alignItems: "center", justifyContent: "center" },
  avatarText: { color: palette.text, fontWeight: "700", fontSize: 16 },
  rowMain: { flex: 1 },
  rowTitle: { color: palette.text, fontSize: 13, fontWeight: "600" },
  rowSubtitle: { color: palette.muted, fontSize: 11, marginTop: 5 },
  rowChevron: { color: palette.faint, fontSize: 24 },
  formCard: { backgroundColor: palette.surface, borderWidth: 1, borderColor: palette.border, borderRadius: 13, padding: 16, marginBottom: 24 },
  cardTitle: { color: palette.text, fontSize: 15, fontWeight: "600", marginBottom: 5 },
  fieldWrap: { marginTop: 14 },
  fieldLabel: { color: palette.muted, fontSize: 10, marginBottom: 7 },
  input: { color: palette.text, borderWidth: 1, borderColor: palette.border, borderRadius: 8, paddingHorizontal: 12, paddingVertical: 11, fontSize: 13, backgroundColor: palette.background },
  errorText: { color: "#F09A9A", fontSize: 11, marginTop: 10 },
  sectionLabel: { color: palette.faint, fontSize: 9, letterSpacing: 1.5, marginBottom: 7, marginTop: 5 },
  smallAction: { borderWidth: 1, borderColor: palette.border, borderRadius: 7, paddingHorizontal: 10, paddingVertical: 8 },
  smallActionText: { color: palette.accent, fontSize: 10, fontWeight: "700" },
  conversationHeader: { flexDirection: "row", alignItems: "center", gap: 10, marginBottom: 18 },
  backButton: { width: 34, height: 38, justifyContent: "center" },
  backButtonText: { color: palette.accent, fontSize: 32 },
  conversationIdentity: { flex: 1 },
  localBadge: { borderRadius: 5, borderWidth: 1, borderColor: palette.border, paddingHorizontal: 7, paddingVertical: 5 },
  localBadgeText: { color: palette.warning, fontSize: 8, letterSpacing: 1 },
  messageList: { flex: 1, minHeight: 240 },
  messageContent: { flexGrow: 1, justifyContent: "flex-end", gap: 10, paddingBottom: 14 },
  emptyConversation: { alignItems: "center", paddingVertical: 38 },
  messageBubble: { alignSelf: "flex-end", maxWidth: "88%", backgroundColor: palette.surfaceRaised, borderWidth: 1, borderColor: palette.border, borderRadius: 13, borderBottomRightRadius: 4, padding: 12 },
  messageText: { color: palette.text, fontSize: 13, lineHeight: 19 },
  messageMeta: { color: palette.faint, fontSize: 8, marginTop: 8, letterSpacing: 0.5 },
  composer: { flexDirection: "row", alignItems: "flex-end", gap: 8, backgroundColor: palette.surface, borderWidth: 1, borderColor: palette.border, borderRadius: 12, padding: 7, marginTop: 10 },
  composerInput: { flex: 1, maxHeight: 100, minHeight: 38, color: palette.text, fontSize: 13, paddingHorizontal: 8, paddingVertical: 8 },
  sendButton: { width: 38, height: 38, borderRadius: 9, backgroundColor: palette.accentDark, alignItems: "center", justifyContent: "center" },
  sendButtonText: { color: palette.text, fontSize: 20, fontWeight: "700" },
  securityCard: { backgroundColor: palette.surface, borderWidth: 1, borderColor: palette.border, borderRadius: 13, padding: 17, marginBottom: 18 },
  securityLabel: { color: palette.accent, fontSize: 9, letterSpacing: 1.5, fontWeight: "700" },
  securityValue: { color: palette.text, fontSize: 20, fontWeight: "600", marginTop: 9 },
  securityCopy: { color: palette.muted, fontSize: 12, lineHeight: 19, marginTop: 9 },
  statusRow: { flexDirection: "row", alignItems: "center", gap: 10, paddingVertical: 14, borderBottomWidth: 1, borderBottomColor: palette.border },
  statusDot: { width: 7, height: 7, borderRadius: 4, backgroundColor: palette.warning },
  pending: { color: palette.warning, fontSize: 8, letterSpacing: 0.8 },
  footnote: { color: palette.faint, fontSize: 10, lineHeight: 16, marginTop: 20 },
  tabBar: { flexDirection: "row", borderTopWidth: 1, borderTopColor: palette.border, backgroundColor: palette.background, paddingTop: 9, paddingBottom: Platform.OS === "ios" ? 20 : 10 },
  tab: { flex: 1, alignItems: "center", gap: 4, paddingVertical: 5 },
  tabSymbol: { color: palette.faint, fontSize: 19 },
  tabText: { color: palette.faint, fontSize: 9 },
  tabActive: { color: palette.accent },
});
