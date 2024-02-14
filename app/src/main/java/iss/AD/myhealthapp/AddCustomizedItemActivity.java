package iss.AD.myhealthapp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddCustomizedItemActivity extends AppCompatActivity {
    EditText mcustomize_nameOfFood;
    EditText mcustomize_quantity;
    EditText mcustomize_calorie;
    EditText mcustomize_protein;
    EditText mcustomize_fat;
    EditText mcustomize_carbohydrates;
    Button mAddToCountBtn,mReturnBtn;
    private ImageView imageView;
    private Button buttonPickImage;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private Bitmap temporaryBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_customized_item);

        mcustomize_nameOfFood = findViewById(R.id.customize_nameOfFood);
        mcustomize_quantity = findViewById(R.id.customize_quantity);
        mcustomize_calorie = findViewById(R.id.customize_calorie);
        mcustomize_protein = findViewById(R.id.customize_protein);
        mcustomize_fat = findViewById(R.id.customize_fat);
        mcustomize_carbohydrates = findViewById(R.id.customize_carbohydrates);


        mAddToCountBtn = findViewById(R.id.AddToCountBtn);
//        mAddToCountBtn.setOnClickListener(view -> createFood());
        mAddToCountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createFood();
                startActivity(new Intent(AddCustomizedItemActivity.this, SelectActivity.class));
            }
        });


        imageView = findViewById(R.id.imageView);
        buttonPickImage = findViewById(R.id.buttonPickImage);
        // Initialize the Activity Result Launcher
        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {

                            temporaryBitmap = loadBitmapFromUri(imageUri);
                            imageView.setImageBitmap(temporaryBitmap);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
        buttonPickImage.setOnClickListener(view -> openImagePicker());


        mReturnBtn = findViewById(R.id.returnBtn);
        mReturnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private Bitmap loadBitmapFromUri(Uri imageUri) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return ImageDecoder.decodeBitmap(ImageDecoder.createSource(getContentResolver(), imageUri));
        } else {
            return MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
        }
    }

    private void saveTemporaryImage(Bitmap temporaryBitmap, String name) {
        if (temporaryBitmap != null) {
            // Save the temporary image to internal storage
            try {
                // Get user ID from SharedPreferences
                SharedPreferences pref = getSharedPreferences("user_credentials", MODE_PRIVATE);
                int userId = pref.getInt("userId", -1);

                // Create a file name with user ID tag
                String fileName = "food_image_" + userId + "_" + name + ".jpg";

                // Open file output stream
                OutputStream outputStream = openFileOutput(fileName, MODE_PRIVATE);

                // Compress the bitmap to JPEG format
                temporaryBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

                // Close the output stream
                outputStream.close();

                // Display a message or handle the file as needed
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void createFood() {
        final SharedPreferences pref =
                getSharedPreferences("user_credentials", MODE_PRIVATE);
        int userId = pref.getInt("userId",-1);

        String name = mcustomize_nameOfFood.getText().toString().trim();
        String quantity_description = mcustomize_quantity.getText().toString().trim();
        String cal = mcustomize_calorie.getText().toString().trim();
        String protein = mcustomize_protein.getText().toString().trim();
        String fat = mcustomize_fat.getText().toString().trim();
        String carb = mcustomize_carbohydrates.getText().toString().trim();
        String myUserId = Integer.toString(userId);

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("name", name);
            jsonBody.put("quantity_description", quantity_description);
            jsonBody.put("cal", cal);
            jsonBody.put("protein", protein);
            jsonBody.put("fat", fat);
            jsonBody.put("cab", carb);
            jsonBody.put("userId", myUserId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonBody.toString());
        String local_host = getResources().getString(R.string.local_host);

        // Build the POST request
        Request request = new Request.Builder()
                .url("http://" + local_host + ":8080/api/food")
                .post(requestBody)
                .build();

        // Make the POST request
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    // Handle the successful response if needed
                    Log.d("CreateFood", "Food creation successful");
                    saveTemporaryImage(temporaryBitmap, name);
                    runOnUiThread(() -> {
                        // Show Toast
                        Toast.makeText(AddCustomizedItemActivity.this, "Food has been created successfully", Toast.LENGTH_LONG).show();

                        // Navigate back to LoginPage
//                                Intent intent = new Intent(AddCustomizedItemActivity.this, LoginPage.class);
//                                startActivity(intent);
//                                finish(); // Close the current activity to prevent going back to it with the back button
                    });
                } else {
                    Log.e("CreateFood", "Unexpected response code: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                Log.e("CreateFood", "Network request failed: " + e.getMessage());
            }

        });
    }
}


/*
package iss.AD.myhealthapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddCustomizedItemActivity extends AppCompatActivity {
    EditText mcustomize_nameOfFood;
    EditText mcustomize_quantity;
    EditText mcustomize_calorie;
    EditText mcustomize_protein;
    EditText mcustomize_fat;
    EditText mcustomize_carbohydrates;
    Button mAddToCountBtn,mReturnBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_customized_item);

        mcustomize_nameOfFood = findViewById(R.id.customize_nameOfFood);
        mcustomize_quantity = findViewById(R.id.customize_quantity);
        mcustomize_calorie = findViewById(R.id.customize_calorie);
        mcustomize_protein = findViewById(R.id.customize_protein);
        mcustomize_fat = findViewById(R.id.customize_fat);
        mcustomize_carbohydrates = findViewById(R.id.customize_carbohydrates);


        mAddToCountBtn = findViewById(R.id.AddToCountBtn);
//        mAddToCountBtn.setOnClickListener(view -> createFood());
        mAddToCountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createFood();
                startActivity(new Intent(AddCustomizedItemActivity.this, SelectActivity.class));
            }
        });

        mReturnBtn = findViewById(R.id.returnBtn);
        mReturnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }
    private void createFood() {
        final SharedPreferences pref =
                getSharedPreferences("user_credentials", MODE_PRIVATE);
        int userId = pref.getInt("userId",-1);

        String name = mcustomize_nameOfFood.getText().toString().trim();
        String quantity_description = mcustomize_quantity.getText().toString().trim();
        String cal = mcustomize_calorie.getText().toString().trim();
        String protein = mcustomize_protein.getText().toString().trim();
        String fat = mcustomize_fat.getText().toString().trim();
        String carb = mcustomize_carbohydrates.getText().toString().trim();
        String myUserId = Integer.toString(userId);

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("name", name);
            jsonBody.put("quantity_description", quantity_description);
            jsonBody.put("cal", cal);
            jsonBody.put("protein", protein);
            jsonBody.put("fat", fat);
            jsonBody.put("carbrea", carb);
            jsonBody.put("userId", myUserId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonBody.toString());
        String local_host = getResources().getString(R.string.local_host);

        // Build the POST request
        Request request = new Request.Builder()
                .url("http://" + local_host + ":8080/api/food")
                .post(requestBody)
                .build();

        // Make the POST request
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    // Handle the successful response if needed
                    Log.d("CreateFood", "Food creation successful");
                    runOnUiThread(() -> {
                        // Show Toast
                        Toast.makeText(AddCustomizedItemActivity.this, "Food has been created successfully", Toast.LENGTH_LONG).show();

                        // Navigate back to LoginPage
//                                Intent intent = new Intent(AddCustomizedItemActivity.this, LoginPage.class);
//                                startActivity(intent);
//                                finish(); // Close the current activity to prevent going back to it with the back button
                    });
                } else {
                    Log.e("CreateFood", "Unexpected response code: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                Log.e("CreateFood", "Network request failed: " + e.getMessage());
            }

        });
    }
}
 */