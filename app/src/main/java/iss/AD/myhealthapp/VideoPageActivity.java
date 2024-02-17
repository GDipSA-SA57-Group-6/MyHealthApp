package iss.AD.myhealthapp;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.SharedPreferences;
import android.widget.Toast;
import android.content.Context;
import androidx.preference.PreferenceManager;
import android.util.Log;
import android.content.Intent;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;





import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import iss.AD.myhealthapp.network.AdvApiService;
import iss.AD.myhealthapp.network.VideoApiService;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VideoPageActivity extends AppCompatActivity {
    private List<VideoInfo> videoList = new ArrayList<>();
    private VideoAdapter adapter;
    private RecyclerView recyclerView;
    private VideoApiService apiService;
    private AdvApiService AdvService;
    private Dialog adDialog;
    private List<AdvInfo> advList = new ArrayList<>();
    private boolean adDisplayed = false;
    private Runnable showAdRunnable;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);




//        Intent intent = getIntent();
//        if (intent != null && intent.hasExtra("videoList")) {
//            List<VideoInfo> videoList = intent.getParcelableArrayListExtra("videoList");
//            // 初始化RecyclerView
//            RecyclerView recyclerView = findViewById(R.id.recyclerView);
//            recyclerView.setLayoutManager(new LinearLayoutManager(this));
//
//            // 初始化适配器
//            VideoAdapter adapter = new VideoAdapter(this, videoList);
//            recyclerView.setAdapter(adapter);
//        }
        SharedPreferences prefs = getSharedPreferences("user_credentials", MODE_PRIVATE);
        int userId = prefs.getInt("userId", -1); // 默认值为-1表示未找到
        Log.d("VideoPageActivity", "User ID from SharedPreferences: " + userId);


        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new VideoAdapter(this, new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // 初始化Retrofit和API服务
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/") // 使用本地地址进行测试
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        AdvService = retrofit.create(AdvApiService.class);
        apiService = retrofit.create(VideoApiService.class);

        // 调用getVideos获取所有视频
        apiService.getVideos().enqueue(new Callback<List<VideoInfo>>() {
            @Override
            public void onResponse(Call<List<VideoInfo>> call, Response<List<VideoInfo>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // 更新RecyclerView的适配器数据
                    adapter.updateVideoList(response.body());
                } else {
                    Toast.makeText(VideoPageActivity.this, "Failed to fetch videos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<VideoInfo>> call, Throwable t) {
                Toast.makeText(VideoPageActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });


        //下拉栏
        Spinner typeSpinner = findViewById(R.id.typeSpinner);
        String[] videoTypes = {"All Videos", "Healthy Lose Weight", "Healthy Maintain Weight", "Healthy Gain Muscle", "Heart Disease", "Diabetes", "Heart Disease and Diabetes"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, videoTypes);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(spinnerAdapter);

        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d("VideoPageActivity", "Spinner item selected: position " + position);
                // 基于选择的类别过滤视频列表
                if (position == 0) {
                    fetchAllVideos();
                    fetchAdvByType(userId);
                } else {
                    fetchVideosByType(position);
                    fetchAdvByType(userId);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

//        // 从SharedPreferences获取用户ID
//        SharedPreferences prefs = getSharedPreferences("user_credentials", MODE_PRIVATE);
//        int userId = prefs.getInt("userId", -1); // 默认值为-1表示未找到
//        Log.d("VideoPageActivity", "User ID from SharedPreferences: " + userId);
//
//        if (userId == -1) {
//            // 用户未登录，显示提示并返回
//            Toast.makeText(this, "No recommend videos.", Toast.LENGTH_LONG).show();
//            finish(); // 结束当前Activity，可能需要跳转到登录页面
//        } else {
//            // 用户已登录，根据用户ID获取推荐视频
//            fetchRecommendedVideos(String.valueOf(userId));
//            fetchAdvByType(userId);
//        }

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                showAd();
            }
        }, 3000);

    }

    private void fetchRecommendedVideos(String userId) {
        Call<Integer> recommendCall = apiService.recommendVideo(Integer.parseInt(userId));

        recommendCall.enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.isSuccessful()) {
                    Integer type = response.body();
                    if (type != null) {
                        fetchVideosByType(type);
                    } else {
                        Toast.makeText(VideoPageActivity.this, "Failed to get video recommendations.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                Toast.makeText(VideoPageActivity.this, "Network error.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchVideosByType(int type) {
        Call<List<VideoInfo>> videosCall = apiService.getVideosByType(type);

        videosCall.enqueue(new Callback<List<VideoInfo>>() {
            @Override
            public void onResponse(Call<List<VideoInfo>> call, Response<List<VideoInfo>> response) {
                if (response.isSuccessful()) {
                    Log.d("VideoPageActivity", "Received videos for type " + type + ": " + response.body().size() + " videos");
                    videoList.clear();
                    videoList.addAll(response.body());
                    adapter.updateVideoList(videoList);
//                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(VideoPageActivity.this, "Error fetching videos.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<VideoInfo>> call, Throwable t) {
                Toast.makeText(VideoPageActivity.this, "Network error.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void fetchAdvByType(int userId) {
        Call<List<AdvInfo>> advCall = AdvService.getAdvertisements(userId);

        advCall.enqueue(new Callback<List<AdvInfo>>() {
            @Override
            public void onResponse(Call<List<AdvInfo>> call, Response<List<AdvInfo>> response) {
                if (response.isSuccessful()) {
                    advList.clear();
                    advList.addAll(response.body());
                } else {
                    Toast.makeText(VideoPageActivity.this, "Error fetching adv.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<AdvInfo>> call, Throwable t) {
                Toast.makeText(VideoPageActivity.this, "Network error.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showAd() {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        if (advList == null || advList.isEmpty()) {
            Log.d("VideoPageActivity", "No advertisements available.");
            // 这里可以处理没有广告的情况，比如显示默认广告或者不显示广告
            return;
        }

        Log.d("VideoPageActivity","advList size before random selection:" + advList.size());

        LayoutInflater inflater = LayoutInflater.from(this);
        View adView = inflater.inflate(R.layout.activity_video, null);


        ImageView adImageView = adView.findViewById(R.id.adImageView);
        TextView adDescriptionTextView = adView.findViewById(R.id.adDescriptionTextView);

        Random random = new Random();
        int randomIndex = random.nextInt(advList.size());
        AdvInfo randomAdv = advList.get(randomIndex);
        String imageDataUrl = randomAdv.getAdvUrl();

//        Log.d("VideoPageActivity","advList size before random selection:" + imageDataUrl);
//        byte[] imageData = Base64.decode(imageDataUrl.split(",")[1], Base64.DEFAULT);

        if (imageDataUrl != null && imageDataUrl.contains(",")) {
            byte[] imageData = Base64.decode(imageDataUrl.split(",")[1], Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
            adImageView.setImageBitmap(bitmap);
            adDescriptionTextView.setText(randomAdv.getDescription());
        } else {
            Log.d("VideoPageActivity", "Adv URL format incorrect or null");
            // 可以设置一个默认图片或进行其他处理
        }

//        Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
//
//        adImageView.setImageBitmap(bitmap);
//        adDescriptionTextView.setText("这里是广告的描述信息，可以是一段文字。"); // 替换为你的广告描述
        adDescriptionTextView.setText(randomAdv.getDescription());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Advertisement");
        builder.setView(adView);

        // 设置点击对话框外部区域不关闭对话框
        builder.setCancelable(false);

        Runnable showAdRunnable = new Runnable() {
            @Override
            public void run() {
                showAd();
            }
        };

        //关闭广告对话框后重新计时，计时结束后再次加载广告
        builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dismissAd();
                Handler handler = new Handler();
                handler.postDelayed(showAdRunnable, 5000);
            }
        });

        // 创建广告对话框
        adDialog = builder.create();

        adDialog.show();
    }

    private void dismissAd() {
        if (adDialog != null && adDialog.isShowing()) {
            adDialog.dismiss();
        }
    }

    private void fetchAllVideos() {
        Call<List<VideoInfo>> call = apiService.getVideos(); // 使用之前定义的getVideos()方法

        call.enqueue(new Callback<List<VideoInfo>>() {
            @Override
            public void onResponse(Call<List<VideoInfo>> call, Response<List<VideoInfo>> response) {
                if (response.isSuccessful()) {
                    Log.d("VideoPageActivity", "Received all videos: " + response.body().size() + " videos");
                    // 如果请求成功，更新视频列表
                    List<VideoInfo> videos = response.body();
                    if (videos != null) {
                        adapter.updateVideoList(videos); // 使用VideoAdapter中的updateVideoList方法更新数据
                    } else {
                        Toast.makeText(VideoPageActivity.this, "Video list is empty.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(VideoPageActivity.this, "Failed to fetch videos.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<VideoInfo>> call, Throwable t) {
                Toast.makeText(VideoPageActivity.this, "Network error.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}