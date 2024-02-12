package iss.AD.myhealthapp;

import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.components.AxisBase;

public class DateValueFormatter extends ValueFormatter {
    private String[] dates; // 日期字符串数组

    public DateValueFormatter(String[] dates) {
        this.dates = dates;
    }

    @Override
    public String getAxisLabel(float value, AxisBase axis) {
        int index = Math.round(value);
        if (index >= 0 && index < dates.length) {
            return dates[index];
        } else {
            return "";
        }
    }
}
