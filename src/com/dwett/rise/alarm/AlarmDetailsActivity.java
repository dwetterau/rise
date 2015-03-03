package com.dwett.rise.alarm;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

import com.dwett.rise.R;

/**
 * @author david
 */
public class AlarmDetailsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.alarmDetails);
    }
}
