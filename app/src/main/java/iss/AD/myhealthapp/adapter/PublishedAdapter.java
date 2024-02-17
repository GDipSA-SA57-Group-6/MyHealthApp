package iss.AD.myhealthapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import iss.AD.myhealthapp.model.GroupHub;
import iss.AD.myhealthapp.R;

public class PublishedAdapter extends RecyclerView.Adapter<PublishedAdapter.Viewholder> {
    private ArrayList<GroupHub> items;

    public PublishedAdapter(ArrayList<GroupHub> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflator = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_published, parent, false);
        return new Viewholder(inflator);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {
        holder.title.setText(items.get(position).getName());
        holder.date.setText(items.get(position).getEndTime());

        float percentage = ((float) (items.get(position).getInitialQuantity() - items.get(position).getQuantity()) / items.get(position).getInitialQuantity()) * 100;
        String percentageString = String.format("%.2f%%", percentage);

        holder.progressPercentTxt.setText(percentageString);
        holder.progressBar.setProgress((int)percentage);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class Viewholder extends RecyclerView.ViewHolder {
        private TextView title;
        private TextView date;
        private ProgressBar progressBar;
        private TextView progressPercentTxt;

        public Viewholder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.titleTxtPublished);
            date = itemView.findViewById(R.id.textViewDatePublished);
            progressBar = itemView.findViewById(R.id.progressBarPublished);
            progressPercentTxt = itemView.findViewById(R.id.percentTxtPublished);
        }
    }
}
