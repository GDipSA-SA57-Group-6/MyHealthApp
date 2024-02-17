package iss.AD.myhealthapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;

import iss.AD.myhealthapp.adapter.GroupHubAdapter;
import iss.AD.myhealthapp.model.GroupHub;
import iss.AD.myhealthapp.network.NetworkUtils;
import iss.AD.myhealthapp.R; // 确保导入R类

public class GrouphubActivity extends AppCompatActivity {
    private static final String TAG = "GrouphubActivity";
    // 移除View Binding相关代码
    private RecyclerView viewOngoing;
    private RecyclerView.Adapter adapterOngoing;
    private static Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grouphub); // 设置内容视图直接引用布局ID

        // 使用findViewById代替View Binding初始化视图
        viewOngoing = findViewById(R.id.viewOngoing);

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

        fetchGroupHubData();
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
