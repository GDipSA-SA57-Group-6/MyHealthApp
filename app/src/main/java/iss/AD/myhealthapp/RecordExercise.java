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
        /*
        {
            "userId": 1,
            "exerciseDate": "2024-02-01",
            "caloriesBurnt": 2000
        }
        */
        LocalDate exerciseDate = LocalDate.now();
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

        // Build the PUT request
        Request request = new Request.Builder()
                .url("http://192.168.1.98:8080/api/daily-exercise/update-calories")
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



/* CORRECT CODE BEFORE ADD
public class RecordExercise extends AppCompatActivity {
    Button btnPost;
    Spinner spinnerExerciseChoice, spinnerExerciseTime;
    List<Exercise> exerciseChoiceList;
    List<String> exerciseTimeList;
    ArrayAdapter<String> exerciseChoiceAdapter;
    ArrayAdapter<String> exerciseTimeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_exercise);

        btnPost = findViewById(R.id.btnPost);

        spinnerExerciseChoice = findViewById(R.id.spinnerExerciseChoice);
        spinnerExerciseTime = findViewById(R.id.spinnerExerciseTime);

        // Create a new ArrayList with the Exercise objects at the class level
        exerciseChoiceList = new ArrayList<>();
        // Add Exercise objects to the list
        exerciseChoiceList.add(new Exercise("Running", 360));
        exerciseChoiceList.add(new Exercise("Swimming", 250));
        exerciseChoiceList.add(new Exercise("Walking", 150));
        exerciseChoiceList.add(new Exercise("Badminton", 200));
        exerciseChoiceList.add(new Exercise("Cycling", 200));

        // Create an adapter for exercise choices
        exerciseChoiceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getExerciseNames());
        exerciseChoiceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerExerciseChoice.setAdapter(exerciseChoiceAdapter);
        // Set a listener to check if the user selects an item
        spinnerExerciseChoice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Handle item selection if needed
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
        // Set the default selected value to empty
        spinnerExerciseChoice.setSelection(0);


        // Initialize the list of exercise times
        exerciseTimeList = Arrays.asList("", "30 min", "60 min", "90 min", "120 min");
        // Create an adapter for exercise times
        exerciseTimeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, exerciseTimeList);
        exerciseTimeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerExerciseTime.setAdapter(exerciseTimeAdapter);
        spinnerExerciseTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Check if the selected item is empty
                if (position == 0) {
                    // Display a Toast message indicating that the user must choose a gender
                    showToast("Please choose a time");
                    // Disable form submission until a gender is chosen
                    btnPost.setEnabled(false);
                } else {
                    // Enable form submission when a gender is chosen
                    btnPost.setEnabled(true);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                btnPost.setEnabled(false);
            }
        });
        // Set the default selected value to empty
        spinnerExerciseTime.setSelection(0);
        btnPost.setEnabled(false);

        btnPost.setOnClickListener(view -> {
            // Calculate calories burnt for the selected exercise and time
            Exercise selectedExercise = exerciseChoiceList.get(spinnerExerciseChoice.getSelectedItemPosition());
            String selectedTime = (String) spinnerExerciseTime.getSelectedItem();
            int caloriesBurnt = calculateCaloriesBurntWithExerciseAndTime(selectedExercise, selectedTime);

            showToast("Calories Burnt: " + caloriesBurnt);
        });
    }

    private List<String> getExerciseNames() {
        List<String> exerciseNames = new ArrayList<>();
        for (Exercise exercise : exerciseChoiceList) {
            exerciseNames.add(exercise.getName());
        }
        return exerciseNames;
    }

    private int calculateCaloriesBurntWithExerciseAndTime(Exercise exercise, String exerciseTime) {
        // Assuming the exercise time is provided in the format "X min"
        int selectedTime = Integer.parseInt(exerciseTime.split(" ")[0]);

        // Calculate calories burnt based on the exercise's burn rate and selected time
        return (exercise.getCaloriesBurntPer30Minutes() * selectedTime) / 30;
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }
}
*/


/*
package iss.AD.myhealthapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;

import okhttp3.OkHttpClient;

public class RecordExercise extends AppCompatActivity {

    OkHttpClient client;
    Button btnReturn, btnPost;
    Spinner spinnerExerciseChoice, spinnerExerciseTime;
    List<String> exerciseChoiceList, exerciseTimeList;
    ArrayAdapter<String> exerciseChoiceAdapter, exerciseTimeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_exercise);

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

        spinnerExerciseChoice = findViewById(R.id.spinnerExerciseChoice);
        // Initialize the list and adapter with "Running" and "Swimming"
        //exerciseChoiceList = Arrays.asList("","Running", "Swimming","Walking","Badminton");
        exerciseChoiceList = Arrays.asList(//choice of exercise)
        exerciseChoiceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, exerciseChoiceList);
        exerciseChoiceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerExerciseChoice.setAdapter(exerciseChoiceAdapter);
        // Set a listener to check if the user selects an item
        spinnerExerciseChoice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
        // Set the default selected value to empty
        spinnerExerciseChoice.setSelection(0);




        spinnerExerciseTime = findViewById(R.id.spinnerExerciseTime);
        exerciseTimeList = Arrays.asList("","30 min", "60 min","90 min", "120 min");
        exerciseTimeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, exerciseTimeList);
        exerciseTimeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerExerciseTime.setAdapter(exerciseTimeAdapter);
        spinnerExerciseTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
        // Set the default selected value to empty
        spinnerExerciseTime.setSelection(0);

        //exercise per 30 minutes: swimming is 250, running is 360, walking is 150, badminton is 200, cycling is 200
        int caloriesBurnt = calculateCaloriesBurntWithExerciseAndTime(set of exercise and time);



        //if caloriesBurnt = 0 not allow for btnPost, showToast("Please choose your exercise choice and time to record.")

        btnPost = findViewById(R.id.btnPost);
        btnPost.setOnClickListener(view -> recordExercise(userId, caloriesBurnt));
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    private void calculateCaloriesBurntWithExerciseAndTime(){

    }

    private void recordExercise(int userId, int caloriesBurnt){

    }
}

*/

/*
public class ExerciseAdapter extends ArrayAdapter<Exercise> {

    public ExerciseAdapter(Context context, List<Exercise> exerciseList) {
        super(context, 0, exerciseList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Customize the view for the spinner dropdown items
        TextView textView = (TextView) super.getView(position, convertView, parent);
        textView.setText(getItem(position).getName()); // Display the exercise name
        return textView;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Customize the view for the spinner dropdown items
        TextView textView = (TextView) super.getDropDownView(position, convertView, parent);
        textView.setText(getItem(position).getName()); // Display the exercise name
        return textView;
    }
}
*/

/*
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    tools:context=".RecordExercise">
    <TextView
        android:id="@+id/recordExerciseTextView"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="32dp"
        android:textSize="40sp"
        android:gravity="center"
        android:text="Record Exercise"
        android:layout_marginBottom="16dp"/>



    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:textSize="20sp"
        android:text="Record my Exercise and Time:"
        android:layout_marginLeft="30dp"
        android:layout_marginTop="10dp"/>


    <!-- Exercise Selection -->
    <androidx.cardview.widget.CardView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:layout_margin="16dp"
        app:cardCornerRadius="8dp">

        <Spinner
            android:id="@+id/spinnerExerciseChoice"
            android:layout_width="240dp"
            android:layout_height="50dp"
            android:layout_marginLeft="20dp"
            android:layout_marginTop="10dp"
            android:hint="Select Exercise"
            android:spinnerMode="dropdown"
            android:ems="10" />

        <Spinner
            android:id="@+id/spinnerExerciseTime"
            android:layout_width="125dp"
            android:layout_height="50dp"
            android:layout_marginLeft="260dp"
            android:layout_marginTop="10dp"
            android:hint="Select Time"
            android:spinnerMode="dropdown"
            android:ems="10" />
    </androidx.cardview.widget.CardView>

    <!-- Add Record Button -->
    <Button
        android:id="@+id/btnAdd"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginLeft="270dp"
        android:layout_marginTop="5dp"
        android:text="+"
        android:textStyle="bold"
        android:textSize="25sp"/>


    <LinearLayout
        android:layout_marginTop="15dp"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal">

        <Button
            android:id="@+id/btnReturn"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Return"
            android:layout_marginLeft="100dp"
            android:layout_marginRight="30dp"/>

        <Button
            android:id="@+id/btnPost"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Submit"/>

    </LinearLayout>



</LinearLayout>
*/





    /*
    private void addExerciseRow() {
        // Inflate exercise entry layout
        View exerciseEntry = getLayoutInflater().inflate(R.layout.exercise_entry, null);

        // Find Spinners in the exercise entry layout
        Spinner spinnerExerciseChoice = exerciseEntry.findViewById(R.id.spinnerExerciseChoice);
        Spinner spinnerExerciseTime = exerciseEntry.findViewById(R.id.spinnerExerciseTime);

        // Create an adapter for exercise choices
        ArrayAdapter<String> exerciseChoiceAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, getExerciseNames());
        exerciseChoiceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerExerciseChoice.setAdapter(exerciseChoiceAdapter);

        // Create an adapter for exercise times
        ArrayAdapter<String> exerciseTimeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, exerciseTimeList);
        exerciseTimeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerExerciseTime.setAdapter(exerciseTimeAdapter);

        // Set unique IDs for Spinners to distinguish between entries
        spinnerExerciseChoice.setId(View.generateViewId());
        spinnerExerciseTime.setId(View.generateViewId());

        // Add exercise entry to the layout
        exerciseLayout.addView(exerciseEntry);
    }
    */