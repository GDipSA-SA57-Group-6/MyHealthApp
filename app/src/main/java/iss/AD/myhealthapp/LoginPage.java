package iss.AD.myhealthapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class LoginPage extends AppCompatActivity {
    private EditText mUsernameTxt, mPasswordTxt;
    private Button mLoginBtn,mCreateBtn;
    OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        mCreateBtn = findViewById(R.id.btnCreate);
        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start CreateUserActivity when the button is clicked
                Intent intent = new Intent(LoginPage.this, CreateUser.class);
                startActivity(intent);
            }
        });

        SharedPreferences pref = getSharedPreferences(
                "user_credentials", MODE_PRIVATE);
        if (pref.contains("name") && pref.contains("password") && pref.contains("userId")) {
            startProtectedActivity();
        }


        mUsernameTxt = findViewById(R.id.txtUsername);
        mPasswordTxt = findViewById(R.id.txtPassword);
        mLoginBtn = findViewById(R.id.btnLogin);

        client = new OkHttpClient();

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = mUsernameTxt.getText().toString();
                String password = mPasswordTxt.getText().toString();
                logIn(name, password);
                //用于开发时跳过密码验证的部分
//                startProtectedActivity();
            }
        });
    }
    private void logIn(String name, String password) {
        // Build the JSON request body
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("name", name);
            jsonBody.put("password", password);

        } catch (JSONException e) {
            e.printStackTrace();
            showToast("Error creating JSON request body");
        }

        RequestBody requestBody = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json"));

        // Create the login request
        Request request = new Request.Builder()
                .url("http://10.0.2.2:8080/api/login")
                .post(requestBody)
                .build();


        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    ResponseBody responseBody = response.body();
                    if (responseBody != null) {
                        String bodyString = responseBody.string();
                        Log.d("LoginPage", "Response body: " + bodyString);

                        // Remove the check for JSON object
                        // Directly parse the response as a string
                        String userId = bodyString.trim();  // Trim to remove any leading or trailing whitespace

                        // Close the response body after use
                        responseBody.close();

                        // Store userId in SharedPreferences
                        SharedPreferences pref = getSharedPreferences("user_credentials", MODE_PRIVATE);
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putString("name", name);
                        editor.putString("password", password);
                        editor.putInt("userId", Integer.parseInt(userId));
                        editor.apply();

                        // Inform the user and proceed to the next activity
                        runOnUiThread(() -> {
                            showToast("Login successful!");
                            startProtectedActivity();
                        });
                    } else {
                        Log.d("LoginPage", "Empty response body");
                    }
                } else {
                    runOnUiThread(() -> showToast("Login user failed. Please enter correct Username and Password."));
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                Log.e("LoginPage", "Network request failed: " + e.getMessage());

            }
        });
    }

    private void startProtectedActivity() {
        Intent intent = new Intent(this, Dashboard.class);
        startActivity(intent);
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }
}


/*
    private void storeUserId(String name, String password) {
        // Build the JSON request body
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("name", name);
            jsonBody.put("password", password);

        } catch (JSONException e) {
            e.printStackTrace();
            showToast("Error creating JSON request body");
        }

        RequestBody requestBody = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url("http://10.0.2.2:8080/api/login/getuserid")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                Log.e("GetUser", "Network request failed: " + e.getMessage());
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseBody;

                if (response.isSuccessful()) {
                    responseBody = response.body().string();

                    try {
                        JSONObject jsonObject = new JSONObject(responseBody);

                        int userId = jsonObject.getInt("userId");

                        SharedPreferences pref = getSharedPreferences(
                                "user_credentials", MODE_PRIVATE);
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putInt("userId", userId);
                        editor.apply();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("GetUserId", "Error parsing JSON response");
                        throw new RuntimeException(e);
                    }
                } else {
                    Log.e("GetUserId", "Unexpected response code: " + response.code());
                }
            }
        });
    }
    */

/*
public class LoginPage extends AppCompatActivity {
    private EditText mUsernameTxt, mPasswordTxt;
    private Button mLoginBtn,mCreateBtn;
    OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        mCreateBtn = findViewById(R.id.btnCreate);
        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start CreateUserActivity when the button is clicked
                Intent intent = new Intent(LoginPage.this, CreateUser.class);
                startActivity(intent);
            }
        });

        mUsernameTxt = findViewById(R.id.txtUsername);
        mPasswordTxt = findViewById(R.id.txtPassword);
        mLoginBtn = findViewById(R.id.btnLogin);

        client = new OkHttpClient();

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = mUsernameTxt.getText().toString();
                String password = mPasswordTxt.getText().toString();
                logIn(name, password);
            }
        });
    }
    private void logIn(String name, String password) {
        // Build the JSON request body
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("name", name);
            jsonBody.put("password", password);

        } catch (JSONException e) {
            e.printStackTrace();
            showToast("Error creating JSON request body");
        }

        RequestBody requestBody = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json"));

        // Create the login request
        Request request = new Request.Builder()
                .url("http://10.0.2.2:8080/api/login")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    ResponseBody responseBody = response.body();
                    if (responseBody != null) {
                        String bodyString = responseBody.string();
                        Log.d("LoginPage", "Response body: " + bodyString);
                        responseBody.close();  // Close the response body after use

                        SharedPreferences pref = getSharedPreferences(
                                "user_credentials", MODE_PRIVATE);
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putString("name", name);
                        editor.putString("password", password);
                        editor.apply();
                        // Inform the user and proceed to the next activity
                        runOnUiThread(() -> {
                            showToast("Login successful!");
                            startProtectedActivity();
                        });
                    } else {
                        Log.d("LoginPage", "Empty response body");
                    }
                }else{
                    runOnUiThread(() -> {
                        showToast("Login user failed. Please enter correct Username and Password.");
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                Log.e("LoginPage", "Network request failed: " + e.getMessage());

            }
        });
    }

    private void startProtectedActivity() {
        Intent intent = new Intent(this, Dashboard.class);
        startActivity(intent);
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }
}
 */
