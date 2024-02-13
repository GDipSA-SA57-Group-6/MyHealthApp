package iss.AD.myhealthapp;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import iss.AD.myhealthapp.network.VideoApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.ArrayList;
import java.util.List;

public class VideoPageActivity extends AppCompatActivity {

    private List<VideoInfo> videoList = new ArrayList<>();
    private VideoAdapter adapter;
    private RecyclerView recyclerView;
    private VideoApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new VideoAdapter(this, videoList);
        recyclerView.setAdapter(adapter);

        String local_host = getResources().getString(R.string.local_host);
        // 创建Retrofit实例
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://" + local_host + ":8080")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(VideoApiService.class);


        SharedPreferences prefs = getSharedPreferences("user_credentials", MODE_PRIVATE);
        int userId = prefs.getInt("userId", -1);

        if (userId == -1) {
            // 用户未登录，跳转到登录页面
            Toast.makeText(this, "Please log in to continue.",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // 用户已登录，获取推荐视频类型
        fetchRecommendedVideos(String.valueOf(userId));
    }

    private void fetchRecommendedVideos(String userId) {
        Call<Integer> recommendCall = apiService.recommendVideo(Integer.parseInt(userId));

        recommendCall.enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.isSuccessful()) {
                    int type = response.body();
                    fetchVideosByType(type);
                } else {
                    Toast.makeText(VideoPageActivity.this, "Error fetching video recommendation type.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                Toast.makeText(VideoPageActivity.this, "Network error.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void fetchVideosByType(int type) {
        Call<List<VideoInfo>> videosCall = apiService.getVideosByType(type);

        videosCall.enqueue(new Callback<List<VideoInfo>>() {
            @Override
            public void onResponse(Call<List<VideoInfo>> call, Response<List<VideoInfo>> response) {
                if (response.isSuccessful()) {
                    videoList.clear();
                    videoList.addAll(response.body());
                    adapter.notifyDataSetChanged();
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
}