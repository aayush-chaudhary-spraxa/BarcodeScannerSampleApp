/**
 * Barcode Scanner Sample App
 * Demonstrates scanning via a Zebra device's hardware scanner (DataWedge).
 *
 * @format
 */

import { useEffect, useState } from 'react';
import {
  Button,
  FlatList,
  StatusBar,
  StyleSheet,
  Text,
  useColorScheme,
  View,
} from 'react-native';
import {
  SafeAreaProvider,
  useSafeAreaInsets,
} from 'react-native-safe-area-context';
import { DataWedge, ScanEvent } from './DataWedge';

function App() {
  const isDarkMode = useColorScheme() === 'dark';

  return (
    <SafeAreaProvider>
      <StatusBar barStyle={isDarkMode ? 'light-content' : 'dark-content'} />
      <AppContent isDarkMode={isDarkMode} />
    </SafeAreaProvider>
  );
}

function AppContent({ isDarkMode }: { isDarkMode: boolean }) {
  const safeAreaInsets = useSafeAreaInsets();
  const [scans, setScans] = useState<ScanEvent[]>([]);

  useEffect(() => {
    DataWedge.createProfile();

    const subscription = DataWedge.addScanListener(event => {
      setScans(previous => [event, ...previous].slice(0, 50));
    });

    return () => subscription.remove();
  }, []);

  const theme = isDarkMode ? darkTheme : lightTheme;

  return (
    <View
      style={[
        styles.container,
        theme.background,
        {
          paddingTop: safeAreaInsets.top,
          paddingBottom: safeAreaInsets.bottom,
        },
      ]}>
      <View style={styles.header}>
        <Text style={[styles.title, theme.text]}>Barcode Scanner</Text>
        <Text style={[styles.subtitle, theme.subtleText]}>
          Press the hardware scan trigger, or use the button below
        </Text>
      </View>

      <View style={styles.triggerRow}>
        <Button title="Soft Scan Trigger" onPress={DataWedge.startSoftScan} />
      </View>

      {scans.length === 0 ? (
        <View style={styles.empty}>
          <Text style={theme.subtleText}>No scans yet</Text>
        </View>
      ) : (
        <FlatList
          style={styles.list}
          data={scans}
          keyExtractor={(_, index) => String(index)}
          renderItem={({ item }) => (
            <View style={[styles.row, theme.row]}>
              <Text style={[styles.rowData, theme.text]}>{item.data}</Text>
              <Text style={[styles.rowMeta, theme.subtleText]}>
                {item.labelType} · {item.source}
              </Text>
            </View>
          )}
        />
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  header: {
    paddingHorizontal: 20,
    paddingTop: 16,
    paddingBottom: 8,
  },
  title: {
    fontSize: 24,
    fontWeight: '700',
  },
  subtitle: {
    fontSize: 14,
    marginTop: 4,
  },
  triggerRow: {
    paddingHorizontal: 20,
    paddingVertical: 12,
  },
  empty: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  list: {
    flex: 1,
  },
  row: {
    paddingHorizontal: 20,
    paddingVertical: 12,
    borderBottomWidth: StyleSheet.hairlineWidth,
  },
  rowData: {
    fontSize: 16,
    fontWeight: '600',
  },
  rowMeta: {
    fontSize: 12,
    marginTop: 2,
  },
});

const lightTheme = StyleSheet.create({
  background: { backgroundColor: '#ffffff' },
  text: { color: '#111111' },
  subtleText: { color: '#666666' },
  row: { borderBottomColor: '#e0e0e0' },
});

const darkTheme = StyleSheet.create({
  background: { backgroundColor: '#121212' },
  text: { color: '#f5f5f5' },
  subtleText: { color: '#a0a0a0' },
  row: { borderBottomColor: '#2a2a2a' },
});

export default App;
