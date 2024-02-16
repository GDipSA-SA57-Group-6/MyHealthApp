package iss.AD.myhealthapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;


import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.List;
import iss.AD.myhealthapp.network.VideoApiService;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import android.widget.Toast;
import java.util.ArrayList;
import retrofit2.http.GET;
import retrofit2.http.Query;



import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.Request;

public class DailySummary extends AppCompatActivity {
    OkHttpClient client;
    //Button mBtnSetHealthTarget, mBtnRecordExercise;
    private NutritionTracker proteinTracker, fatsTracker, carbsTracker;

    //Calories
    private ProgressBar progressBar;
    private TextView textViewSuggestion, textViewSuggestionTitle;

    private TextView textViewNumFoodIntake, textViewNumExerciseCaloriesBurned,textViewNumCompare,textViewNumCaloriesRequired;

    private int progressColor;
    private int caloriesRequired,exerciseCaloriesBurned,dailyCarbsSum,dailyFatsSum,dailyCalSum,dailyProteinSum;
    private String genderInClass;
    private VideoApiService apiService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_summary);

        //初始化retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(VideoApiService.class);

        /*
        Button mBtnSetHealthTarget = findViewById(R.id.btnSetHealthTarget);
        mBtnSetHealthTarget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DailySummary.this, SetHealthTarget.class);
                startActivity(intent);
            }
        });

        Button mBtnRecordExercise = findViewById(R.id.btnRecordExercise);
        mBtnRecordExercise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DailySummary.this, RecordExercise.class);
                startActivity(intent);
            }
        });


        Button mBtnRecordFoodConsumption = findViewById(R.id.btnRecordFoodConsumption);
        mBtnRecordFoodConsumption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DailySummary.this, SelectActivity.class));
            }
        });
        */

        Button mBtnReturn = findViewById(R.id.btnReturn);
        mBtnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start CreateUserActivity when the button is clicked
                Intent intent = new Intent(DailySummary.this, Dashboard.class);
                startActivity(intent);
            }
        });


        client = new OkHttpClient();

        final SharedPreferences pref =
                getSharedPreferences("user_credentials", MODE_PRIVATE);
        int userId = pref.getInt("userId",-1);


        textViewNumFoodIntake = findViewById(R.id.textViewNumFoodIntake);
        textViewNumExerciseCaloriesBurned = findViewById(R.id.textViewNumExerciseCaloriesBurned);
        textViewNumCompare = findViewById(R.id.textViewNumCompare);
        textViewNumCaloriesRequired = findViewById(R.id.textViewNumCaloriesRequired);

        textViewSuggestionTitle = findViewById(R.id.textViewSuggestionTitle);
        String text = "Suggestion to optimise your health:";
        SpannableString spannableString = new SpannableString(text);
        spannableString.setSpan(new UnderlineSpan(), 0, text.length(), 0);
        textViewSuggestionTitle.setText(spannableString);

        progressBar = findViewById(R.id.progressBar);
        textViewSuggestion = findViewById(R.id.textViewSuggestion);

        // Initialize NutritionTracker instance
        proteinTracker = new NutritionTracker(
                findViewById(R.id.proteinProgressBar),
                findViewById(R.id.proteinTextViewTarget),
                findViewById(R.id.proteinTextViewStatus),
                findViewById(R.id.proteinTextViewDifferent)
        );

        fatsTracker = new NutritionTracker(
                findViewById(R.id.fatsProgressBar),
                findViewById(R.id.fatsTextViewTarget),
                findViewById(R.id.fatsTextViewStatus),
                findViewById(R.id.fatsTextViewDifferent)
        );

        carbsTracker = new NutritionTracker(
                findViewById(R.id.carbsProgressBar),
                findViewById(R.id.carbsTextViewTarget),
                findViewById(R.id.carbsTextViewStatus),
                findViewById(R.id.carbsTextViewDifferent)
        );


        // CountDownLatch #1
        CompletableFuture<Void> firstTask = getGenderTargetCaloriesByUserId(userId);

        firstTask.thenRun(() -> {
            // This code will run after the completion of the first task
            CountDownLatch latch = new CountDownLatch(2); // Number of asynchronous tasks after the first one

            // Execute the second task
            getCaloriesBurntTodayByUserId(userId, latch);//set this as second task

            // Execute the third task
            getDailySubmissionByUserId(userId, latch);

            // Use await to wait for all requests to complete
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            updateUI();

        });


        //video button
//        Button btnVideo = findViewById(R.id.btnVideo);
//        btnVideo.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                int caloriesRequired = getCaloriesRequired();
//                int foodIntake = getDailyCalSum();
//                int exerciseCaloriesBurned = getExerciseCaloriesBurned();
//
//                int difference = foodIntake - exerciseCaloriesBurned - caloriesRequired;
//
//                // 根据difference的值决定调用哪个API
//                int type = (difference <= 150) ? 6 : (difference <= 350) ? 1 : 2;
//                retrofit2.Call<List<VideoInfo>> call = apiService.getVideosByType(type);
//
//
//                call.enqueue(new retrofit2.Callback<List<VideoInfo>>() {
//                    @Override
//                    public void onResponse(retrofit2.Call<List<VideoInfo>> call, retrofit2.Response<List<VideoInfo>> response) {
//                        if (response.isSuccessful()) {
//                            List<VideoInfo> videoList = response.body();
//                            Intent intent = new Intent(DailySummary.this, VideoPageActivity.class);
//                            intent.putParcelableArrayListExtra("videoList", (ArrayList<VideoInfo>) videoList);
//                            startActivity(intent);
//                        } else {
//                            Toast.makeText(DailySummary.this, "Error fetching videos.", Toast.LENGTH_LONG).show();
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(retrofit2.Call<List<VideoInfo>> call, Throwable t) {
//                        Toast.makeText(DailySummary.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                });
//            }
//        });


    }

    //SAVE TO CLASS
    public void saveToClassGenderByUserId(String gender) {
        this.genderInClass = gender;
        Log.d("DailySummary", "Gender saved to class: " + gender);
    }

    public String getGenderInClass() {
        Log.d("DailySummary", "Returning gender from class: " + genderInClass);
        return genderInClass;
    }

    public void saveToClassCaloriesRequiredByUserId(int targetCalories) {
        this.caloriesRequired = targetCalories;
        Log.d("DailySummary", "Calories required saved to class: " + targetCalories);
    }

    public int getCaloriesRequired() {
        Log.d("DailySummary", "Returning calories required from class: " + caloriesRequired);
        return caloriesRequired;
    }

    public void saveToClassExerciseCaloriesBurnedByUserId(int caloriesBurnt) {
        this.exerciseCaloriesBurned = caloriesBurnt;
        Log.d("DailySummary", "Exercise calories burned saved to class: " + caloriesBurnt);
    }

    public int getExerciseCaloriesBurned() {
        Log.d("DailySummary", "Returning exercise calories burned from class: " + exerciseCaloriesBurned);
        return exerciseCaloriesBurned;
    }

    public void saveToClassProteinSumByUserId(int dailyProteinSum) {
        this.dailyProteinSum = dailyProteinSum;
    }

    public int getDailyProteinSum() {
        return dailyProteinSum;
    }

    public void saveToClassFatsSumByUserId(int dailyFatsSum) {
        this.dailyFatsSum = dailyFatsSum;
    }

    public int getDailyFatsSum() {
        return dailyFatsSum;
    }

    public void saveToClassCarbsSumByUserId(int dailyCarbsSum) {
        this.dailyCarbsSum = dailyCarbsSum;
    }

    public int getDailyCarbsSum() { return dailyCarbsSum; }

    public void saveToClassCalSumByUserId(int dailyCalSum) {
        this.dailyCalSum = dailyCalSum;
    }

    public int getDailyCalSum() { return dailyCalSum; }



    // CountDownLatch #1
    public CompletableFuture<Void> getGenderTargetCaloriesByUserId(int userId){
        CompletableFuture<Void> future = new CompletableFuture<>();
        String local_host = getResources().getString(R.string.local_host);
        String apiUrl = "http://" + local_host + ":8080/api/user/get/" + userId;

        Request request = new Request.Builder()
                .url(apiUrl)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                Log.e("getGenderTargetCaloriesByUserId", "Network request failed: " + e.getMessage());
                future.completeExceptionally(e);
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
                        //String genderUp = gender.substring(0, 1).toUpperCase() + gender.substring(1);
                        String emailAddress = jsonObject.getString("emailAddress");
                        int targetCalories = jsonObject.getInt("targetCalories");

                        Log.d("getGenderTargetCaloriesByUserId", "User details fetched successfully. Gender: " + gender + ", Target Calories: " + targetCalories);

                        saveToClassGenderByUserId(gender);
                        saveToClassCaloriesRequiredByUserId(targetCalories);
                        future.complete(null);


                    }catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("getGenderTargetCaloriesByUserId", "Error parsing JSON response");
                        future.completeExceptionally(e);
                    }
                } else {
                    Log.e("getGenderTargetCaloriesByUserId", "Unexpected response code: " + response.code());
                    future.completeExceptionally(new RuntimeException("Unexpected response code: " + response.code()));
                }
            }
        });
        return future;
    }

    public void getCaloriesBurntTodayByUserId(int userId, CountDownLatch latch){
        String local_host = getResources().getString(R.string.local_host);
        String apiUrl = "http://" + local_host + ":8080/api/daily-exercise/calories-burnt-today/" + userId;

        Request request = new Request.Builder()
                .url(apiUrl)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                Log.e("getCaloriesBurntTodayByUserId", "Network request failed: " + e.getMessage());
                latch.countDown();
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseBody;

                if (response.isSuccessful()) {
                    responseBody = response.body().string();
                    try {
                        int caloriesBurnt = Integer.parseInt(responseBody);

                        Log.d("CaloriesBurnt", "Calories burnt today: " + caloriesBurnt);

                        saveToClassExerciseCaloriesBurnedByUserId(caloriesBurnt);
                        latch.countDown();


                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        Log.e("getCaloriesBurntTodayByUserId", "Error parsing calories burnt as an integer");
                        latch.countDown();
                    }
                } else {
                    Log.e("getCaloriesBurntTodayByUserId", "Unexpected response code: " + response.code());
                    latch.countDown();
                }
            }
        });
    }

    public void getDailySubmissionByUserId(int userId, CountDownLatch latch) {
        String localHost = getResources().getString(R.string.local_host);
        String apiUrl = "http://" + localHost + ":8080/api/get-submission-by-userid/" + userId;

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(apiUrl)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Handle failure (e.g., network issues)
                e.printStackTrace();
                latch.countDown(); // Signal that the operation is complete
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        // Handle the JSON response
                        System.out.println(responseBody);

                        //Parse the JSON response if needed
                        JSONObject jsonObject = new JSONObject(responseBody);
                        int carbSum = jsonObject.getInt("carb_sum");
                        int fatSum = jsonObject.getInt("fat_sum");
                        int calSum = jsonObject.getInt("cal_sum");
                        int proteinSum = jsonObject.getInt("protein_sum");

                        saveToClassCarbsSumByUserId(carbSum);
                        saveToClassFatsSumByUserId(fatSum);
                        saveToClassCalSumByUserId(calSum);
                        saveToClassProteinSumByUserId(proteinSum);
                        latch.countDown();

                    } else {
                        // Handle unsuccessful response (e.g., non-2xx status code)
                        System.out.println("Unsuccessful response: " + response.code());
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                } finally {
                    response.close();
                    latch.countDown(); // Signal that the operation is complete
                }
            }
        });
    }




    public void updateUI(){
        Log.d("DailySummary", "Updating UI");
        runOnUiThread(() -> {
            //Nutrition
            String gender = getGenderInClass();
            int proteinTarget = proteinTargetBasedOnUserGender(gender);
            int fatsTarget = fatsTargetBasedOnUserGender(gender);
            int carbsTarget = carbsTargetBasedOnUserGender(gender);

            proteinTracker.setTarget(proteinTarget);
            proteinTracker.setCurrentStatus(getDailyProteinSum());
            proteinTracker.trackNutrition(this);

            fatsTracker.setTarget(fatsTarget);
            fatsTracker.setCurrentStatus(getDailyFatsSum());
            fatsTracker.trackNutrition(this);

            carbsTracker.setTarget(carbsTarget);
            carbsTracker.setCurrentStatus(getDailyCarbsSum());
            carbsTracker.trackNutrition(this);

            //Calories
            int caloriesRequired = getCaloriesRequired();
            int foodIntake = getDailyCalSum();
            int exerciseCaloriesBurned = getExerciseCaloriesBurned();

            textViewNumCaloriesRequired.setText(String.valueOf(caloriesRequired));
            textViewNumFoodIntake.setText(String.valueOf(foodIntake));
            textViewNumExerciseCaloriesBurned.setText(String.valueOf(exerciseCaloriesBurned));

            int difference = foodIntake - exerciseCaloriesBurned - caloriesRequired;

            if (difference == 0) {
                textViewNumCompare.setText("=");
            } else if (difference < -100) {
                textViewNumCompare.setText("<");
            } else if (difference >= -100 && difference <= 100) {
                textViewNumCompare.setText("≈");
            } else if (difference > 100) {
                textViewNumCompare.setText(">");
            }

            suggestionBasedOnHealthTarget(caloriesRequired, foodIntake, exerciseCaloriesBurned);
        });
    }

    private int proteinTargetBasedOnUserGender(String gender) {
        if ("male".equalsIgnoreCase(gender)) {
            return 56;
        } else if ("female".equalsIgnoreCase(gender)) {
            return 46;
        } else {
            return -1;
        }
    }

    private int fatsTargetBasedOnUserGender(String gender) {
        if ("male".equalsIgnoreCase(gender)) {
            return 90;
        } else if ("female".equalsIgnoreCase(gender)) {
            return 70;
        } else {
            return -1;
        }
    }

    private int carbsTargetBasedOnUserGender(String gender) {
        if ("male".equalsIgnoreCase(gender)) {
            return 1680;
        } else if ("female".equalsIgnoreCase(gender)) {
            return 1200;
        } else {
            return -1;
        }
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