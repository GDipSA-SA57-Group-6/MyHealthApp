package iss.AD.myhealthapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

import iss.AD.myhealthapp.adapter.GroupHubAdapter;
import iss.AD.myhealthapp.model.GroupHub;
import iss.AD.myhealthapp.network.NetworkUtils;
import iss.AD.myhealthapp.R; // 确保导入R类

public class GrouphubActivity extends AppCompatActivity {
    private static final String TAG = "GrouphubActivity";
    private ArrayList<GroupHub> groupHubList = new ArrayList<>();
    private RecyclerView viewOngoing;
    private RecyclerView.Adapter adapterOngoing;
    private static Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grouphub); // 设置内容视图直接引用布局ID

        // 使用findViewById代替View Binding初始化视图
        viewOngoing = findViewById(R.id.viewOngoing);

        final SharedPreferences pref =
                getSharedPreferences("user_credentials", MODE_PRIVATE);
        Integer userId = pref.getInt("userId",-1);
        String name = pref.getString("name","");

        TextView textView = findViewById(R.id.textView2);
        String customText = "Hi, " + name + "!";
        textView.setText(customText);
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                if (msg.what == 1) {
                    ArrayList<GroupHub> groupHubList = (ArrayList<GroupHub>) msg.obj;
                    Log.d(TAG, "Data has changed, Trying to modify UI");
                    initRecyclerView(groupHubList);
                }
                return true;
            }
        });


        fetchGroupHubData(); // 默认加载所有GroupHub数据

        // 处理从其他Activity传入的Intent
        handleIntentSearch();

        EditText editTextSearch = findViewById(R.id.editTextText);
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                // 在用户输入后触发搜索
                new Thread(() -> {
                    ArrayList<GroupHub> searchResults = NetworkUtils.fetchGroupHubBySearch(s.toString());
                    // 回到主线程更新UI
                    runOnUiThread(() -> initRecyclerView(searchResults));
                }).start();
            }
        });

    }
    private void handleIntentSearch() {
        Intent intent = getIntent();
        if (intent != null) {
            // 根据intent中的数据执行搜索
            String exerciseType = intent.getStringExtra("exercise");
            String[] exerciseKeywords = intent.getStringArrayExtra("exerciseKeywords");

            // 构造搜索关键字
            String searchQuery = "";
            if (exerciseType != null) {
                searchQuery = exerciseType;
            } else if (exerciseKeywords != null && exerciseKeywords.length > 0) {
                searchQuery = String.join(" ", exerciseKeywords);
            }

            if (!searchQuery.isEmpty()) {
                // 执行搜索并更新UI
                final String finalSearchQuery = searchQuery; // 创建一个effectively final的变量用于lambda表达式
                new Thread(() -> {
                    ArrayList<GroupHub> searchResults = NetworkUtils.fetchGroupHubBySearch(finalSearchQuery);
                    // 回到主线程更新UI
                    runOnUiThread(() -> initRecyclerView(searchResults));
                }).start();
            }
        }
    }
    private void fetchGroupHubData() {
        new Thread(() -> {
            try {
                ArrayList<GroupHub> groupHubList = NetworkUtils.fetchAllGroupHub(); // 通过网络获取到所有的对象
                Message message = new Message();  // 创建消息对象，传递给主线程处理
                message.what = 1; // 消息标识
                message.obj = groupHubList;
                handler.sendMessage(message); // 发送消息给主线程
            } catch (Exception e) {
                Log.e(TAG, "Error fetching data", e);
            }
        }).start();
    }

    private void initRecyclerView(ArrayList<GroupHub> groupHubList) {
        viewOngoing.setLayoutManager(new GridLayoutManager(this, 2));
        adapterOngoing = new GroupHubAdapter(groupHubList, handler);
        viewOngoing.setAdapter(adapterOngoing);
    }
}
