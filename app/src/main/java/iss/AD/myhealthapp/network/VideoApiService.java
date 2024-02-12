package iss.AD.myhealthapp.network;

import retrofit2.Call;
import retrofit2.http.GET;
import java.util.List;
import iss.AD.myhealthapp.VideoInfo;
public interface VideoApiService {
    @GET("api/video")
    Call<List<VideoInfo>> getVideos();
}

