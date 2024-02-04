package iss.AD.myhealthapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
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

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Request;

public class GetUser extends AppCompatActivity {
    OkHttpClient client;
    private EditText txtUserId, txtUserName, txtPassword, txtBirthDate, txtEmail, txtGender;
    private Button btnReturn, btnUpdate, btnDelete;
    private TextView emailClick;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_user);

        txtUserId = findViewById(R.id.txtUserId);
        txtUserName = findViewById(R.id.txtUserName);
        txtPassword = findViewById(R.id.txtPassword);
        txtBirthDate = findViewById(R.id.txtBirthDate);
        txtEmail = findViewById(R.id.txtEmail);
        txtGender = findViewById(R.id.txtGender);

        btnReturn = findViewById(R.id.btnReturn);
        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GetUser.this, Dashboard.class);
                startActivity(intent);
            }
        });

        btnUpdate = findViewById(R.id.btnUpdate);
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GetUser.this, UpdateUser.class);
                startActivity(intent);
            }
        });

        client = new OkHttpClient();

        final SharedPreferences pref =
                getSharedPreferences("user_credentials", MODE_PRIVATE);
        Integer userId = pref.getInt("userId",-1);


        String apiUrl = "http://10.0.2.2:8080/api/user/get/" + String.valueOf(userId);

        Request request = new Request.Builder()
                .url(apiUrl)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                Log.e("GetUser", "Network request failed: " + e.getMessage());
                // Log additional details for debugging
                Log.d("GetUser", "Request URL: " + call.request().url());
                Log.d("GetUser", "Request Method: " + call.request().method());
                Log.d("GetUser", "Request Headers: " + call.request().headers());
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
                    }catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("GetUser", "Error parsing JSON response");
                    }
                } else {
                    Log.e("GetUser", "Unexpected response code: " + response.code());
                    // Log additional details for debugging
                    Log.d("GetUser", "Request URL: " + call.request().url());
                    Log.d("GetUser", "Request Method: " + call.request().method());
                    Log.d("GetUser", "Request Headers: " + call.request().headers());
                    Log.d("GetUser", "Response Body: " + response.body().string());
                }
            }
        });

        btnDelete = findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLogoutConfirmationDialog(userId);

            }
        });

        emailClick = findViewById(R.id.emailClick);
        emailClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("mailto:feedback@mha.com");
                Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
                intent.putExtra(Intent.EXTRA_SUBJECT, "For Feedback");
                intent.putExtra(Intent.EXTRA_TEXT,"Please state your feedback...");

                // Verify that the intent will resolve to an activity
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });
    }
    private void showLogoutConfirmationDialog(int userId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Delete Account");
        builder.setMessage("Are you sure you want to delete your account? Your account will be unrecoverable after delete.");
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked Confirm
                deleteUser(userId);
                deletePref();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
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

    private void deleteUser(int userId) {

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("userId", userId);

        } catch (DateTimeParseException | JSONException e) {
            e.printStackTrace();
        }

        RequestBody requestBody = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json"));

        String apiUrl = "http://10.0.2.2:8080/api/user/delete/" + userId;

        Request request = new Request.Builder()
                .url(apiUrl)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                Log.e("DeleteUser", "Network request failed: " + e.getMessage());

                runOnUiThread(() -> {
                    showToast("Network request failed. Check logs for details.");
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    // Handle the successful response if needed
                    Log.d("DeleteUser", "User deletion successful");

                    runOnUiThread(() -> {
                        showToast("Your account is successfully deleted.");
                        Intent intent = new Intent(GetUser.this, LoginPage.class);
                        startActivity(intent);

                    });
                } else {
                    Log.e("DeleteUser", "Unexpected response code: " + response.code());

                    // Log additional information if needed
                    Log.d("DeleteUser", "Request URL: " + call.request().url());
                    Log.d("DeleteUser", "Request Method: " + call.request().method());
                    Log.d("DeleteUser", "Response Body: " + response.body().string());

                    runOnUiThread(() -> {
                        showToast("Unexpected response code. Check logs for details.");
                    });
                }
            }
        });
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
//SUCCESS VERSION OF GET THE WHOLE USER 1 OBJECT:

public class GetUser extends AppCompatActivity {
    OkHttpClient client;
    TextView getUserTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_user);

        getUserTextView = findViewById(R.id.getUserTextView);

        client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("http://10.0.2.2:8080/api/user/get/1")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                Log.e("GetUser", "Network request failed: " + e.getMessage());
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    // Parse the JSON response manually
                    String responseBody = response.body().string();

                    runOnUiThread(() -> {
                        // UI updates on the main thread
                        getUserTextView.setText(responseBody);
                    });
                } else {
                    Log.e("GetUser", "Unexpected response code: " + response.code());
                }
            }
        });
    }
}

 */


    /*

    Button buttonPost;
    String getUrl = "http://localhost:8080/api/user/get/2";
    String postUrl = "http://localhost:8080/api/user/delete/1";

        buttonPost = findViewById(R.id.btnPost);
        buttonPost.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                postUser();
            }
        });
        */


    /*
    public void postUser(){
        RequestBody requestBody = new FormBody.Builder()
                .add("key_name","Demo Value")
                .build();

        Request request = new Request.Builder().url(postUrl).post(requestBody).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // Use response.body().string() to obtain the ResponseBody as a string
                            String responseBody = response.body().string();
                            getusertextview.setText(responseBody);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }
    */

        /*
        Request request = new Request.Builder().url(getUrl).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try (ResponseBody responseBody = response.body()) {
                            if (responseBody != null) {
                                String responseBodyString = responseBody.string();
                                getusertextview.setText(responseBodyString);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });*/
