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


    @GET("/video/path/{userId}")
    Call<Integer> recommendVideo(@Path("userId") int userId);


    @GET("/video/videos/{type}")
    Call<List<VideoInfo>> getVideosByType(@Path("type") int type);
}

