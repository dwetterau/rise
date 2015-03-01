package com.dwett.rise.alarm;

import android.net.Uri;

/**
 * @author david
 */
public class AlarmModel {

    public static final int SUNDAY = 0;
    public static final int MONDAY = 1;
    public static final int TUESDAY = 2;
    public static final int WEDNESDAY = 3;
    public static final int THURSDAY = 4;
    public static final int FRIDAY = 5;
    public static final int SATURDAY = 6;

    private long id;
    private int timeHour;
    private int timeMinute;
    private boolean [] repeatingDays;
    private boolean repeatWeekly;
    private Uri alarmTone;
    private String name;
    private boolean isEnabled;

    public AlarmModel() {
        repeatingDays = new boolean[7];
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getTimeHour() {
        return timeHour;
    }

    public void setTimeHour(int timeHour) {
        this.timeHour = timeHour;
    }

    public int getTimeMinute() {
        return timeMinute;
    }

    public void setTimeMinute(int timeMinute) {
        this.timeMinute = timeMinute;
    }

    public boolean isRepeatWeekly() {
        return repeatWeekly;
    }

    public void setRepeatWeekly(boolean repeatWeekly) {
        this.repeatWeekly = repeatWeekly;
    }

    public Uri getAlarmTone() {
        return alarmTone;
    }

    public void setAlarmTone(Uri alarmTone) {
        this.alarmTone = alarmTone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public void setRepeatingDay(int day, boolean value) {
        repeatingDays[day] = value;
    }

    public boolean getRepeatingDay(int day) {
        return repeatingDays[day];
    }
}
