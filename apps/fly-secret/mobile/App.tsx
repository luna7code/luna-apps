import React from "react";
import { SafeAreaView, StatusBar, StyleSheet, Text, View } from "react-native";

const sections = ["Chats", "Calls", "Contacts", "Settings"];

export default function App() {
  return (
    <SafeAreaView style={styles.screen}>
      <StatusBar barStyle="light-content" backgroundColor="#171113" />
      <View style={styles.header}>
        <Text style={styles.brand}>FLY <Text style={styles.brandAccent}>SECRET</Text></Text>
        <Text style={styles.kicker}>PRIVATE COMMUNICATION</Text>
      </View>
      <View style={styles.body}>
        <Text style={styles.overline}>YOUR PERSONAL SPACE</Text>
        <Text style={styles.title}>Your conversations.{"\n"}<Text style={styles.accent}>Your control.</Text></Text>
        <Text style={styles.description}>A private communication workspace, designed around user control and security-first architecture.</Text>
        <View style={styles.panel}>
          <Text style={styles.panelTitle}>Foundation build</Text>
          <Text style={styles.panelText}>The application shell is ready. Product modules will be added in approved development phases.</Text>
          <Text style={styles.version}>FLY SECRET · 0.1.0</Text>
        </View>
      </View>
      <View style={styles.tabBar}>
        {sections.map((section, index) => (
          <View key={section} style={styles.tab}>
            <View style={[styles.dot, index === 0 && styles.activeDot]} />
            <Text style={[styles.tabText, index === 0 && styles.activeText]}>{section}</Text>
          </View>
        ))}
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  screen: { flex: 1, backgroundColor: "#171113" },
  header: { paddingHorizontal: 24, paddingTop: 18, paddingBottom: 20, borderBottomWidth: 1, borderBottomColor: "#33272c" },
  brand: { color: "#fff", fontSize: 17, fontWeight: "800", letterSpacing: 3 },
  brandAccent: { color: "#bd7889" },
  kicker: { color: "#8f7d83", fontSize: 9, letterSpacing: 2, marginTop: 8 },
  body: { flex: 1, paddingHorizontal: 24, paddingTop: 72 },
  overline: { color: "#bd7889", fontSize: 10, letterSpacing: 2, fontWeight: "700" },
  title: { color: "#f5f0f0", fontSize: 43, lineHeight: 48, fontWeight: "600", letterSpacing: -2, marginTop: 20 },
  accent: { color: "#bd7889" },
  description: { color: "#a99da1", fontSize: 14, lineHeight: 23, marginTop: 18, maxWidth: 340 },
  panel: { backgroundColor: "#21191c", borderColor: "#382a30", borderWidth: 1, borderRadius: 14, padding: 22, marginTop: 46 },
  panelTitle: { color: "#f5f0f0", fontSize: 17, fontWeight: "600" },
  panelText: { color: "#a99da1", fontSize: 13, lineHeight: 21, marginTop: 10 },
  version: { color: "#806f75", fontSize: 9, letterSpacing: 1.5, marginTop: 22 },
  tabBar: { flexDirection: "row", borderTopWidth: 1, borderTopColor: "#33272c", paddingVertical: 14, paddingHorizontal: 8 },
  tab: { flex: 1, alignItems: "center", gap: 7 },
  dot: { width: 6, height: 6, borderRadius: 3, backgroundColor: "#71646a" },
  activeDot: { backgroundColor: "#bd7889" },
  tabText: { color: "#8f7d83", fontSize: 10 },
  activeText: { color: "#f5f0f0" }
});