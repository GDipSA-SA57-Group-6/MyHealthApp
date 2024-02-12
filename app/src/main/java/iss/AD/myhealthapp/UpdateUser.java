package iss.AD.myhealthapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;

public class UpdateUser extends AppCompatActivity {
    private TextView confirmPasswordLabel;
    private Button btnUpdate, btnReturn;
    private EditText txtUserId,txtUserName, txtPassword, txtBirthDate, txtEmail,txtGender, txtPasswordConfirm;
    private OkHttpClient client;
    private boolean isPasswordEditTextClicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_user);

        txtUserId = findViewById(R.id.txtUserId);
        txtUserName = findViewById(R.id.txtUserName);
        txtPassword = findViewById(R.id.txtPassword);
        confirmPasswordLabel = findViewById(R.id.confirmPasswordLabel);
        txtPasswordConfirm = findViewById(R.id.txtPasswordConfirm);
        txtBirthDate = findViewById(R.id.txtBirthDate);
        txtEmail = findViewById(R.id.txtEmail);
        txtGender = findViewById(R.id.txtGender);

        client = new OkHttpClient();

        final SharedPreferences pref =
                getSharedPreferences("user_credentials", MODE_PRIVATE);
        int userId = pref.getInt("userId",-1);

        retrieveUserDetails(userId);

        btnReturn = findViewById(R.id.btnReturn);
        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnUpdate = findViewById(R.id.btnUpdate);
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateUser(userId);
            }
        });

        // Set a focus change listener on txtPassword EditText
        txtPassword.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                // Password EditText is clicked
                isPasswordEditTextClicked = true;
                showConfirmPasswordFields();
            }
        });
    }

    private void showConfirmPasswordFields() {
        // Show Confirm Password TextView and EditText only if Password EditText is clicked
        if (isPasswordEditTextClicked) {
            confirmPasswordLabel.setVisibility(View.VISIBLE);
            txtPasswordConfirm.setVisibility(View.VISIBLE);
        }
    }


    private void retrieveUserDetails(int userId) {

        String apiUrl = "http://192.168.1.98:8080/api/user/get/" + userId;

        Request request = new Request.Builder()
                .url(apiUrl)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                Log.e("RetrieveUserDetails", "Network request failed: " + e.getMessage());
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

                        runOnUiThread(() -> {
                            // UI updates on the main thread
                            txtUserId.setText(String.valueOf(userId));
                            txtUserName.setText(name);
                            txtPassword.setText(password);
                            txtBirthDate.setText(formatter.format(birthDate)); // Set formatted birth date to the corresponding TextView
                            txtEmail.setText(emailAddress);
                            txtGender.setText(genderUp);
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("RetrieveUserDetails", "Error parsing JSON response");
                    }
                } else {
                    Log.e("RetrieveUserDetails", "Unexpected response code: " + response.code());
                }
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }
    private boolean isValidName(String name) {
        // Implement logic to check name validity based on the constraints
        return name != null && name.length() >= 4 && name.length() <= 20;
    }

    private boolean isValidPassword(String password) {
        if (password == null || password.length() < 4 || password.length() > 20
                || password.contains(" ")) {
            return false;
        }
        // Check the pattern requirement
        return password.matches("^(?=.*[a-zA-Z])(?=.*[0-9]).*$");
    }
    private boolean isValidBirthDate(String birthDateStr) {
        return birthDateStr != null && birthDateStr.matches("^\\d{4}-\\d{2}-\\d{2}$");
    }
    private boolean isValidEmail(String email) {
        // Implement logic to check email validity based on the constraints
        return email != null && email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    }

    private void updateUser(int userId) {
        String name = txtUserName.getText().toString().trim();
        if (isValidName(name)) {
            // Valid name and password, proceed
        } else {
            showToast("User Name must be 4-20 characters.");
            return;
        }

        String password = txtPassword.getText().toString().trim();
        if (isValidPassword(password)) {
            // Valid password, proceed
        } else {
            // Show error message
            showToast("Password must be 4-20 characters long. " +
                    "Must contain at least one letter and one number. Spaces are not allowed.");
            return;
        }


        if (isPasswordEditTextClicked) {
            // Check confirmation password only if Password EditText is clicked
            String passwordConfirm = txtPasswordConfirm.getText().toString().trim();
            if (!password.equals(passwordConfirm)) {
                showToast("Passwords do not match. Please enter the same password in both fields.");
                return;
            }
        }

        String birthDateStr = txtBirthDate.getText().toString().trim(); // Extract birth date as string
        if (isValidBirthDate(birthDateStr)) {
            // Valid birth date format, proceed
        } else {
            // Show error message
            showToast("Invalid birth date format. Please enter the date in YYYY-MM-DD format.");
            return;
        }
        // Convert birth date string to LocalDate
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate birthDate = LocalDate.parse(birthDateStr, formatter);

        String emailAddress = txtEmail.getText().toString().trim();
        if (isValidEmail(emailAddress)) {
            // Valid email address, proceed
        } else {
            // Show error message
            showToast("Invalid email address format. Please enter a valid email address.");
            return;
        }


        // Build the JSON request body
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("name", name);
            jsonBody.put("password", password);
            jsonBody.put("birthDate", formatter.format(birthDate)); // Include birth date in the JSON request
            jsonBody.put("emailAddress", emailAddress);

        } catch (DateTimeParseException | JSONException e) {
            e.printStackTrace();
        }

        RequestBody requestBody = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json"));

        // Build the PUT request
        Request request = new Request.Builder()
                .url("http://10.0.2.2:8080/api/user/update/" + userId)
                .post(requestBody)
                .build();


        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                Log.e("UpdateUser", "Network request failed: " + e.getMessage());

                // Log additional information if needed
                Log.d("UpdateUser", "Request URL: " + call.request().url());
                Log.d("UpdateUser", "Request Method: " + call.request().method());

                // You can log more details about the request if necessary

                runOnUiThread(() -> {
                    showToast("Network request failed. Check logs for details.");
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    // Handle the successful response if needed
                    Log.d("UpdateUser", "User update successful");
                    runOnUiThread(() -> {
                        showToast("Your account information updated successfully");
                        // Delay for 2 seconds before navigating back to GetUser activity
                        new Handler().postDelayed(() -> {
                            Intent intent = new Intent(UpdateUser.this, GetUser.class);
                            startActivity(intent);
                            finish();
                        }, 2000);
                    });
                } else {
                    Log.e("UpdateUser", "Unexpected response code: " + response.code());

                    // Log additional information if needed
                    Log.d("UpdateUser", "Request URL: " + call.request().url());
                    Log.d("UpdateUser", "Request Method: " + call.request().method());
                    Log.d("UpdateUser", "Response Body: " + response.body().string());

                    runOnUiThread(() -> {
                        showToast("Unexpected response code. Check logs for details.");
                    });
                }
            }
        });

    }
}