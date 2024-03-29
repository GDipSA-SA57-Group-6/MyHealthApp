package iss.AD.myhealthapp;

import android.content.Context;
import android.graphics.drawable.LayerDrawable;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

public class NutritionTracker {
    private int target, currentStatus;
    private ProgressBar progressBar;
    private TextView textViewTarget, textViewStatus, textViewDifferent;


    public NutritionTracker(ProgressBar progressBar, TextView textViewTarget,TextView textViewStatus, TextView textViewDifferent) {
        this.progressBar = progressBar;
        this.textViewTarget = textViewTarget;
        this.textViewStatus = textViewStatus;
        this.textViewDifferent = textViewDifferent;
    }

    public void setTarget(int target) {
        this.target = target;
    }

    public void setCurrentStatus(int currentStatus) {
        this.currentStatus = currentStatus;
    }

    public void trackNutrition(Context context) {
        textViewTarget.setText("Need: " + target + " g");
        textViewStatus.setText("Eaten: " + currentStatus + " g");

        LayerDrawable layerDrawable = (LayerDrawable) progressBar.getProgressDrawable();
        int progressLayerIndex = 1;

        int progressColor;
        String statusText;

        if (currentStatus > target) {
            progressColor = ContextCompat.getColor(context, R.color.red);
            int diff = currentStatus - target;
            statusText = "Over by: " + diff + " g";
        } else {
            progressColor = ContextCompat.getColor(context, android.R.color.holo_green_light);
            int diff = target - currentStatus;
            statusText = "To eat: " + diff + " g";
        }

        layerDrawable.getDrawable(progressLayerIndex).setTint(progressColor);
        progressBar.setMax(target);
        progressBar.setProgress(currentStatus);
        textViewDifferent.setText(statusText);
    }
}