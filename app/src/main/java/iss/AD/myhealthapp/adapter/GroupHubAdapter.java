package iss.AD.myhealthapp.adapter;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.ArrayList;

import iss.AD.myhealthapp.model.GroupHub;
import iss.AD.myhealthapp.network.NetworkUtils;
import iss.AD.myhealthapp.R;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GroupHubAdapter extends RecyclerView.Adapter<GroupHubAdapter.Viewholder> {
    private ArrayList<GroupHub> items;
    private Context context;

    private static final String BASE_URL = "http://10.0.2.2/api/group-hub/";

    // 接收后台线程的消息，重要，不能什么都用提醒主线程数据已经更改了，因为团队合作中，当前的主线程并不是团队合作的主线程
    private Handler handler;

    public GroupHubAdapter(ArrayList<GroupHub> items, Handler handler) {
        this.items = items;
        this.handler = handler;
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View inflator = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_ongoing, parent, false);
        return new Viewholder(inflator);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, @SuppressLint("RecyclerView") int position) {
        holder.title.setText(items.get(position).getName());
        holder.date.setText(items.get(position).getEndTime());

        float percentage = ((float) (items.get(position).getInitialQuantity() - items.get(position).getQuantity()) / items.get(position).getInitialQuantity()) * 100;
        String percentageString = String.format("%.2f%%", percentage);

        holder.progressBarPercent.setText(percentageString);

        int drawableResourceId = holder.itemView.getResources()
                .getIdentifier("ongoing4", "drawable", context.getPackageName());
        Glide.with(context)
                .load(drawableResourceId)
                .into(holder.pic);

        holder.progressBar.setProgress((int)percentage);
        if(position == 0){
            holder.layout.setBackgroundResource(R.drawable.dark_background);
            holder.title.setTextColor(context.getColor(R.color.white));
            holder.date.setTextColor(context.getColor(R.color.white));
            holder.progressTxt.setTextColor(context.getColor(R.color.white));
            holder.progressBarPercent.setTextColor(context.getColor(R.color.white));
            holder.pic.setColorFilter(ContextCompat.getColor(context, R.color.white), PorterDuff.Mode.SRC_IN);
            holder.progressBar.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.white)));
        }else {
            holder.layout.setBackgroundResource(R.drawable.light_background);
            holder.title.setTextColor(context.getColor(R.color.dark_blue));
            holder.date.setTextColor(context.getColor(R.color.dark_blue));
            holder.progressTxt.setTextColor(context.getColor(R.color.dark_blue));
            holder.progressBarPercent.setTextColor(context.getColor(R.color.dark_blue));
            holder.pic.setColorFilter(ContextCompat.getColor(context, R.color.dark_blue), PorterDuff.Mode.SRC_IN);
            holder.progressBar.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.dark_blue)));

        }

        /**
         * 长按逻辑
         */
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // 创建 PopupMenu
                PopupMenu popupMenu = new PopupMenu(context, v);
                popupMenu.inflate(R.menu.popup_menu);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.popup_subscribe) {
                            new Thread(() -> {
                                try {
                                    Long groupId = items.get(position).getId();
                                    // 之后从preference里面读取
                                    int userId = 3;
                                    sendGetRequest(userId, groupId);
                                    Log.d("Subscribe event", groupId.toString());

                                    // 向主线程发送消息
                                    Log.d("Data has changed", "Trying to send data to Host Activity");

                                    ArrayList<GroupHub> groupHubList = NetworkUtils.fetchAllGroupHub(); // 通过网络获取到所有的对象
                                    Message message = new Message();  // 创建消息对象，传递给主线程处理
                                    message.what = 1; // 消息标识，
                                    message.obj = groupHubList;
                                    handler.sendMessage(message); // 发送消息给主线程

                                } catch (Exception e) {
                                    Log.e("ERROR", "Error fetching data", e);
                                }
                            }).start();

                            return true;
                        } else {
                            return false;
                        }
                    }
                });
                // 显示 PopupMenu
                popupMenu.show();
                return true; // 返回 true 表示已消耗长按事件，不再触发点击事件
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class Viewholder extends RecyclerView.ViewHolder{
        TextView title, date, progressBarPercent, progressTxt;
        ProgressBar progressBar;
        ImageView pic;
        ConstraintLayout layout;

        public Viewholder(@NonNull View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.layout);
            progressTxt = itemView.findViewById(R.id.progressTxt);
            title = itemView.findViewById(R.id.titleTxt);
            date = itemView.findViewById(R.id.dateTxt);
            progressBar = itemView.findViewById(R.id.progressBar);
            progressBarPercent = itemView.findViewById(R.id.percentTxt);
            pic = itemView.findViewById(R.id.pic);
        }
    }


    /**
     * 发送订阅请求
     * @param userId
     * @param groupId
     * @return
     * @throws IOException
     */
    private static String sendGetRequest(int userId, Long groupId) throws IOException {
        OkHttpClient client = new OkHttpClient();

        // 构建带参数的 URL
        String url = BASE_URL + "subscribe?userId=" + userId + "&groupId=" + groupId;

        // 创建请求对象
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        // 发送请求并处理响应
        Response response = client.newCall(request).execute();
        if (response.isSuccessful()) {
            return response.body().string();
        } else {
            throw new IOException("Unexpected response code: " + response);
        }
    }
}
