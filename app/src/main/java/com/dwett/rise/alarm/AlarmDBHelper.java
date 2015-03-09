package com.dwett.rise.alarm;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import com.dwett.rise.alarm.AlarmContract.Alarm;

import java.util.ArrayList;
import java.util.List;

/**
 * @author david
 */
public class AlarmDBHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "rise.db";

    private static final String SQL_CREATE_ALARM_TABLE =
            "CREATE TABLE " + Alarm.TABLE_NAME + "(" +
            Alarm._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            Alarm.COLUMN_NAME_ALARM_NAME + " TEXT," +
            Alarm.COLUMN_NAME_ALARM_TIME_HOUR + " INTEGER," +
            Alarm.COLUMN_NAME_ALARM_TIME_MINUTE + " INTEGER," +
            Alarm.COLUMN_NAME_ALARM_REPEAT_DAYS + " TEXT," +
            Alarm.COLUMN_NAME_ALARM_REPEAT_WEEKLY + " BOOLEAN," +
            Alarm.COLUMN_NAME_ALARM_TONE + " TEXT," +
            Alarm.COLUMN_NAME_ALARM_ENABLED + " BOOLEAN)";

    private static final String SQL_DROP_ALARM_TABLE = "DROP TABLE IF EXISTS " +
            Alarm.TABLE_NAME;

    public AlarmDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ALARM_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DROP_ALARM_TABLE);
        this.onCreate(db);
    }

    private AlarmModel populateModel(Cursor c) {
        AlarmModel model = new AlarmModel();
        model.setId(c.getLong(c.getColumnIndex(Alarm._ID)));
        model.setName(c.getString(c.getColumnIndex(Alarm.COLUMN_NAME_ALARM_NAME)));
        model.setTimeHour(c.getInt(c.getColumnIndex(Alarm.COLUMN_NAME_ALARM_TIME_HOUR)));
        model.setTimeMinute(c.getInt(c.getColumnIndex(Alarm.COLUMN_NAME_ALARM_TIME_MINUTE)));
        model.setRepeatWeekly(
                c.getInt(c.getColumnIndex(Alarm.COLUMN_NAME_ALARM_REPEAT_WEEKLY)) != 0
        );
        String alarmToneString = c.getString(c.getColumnIndex(Alarm.COLUMN_NAME_ALARM_TONE));
        if (alarmToneString.length() > 0) {
            model.setAlarmTone(Uri.parse(alarmToneString));
        } else {
            model.setAlarmTone(null);
        }
        model.setEnabled(c.getInt(c.getColumnIndex(Alarm.COLUMN_NAME_ALARM_ENABLED)) != 0);

        String [] days =
                c.getString(c.getColumnIndex(Alarm.COLUMN_NAME_ALARM_REPEAT_DAYS)).split(",");
        for (int i = 0; i < days.length; i++) {
            model.setRepeatingDay(i, !days[i].equals("false"));
        }
        return model;
    }

    private ContentValues populateContent(AlarmModel model) {
        ContentValues values = new ContentValues();
        values.put(Alarm.COLUMN_NAME_ALARM_NAME, model.getName());
        values.put(Alarm.COLUMN_NAME_ALARM_TIME_HOUR, model.getTimeHour());
        values.put(Alarm.COLUMN_NAME_ALARM_TIME_MINUTE, model.getTimeMinute());
        values.put(Alarm.COLUMN_NAME_ALARM_REPEAT_WEEKLY, model.isRepeatWeekly());
        String alarmTone = model.getAlarmTone() == null ? "" : model.getAlarmTone().toString();
        values.put(Alarm.COLUMN_NAME_ALARM_TONE, alarmTone);

        String days = "";
        for (int i = 0; i < 7; i++) {
            days += model.getRepeatingDay(i) + ",";
        }
        values.put(Alarm.COLUMN_NAME_ALARM_REPEAT_DAYS, days);
        values.put(Alarm.COLUMN_NAME_ALARM_ENABLED, model.isEnabled());
        return values;
    }

    /**
     * Create an alarm based on this model object.
     * @param model an alarm with all fields set (except id)
     * @return the ID of the newly inserted row, or -1 if an error occurred
     */
    public long createAlarm(AlarmModel model) {
        ContentValues values = populateContent(model);
        return getWritableDatabase().insert(Alarm.TABLE_NAME, null, values);
    }

    /**
     * Returns the model with the specified id or null if it isn't found
     * @param id the id of the model to request
     * @return the model with the specified id or null
     */
    public AlarmModel getAlarm(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String select = "SELECT * FROM " + Alarm.TABLE_NAME + " WHERE " + Alarm._ID + " = " + id;
        Cursor c = db.rawQuery(select, null);

        if (c.moveToNext()) {
            return populateModel(c);
        }
        return null;
    }

    /**
     * Updates the model in the database
     * @param model the model object with fields to update set
     * @return the number of row affected by the update
     */
    public int updateAlarm(AlarmModel model) {
        ContentValues values = populateContent(model);
        return this.getWritableDatabase().update(
                Alarm.TABLE_NAME, values,
                Alarm._ID + " = ?", new String [] {String.valueOf(model.getId())}
        );
    }

    /**
     * Deletes the alarm with the specified id from the database
     * @param id the id of the alarm to delete
     * @return the number of rows affected by the delete
     */
    public int deleteAlarm(long id) {
        return this.getWritableDatabase().delete(
                Alarm.TABLE_NAME, Alarm._ID + " = ?", new String [] {String.valueOf(id)}
        );
    }

    /**
     * Gets all of the alarms in the db
     * @return a list of all the alarms in the database (empty if there are none)
     */
    public List<AlarmModel> getAllAlarms() {
        SQLiteDatabase db = this.getReadableDatabase();
        String select = "SELECT * FROM " + Alarm.TABLE_NAME;
        Cursor c = db.rawQuery(select, null);

        List<AlarmModel> alarmList = new ArrayList<>();
        while (c.moveToNext()) {
            alarmList.add(this.populateModel(c));
        }

        return alarmList;
    }
}
