package iss.AD.myhealthapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CreateUser extends AppCompatActivity {

    OkHttpClient client;
    private EditText txtUserName,txtPassword,txtBirthDate,txtEmail, txtPasswordConfirm;
    Button btnCreateUser,btnReturnLoginPage;
    Spinner spinnerGender;
    List<String> genderList;
    ArrayAdapter<String> genderAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user);
        txtUserName = findViewById(R.id.txtUserName);
        txtPassword = findViewById(R.id.txtPassword);
        txtPasswordConfirm = findViewById(R.id.txtPasswordConfirm);
        txtBirthDate = findViewById(R.id.txtBirthDate);
        txtEmail = findViewById(R.id.txtEmail);

        btnCreateUser = findViewById(R.id.btnPost);
        btnCreateUser.setOnClickListener(view -> createUser());

        spinnerGender = findViewById(R.id.spinnerGender);
        // Initialize the list and adapter with "Male" and "Female"
        genderList = Arrays.asList("","Male", "Female");
        genderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, genderList);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(genderAdapter);
        // Set a listener to check if the user selects an item
        spinnerGender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Check if the selected item is empty
                if (position == 0) {
                    // Display a Toast message indicating that the user must choose a gender
                    showToast("Please choose a gender");
                    // Disable form submission until a gender is chosen
                    btnCreateUser.setEnabled(false);
                } else {
                    // Enable form submission when a gender is chosen
                    btnCreateUser.setEnabled(true);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing here
                // Disable form submission until a gender is chosen
                btnCreateUser.setEnabled(false);
            }
        });
        // Set the default selected value to empty
        spinnerGender.setSelection(0);
        // Disable form submission until a gender is chosen
        btnCreateUser.setEnabled(false);

        client = new OkHttpClient();

        btnReturnLoginPage = findViewById(R.id.btnReturnLoginPage);
        btnReturnLoginPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start CreateUserActivity when the button is clicked
                Intent intent = new Intent(CreateUser.this, LoginPage.class);
                startActivity(intent);
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
    private void createUser() {
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

        String passwordConfirm = txtPasswordConfirm.getText().toString().trim();

        if (!password.equals(passwordConfirm)) {
            showToast("Passwords do not match. Please enter the same password in both fields.");
            return;
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

        String selectedGender = spinnerGender.getSelectedItem().toString();

        // Build the JSON request body
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("name", name);
            jsonBody.put("password", password);
            jsonBody.put("birthDate", formatter.format(birthDate)); // Include birth date in the JSON request
            jsonBody.put("emailAddress", emailAddress);
            jsonBody.put("gender", selectedGender.toLowerCase());
        } catch (DateTimeParseException | JSONException e) {
            e.printStackTrace();
        }

        RequestBody requestBody = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json"));

        // Build the POST request
        Request request = new Request.Builder()
                .url("http://192.168.1.98:8080/api/user/create")
                .post(requestBody)
                .build();

        // Make the POST request
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                Log.e("CreateUser", "Network request failed: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    // Handle the successful response if needed
                    Log.d("CreateUser", "User creation successful");
                    runOnUiThread(() -> {
                        showToast("Account has been created successfully");
                        new Handler().postDelayed(() -> {
                            Intent intent = new Intent(CreateUser.this, LoginPage.class);
                            startActivity(intent);
                            finish();
                        }, 2000);
                    });
                } else {
                    Log.e("CreateUser", "Unexpected response code: " + response.code());
                }
            }
        });
    }
}