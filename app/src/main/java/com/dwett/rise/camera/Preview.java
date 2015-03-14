package com.dwett.rise.camera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Camera;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

/**
 * @author david
 */
public class Preview extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceHolder surfaceHolder;

    protected static Camera camera = null;

    public Preview(Context context) {
        super(context);

        surfaceHolder = this.getHolder();
        surfaceHolder.addCallback(this);

        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    public void attachCamera() {
        camera = Camera.open();
        if (camera != null) {
            try {
                camera.setPreviewDisplay(this.surfaceHolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void stopPreviewAndFreeCamera() {
        if (camera != null) {
            camera.stopPreview();
            camera.release();

            // Clear out all camera values
            camera = null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // Preview surface exists, open the camera
        attachCamera();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (camera != null) {
            camera.setDisplayOrientation(90);
            Camera.Parameters parameters = camera.getParameters();
            parameters.set("rotation", 90);
            parameters.set("orientation", "portrait");

            int w = 0, h = 0;
            for (Camera.Size s : parameters.getSupportedPreviewSizes()) {
                if (s.height > h && s.height <= height && s.width > w && s.width <= width) {
                    h = s.height;
                    w = s.width;
                }
            }
            parameters.setPreviewSize(w, h);
            camera.setParameters(parameters);
            camera.startPreview();
            camera.autoFocus(null);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (camera != null) {
            stopPreviewAndFreeCamera();
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        Paint p = new Paint(Color.RED);
        canvas.drawText("Preview", canvas.getWidth() / 2, canvas.getHeight() / 2, p);
    }
}
