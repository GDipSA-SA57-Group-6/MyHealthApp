package iss.AD.myhealthapp.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import iss.AD.myhealthapp.adapter.ArchiveAdapter;
import iss.AD.myhealthapp.adapter.PublishedAdapter;
import iss.AD.myhealthapp.model.GroupHub;
import iss.AD.myhealthapp.databinding.ActivityProfileBinding;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ProfileActivity extends AppCompatActivity {
    ActivityProfileBinding binding;
    private RecyclerView.Adapter adapterArchive, adapterPublished;

    private static final String TAG = "PROFILE ACTIVITY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        new Thread( () -> {
            try {
                OkHttpClient okHttpClient = new OkHttpClient();
                // 这个url为了方便，暂时没有换成带有用户id的命令，
                // 因为安卓界面的登陆不是我做的
                // 所以我不知道id放在哪里
                Request request = new Request.Builder().url("http://192.168.18.35:8080/api/group-hub/get").get().build();
                Response response = okHttpClient.newCall(request).execute();

                if(!response.isSuccessful()) {
                    Log.e(TAG, "Failed to fetch data: " + response);
                    runOnUiThread(() -> Toast.makeText(ProfileActivity.this, "Fetch data failed: " + response, Toast.LENGTH_SHORT).show());
                    return;
                }

                String responseData = response.body().string();
                Gson gson = new Gson();
                Type listType = new TypeToken<List<GroupHub>>(){}.getType();
                ArrayList<GroupHub> groupHubList = gson.fromJson(responseData, listType);

                runOnUiThread( () -> {
                    initRecyclerView(groupHubList);
                    initRecyclerViewPublished(groupHubList);

                    for(GroupHub groupHub : groupHubList) {
                        Log.d(TAG, "Name: " + groupHub.getName() + ", Quantity: " + groupHub.getQuantity());
                    }
                });
            }catch (Exception e) {
                Log.e(TAG, "ERROR FETCHING DATA", e);
                runOnUiThread(() -> {
                    Toast.makeText(ProfileActivity.this, "Network Error " + e.getMessage(), Toast.LENGTH_LONG ).show();
                });
            }
        }).start();
    }

    public void initRecyclerView(ArrayList<GroupHub> items) {
        binding.viewSubscribed.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        adapterArchive = new ArchiveAdapter(items);
        binding.viewSubscribed.setAdapter(adapterArchive);
    }

    public void initRecyclerViewPublished(ArrayList<GroupHub> items) {
        binding.viewPublished.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        adapterPublished = new PublishedAdapter(items);
        binding.viewPublished.setAdapter(adapterPublished);
    }
}