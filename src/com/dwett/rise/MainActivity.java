package com.dwett.rise;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.dwett.rise.camera.Preview;
import com.dwett.rise.camera.ScanTask;


public class MainActivity extends Activity {

    private Preview cameraPreview;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.main);

        cameraPreview = new Preview(this);
        // Add the camera preview at the beginning of the frame layout so it's under the status
        ((FrameLayout) findViewById(R.id.cameraPreview)).addView(cameraPreview, 0);

        initializeHandlers();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void initializeHandlers() {
        Button scanButton = (Button) findViewById(R.id.scanButton);
        TextView scanStatusTextView = (TextView) findViewById(R.id.scanStatus);

        // TODO: Should I save this somewhere?
        new ScanTask(scanButton, scanStatusTextView);
    }
}
