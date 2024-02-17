package iss.AD.myhealthapp.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import iss.AD.myhealthapp.model.GroupHub;
import iss.AD.myhealthapp.R;

public class ArchiveAdapter extends RecyclerView.Adapter<ArchiveAdapter.Viewholder> {
    private ArrayList<GroupHub> items;
    private Context context;

    public ArchiveAdapter(ArrayList<GroupHub> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View inflator = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_subscribed, parent, false);
        return new Viewholder(inflator);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {
        holder.title.setText(items.get(position).getName());

        float percentage = ((float) (items.get(position).getInitialQuantity() - items.get(position).getQuantity()) / items.get(position).getInitialQuantity()) * 100;
        String percentageString = String.format("%.2f%%", percentage);

        holder.progressBarPercent.setText(percentageString);
        holder.progressBar.setProgress((int)percentage);

        if(position == 0) {
            holder.layout.setBackgroundResource(R.drawable.dark_background);
            holder.title.setTextColor(context.getColor(R.color.white));
            holder.progressBarPercent.setTextColor(context.getColor(R.color.white));
            holder.progressBar.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.white)));
            holder.pic.setColorFilter(ContextCompat.getColor(context, R.color.white), PorterDuff.Mode.SRC_IN);
        }else {
            holder.layout.setBackgroundResource(R.drawable.light_background);
            holder.title.setTextColor(context.getColor(R.color.dark_blue));
            holder.progressBarPercent.setTextColor(context.getColor(R.color.dark_blue));
            holder.progressBar.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.dark_blue)));
            holder.pic.setColorFilter(ContextCompat.getColor(context, R.color.dark_blue), PorterDuff.Mode.SRC_IN);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class Viewholder extends RecyclerView.ViewHolder{
        TextView title;
        TextView progressBarPercent;
        ProgressBar progressBar;
        ConstraintLayout layout;
        ImageView pic;

        public Viewholder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.titleTxt);
            progressBarPercent = itemView.findViewById(R.id.percentTxtSubscribed);
            progressBar = itemView.findViewById(R.id.progressBarSubscribed);
            layout = itemView.findViewById(R.id.layoutSubscribed);
            pic = itemView.findViewById(R.id.imageViewSubscribed);
        }
    }
}
