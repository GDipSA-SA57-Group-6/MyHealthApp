package iss.AD.myhealthapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import android.widget.Toast;
import android.util.Log;



import java.util.List;

import iss.AD.myhealthapp.R;
import iss.AD.myhealthapp.VideoInfo;


public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {
    private Context context;
    private List<VideoInfo> videoList;
    private LayoutInflater inflater;

    // 构造方法
    public VideoAdapter(Context context, List<VideoInfo> videoList) {
        this.context = context;
        this.videoList = videoList;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_video, parent, false);
        return new VideoViewHolder(view);
    }

//    @Override
//    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
//        VideoInfo video = videoList.get(position);
//        // 设置图片和描述。使用图片加载库Glide来加载网络图片。
//        Glide.with(context).load(video.getImageUrl()).into(holder.imageView);
//        holder.textViewDescription.setText(video.getDescription());
//
//        holder.imageView.setOnClickListener(v -> {
//            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(video.getVideoUrl()));
//            context.startActivity(intent);
//        });
//    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        VideoInfo video = videoList.get(position);
        Glide.with(context).load(video.getImageUrl()).into(holder.imageView);
        holder.textViewDescription.setText(video.getDescription());

        String videoUrl = video.getVidUrl();

//        Log.d("VideoAdapter", "Video URL at position " + position + ": " + videoUrl);

        if (videoUrl != null && !videoUrl.isEmpty()) {
            holder.imageView.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl));
                context.startActivity(intent);
            });
        } else {
            holder.imageView.setOnClickListener(v -> {
                Toast.makeText(context, "视频链接不可用", Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    // 添加方法来更新视频列表
    public void updateVideoList(List<VideoInfo> newVideoList) {
        Log.d("VideoAdapter", "Updating video list with " + newVideoList.size() + " items.");
        videoList.clear();
        videoList.addAll(newVideoList);
        notifyDataSetChanged(); // 通知适配器数据集已改变
        Log.d("VideoAdapter", "Video list updated and notifyDataSetChanged called.");
    }

    static class VideoViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textViewDescription;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
        }
    }
}
