package com.dwett.rise.camera;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.dwett.rise.ScanActivity;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author david
 */
public class ScanTask implements OnClickListener {

    Button scanButton;
    TextView statusTextView;
    ScanActivity activity;

    public ScanTask(Button scanButton, TextView statusTextView, ScanActivity activity) {
        this.scanButton = scanButton;
        this.statusTextView = statusTextView;

        // Add this as the listener for the button
        this.scanButton.setOnClickListener(this);
        this.activity = activity;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == scanButton.getId()) {
            // There has to be a camera preview, focus it and upon finish, take a picture
            if (Preview.camera != null) {
                scanButton.setEnabled(false);
                statusTextView.setText("Scanning...");
                Preview.camera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        takePicture();
                    }
                });
            }
        }
    }

    public void takePicture() {
        Log.d("ScanTask", "Taking a picture...");
        if (Preview.camera == null) {
            // Nothing we can do without a camera
            return;
        }
        Preview.camera.takePicture(null, null, new JPEGImageToScan());
    }

    private class JPEGImageToScan implements Camera.PictureCallback {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d("ScanTask", "onPictureTaken jpeg " + data);
            new ScanForSmile(data).execute();

            // Restart the preview because the taking of a picture stops it
            camera.startPreview();
        }
    }

    private class ScanForSmile extends AsyncTask<String, Void, String> {

        private byte[] rawPicture;

        public ScanForSmile(byte [] rawPicture) {
            this.rawPicture = rawPicture;
        }

        @Override
        protected void onPreExecute() {
            statusTextView.setText("Processing...");
        }

        @Override
        protected String doInBackground(String... params) {
            if (rawPicture == null) {
                return "Didn't capture picture :(";
            }

            Bitmap bitmap = BitmapFactory.decodeByteArray(this.rawPicture, 0, this.rawPicture.length);

            boolean foundSmile = findSmile(bitmap);

            if (foundSmile) {
                return "success";
            } else {
                return "failure";
            }
        }

        private final double AMPLITUDE_GRANULARITY = 1;
        private final double INTERVAL_SIZE_THRESHOLD = 15;

        protected boolean findSmile(Bitmap bitmap) {
            // Avoid huge memory allocations, access the bitmap through .getPixel
            int w = bitmap.getWidth();
            int h = bitmap.getHeight();

            int centerX = w >> 1;
            int centerY = h >> 1;

            RayResult[] results = new RayResult[4];
            int index = 0;
            for (double angle = 0.0; angle < 2 * Math.PI; angle += Math.PI / 2.0) {
                RayResult current = new RayResult();

                // Traverse the ray
                // Traverse the ray and build the current ray result
                double a = 0.0;
                boolean inInterval = false;
                int startInterval = -1;
                int currentA = 0;
                int baseColor = -1;
                while (true) {
                    int newX = centerX + (int)Math.round(a * Math.cos(angle));
                    int newY = centerY - (int)Math.round(a * Math.sin(angle));

                    // We are outside the image, return
                    if (!isValid(newX, newY, w, h)) {
                        break;
                    }

                    int thisColor = bitmap.getPixel(newX, newY);

                    if (baseColor != -1) {
                        // Compare and see if the same
                        if (!colorMatches(baseColor, thisColor)) {
                            if (!inInterval) {
                                // We are now in an interval
                                inInterval = true;
                                startInterval = thisColor;
                            }
                        } else {
                            // Back to the base color
                            if (inInterval) {
                                // We were in an interval
                                inInterval = false;
                                if (currentA - startInterval > INTERVAL_SIZE_THRESHOLD) {
                                    current.addInterval(new Interval(startInterval, thisColor));
                                }
                            }
                        }
                    } else {
                        // Set this color as the base color
                        baseColor = thisColor;
                    }

                    currentA++;
                    a += AMPLITUDE_GRANULARITY;
                }

                results[index++] = current;
            }

            Log.d("ScanTask", Arrays.toString(results));

            // First assert that 0 and 2 match on at least one interval
            List<Interval> topAndBottom = results[0].matches(results[2]);
            if (topAndBottom.size() == 0) {
                Log.d("ScanTask", "Top and bottom did not match.");
                return false;
            }

            RayResult combined = new RayResult(topAndBottom);

            // Now assert that the top and bottom intervals do not match the sides
            if (combined.matches(results[1]).size() > 0) {
                Log.d("ScanTask", "Combined and right did match.");
                return false;
            }
            if (combined.matches(results[3]).size() > 0) {
                Log.d("ScanTask", "Combined and bottom did match.");
                return false;
            }
            Log.d("ScanTask", "Smile detected.");
            return true;
        }
        
        protected boolean colorMatches(int color1, int color2) {
            // Convert the colors to their rgb values
            RGBColor c1 = new RGBColor(color1);
            RGBColor c2 = new RGBColor(color2);
            return c1.matches(c2);
        }

        protected boolean isValid(int x, int y, int w, int h) {
            return x >= 0 && x < w && y >= 0 && y < h;
        }

        private class RayResult {
            List<Interval> lines;

            public RayResult() {
                lines = new LinkedList<Interval>();
            }

            public RayResult(List<Interval> lines) {
                this();
                this.lines.addAll(lines);
            }

            /**
             * Returns an interval if for some pair of intervals, this ray goes through another ray.
             * @param other another RayResult
             * @return a list of intervals of this Ray that the other one matches empty if none do
             */
            public List<Interval> matches(RayResult other) {
                List<Interval> matches = new LinkedList<Interval>();
                for (Interval interval : this.lines) {
                    for (Interval otherInterval : other.getIntervals()) {
                        if (interval.overlaps(otherInterval)) {
                            matches.add(interval);
                            // Don't want to add this interval more than once
                            break;
                        }
                    }
                }
                return matches;
            }

            public void addInterval(Interval interval) {
                lines.add(interval);
            }

            public List<Interval> getIntervals() {
                return this.lines;
            }

            public String toString() {
                return this.lines.toString();
            }
        }

        private class Interval {

            RGBColor color1;
            RGBColor color2;

            public Interval(int color1, int color2) {
                this.color1 = new RGBColor(color1);
                this.color2 = new RGBColor(color2);
            }

            public boolean overlaps(Interval interval) {
                // If some pair of colors matches, use that
                return this.color1.matches(interval.color1) ||
                        this.color1.matches(interval.color2) ||
                        this.color2.matches(interval.color1) ||
                        this.color2.matches(interval.color2);
            }

            public String toString() {
                return "[" + color1 + ", " + color2 + "]";
            }
        }

        private class RGBColor {

            private final int RGB_DIFF = 50;

            protected int r;
            protected int g;
            protected int b;

            public RGBColor(int color) {
                this.r = (color >> 16) & 0xFF;
                this.g = (color >> 8) & 0xFF;
                this.b = color & 0xFF;
            }

            public boolean matches(RGBColor other) {
                return Math.abs(this.r - other.r) <= RGB_DIFF &&
                        Math.abs(this.g - other.g) <= RGB_DIFF &&
                        Math.abs(this.b - other.b) <= RGB_DIFF;
            }

            public String toString() {
                return "[r: " + r + " g: " + g + " b: " + b + "]";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.equals("success")) {
                statusTextView.setText("Found smile!");
                Intent intent = new Intent();
                activity.setResult(Activity.RESULT_OK, intent);
                activity.finish();
            } else {
                statusTextView.setText("Didn't find smile :(");
                scanButton.setEnabled(true);
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {}
    }
}
