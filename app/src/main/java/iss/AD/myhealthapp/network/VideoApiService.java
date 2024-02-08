package iss.AD.myhealthapp.network;

import retrofit2.Call;
import retrofit2.http.GET;
import java.util.List;
import iss.AD.myhealthapp.VideoInfo;
import retrofit2.http.Path;
import retrofit2.http.Query;
public interface VideoApiService {
    @GET("/video")
    Call<List<VideoInfo>> getVideos();

    // 获取推荐视频类型的API
    @GET("/video/path/{userId}")
    Call<Integer> recommendVideo(@Path("userId") int userId);

    // 根据推荐类型获取视频列表的API
    @GET("/video/videos/{type}")
    Call<List<VideoInfo>> getVideosByType(@Path("type") int type);
}

