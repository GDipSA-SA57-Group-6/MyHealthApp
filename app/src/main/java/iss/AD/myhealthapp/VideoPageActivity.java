package iss.AD.myhealthapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        recyclerView = findViewById(R.id.recyclerView);
        // 设置LayoutManager和Adapter
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new VideoAdapter(this, videoList);
        recyclerView.setAdapter(adapter);

        // 测试用：添加模拟数据
        mockDataForTesting();

        // 实际请求应在后端API就绪后开启
        // fetchDataFromApi();
    }

    // 模拟数据用于测试UI
    private void mockDataForTesting() {
        videoList.add(new VideoInfo("https://free.wzznft.com/i/2024/02/05/w4go6v.png", "视频1描述", "https://www.bilibili.com/video/BV1bH4y1h7BF/?spm_id_from=333.1365.list.card_archive.click"));
        videoList.add(new VideoInfo("https://free.wzznft.com/i/2024/02/05/w4gxj6.png", "视频2描述", "https://www.bilibili.com/video/BV14B421z7rP/?spm_id_from=333.1365.list.card_archive.click&vd_source=53804055fd09dcd8d132a80def05c6d2"));
        // 添加更多模拟数据以测试滚动
        adapter.notifyDataSetChanged();
    }
    private void fetchDataFromApi() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.1.98:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        VideoApiService apiService = retrofit.create(VideoApiService.class);

        // 调用API
        Call<List<VideoInfo>> call = apiService.getVideos();
        call.enqueue(new Callback<List<VideoInfo>>() {
            @Override
            public void onResponse(Call<List<VideoInfo>> call, Response<List<VideoInfo>> response) {
                if (response.isSuccessful()) {
                    videoList.clear();
                    videoList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<VideoInfo>> call, Throwable t) {
                // 处理请求失败
            }
        });
    }
}
