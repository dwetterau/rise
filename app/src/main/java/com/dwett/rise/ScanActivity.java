package com.dwett.rise;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.dwett.rise.camera.Preview;
import com.dwett.rise.camera.ScanTask;

/**
 * @author david
 */
public class ScanActivity extends Activity {

    private Preview cameraPreview;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.scan);

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
        FrameLayout previewView = (FrameLayout) findViewById(R.id.cameraPreview);
        TextView scanStatusTextView = (TextView) findViewById(R.id.scanStatus);

        // TODO: Should I save this somewhere?
        new ScanTask(previewView, scanStatusTextView, this);
    }

    public void foundSmileExit(boolean foundSmile) {
        Intent intent = new Intent();
        this.setResult(Activity.RESULT_OK, intent);
        super.finish();
    }

    @Override
    public void onBackPressed() {
        // Build an alert that they want to exit the scanner without scanning :(
        new AlertDialog.Builder(this)
                .setMessage(R.string.scanConfirmExit)
                .setCancelable(false)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Be sure to clean up the camera before exiting
                        Preview.stopPreviewAndFreeCamera();
                        foundSmileExit(false);
                    }
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }
}