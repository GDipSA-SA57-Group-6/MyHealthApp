package iss.AD.myhealthapp.network;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import iss.AD.myhealthapp.model.GroupHub;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NetworkUtils {

    private static final String TAG = "NetworkUtils";

    public static ArrayList<GroupHub> fetchAllGroupHub() {
        ArrayList<GroupHub> groupHubList = new ArrayList<>();

        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("http://10.0.2.2:8080/api/group-hub/get")
                    .get()
                    .build();

            Response response = client.newCall(request).execute();

            if (!response.isSuccessful()) {
                Log.e(TAG, "Failed to fetch data: " + response);
                return groupHubList;
            }

            String responseData = response.body().string();
            // 打印出原始JSON响应
            Log.d("NetworkUtils", "Response data: " + responseData);

            Gson gson = new Gson();
            Type listType = new TypeToken<List<GroupHub>>(){}.getType();
            groupHubList = gson.fromJson(responseData, listType);
        } catch (IOException e) {
            Log.e(TAG, "Error fetching data", e);
        }

        return groupHubList;
    }

    public static ArrayList<GroupHub> fetchGroupHubBySearch(String query) {
        ArrayList<GroupHub> searchResults = new ArrayList<>();

        try {
            OkHttpClient client = new OkHttpClient();
            // 注意：这里的URL和参数需要根据您的实际API进行调整
            Request request = new Request.Builder()
                    .url("http://10.0.2.2:8080/api/group-hub/search?name=" + query)
                    .get()
                    .build();

            Response response = client.newCall(request).execute();

            if (!response.isSuccessful()) {
                Log.e(TAG, "Failed to fetch search results: " + response);
                return searchResults;
            }

            String responseData = response.body().string();
            Gson gson = new Gson();
            Type listType = new TypeToken<List<GroupHub>>(){}.getType();
            searchResults = gson.fromJson(responseData, listType);
        } catch (IOException e) {
            Log.e(TAG, "Error fetching search results", e);
        }

        return searchResults;
    }

}
