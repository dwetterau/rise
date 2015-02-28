package com.dwett.rise;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.dwett.rise.camera.Preview;
import com.dwett.rise.camera.ScanTask;


public class MainActivity extends Activity {

    private static final int SCAN_FOR_SMILE_REQUEST = 1;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.main);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void startScan(View view) {
        Intent intent = new Intent(this, ScanActivity.class);
        startActivityForResult(intent, SCAN_FOR_SMILE_REQUEST);
    }

    @Override
    protected  void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SCAN_FOR_SMILE_REQUEST) {
            processScanResult(resultCode, data);
        }
    }

    private void processScanResult(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Log.d("MainActivity", "got result from scan, face found");
        } else {
            Log.d("MainActivity", "got result from scan, face not found");
        }
    }
}
