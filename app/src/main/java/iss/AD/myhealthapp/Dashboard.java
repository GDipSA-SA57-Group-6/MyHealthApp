package iss.AD.myhealthapp;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.Request;
import okhttp3.Call;
import okhttp3.Callback;
import java.util.concurrent.CountDownLatch;

public class Dashboard extends AppCompatActivity {
    OkHttpClient client;
    Button mBtnGetUser, mBtnLogout, mBtnSetHealthTarget, mBtnRecordExercise;
    TextView mUserNameTextView;

    BarChart barChartCaloriesBurnt;
    private CountDownLatch latch;

    private ImageView userImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);


        mBtnGetUser = findViewById(R.id.btnGetUser);
        mBtnLogout = findViewById(R.id.btnLogout);
        mBtnSetHealthTarget = findViewById(R.id.btnSetHealthTarget);
        mBtnRecordExercise = findViewById(R.id.btnRecordExercise);
        mUserNameTextView = findViewById(R.id.userNameTextView);

        client = new OkHttpClient();

        final SharedPreferences pref =
                getSharedPreferences("user_credentials", MODE_PRIVATE);
        Integer userId = pref.getInt("userId",-1);
        String name = pref.getString("name","");
        mUserNameTextView.setText(name);


        barChartCaloriesBurnt = findViewById(R.id.barChartCaloriesBurnt);
        CountDownLatch latch = new CountDownLatch(1);
        fetchCaloriesBurntDataInBackground(userId, latch);



        userImageView = findViewById(R.id.userImageView);
        userImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });


        mBtnGetUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userId != -1) {
                    // Pass userId to GetUser class
                    Intent intent = new Intent(Dashboard.this, GetUser.class);
                    startActivity(intent);
                }
            }
        });


        mBtnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutConfirmationDialog();
            }
        });

        mBtnSetHealthTarget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Dashboard.this, SetHealthTarget.class);
                startActivity(intent);
            }
        });

        mBtnRecordExercise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Dashboard.this, RecordExercise.class);
                startActivity(intent);
            }
        });

        Button mBtnDailySummary = findViewById(R.id.btnDailySummary);
        mBtnDailySummary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userId != -1) {
                    // Pass userId to GetUser class
                    Intent intent = new Intent(Dashboard.this, DailySummary.class);
                    startActivity(intent);
                }
            }
        });

        Button btnVideoPage = findViewById(R.id.btnVideoPage);
        btnVideoPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Dashboard.this, VideoPageActivity.class);
                startActivity(intent);
            }
        });


    }


    private void showLogoutConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Logout");
        builder.setMessage("Are you sure you want to logout?");
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked Confirm
                deletePref();
                Intent intent = new Intent(Dashboard.this, LoginPage.class);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked No
                // You can choose to do nothing or handle it accordingly
            }
        });
        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deletePref() {
        // Clear SharedPreferences on successful logout
        SharedPreferences pref = getSharedPreferences("user_credentials", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.apply();

    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }




    // Function to fetch calories burnt data in a separate thread
    private void fetchCaloriesBurntDataInBackground(int userId, CountDownLatch latch) {
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();

            String apiUrl = "http://10.0.2.2:8080/api/daily-exercise/last-7-days/" + userId;

            Request request = new Request.Builder()
                    .url(apiUrl)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    e.printStackTrace();
                    latch.countDown();
                    Log.e("fetchCaloriesBurntDataInBackground", "Request failed: " + e.getMessage());
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    try {
                        if (response.isSuccessful()) {
                            String responseData = response.body().string();
                            JSONArray data = new JSONArray(responseData);

                            Log.d("fetchCaloriesBurntDataInBackground", "Fetched data: " + data.toString());

                            // Process the fetched data
                            processFetchedData(data);
                        } else {
                            // Handle unsuccessful response
                            Log.e("fetchCaloriesBurntDataInBackground", "Unsuccessful response: " + response.code());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("fetchCaloriesBurntDataInBackground", "Error parsing JSON: " + e.getMessage());
                    } finally {
                        latch.countDown();
                    }
                }
            });
        }).start();
    }

    // Process the fetched data
    private void processFetchedData(JSONArray data) {
        // Run on a background thread
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        ArrayList<String> formattedDates = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (int i = 6; i >= 0; i--) {
            calendar.add(Calendar.DAY_OF_YEAR, -1);
            String date = sdf.format(calendar.getTime());
            labels.add(date);

            // Format and add date in MM-dd format
            SimpleDateFormat mmddFormat = new SimpleDateFormat("MM-dd", Locale.getDefault());
            formattedDates.add(mmddFormat.format(calendar.getTime()));
        }

        // Sort labels in ascending order
        Collections.sort(labels);

        // Iterate over the labels and find the corresponding entry in the data
        for (int i = 0; i < labels.size(); i++) {
            String currentDate = labels.get(i);
            JSONObject entry = findEntryByDate(data, currentDate);

            int caloriesBurnt = (entry != null) ? entry.optInt("caloriesBurnt", 0) : 0;
            entries.add(new BarEntry(i, caloriesBurnt));

            Log.d("processFetchedData", "Entry " + i + ": " + caloriesBurnt + ", Date: " + currentDate);
        }

        Log.d("processFetchedData", "Received data: " + data.toString());

        BarDataSet dataSet = new BarDataSet(entries, "Calories Burnt");
        BarData barData = new BarData(dataSet);

        runOnUiThread(() -> {
            barChartCaloriesBurnt.setData(barData);
            barChartCaloriesBurnt.getXAxis().setValueFormatter(new IndexAxisValueFormatter(formattedDates));
            barChartCaloriesBurnt.invalidate();

            Log.d("processFetchedData", "Entries size: " + entries.size());
            Log.d("processFetchedData", "Labels size: " + labels.size());
            for (int i = 0; i < entries.size(); i++) {
                Log.d("processFetchedData", "Entry " + i + ": " + entries.get(i).getY() + ", Label: " + formattedDates.get(i));
            }
        });
    }
    // Helper method to find the entry with a specific date
    private JSONObject findEntryByDate(JSONArray data, String targetDate) {
        for (int i = 0; i < data.length(); i++) {
            JSONObject entry = data.optJSONObject(i);
            if (entry != null) {
                String exerciseDate = entry.optString("exerciseDate", "");
                if (exerciseDate.equals(targetDate)) {
                    return entry;
                }
            }
        }
        return null;
    }



}

    /*
    private void logoutUser() {
        // Build the POST request body (assuming you have a valid requestBody)
        RequestBody requestBody = ...; // Replace this line with your actual request body
        // Build the POST request
        Request request = new Request.Builder()
                .url("http://10.0.2.2:8080/api/logout")
                .post(requestBody)
                .build();

        // Make the POST request
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                Log.e("LogoutUser", "Network request failed: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    // Clear SharedPreferences on successful logout
                    final SharedPreferences pref =
                            getSharedPreferences("user_credentials", MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.clear();
                    editor.apply();

                    runOnUiThread(() -> {
                        showToast("Logout successful!");
                        // Navigate back to LoginPage
                        Intent intent = new Intent(Dashboard.this, LoginPage.class);
                        startActivity(intent);
                        finish(); // Close the current activity to prevent going back to it with the back button
                    });
                } else {
                    Log.e("LogoutUser", "Unexpected response code: " + response.code());
                }
            }
        });
    }
    */