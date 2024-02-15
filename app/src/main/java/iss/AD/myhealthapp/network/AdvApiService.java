package iss.AD.myhealthapp.network;

import iss.AD.myhealthapp.AdvInfo;
import retrofit2.Call;
import retrofit2.http.GET;
import java.util.List;
import iss.AD.myhealthapp.VideoInfo;
import retrofit2.http.Path;

public interface AdvApiService {
    @GET("advertisement/path/{userId}")
    Call<List<AdvInfo>> getAdvertisements(@Path("userId") int userId);
}