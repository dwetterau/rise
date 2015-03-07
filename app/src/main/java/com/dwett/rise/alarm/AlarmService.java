package com.dwett.rise.alarm;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * @author david
 */
public class AlarmService extends Service {

    private static final String TAG = AlarmService.class.getSimpleName();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        AlarmManagerHelper.setAlarms(this);
        return super.onStartCommand(intent, flags, startId);
    }
}
