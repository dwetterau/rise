package com.dwett.rise.camera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.util.LinkedList;
import java.util.List;

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
                return "Found smile!";
            } else {
                return "Didn't find smile :(";
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

            // Starting in the center, fire rays off at 360 degree angles and look
            // for a thin line of a different color.
            // We want to see the pattern: no match, match, no match, match, no match
            //                    state =  0         1      0         1      0
            //                  changes =  0         1      2         3      4
            // any deviation and we return false;
            int state = 0;
            int changes = 0;
            RayResult last;
            RayResult current = null;
            for (double angle = 0.0; angle < 2 * Math.PI; angle += Math.PI / 180.0) {
                last = current;
                current = new RayResult();

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
                                startInterval = currentA;
                            }
                        } else {
                            // Back to the base color
                            if (inInterval) {
                                // We were in an interval
                                inInterval = false;
                                if (currentA - startInterval > INTERVAL_SIZE_THRESHOLD) {
                                    current.addInterval(new Interval(startInterval, currentA - 1));
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

                // We have finished constructing the current RayResult, compare it to the last one
                if (last == null) {
                    // This is the first RayResult, continue
                    continue;
                }
                List<Interval> matchingIntervals = current.matches(last);
                if (matchingIntervals.size() > 0) {
                    // We are in a line segment, check state and update if we switch
                    if (state == 0) {
                        // We need to switch
                        state = 1;
                        changes++;
                    }
                } else {
                    // We are no longer in a line segment, check state and update if we switch
                    if (state == 1) {
                        state = 0;
                        changes++;
                    }
                }
                if (changes > 4) {
                    return false;
                }
            }
            Log.d("ScanTask", "end state & changes " + state + " " + changes);
            return state == 0 && changes == 4;
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

            // The amount intervals are required to overlap
            private static final int REQUIRED_OVERLAP = 1;

            int start;
            int end;

            public Interval(int start, int end) {
                this.start = start;
                this.end = end;
            }

            public boolean overlaps(Interval interval) {
                return REQUIRED_OVERLAP <=
                        Math.min(interval.end, this.end) - Math.max(interval.start, this.start);
            }

            public String toString() {
                return "[" + start + ", " + end + "]";
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
            statusTextView.setText(result);
            scanButton.setEnabled(true);
        }

        @Override
        protected void onProgressUpdate(Void... values) {}
    }
}
