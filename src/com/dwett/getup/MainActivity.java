package com.dwett.getup;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.*;

public class MainActivity extends Activity {

    WifiManager wifi_service;
    WifiScanReceiver receiver;
    HashMap<String, Integer> initialLocationMap;
    HashMap<String, Integer> currentLocationMap;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        wifi_service = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        setContentView(R.layout.main);

        initializeHandlers();
    }

    @Override
    public void onResume() {
        super.onResume();
        startWifiMonitor();
    }

    @Override
    public void onPause() {
        stopWifiMonitor();
        super.onPause();
    }

    private void initializeHandlers() {
        Button setLocationButton = (Button) findViewById(R.id.set_location);
        setLocationButton.setOnClickListener(setLocationListener);
    }

    // 0.5 sec
    private final static int WIFI_POLLING_INTERVAL = 500;

    void startWifiMonitor() {
        receiver = new WifiScanReceiver();
        registerReceiver(receiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        initialLocationMap = null;
        currentLocationMap = null;

        wifiScannerTask.run();
    }

    void stopWifiMonitor() {
        unregisterReceiver(receiver);
        wifiMonitorHandler.removeCallbacks(wifiCheckerTask);
    }

    private final View.OnClickListener setLocationListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            initialLocationMap = null;
            currentLocationMap = null;
        }
    };

    private final Handler wifiMonitorHandler = new Handler();

    private final Runnable wifiScannerTask = new Runnable() {
        @Override
        public void run() {
            wifi_service.startScan();
        }
    };

    private final Runnable wifiCheckerTask = new Runnable() {
        @Override
        public void run() {
            WifiInfo wifiInfo = wifi_service.getConnectionInfo();
            List<ScanResult> wifiScanResults = wifi_service.getScanResults();
            String wifiString = getStatusString(wifiInfo, wifiScanResults);

            TextView textView = (TextView) findViewById(R.id.wifi_status);
            textView.setText(wifiString);

            updateLocationMaps(wifiScanResults);

            wifiMonitorHandler.postDelayed(wifiScannerTask, WIFI_POLLING_INTERVAL);
        }

        private String getStatusString(WifiInfo wifiInfo, List<ScanResult> scanResults) {
            StringBuilder sb = new StringBuilder();

            // Print the info for this wifi station
            sb.append("Connected to: \n");
            sb.append(wifiInfo.getSSID());
            sb.append(": ");
            sb.append(wifiInfo.getRssi());
            sb.append('\n');

            // Sort the results
            TreeSet<ScanResult> sortedResults = new TreeSet<ScanResult>(new Comparator<ScanResult>() {
                @Override
                public int compare(ScanResult scanResult, ScanResult scanResult2) {
                    return (Integer.valueOf(scanResult2.level)).compareTo(scanResult.level);
                }
            });
            sortedResults.addAll(scanResults);

            sb.append("Nearby: \n");
            for (ScanResult result : sortedResults) {
                // Print out this result
                sb.append(result.SSID);
                sb.append(": ");
                sb.append(result.level);
                sb.append('\n');
            }

            return sb.toString();
        }

        private void updateLocationMaps(List<ScanResult> wifiScanResults) {
            if (initialLocationMap == null) {
                initialLocationMap = new HashMap<String, Integer>();
                currentLocationMap = new HashMap<String, Integer>();
                fillMap(initialLocationMap, wifiScanResults);
            } else {
                fillMap(currentLocationMap, wifiScanResults);
                wifiMonitorHandler.post(compareLocation);
            }
        }

        private void fillMap(Map<String, Integer> locationMap, List<ScanResult> wifiScanResults) {
            locationMap.clear();
            for (ScanResult result : wifiScanResults) {
                locationMap.put(result.SSID, result.level);
            }
        }
    };

    private final Runnable compareLocation = new Runnable() {

        @Override
        public void run() {
            // Compare the initial location to the current location and print if it's different
            boolean different = this.isDifferent(initialLocationMap, currentLocationMap);
            String locationText = (different) ? "Different place" : "Same place";

            TextView textView = (TextView) findViewById(R.id.location_status);
            textView.setText(locationText);
        }

        private boolean isDifferent(
                Map<String, Integer> locationMap1, Map<String, Integer> locationMap2) {
            Set<String> allKeys = new TreeSet<String>();

            for (String key : locationMap1.keySet()) {
                allKeys.add(key);
            }
            for (String key : locationMap2.keySet()) {
                allKeys.add(key);
            }
            double differenceWeighting = 1000.0;
            double missingPointValue = 300;
            double missingPointDenominator = 4.0;
            double total = 0.0;
            double threshold = 20.0;

            for (String key : allKeys) {
                double value1, value2;
                if (locationMap1.containsKey(key)) {
                    value1 = locationMap1.get(key);
                    if (locationMap2.containsKey(key)) {
                        value2 = locationMap2.get(key);
                    } else {
                        value1 = Math.abs(value1);
                        value2 = (missingPointValue + value1) / missingPointDenominator;
                    }
                } else {
                    value2 = Math.abs(locationMap2.get(key));
                    value1 = (missingPointValue + value2) / missingPointDenominator;
                }
                total += differenceWeighting * Math.abs((value2 - value1) / (value1 * value1));
            }
            TextView debugTextView = (TextView) findViewById(R.id.location_debug);
            debugTextView.setText(total + " / " + threshold);

            return total > threshold;
        }
    };

    class WifiScanReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context c, Intent intent) {
            wifiMonitorHandler.post(wifiCheckerTask);
        }
    }
}
