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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ViewItemRecordedActivity extends AppCompatActivity{

    TextView mtextview;
    Button mcalculateBtn, mBackToSelectBtn;
    //List<Food> myFoodList = new ArrayList<>();
    HashMap<Food, Integer> receivedData = null;
    int classUserId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_item_recorded);

        mtextview =findViewById(R.id.textView);

        final SharedPreferences pref =
                getSharedPreferences("user_credentials", MODE_PRIVATE);
        int userId = pref.getInt("userId",-1);
        saveToClassUserId(userId);


        Intent intent = getIntent();

        if (intent.hasExtra("data")) {
            receivedData = (HashMap<Food, Integer>) intent.getSerializableExtra("data");

            Set<Food> keySet = receivedData.keySet();

            if (!keySet.isEmpty()) {
                StringBuilder displayText = new StringBuilder();

                for (Food food : keySet) {
                    String name = food.getName();
                    String description = food.getQuantityDescription();
                    int times= receivedData.get(food);
                    //myFoodList.add(food);
                    displayText.append(name + ",").append(" ").append(description+ ",").append(" ").append("have "+ times + " times").append("\n");
                }


                String nameOfFood = intent.getStringExtra("nameOfFood");

                mtextview.setText(displayText.toString());

            }
            else {
                mtextview.setText("No data available");
            }




        }

        mBackToSelectBtn = findViewById(R.id.backToSelectBtn);

        mBackToSelectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        mcalculateBtn = findViewById(R.id.calculateBtn);
        mcalculateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calculate(receivedData);
                startActivity(new Intent(ViewItemRecordedActivity.this, Dashboard.class));
            }
        });
    }

    public void saveToClassUserId(int userId) {
        this.classUserId = userId;
        Log.d("saveToClassUserId", "saveToClassUserId: " + classUserId);
    }

    public int getClassUserId() {
        Log.d("getUserIdInClass", "getUserIdInClass: " + classUserId);
        return classUserId;
    }
    private void calculate(HashMap<Food, Integer> receivedData) {
        LocalDate currentDate = LocalDate.now();
        Submission mySubmission = new Submission();
        mySubmission.userId = getClassUserId();
        mySubmission.date = currentDate ;

        for (Map.Entry<Food, Integer> entry : receivedData.entrySet())
        {
            SubmissionItem submissionItem = new SubmissionItem();
            submissionItem.food = entry.getKey();
            submissionItem.times = entry.getValue();
            if (mySubmission.submissionItems == null)
                mySubmission.submissionItems = new ArrayList<>();
            mySubmission.submissionItems.add(submissionItem);
        }

        /*
        // https://stackoverflow.com/questions/51183967/deserialize-date-attribute-of-json-into-localdate/51401465
        private static final Gson gson = new GsonBuilder().registerTypeAdapter(LocalDate.class, new JsonDeserializer<LocalDate>() {
            @Override
            public LocalDate deserialize(JsonElement json, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
                return LocalDate.parse(json.getAsJsonPrimitive().getAsString());
            }
        }).create();

        // https://stackoverflow.com/questions/66716526/gson-does-not-correctly-serialize-localdate
        */

        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(new TypeToken<LocalDate>(){}.getType(), new LocalDateConverter());
        Gson gson = builder.create();
        //Gson gson = new Gson();
        String json = gson.toJson(mySubmission);

//        JSONObject jsonBody = new JSONObject();
//        try {
//            jsonBody.put("name", name);
//            jsonBody.put("quantity_description", quantity_description);
//            jsonBody.put("cal", cal);
//            jsonBody.put("protein", protein);
//            jsonBody.put("fat", fat);
//            jsonBody.put("carbrea", carb);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }


        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), json);

        // Build the POST request
        Request request = new Request.Builder()
                .url("http://10.0.2.2:8080/api/submission")
                .post(requestBody)
                .build();

        // Make the POST request
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    // Handle the successful response if needed
                    Log.d("Add to Calculate", "Add to Calculate successful");
                    runOnUiThread(() -> {
                        // Show Toast
                        Toast.makeText(ViewItemRecordedActivity.this, "Add to Calculate successfully", Toast.LENGTH_LONG).show();

                        // Navigate back to LoginPage
//                                Intent intent = new Intent(AddCustomizedItemActivity.this, LoginPage.class);
//                                startActivity(intent);
//                                finish(); // Close the current activity to prevent going back to it with the back button
                    });
                } else {
                    Log.e("Add to Calculate", "Unexpected response code: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                Log.e("Add to Calculate", "Network request failed: " + e.getMessage());
            }

        });
    }

    public class LocalDateConverter implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {
        public JsonElement serialize(LocalDate src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(DateTimeFormatter.ISO_LOCAL_DATE.format(src));
        }

        public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            return DateTimeFormatter.ISO_LOCAL_DATE.parse(json.getAsString(), LocalDate::from);
        }
    }

}