package iss.AD.myhealthapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Request;

public class SetHealthTarget extends AppCompatActivity {
    OkHttpClient client;
    Button btnReturn, btnLoseWeight, btnMaintainHealth, btnGainWeight, btnUpdate;
    TextView getCurrentTextView;
    private String genderInClass;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_health_target);

        client = new OkHttpClient();
        btnReturn = findViewById(R.id.btnReturn);
        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start CreateUserActivity when the button is clicked
                Intent intent = new Intent(SetHealthTarget.this, Dashboard.class);
                startActivity(intent);
            }
        });


        final SharedPreferences pref =
                getSharedPreferences("user_credentials", MODE_PRIVATE);
        int userId = pref.getInt("userId",-1);

        getCurrentTextView = findViewById(R.id.getCurrentTextView);
        displayCurrentTextView(userId);

        btnLoseWeight = findViewById(R.id.btnLoseWeight);
        btnMaintainHealth = findViewById(R.id.btnMaintainHealth);
        btnGainWeight = findViewById(R.id.btnGainWeight);

        btnLoseWeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserTargetCalories("LoseWeight", userId);
            }
        });

        btnMaintainHealth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserTargetCalories("MaintainHealth", userId);
            }
        });

        btnGainWeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserTargetCalories("GainWeight", userId);
            }
        });
    }

    private int calculateTargetCalories(String userGender, String target) {
        switch (userGender.toLowerCase()) {
            case "male":
                switch (target.toLowerCase()) {
                    case "loseweight":
                        return 1900;
                    case "maintainhealth":
                        return 2500;
                    case "gainweight":
                        return 2800;
                    default:
                        return 2500;
                }
            case "female":
                switch (target.toLowerCase()) {
                    case "loseweight":
                        return 1400;
                    case "maintainhealth":
                        return 2000;
                    case "gainweight":
                        return 2300;
                    default:
                        return 2000;
                }
            default:
                return -1;
        }
    }

    public void updateUserTargetCalories (String target, int userId){
        int targetCalories = calculateTargetCalories(genderInClass, target);

        JSONObject jsonBody = new JSONObject();

        try {
            jsonBody.put("targetCalories", targetCalories);

        } catch (DateTimeParseException | JSONException e) {
            e.printStackTrace();
        }

        RequestBody requestBody = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json"));

        String apiUrl = "http://10.0.2.2:8080/api/user/updatecalories/" + userId;
        // Build the POST request
        Request request = new Request.Builder()
                .url(apiUrl)
                .post(requestBody)
                .build();

        // Make the POST request
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                Log.e("UpdateCalories", "UpdateCalories request failed: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    // Handle the successful response if needed
                    Log.d("UpdateCalories", "UpdateCalories successful");
                    runOnUiThread(() -> {
                        Toast.makeText(SetHealthTarget.this, "Health Target has been updated successfully", Toast.LENGTH_LONG).show();
                        displayCurrentTextView(userId);
                    });
                } else {
                    Log.e("UpdateCalories", "Unexpected response code: " + response.code());
                }
            }
        });

    }


    public void displayCurrentTextView(int userId){
        String apiUrl = "http://10.0.2.2:8080/api/user/get/" + userId;

        Request request = new Request.Builder()
                .url(apiUrl)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                Log.e("GetUserCalories", "Network request failed: " + e.getMessage());
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseBody;

                if (response.isSuccessful()) {
                    responseBody = response.body().string();

                    try {
                        JSONObject jsonObject = new JSONObject(responseBody);

                        int userId = jsonObject.getInt("userId");
                        String name = jsonObject.getString("name");
                        String password = jsonObject.getString("password");
                        String birthDateString = jsonObject.getString("birthDate"); // Extract birth date as string
                        // Convert birth date string to LocalDate using DateTimeFormatter
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        LocalDate birthDate = LocalDate.parse(birthDateString, formatter);
                        String gender = jsonObject.getString("gender");
                        String genderUp = gender.substring(0, 1).toUpperCase() + gender.substring(1);
                        String emailAddress = jsonObject.getString("emailAddress");
                        int targetCalories = jsonObject.getInt("targetCalories");

                        runOnUiThread(() -> {
                            getCurrentTextView.setText(displayTarget(gender,targetCalories));
                            saveUserGenderToClass(gender);
                        });
                    }catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("GetUser", "Error parsing JSON response");
                    }
                } else {
                    Log.e("GetUser", "Unexpected response code: " + response.code());
                }
            }
        });
    }

    private String displayTarget(String gender, int targetCalories) {
        switch (gender.toLowerCase()) {
            case "male":
                switch (targetCalories) {
                    case 1900:
                        return "Lose Weight";
                    case 2500:
                        return "Maintain Health";
                    case 2800:
                        return "Gain Weight";
                    default:
                        return "Maintain Health"; // Default for maintaining health
                }
            case "female":
                switch (targetCalories) {
                    case 1400:
                        return "Lose Weight";
                    case 2000:
                        return "Maintain Health";
                    case 2300:
                        return "Gain Weight";
                    default:
                        return "Maintain Health"; // Default for maintaining health
                }
            default:
                return "Invalid gender"; // Handle other cases as needed
        }
    }

    private String saveUserGenderToClass(String gender){
        genderInClass = gender;
        return genderInClass;
    }

}
