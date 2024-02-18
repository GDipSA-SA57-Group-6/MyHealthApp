package iss.AD.myhealthapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class RecordExercise extends AppCompatActivity {
    OkHttpClient client;
    private LinearLayout container;
    private Button minusButton, addButton, postButton, btnReturn, btnInfoDisplay;
    private List<Exercise> exercises;
    private String[] timeArray = {"", "30 min", "60 min", "90 min", "120 min"};
    private int caloriesBurnt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_exercise);

        container = findViewById(R.id.container);
        addButton = findViewById(R.id.btnAdd);
        postButton = findViewById(R.id.btnPost);

        client = new OkHttpClient();

        final SharedPreferences pref =
                getSharedPreferences("user_credentials", MODE_PRIVATE);
        Integer userId = pref.getInt("userId", -1);

        btnReturn = findViewById(R.id.btnReturn);
        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start CreateUserActivity when the button is clicked
                Intent intent = new Intent(RecordExercise.this, Dashboard.class);
                startActivity(intent);
            }
        });

        exercises = new ArrayList<>();
        exercises.add(new Exercise("Running", 360));
        exercises.add(new Exercise("Swimming", 250));
        exercises.add(new Exercise("Walking", 150));
        exercises.add(new Exercise("Badminton", 200));
        exercises.add(new Exercise("Cycling", 200));

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addExerciseRow();
            }
        });

        minusButton = findViewById(R.id.btnMinus); // Assuming you have a button with id btnMinus in your layout
        minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeExerciseRow();
            }
        });

        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calculateTotalCalories(userId);
            }
        });


        btnInfoDisplay = findViewById(R.id.btnInfoDisplay);
        btnInfoDisplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RecordExercise.this, RecordExerciseDisplay.class);
                startActivity(intent);
            }
        });

    }

    private void addExerciseRow() {
        if (container.getChildCount() < 9) {
            View rowView = getLayoutInflater().inflate(R.layout.exercise_entry, null, false);
            container.addView(rowView);

            Spinner exerciseSpinner = rowView.findViewById(R.id.exerciseSpinner);
            Spinner timeSpinner = rowView.findViewById(R.id.timeSpinner);

            ArrayAdapter<Exercise> exerciseAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, exercises);
            exerciseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            exerciseSpinner.setAdapter(exerciseAdapter);
            timeSpinner.setAdapter(createTimeAdapter());
        } else {
            Toast.makeText(RecordExercise.this, "Maximum limit reached (9 items)", Toast.LENGTH_LONG).show();
        }
    }

    private void calculateTotalCalories(int userId) {
        if (container.getChildCount() == 0) {
            Toast.makeText(RecordExercise.this, "Please add an exercise record", Toast.LENGTH_LONG).show();
            return;
        }

        int totalCalories = 0;
        boolean allTimesSelected = true;

        for (int i = 0; i < container.getChildCount(); i++) {
            View rowView = container.getChildAt(i);
            Spinner exerciseSpinner = rowView.findViewById(R.id.exerciseSpinner);
            Spinner timeSpinner = rowView.findViewById(R.id.timeSpinner);

            Exercise selectedExercise = (Exercise) exerciseSpinner.getSelectedItem();
            String selectedTime = timeSpinner.getSelectedItem().toString();

            if (selectedTime.isEmpty()) {
                allTimesSelected = false;
                break;  // No need to continue checking other spinners if one is empty
            } else {
                int timeInMinutes = Integer.parseInt(selectedTime.split(" ")[0]);
                totalCalories += (selectedExercise.getCaloriesBurntPer30Minutes() * timeInMinutes) / 30;
            }
        }

        if (allTimesSelected) {
            //how to pass this to class variable?
            int caloriesBurnt = totalCalories;
            recordExerciseToDatabase(userId,caloriesBurnt);
        } else {
            Toast.makeText(RecordExercise.this, "Please select time for all exercises", Toast.LENGTH_LONG).show();
        }
    }

    private ArrayAdapter<CharSequence> createTimeAdapter() {
        return new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, timeArray);
    }

    private void removeExerciseRow() {
        int childCount = container.getChildCount();
        if (childCount > 0) {
            container.removeViewAt(childCount - 1);
        } else {
            Toast.makeText(RecordExercise.this, "No rows to remove", Toast.LENGTH_LONG).show();
        }
    }

    private void recordExerciseToDatabase(int userId, int caloriesBurnt){
        ZoneId zoneId = ZoneId.of("Asia/Singapore");
        LocalDate exerciseDate = LocalDate.now(zoneId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("userId", userId);
            jsonBody.put("exerciseDate", formatter.format(exerciseDate));
            jsonBody.put("caloriesBurnt", caloriesBurnt);

        } catch (DateTimeParseException | JSONException e) {
            e.printStackTrace();
        }

        RequestBody requestBody = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json"));
        String local_host = getResources().getString(R.string.local_host);
        // Build the PUT request
        Request request = new Request.Builder()
                .url("http://" + local_host + ":8080/api/daily-exercise/update-calories")
                .post(requestBody)
                .build();


        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                Log.e("RecordExercise", "Network request failed: " + e.getMessage());

                // Log additional information if needed
                Log.d("RecordExercise", "Request URL: " + call.request().url());
                Log.d("RecordExercise", "Request Method: " + call.request().method());

                // You can log more details about the request if necessary

                runOnUiThread(() -> {
                    Toast.makeText(RecordExercise.this, "Network request failed. Check logs for details.", Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    // Handle the successful response if needed
                    Log.d("RecordExercise", "Exercise recorded successful");
                    runOnUiThread(() -> {
                        Toast.makeText(RecordExercise.this, ("Total calories burnt by exercise: "  +
                                + caloriesBurnt + "\n have been recorded successfully."), Toast.LENGTH_LONG).show();
                        // Delay for 2 seconds before navigating back to GetUser activity
                        new Handler().postDelayed(() -> {
                            Intent intent = new Intent(RecordExercise.this, Dashboard.class);
                            startActivity(intent);
                            finish();
                        }, 2000);
                    });
                } else {
                    Log.e("RecordExercise", "Unexpected response code: " + response.code());

                    // Log additional information if needed
                    Log.d("RecordExercise", "Request URL: " + call.request().url());
                    Log.d("RecordExercise", "Request Method: " + call.request().method());
                    Log.d("RecordExercise", "Response Body: " + response.body().string());

                    runOnUiThread(() -> {
                        Toast.makeText(RecordExercise.this, "Unexpected response code. Check logs for details.", Toast.LENGTH_LONG).show();
                    });
                }
            }
        });

    }
}