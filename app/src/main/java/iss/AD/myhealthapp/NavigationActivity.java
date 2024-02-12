package iss.AD.myhealthapp;

// MainActivity.java


import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class NavigationActivity extends AppCompatActivity {

    private Button btnHeartDisease;
    private Button btnDiabetes;
    private LocalDate baseDate = LocalDate.parse("2024-01-01", DateTimeFormatter.ISO_LOCAL_DATE);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        btnHeartDisease = findViewById(R.id.btnHeartDisease);
        btnDiabetes = findViewById(R.id.btnDiabetes);

        btnHeartDisease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 启动心脏病预测Activity
                Intent intent = new Intent(NavigationActivity.this, HeartDiseaseMainActivity.class);
                startActivity(intent);
            }
        });

        btnDiabetes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 启动糖尿病预测Activity
                Intent intent = new Intent(NavigationActivity.this, DiabetesMainActivity.class);
                startActivity(intent);
            }
        });
        fetchLastSevenHeartDiseasePredictions();
        fetchLastSevenDiabetesPredictions();
    }
    private int getUserIdFromPreferences() {
        SharedPreferences pref = getSharedPreferences("user_credentials", MODE_PRIVATE);
        // 默认值-1表示用户ID不存在
        return pref.getInt("userId", -1);
    }

    private void fetchLastSevenHeartDiseasePredictions() {
        int userId = getUserIdFromPreferences();
        if (userId == -1) {
            return;
        }
        String url = "http://192.168.1.98:8080/api/heartDiseasePredictionsLastSevenDays?userId=" + userId;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // 在这里处理请求失败的情况，例如通过日志记录错误信息
                Log.e("HTTP_ERROR", "Request failed", e);
                // 可能还需要更新UI或通知用户，确保这些操作在主线程上执行
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 在这里更新UI或通知用户
                    }
                });
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        JSONArray jsonArray = new JSONArray(responseData);
                        List<Entry> entries = new ArrayList<>();
                        String[] dates = new String[jsonArray.length()]; // 日期字符串数组

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            JSONObject idObject = jsonObject.getJSONObject("id"); // 获取嵌套的 "id" 对象
                            String date = idObject.getString("date"); // 从嵌套的 "id" 对象中获取日期
                            float probability = (float) jsonObject.getDouble("predictionProbability");
                            dates[i] = date;
                            entries.add(new Entry(i, probability));

                        }
                        // 使用日期字符串数组作为X轴标签
                        runOnUiThread(() -> showHeartDiseaseChart(entries, dates));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else {
                    throw new IOException("Unexpected code " + response);
                }
            }
        });
    }
    private void fetchLastSevenDiabetesPredictions() {
        int userId = getUserIdFromPreferences();
        if (userId == -1) {
            return;
        }
        String url = "http://192.168.1.98:8080/api/diabetesPredictionsLastSevenDays?userId=" + userId;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // 在这里处理请求失败的情况，例如通过日志记录错误信息
                Log.e("HTTP_ERROR", "Request failed", e);
                // 可能还需要更新UI或通知用户，确保这些操作在主线程上执行
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 在这里更新UI或通知用户
                    }
                });
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        JSONArray jsonArray = new JSONArray(responseData);
                        List<Entry> entries = new ArrayList<>();
                        String[] dates = new String[jsonArray.length()]; // 日期字符串数组

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            JSONObject idObject = jsonObject.getJSONObject("id"); // 获取嵌套的 "id" 对象
                            String date = idObject.getString("date"); // 从嵌套的 "id" 对象中获取日期
                            float probability = (float) jsonObject.getDouble("predictionProbability");
                            dates[i] = date;
                            entries.add(new Entry(i, probability));

                        }
                        // 使用日期字符串数组作为X轴标签
                        runOnUiThread(() -> showDiabetesChart(entries, dates));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else {
                    throw new IOException("Unexpected code " + response);
                }
            }
        });
    }


    private void showHeartDiseaseChart(List<Entry> entries, String[] dates) {
        LineChart chart = findViewById(R.id.heartDiseaseChart);
        LineDataSet dataSet = new LineDataSet(entries, "probability of heart disease");
        // 配置数据集...
        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        // 设置自定义的X轴标签格式化器
        chart.getXAxis().setValueFormatter(new DateValueFormatter(dates));

        chart.invalidate();

        chart.getXAxis().setDrawGridLines(false); // 隐藏X轴的网格线
        chart.getAxisLeft().setDrawGridLines(false); // 隐藏左侧Y轴的网格线
        chart.getAxisRight().setDrawGridLines(false); // 隐藏右侧Y轴的网格线

    }
    private void showDiabetesChart(List<Entry> entries, String[] dates) {
        LineChart chart = findViewById(R.id.diabetesChart);
        LineDataSet dataSet = new LineDataSet(entries, "probability of diabetes");
        // 配置数据集...
        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        // 设置自定义的X轴标签格式化器
        chart.getXAxis().setValueFormatter(new DateValueFormatter(dates));

        chart.invalidate();

        chart.getXAxis().setDrawGridLines(false); // 隐藏X轴的网格线
        chart.getAxisLeft().setDrawGridLines(false); // 隐藏左侧Y轴的网格线
        chart.getAxisRight().setDrawGridLines(false); // 隐藏右侧Y轴的网格线

    }



}
