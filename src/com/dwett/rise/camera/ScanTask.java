package com.dwett.rise.camera;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * @author david
 */
public class ScanTask implements OnClickListener {

    Button scanButton;
    TextView statusTextView;

    public ScanTask(Button scanButton, TextView statusTextView) {
        this.scanButton = scanButton;
        this.statusTextView = statusTextView;

        // Add this as the listener for the button
        this.scanButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == scanButton.getId()) {
            if (Preview.camera != null) {
                Preview.camera.autoFocus(null);
            }

            new ScanForSmile().execute("");
        }
    }

    private class ScanForSmile extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            scanButton.setEnabled(false);
            statusTextView.setText("Scanning...");
        }

        @Override
        protected String doInBackground(String... params) {
            // TODO: The image detection here
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.interrupted();
            }

            if (Math.random() < .5) {
                return "Found smile!";
            } else {
                return "Didn't find smile :(";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            statusTextView.setText(result);
            scanButton.setEnabled(true);
        }

        @Override
        protected void onProgressUpdate(Void... values) {}
    }
}
