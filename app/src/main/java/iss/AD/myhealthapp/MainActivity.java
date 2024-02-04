package iss.AD.myhealthapp;

import static java.lang.StrictMath.abs;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView textViewSuggestion, textViewSuggestionTitle;
    private int progressColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewSuggestionTitle = findViewById(R.id.textViewSuggestionTitle);
        String text = "Optimizing Your Wellness:";
        SpannableString spannableString = new SpannableString(text);
        spannableString.setSpan(new UnderlineSpan(), 0, text.length(), 0);
        textViewSuggestionTitle.setText(spannableString);

        progressBar = findViewById(R.id.progressBar);
        textViewSuggestion = findViewById(R.id.textViewSuggestion);

        // Set the target and current status
        int caloriesRequired = 2700;
        int foodIntake = 3001;
        int exerciseCaloriesBurned = 100;

        suggestionBasedOnHealthTarget(caloriesRequired, foodIntake, exerciseCaloriesBurned);
    }

    private void suggestionBasedOnHealthTarget(int caloriesRequired,
                                               int foodIntake, int exerciseCaloriesBurned) {
        int difference = foodIntake - exerciseCaloriesBurned - caloriesRequired;

        // Get the progressDrawable, which is a LayerDrawable
        LayerDrawable layerDrawable = (LayerDrawable) progressBar.getProgressDrawable();
        // Find the progress layer by its index
        int progressLayerIndex = 1; // Assuming the progress layer is at index 1
        progressColor = ContextCompat.getColor(this, android.R.color.holo_green_light);

        if (difference < -100) {
            textViewSuggestion.setText("Keep fueling your body for a healthy balance!");
        } else if (difference >= -100 && difference <= 100) {
            textViewSuggestion.setText("Good job! You're maintaining a generally healthy lifestyle!");
        } else if (difference > 100) {
            progressColor = ContextCompat.getColor(this, R.color.red);
            if (difference > 100 && difference <= 150) {
                textViewSuggestion.setText("Calorie Surplus: "+difference+". \nConsider 30 minutes of brisk walking or light jogging.");
            } else if (difference > 150 && difference <= 250) {
                textViewSuggestion.setText("Calorie Surplus: "+difference+". \nConsider in 45 minutes of moderate-intensity exercise.");
            } else if (difference > 250 && difference <= 350) {
                textViewSuggestion.setText("Calorie Surplus: "+difference+". \nBased on your health condition, consider engaging in 1 hour of a combination of cardio and strength training exercises.");
            } else if (difference > 350) {
                textViewSuggestion.setText("Calorie Surplus: "+difference+". \nBased on your health condition, consider incorporating 1.5 hours of varied workouts, including cardio and strength training.");
            } else {
                textViewSuggestion.setText("Let's keep an active lifestyle.");
            }
        } else {
            textViewSuggestion.setText("Let's practice a healthy lifestyle");
        }

        // Set the tint color for the progress layer
        layerDrawable.getDrawable(progressLayerIndex).setTint(progressColor);

        // Set the maximum and current progress
        progressBar.setMax(caloriesRequired);
        progressBar.setProgress(foodIntake - exerciseCaloriesBurned);
    }
}
/*
public class MainActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView textViewStatus, textViewTarget, textViewDifferent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.progressBar);
        textViewStatus = findViewById(R.id.textViewStatus);
        textViewDifferent = findViewById(R.id.textViewDifferent);
        // Set the target and current status
        int target = 1900;
        int currentStatus = 2610;

        textViewTarget = findViewById(R.id.textViewTarget);
        textViewTarget.setText("Target: " + target + " g");
        textViewStatus.setText("Current Status: " + currentStatus + " g");

        // Get the progressDrawable, which is a LayerDrawable
        LayerDrawable layerDrawable = (LayerDrawable) progressBar.getProgressDrawable();
        // Find the progress layer by its index
        int progressLayerIndex = 1; // Assuming the progress layer is at index 1
        int progressColor;

        if (currentStatus > target) {
            // If exceeded, set the progress bar color to red
            progressColor = ContextCompat.getColor(this, R.color.red);

            int diff = currentStatus - target;
            textViewDifferent.setText("Over Target: " + diff+ " g");
        }else{
            progressColor = ContextCompat.getColor(this, android.R.color.holo_green_light);
            int diff = currentStatus - target;
            textViewDifferent.setText("Need more: " + abs(diff)+ " g");
        }
        // Set the tint color for the progress layer
        layerDrawable.getDrawable(progressLayerIndex).setTint(progressColor);

        progressBar.setMax(target);
        progressBar.setProgress(currentStatus);

    }


<?xml version="1.0" encoding="utf-8"?>
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:gravity="center"
    android:padding="16dp"
    tools:context=".MainActivity">

    <TextView
        android:id="@+id/textViewTarget"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Target: 100"/>
    <FrameLayout
        android:layout_width="130dp"
        android:layout_height="100dp"
        android:layout_margin="16dp"
        android:layout_gravity="center">
        <ProgressBar
            android:id="@+id/progressBar"
            style="?android:attr/progressBarStyleHorizontal"
            android:layout_width="match_parent"
            android:layout_height="100dp"
            android:layout_margin="16dp"
            android:progressDrawable="@drawable/custom_progress"
            android:indeterminate="false"
            android:rotation="270"
            android:layout_gravity="center"
            android:visibility="visible"
            android:progressTint="@android:color/holo_green_light"
            />

    </FrameLayout>

    <TextView
        android:id="@+id/textViewStatus"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Current Status: 50"/>
    <TextView
        android:id="@+id/textViewDifferent"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Different"/>

</LinearLayout>
 */