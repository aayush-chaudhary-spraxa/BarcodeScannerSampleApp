import { DeviceEventEmitter, NativeModules } from 'react-native';

const { DataWedgeModule } = NativeModules;

export type ScanEvent = {
  data: string;
  labelType: string;
  source: string;
};

/**
 * Thin wrapper around the native DataWedgeModule. On non-Zebra devices
 * (e.g. an emulator or a phone without DataWedge installed) the native
 * module is still present but scans will simply never arrive, since
 * DataWedge itself isn't there to broadcast them.
 */
export const DataWedge = {
  createProfile(): void {
    DataWedgeModule.createProfile();
  },

  startSoftScan(): void {
    DataWedgeModule.startSoftScan();
  },

  stopSoftScan(): void {
    DataWedgeModule.stopSoftScan();
  },

  addScanListener(callback: (event: ScanEvent) => void) {
    return DeviceEventEmitter.addListener('onDataWedgeScan', callback);
  },
};
