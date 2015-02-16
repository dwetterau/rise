package com.dwett.rise;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.FrameLayout;

import com.dwett.rise.camera.Preview;


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
        ((FrameLayout) findViewById(R.id.cameraPreview)).addView(cameraPreview);

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
        //Button setLocationButton = (Button) findViewById(R.id.set_location);
    }
}
