package com.dwett.rise;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import com.dwett.rise.alarm.AlarmDBHelper;
import com.dwett.rise.alarm.AlarmDetailsActivity;
import com.dwett.rise.alarm.AlarmListAdapter;
import com.dwett.rise.alarm.AlarmManagerHelper;
import com.dwett.rise.alarm.AlarmModel;

import java.util.List;


public class MainActivity extends ListActivity {

    private static final int ALARM_DETAILS_REQUEST = 1;

    public static final String EXTRA_ALARM_DETAILS_ID = "alarmDetailsId";

    private AlarmListAdapter alarmListAdapter;
    private AlarmDBHelper dbHelper;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.main);
        this.initializeAlarmList();
    }

    private void initializeAlarmList() {
        this.dbHelper = new AlarmDBHelper(this);
        List<AlarmModel> alarmModelList = dbHelper.getAllAlarms();

        this.alarmListAdapter = new AlarmListAdapter(this, alarmModelList);

        setListAdapter(this.alarmListAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void setAlarmEnabled(long id, boolean isEnabled) {
        AlarmModel model = dbHelper.getAlarm(id);
        model.setEnabled(isEnabled);
        dbHelper.updateAlarm(model);

        AlarmManagerHelper.setAlarms(this);

        alarmListAdapter.setAlarmModelList(dbHelper.getAllAlarms());
        alarmListAdapter.notifyDataSetChanged();
    }

    public void editAlarmDetails(long id) {
        Intent intent = new Intent(this, AlarmDetailsActivity.class);
        intent.putExtra(EXTRA_ALARM_DETAILS_ID, id);
        startActivityForResult(intent, ALARM_DETAILS_REQUEST);
    }

    public void addAlarm(View view) {
        editAlarmDetails(-1L);
    }


    protected  void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ALARM_DETAILS_REQUEST) {
                processAlarmDetailsResult(resultCode, data);
        }
    }

    private void processAlarmDetailsResult(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            this.alarmListAdapter.setAlarmModelList(this.dbHelper.getAllAlarms());
            this.alarmListAdapter.notifyDataSetChanged();
        }
    }
}
