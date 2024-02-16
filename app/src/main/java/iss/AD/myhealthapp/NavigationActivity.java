package iss.AD.myhealthapp;

// MainActivity.java


import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class NavigationActivity extends AppCompatActivity {

    private Button btnHeartDisease;
    private Button btnDiabetes;
    private LocalDate baseDate = LocalDate.parse("2024-01-01", DateTimeFormatter.ISO_LOCAL_DATE);
    private RecyclerView videoRecyclerView;
    private VideoAdapter videoAdapter;
    private List<VideoInfo> videoList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        Button mBtnReturn = findViewById(R.id.btnReturn);
        mBtnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start CreateUserActivity when the button is clicked
                Intent intent = new Intent(NavigationActivity.this, Dashboard.class);
                startActivity(intent);
            }
        });

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
        videoRecyclerView = findViewById(R.id.videoRecyclerView);
        videoRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 假设您已经有了视频列表 videoList，可能是通过网络请求获取的
        videoAdapter = new VideoAdapter(this, videoList);
        videoRecyclerView.setAdapter(videoAdapter);

        // 假设这个方法是您用来获取视频列表并更新 RecyclerView 的


    }
    @Override
    protected void onResume() {
        super.onResume();

        fetchLastSevenHeartDiseasePredictions();
        fetchLastSevenDiabetesPredictions();
        fetchLatestHeartDiseasePrediction();
        fetchLatestDiabetesPrediction();
        fetchVideos();
    }
    private void fetchVideos() {
        int userId = getUserIdFromPreferences();
        if (userId == -1) {
            return;
        }
        TextView tvVideoRecommendation = findViewById(R.id.tvVideoRecommendation);
        // 检查用户是否健康
        checkUserHealth(userId, isHealthy -> {
            if (isHealthy) {
                // 用户健康，不推荐视频
                runOnUiThread(() -> {
                    videoList.clear();
                    videoAdapter.notifyDataSetChanged();
                    tvVideoRecommendation.setVisibility(View.GONE);
                });
            } else {
                // 用户不健康，根据用户类型获取视频
                getUserType(userId, userType -> {
                    if (userType != null && userType > 0) {
                        getVideosByType(userType, videos -> {
                            videoList.clear();
                            if (videos != null) {
                                videoList.addAll(videos);
                                // 在主线程更新 UI
                                runOnUiThread(() -> {
                                    videoAdapter.notifyDataSetChanged();
                                    tvVideoRecommendation.setVisibility(View.VISIBLE);
                                });
                            }
                        });
                    }
                });
            }
        });
    }

    private void checkUserHealth(int userId, Consumer<Boolean> callback) {
        // 同时获取心脏病和糖尿病的最新预测结果
        String heartDiseaseUrl = "http://" + getResources().getString(R.string.local_host) + ":8080/api/latestHeartDiseasePredictionClass?userId=" + userId;
        String diabetesUrl = "http://" + getResources().getString(R.string.local_host) + ":8080/api/latestDiabetesPredictionClass?userId=" + userId;

        OkHttpClient client = new OkHttpClient();
        Request heartDiseaseRequest = new Request.Builder().url(heartDiseaseUrl).build();
        Request diabetesRequest = new Request.Builder().url(diabetesUrl).build();

        client.newCall(heartDiseaseRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("HTTP_ERROR", "Request failed", e);
                callback.accept(false);
            }

            @Override
            public void onResponse(Call call, Response heartDiseaseResponse) throws IOException {
                if (heartDiseaseResponse.isSuccessful()) {
                    int heartDiseasePredictionClass = Integer.parseInt(heartDiseaseResponse.body().string());
                    client.newCall(diabetesRequest).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.e("HTTP_ERROR", "Request failed", e);
                            callback.accept(false);
                        }

                        @Override
                        public void onResponse(Call call, Response diabetesResponse) throws IOException {
                            if (diabetesResponse.isSuccessful()) {
                                int diabetesPredictionClass = Integer.parseInt(diabetesResponse.body().string());
                                // 如果两种病的预测结果都是 0，则认为用户健康
                                callback.accept(heartDiseasePredictionClass == 0 && diabetesPredictionClass == 0);
                            } else {
                                callback.accept(false);
                            }
                        }
                    });
                } else {
                    callback.accept(false);
                }
            }
        });
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
        String local_host = getResources().getString(R.string.local_host);
        String url = "http://" + local_host + ":8080/api/heartDiseasePredictionsLastSevenDays?userId=" + userId;
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
                            dates[jsonArray.length() - 1 - i] = date;
                            entries.add(new Entry(jsonArray.length() - 1 - i, probability));

                        }
                        Collections.sort(entries, new EntryXComparator());
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
        String local_host = getResources().getString(R.string.local_host);
        String url = "http://" + local_host + ":8080/api/diabetesPredictionsLastSevenDays?userId=" + userId;
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
                            dates[jsonArray.length() - 1 - i] = date;
                            entries.add(new Entry(jsonArray.length() - 1 - i, probability));

                        }
                        Collections.sort(entries, new EntryXComparator());
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

    private void fetchLatestHeartDiseasePrediction() {
        int userId = getUserIdFromPreferences();
        if (userId == -1) {
            return;
        }
        String url = "http://" + getResources().getString(R.string.local_host) + ":8080/api/latestHeartDiseasePredictionClass?userId=" + userId;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("HTTP_ERROR", "Request failed", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    int predictionClass = Integer.parseInt(responseData);
                    runOnUiThread(() -> {
                        TextView heartDiseasePredictionResult = findViewById(R.id.heartDiseasePredictionResult);
                        if (predictionClass == 1) {
                            heartDiseasePredictionResult.setText("You are highly likely to get heart diseases.");
                        } else {
                            heartDiseasePredictionResult.setText("You are not likely to get heart diseases");
                        }
                    });
                }
            }
        });
    }
    private void fetchLatestDiabetesPrediction() {
        int userId = getUserIdFromPreferences();
        if (userId == -1) {
            return;
        }
        String url = "http://" + getResources().getString(R.string.local_host) + ":8080/api/latestDiabetesPredictionClass?userId=" + userId;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("HTTP_ERROR", "Request failed", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    int predictionClass = Integer.parseInt(responseData);
                    runOnUiThread(() -> {
                        TextView diabetesPredictionResult = findViewById(R.id.diabetesPredictionResult);
                        if (predictionClass == 1) {
                            diabetesPredictionResult.setText("You are highly likely to get diabetes.");
                        } else {
                            diabetesPredictionResult.setText("You are not likely to get diabetes");
                        }
                    });
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

    private void getUserType(int userId, final Consumer<Integer> callback) {
        OkHttpClient client = new OkHttpClient();


        String local_host = getResources().getString(R.string.local_host);
        String url = "http://" + local_host + ":8080/video/path/" + userId;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                callback.accept(null);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    int userType = Integer.parseInt(response.body().string());
                    callback.accept(userType);
                } else {
                    callback.accept(null);
                }
            }
        });
    }
    private void getVideosByType(int type, final Consumer<List<VideoInfo>> callback) {
        OkHttpClient client = new OkHttpClient();

        String local_host = getResources().getString(R.string.local_host);
        String url = "http://" + local_host + ":8080/video/videos/" + type;
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                callback.accept(Collections.emptyList());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    List<VideoInfo> videos = new Gson().fromJson(responseBody, new TypeToken<List<VideoInfo>>(){}.getType());
                    callback.accept(videos);
                } else {
                    callback.accept(Collections.emptyList());
                }
            }
        });
    }





}
