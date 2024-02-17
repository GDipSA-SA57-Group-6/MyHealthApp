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
                    .url("http://192.168.18.35:8080/api/group-hub/get")
                    .get()
                    .build();

            Response response = client.newCall(request).execute();

            if (!response.isSuccessful()) {
                Log.e(TAG, "Failed to fetch data: " + response);
                return groupHubList;
            }

            String responseData = response.body().string();
            Gson gson = new Gson();
            Type listType = new TypeToken<List<GroupHub>>(){}.getType();
            groupHubList = gson.fromJson(responseData, listType);
        } catch (IOException e) {
            Log.e(TAG, "Error fetching data", e);
        }

        return groupHubList;
    }
}
