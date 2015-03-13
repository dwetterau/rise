package com.dwett.rise;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.dwett.rise.alarm.AlarmManagerHelper;

import java.io.IOException;

/**
 * @author david
 */
public class AlarmScreenActivity extends Activity {

    public final String TAG = this.getClass().getSimpleName();

    private PowerManager.WakeLock wakeLock;
    private MediaPlayer mediaPlayer;

    // Waits 10 minutes before dismissing
    private static final int WAKELOCK_TIMEOUT = 10 * 60 * 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.alarm_screen);

        String name = getIntent().getStringExtra(AlarmManagerHelper.NAME);
        int timeHour = getIntent().getIntExtra(AlarmManagerHelper.TIME_HOUR, 0);
        int timeMinute = getIntent().getIntExtra(AlarmManagerHelper.TIME_MINUTE, 0);
        String tone = getIntent().getStringExtra(AlarmManagerHelper.TONE);

        TextView textViewName = (TextView) findViewById(R.id.alarmScreenName);
        textViewName.setText(name);

        wakeUpAndAlarm(tone);
        releaseWakeLock();
    }

    /**
     * Initializes the media player, begins playing the ringtone selected for this alarm
     * @param tone the string representing the uri for the ringtone
     */
    private void wakeUpAndAlarm(String tone) {
        mediaPlayer = new MediaPlayer();
        try {
            if (tone != null && !tone.equals("")) {
                Uri toneUri = Uri.parse(tone);
                mediaPlayer.setDataSource(this, toneUri);
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                mediaPlayer.setLooping(true);
                mediaPlayer.prepare();
                mediaPlayer.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void releaseWakeLock() {
        Runnable releaseWakeLock = new Runnable() {
            @Override
            public void run() {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

                if (wakeLock != null && wakeLock.isHeld()) {
                    wakeLock.release();
                }
            }
        };
        new Handler().postDelayed(releaseWakeLock, WAKELOCK_TIMEOUT);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Keep the screen on
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        // Get the WakeLock
        PowerManager powerManager =
                (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        if (wakeLock == null) {
            wakeLock = powerManager.newWakeLock(
                    (PowerManager.FULL_WAKE_LOCK | PowerManager.SCREEN_BRIGHT_WAKE_LOCK), TAG);
        }
        if (!wakeLock.isHeld()) {
            wakeLock.acquire();
        }
    }

    @Override
    protected void onPause() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
        super.onPause();
    }

    private static final int SCAN_FOR_SMILE_REQUEST = 2;

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
            dismissAlarm();
        } else {
            Log.d("MainActivity", "got result from scan, face not found");
            // They chose to exit out of the scanning, must have agreed to the confirmation dialogue
            dismissAlarm();
        }
    }

    private void dismissAlarm() {
        mediaPlayer.stop();
        finish();
    }
}
