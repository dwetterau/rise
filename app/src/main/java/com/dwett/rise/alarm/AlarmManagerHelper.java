package com.dwett.rise.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Calendar;
import java.util.List;

/**
 * @author david
 */
public class AlarmManagerHelper extends BroadcastReceiver {

    // The string attribute names for creating the alarm service
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String TIME_HOUR = "timeHour";
    public static final String TIME_MINUTE = "timeMinute";
    public static final String TONE = "alarmTone";

    @Override
    public void onReceive(Context context, Intent intent) {
        setAlarms(context);
    }

    public static void setAlarms(Context context) {
        // Cancel all the old alarms
        cancelAlarms(context);

        AlarmDBHelper dbHelper = new AlarmDBHelper(context);
        List<AlarmModel> alarmModelList = dbHelper.getAllAlarms();

        for (AlarmModel alarmModel : alarmModelList) {
            PendingIntent pendingIntent = createPendingIntent(context, alarmModel);

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, alarmModel.getTimeHour());
            calendar.set(Calendar.MINUTE, alarmModel.getTimeMinute());
            calendar.set(Calendar.SECOND, 0);

            Calendar calendarInstance = Calendar.getInstance();
            final int nowDay = calendarInstance.get(Calendar.DAY_OF_WEEK);
            final int nowHour = calendarInstance.get(Calendar.HOUR_OF_DAY);
            final int nowMinute = calendarInstance.get(Calendar.MINUTE);
            boolean alarmSet = false;

            // See if the alarm is later in the week
            for (int day = Calendar.SUNDAY; day <= Calendar.SATURDAY; day++) {
                if (alarmModel.getRepeatingDay(day - 1) && day >= nowDay &&
                        !(day == nowDay && alarmModel.getTimeHour() < nowHour) &&
                        !(day == nowDay && alarmModel.getTimeHour() == nowHour &&
                        alarmModel.getTimeMinute() <= nowMinute)) {

                    calendar.set(Calendar.DAY_OF_WEEK, day);
                    AlarmManagerHelper.setAlarm(context, calendar, pendingIntent);
                    alarmSet = true;
                    break;
                }
            }

            // Otherwise the alarm is earlier in the week
            if (!alarmSet) {
                for (int day = Calendar.SUNDAY; day <= Calendar.SATURDAY; day++) {
                    if (alarmModel.getRepeatingDay(day - 1) && day <= nowDay &&
                            alarmModel.isRepeatWeekly()) {
                        calendar.set(Calendar.DAY_OF_WEEK, day);
                        calendar.add(Calendar.WEEK_OF_YEAR, 1);

                        setAlarm(context, calendar, pendingIntent);
                        break;
                    }
                }
            }
        }
    }

    private static void setAlarm(Context context, Calendar calendar, PendingIntent pendingIntent) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }

    public static void cancelAlarms(Context context) {
        AlarmDBHelper dbHelper = new AlarmDBHelper(context);
        List<AlarmModel> alarmModelList = dbHelper.getAllAlarms();

        for (AlarmModel alarmModel : alarmModelList) {
            if (alarmModel.isEnabled()) {
                PendingIntent pendingIntent = createPendingIntent(context, alarmModel);
                AlarmManager alarmManager =
                        (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarmManager.cancel(pendingIntent);
            }
        }
    }

    public static PendingIntent createPendingIntent(Context context, AlarmModel model) {
        Intent intent = new Intent(context, AlarmService.class);
        intent.putExtra(ID, model.getId());
        intent.putExtra(NAME, model.getName());
        intent.putExtra(TIME_HOUR, model.getTimeHour());
        intent.putExtra(TIME_MINUTE, model.getTimeMinute());
        intent.putExtra(TONE, model.getAlarmTone().toString());

        return PendingIntent.getService(
                context, (int) model.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

}
