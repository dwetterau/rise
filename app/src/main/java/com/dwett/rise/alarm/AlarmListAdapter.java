package com.dwett.rise.alarm;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.dwett.rise.MainActivity;
import com.dwett.rise.R;

import java.util.List;

/**
 * @author david
 */
public class AlarmListAdapter extends BaseAdapter {
    private Context context;
    private List<AlarmModel> alarmModelList;

    public AlarmListAdapter(Context context, List<AlarmModel> alarmModelList) {
        this.context = context;
        this.alarmModelList = alarmModelList;
    }

    public void setAlarmModelList(List<AlarmModel> alarmModelList) {
        this.alarmModelList = alarmModelList;
    }

    @Override
    public int getCount() {
        return this.alarmModelList.size();
    }

    @Override
    public Object getItem(int position) {
        return this.alarmModelList.get(position);
    }

    @Override
    public long getItemId(int position) {
        if (position < 0 || position >= alarmModelList.size()) {
            return -1;
        }
        return this.alarmModelList.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater =
                    (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.alarm_list_item, parent, false);
        }
        AlarmModel model = (AlarmModel) this.getItem(position);
        TextView textTime = (TextView) convertView.findViewById(R.id.alarmItemTime);
        int h = model.getTimeHour();
        int m = model.getTimeMinute();
        String period;
        if (h < 12) {
            period = "am";
        } else {
            period = "pm";
        }
        textTime.setText(String.format("%d : %02d %s", h % 12, m, period));

        TextView textName = (TextView) convertView.findViewById(R.id.alarmItemName);
        textName.setText(model.getName());

        int [] itemIds = {
                R.id.alarmItemSunday, R.id.alarmItemMonday, R.id.alarmItemTuesday,
                R.id.alarmItemWednesday, R.id.alarmItemThursday, R.id.alarmItemFriday,
                R.id.alarmItemSaturday
        };
        int [] dayIndices = {
                AlarmModel.SUNDAY, AlarmModel.MONDAY, AlarmModel.TUESDAY, AlarmModel.WEDNESDAY,
                AlarmModel.THURSDAY, AlarmModel.FRIDAY, AlarmModel.SATURDAY
        };
        for (int i = 0; i < itemIds.length; i++) {
            updateTextColor(
                    (TextView) convertView.findViewById(itemIds[i]),
                    model.getRepeatingDay(dayIndices[i])
            );
        }

        // Make the toggle update the model in the database
        ToggleButton toggleButton = (ToggleButton) convertView.findViewById(R.id.alarmItemToggle);
        toggleButton.setTag(model.getId());
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ((MainActivity) context).setAlarmEnabled(((Long) buttonView.getTag()), isChecked);
            }
        });
        toggleButton.setChecked(model.isEnabled());

        // Make clicking anywhere else on the view start the edit details activity
        convertView.setTag(model.getId());
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) context).editAlarmDetails((Long) v.getTag());
            }
        });

        return convertView;
    }

    /**
     * Sets the color of a day to white (if on) or dark gray
     * @param view the textview of the day letter
     * @param isOn whether or not the alarm repeats for the day
     */
    private void updateTextColor(TextView view, boolean isOn) {
        if (isOn) {
            view.setTextColor(Color.WHITE);
        } else {
            view.setTextColor(Color.DKGRAY);
        }
    }

    @Override
    public boolean isEmpty() {
        return this.alarmModelList.isEmpty();
    }
}
