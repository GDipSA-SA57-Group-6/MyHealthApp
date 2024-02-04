package iss.AD.myhealthapp;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import okhttp3.OkHttpClient;

public class Dashboard extends AppCompatActivity {
    OkHttpClient client;
    Button mBtnGetUser, mBtnLogout, mBtnSetHealthTarget, mBtnRecordExercise;
    TextView mUserNameTextView;

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