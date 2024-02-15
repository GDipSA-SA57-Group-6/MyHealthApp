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
        if ((int) value >= 0 && (int) value < dates.length) {
            String fullDate = dates[(int) value];
            // 假设日期格式为 "yyyy-MM-dd"
            String[] dateParts = fullDate.split("-");
            return dateParts[1] + "-" + dateParts[2]; // 返回 "MM-dd" 格式的日期
        }
        return "";
    }
}
