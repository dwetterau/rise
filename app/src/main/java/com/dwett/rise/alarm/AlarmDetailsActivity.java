package com.dwett.rise.alarm;

import android.app.Activity;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.dwett.rise.MainActivity;
import com.dwett.rise.R;

/**
 * @author david
 */
public class AlarmDetailsActivity extends Activity {

    protected static final int RINGTONE_REQUEST = 1;

    private AlarmModel alarmDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.alarm_details);

        long id = getIntent().getExtras().getLong(MainActivity.EXTRA_ALARM_DETAILS_ID);
        initializeAlarmDetails(id);
    }

    private void initializeAlarmDetails(long id) {
        if (id < 0) {
            this.alarmDetails = new AlarmModel();
            this.alarmDetails.setId(id);
        } else {
            AlarmDBHelper dbHelper = new AlarmDBHelper(this);
            this.alarmDetails = dbHelper.getAlarm(id);

            // Set all the elements in the view to the right values
            TimePicker timePicker = (TimePicker) findViewById(R.id.alarmDetailsTimePicker);
            timePicker.setCurrentHour(alarmDetails.getTimeHour());
            timePicker.setCurrentMinute(alarmDetails.getTimeMinute());

            EditText alarmNameEditText = (EditText) findViewById(R.id.alarmDetailsName);
            alarmNameEditText.setText(alarmDetails.getName());

            CheckBox checkBoxWeekly = (CheckBox) findViewById(R.id.alarmDetailsRepeatWeekly);
            checkBoxWeekly.setChecked(alarmDetails.isRepeatWeekly());

            CheckBox checkBoxSunday = (CheckBox) findViewById(R.id.alarmDetailsSunday);
            checkBoxSunday.setChecked(alarmDetails.getRepeatingDay(AlarmModel.SUNDAY));

            CheckBox checkBoxMonday = (CheckBox) findViewById(R.id.alarmDetailsMonday);
            checkBoxMonday.setChecked(alarmDetails.getRepeatingDay(AlarmModel.MONDAY));

            CheckBox checkBoxTuesday = (CheckBox) findViewById(R.id.alarmDetailsTuesday);
            checkBoxTuesday.setChecked(alarmDetails.getRepeatingDay(AlarmModel.TUESDAY));

            CheckBox checkBoxWednesday = (CheckBox) findViewById(R.id.alarmDetailsWednesday);
            checkBoxWednesday.setChecked(alarmDetails.getRepeatingDay(AlarmModel.WEDNESDAY));

            CheckBox checkBoxThursday = (CheckBox) findViewById(R.id.alarmDetailsThursday);
            checkBoxThursday.setChecked(alarmDetails.getRepeatingDay(AlarmModel.THURSDAY));

            CheckBox checkBoxFriday = (CheckBox) findViewById(R.id.alarmDetailsFriday);
            checkBoxFriday.setChecked(alarmDetails.getRepeatingDay(AlarmModel.FRIDAY));

            CheckBox checkBoxSaturday = (CheckBox) findViewById(R.id.alarmDetailsSaturday);
            checkBoxSaturday.setChecked(alarmDetails.getRepeatingDay(AlarmModel.SATURDAY));

            if (alarmDetails.getAlarmTone() != null) {
                TextView selectionText = (TextView) findViewById(R.id.alarmDetailsToneSelection);
                selectionText.setText(
                        RingtoneManager.getRingtone(this, alarmDetails.getAlarmTone())
                                .getTitle(this));
            }
        }
    }

    private void populateAlarmDetails() {
        // Grab the time from the timepicker
        TimePicker timePicker = (TimePicker) findViewById(R.id.alarmDetailsTimePicker);
        alarmDetails.setTimeMinute(timePicker.getCurrentMinute());
        alarmDetails.setTimeHour(timePicker.getCurrentHour());

        EditText alarmNameEditText = (EditText) findViewById(R.id.alarmDetailsName);
        alarmDetails.setName(alarmNameEditText.getText().toString());

        CheckBox checkBoxWeekly = (CheckBox) findViewById(R.id.alarmDetailsRepeatWeekly);
        alarmDetails.setRepeatWeekly(checkBoxWeekly.isChecked());

        // Copy in all the boolean values for playing on various days
        CheckBox checkBoxSunday = (CheckBox) findViewById(R.id.alarmDetailsSunday);
        alarmDetails.setRepeatingDay(AlarmModel.SUNDAY, checkBoxSunday.isChecked());

        CheckBox checkBoxMonday = (CheckBox) findViewById(R.id.alarmDetailsMonday);
        alarmDetails.setRepeatingDay(AlarmModel.MONDAY, checkBoxMonday.isChecked());

        CheckBox checkBoxTuesday = (CheckBox) findViewById(R.id.alarmDetailsTuesday);
        alarmDetails.setRepeatingDay(AlarmModel.TUESDAY, checkBoxTuesday.isChecked());

        CheckBox checkBoxWednesday = (CheckBox) findViewById(R.id.alarmDetailsWednesday);
        alarmDetails.setRepeatingDay(AlarmModel.WEDNESDAY, checkBoxWednesday.isChecked());

        CheckBox checkBoxThursday = (CheckBox) findViewById(R.id.alarmDetailsThursday);
        alarmDetails.setRepeatingDay(AlarmModel.THURSDAY, checkBoxThursday.isChecked());

        CheckBox checkBoxFriday = (CheckBox) findViewById(R.id.alarmDetailsFriday);
        alarmDetails.setRepeatingDay(AlarmModel.FRIDAY, checkBoxFriday.isChecked());

        CheckBox checkBoxSaturday = (CheckBox) findViewById(R.id.alarmDetailsSaturday);
        alarmDetails.setRepeatingDay(AlarmModel.SATURDAY, checkBoxSaturday.isChecked());

        alarmDetails.setEnabled(true);
    }

    public void saveAlarmDetails(View view) {
        populateAlarmDetails();

        // Cancel all pending alarms
        AlarmManagerHelper.cancelAlarms(this);

        // Save the alarm to the database
        AlarmDBHelper dbHelper = new AlarmDBHelper(this);
        if (alarmDetails.getId() < 0) {
            dbHelper.createAlarm(alarmDetails);
        } else {
            dbHelper.updateAlarm(alarmDetails);
        }

        // Set all of the alarms again
        AlarmManagerHelper.setAlarms(this);

        // End the activity
        setResult(RESULT_OK);
        super.finish();
    }

    public void openRingtonePicker(View view) {
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        startActivityForResult(intent, RINGTONE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == RINGTONE_REQUEST) {
                alarmDetails.setAlarmTone(
                       (Uri) data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI));

                TextView selectionText = (TextView) findViewById(R.id.alarmDetailsToneSelection);
                selectionText.setText(RingtoneManager.getRingtone(
                        this, alarmDetails.getAlarmTone()).getTitle(this));

            }
        }
    }
}
