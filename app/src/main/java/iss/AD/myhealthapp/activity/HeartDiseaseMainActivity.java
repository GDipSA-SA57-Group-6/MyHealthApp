package iss.AD.myhealthapp.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import iss.AD.myhealthapp.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HeartDiseaseMainActivity extends AppCompatActivity {

    private EditText ageInput, sexInput, cpInput, trestbpsInput, thalachInput, exangInput;
    private TextView errorAge, errorSex, errorCp, errorTrestbps, errorThalach, errorExang,dateTextView;
    private Button submitBtn;
    private TextView resultTextView;
    private Button clearBtn;
    private Button backToMainBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_disease_main);
        resultTextView = findViewById(R.id.resultTextView);
        ageInput = findViewById(R.id.ageInput);
        sexInput = findViewById(R.id.sexInput);
        cpInput = findViewById(R.id.cpInput);
        trestbpsInput = findViewById(R.id.trestbpsInput);
        thalachInput = findViewById(R.id.thalachInput);
        exangInput = findViewById(R.id.exangInput);
        errorAge = findViewById(R.id.errorAge);
        errorSex = findViewById(R.id.errorSex);
        errorCp = findViewById(R.id.errorCp);
        errorTrestbps = findViewById(R.id.errorTrestbps);
        errorThalach = findViewById(R.id.errorThalach);
        errorExang = findViewById(R.id.errorExang);
        backToMainBtn = findViewById(R.id.backToMainBtn);
        backToMainBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 返回主界面
                finish();
            }
        });
        submitBtn = findViewById(R.id.submitBtn);

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String age = ageInput.getText().toString();
                String sex = sexInput.getText().toString();
                String cp = cpInput.getText().toString();
                String trestbps = trestbpsInput.getText().toString();
                String thalach = thalachInput.getText().toString();
                String exang = exangInput.getText().toString();
                sendPredictionRequest(age, sex, cp, trestbps, thalach, exang, new PredictionResultCallback() {
                    @Override
                    public void onResultReceived(int predictionClass, double predictionProbability) {

                        storeHeartDiseaseData(cp, trestbps, thalach, exang, predictionClass,predictionProbability);
                    }
                });

            }
        });

        dateTextView = findViewById(R.id.dateDisplay);
        String formattedDate = getCurrentDate();

        dateTextView.setText("Date: " + formattedDate);
        clearBtn = findViewById(R.id.clearBtn);

        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               
                cpInput.setText("");
                trestbpsInput.setText("");
                thalachInput.setText("");
                exangInput.setText("");
                resultTextView.setText("");
                errorAge.setText("");
                errorSex.setText("");
                errorCp.setText("");
                errorTrestbps.setText("");
                errorThalach.setText("");
                errorExang.setText("");
            }
        });
        final SharedPreferences pref = getSharedPreferences("user_credentials", MODE_PRIVATE);
        Integer userId = pref.getInt("userId",-1);

        if (userId != -1) {
            // 如果用户ID有效，调用方法获取用户信息
            fetchUserInfo(userId);
        } else {
            // 如果没有找到有效的用户ID，可能需要处理用户未登录的情况
            Toast.makeText(this, "User not logged in", Toast.LENGTH_LONG).show();
        }
    }
    private interface PredictionResultCallback {
        void onResultReceived(int predictionClass, double predictionProbability);
    }
    public String getCurrentDate() {
        ZoneId zoneId = ZoneId.of("Asia/Singapore");
        LocalDate currentDate = LocalDate.now(zoneId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return currentDate.format(formatter);
    }
    private void storeHeartDiseaseData(String cp, String trestbps, String thalach, String exang,int predictionClass, double predictionProbability) {
        int userId = getUserId();
        String currentDate = getCurrentDate();

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("userId", userId);
            jsonObject.put("date", currentDate);
            jsonObject.put("cp", cp);
            jsonObject.put("trestbps", trestbps);
            jsonObject.put("thalach", thalach);
            jsonObject.put("exang", exang);
            jsonObject.put("predictionClass", predictionClass);
            jsonObject.put("predictionProbability", predictionProbability);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonObject.toString());
        String local_host = getResources().getString(R.string.local_host);
        Request request = new Request.Builder()
                .url("http://" + local_host + ":8080/api/heartDiseaseData")
                .post(body)
                .build();

        new OkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                // Handle failure, possibly on the UI thread if you need to update the UI
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    // 处理非成功的HTTP响应，将UI更新操作切回主线程
                    runOnUiThread(() -> {
                        Toast.makeText(HeartDiseaseMainActivity.this, "错误：" + response.code(), Toast.LENGTH_SHORT).show();
                    });
                    return;
                }
                // 处理成功响应
                final String responseBody = response.body().string();
                runOnUiThread(() -> handleUserInfoResponse(responseBody));
            }
        });
    }
    private int getUserId() {
        SharedPreferences pref = getSharedPreferences("user_credentials", MODE_PRIVATE);
        return pref.getInt("userId", -1); // Returns -1 if "userId" doesn't exist
    }

    private void fetchUserInfo(Integer userId) {
        String local_host = getResources().getString(R.string.local_host);
        String apiUrl = "http://" + local_host + ":8080/api/user/get/" + String.valueOf(userId);

        Request request = new Request.Builder()
                .url(apiUrl)
                .build();

        new OkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // 处理请求失败的情况
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String responseBody = response.body().string();
                    // 在UI线程上处理和更新UI
                    runOnUiThread(() -> handleUserInfoResponse(responseBody));
                }
            }
        });
    }
    private void handleUserInfoResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            String birthDate = jsonObject.getString("birthDate");
            String gender = jsonObject.getString("gender");
            // 计算年龄
            int age = calculateAge(birthDate);
            // 更新UI，这里只是示例，您可能需要根据实际情况调整
            updateUIWithUserInfo(age, gender);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private int calculateAge(String birthDateStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate birthDate = LocalDate.parse(birthDateStr, formatter);
        LocalDate currentDate = LocalDate.now();
        return Period.between(birthDate, currentDate).getYears();
    }

    private void updateUIWithUserInfo(int age, String gender) {

        ageInput.setText(String.valueOf(age));


        sexInput.setText(gender.equalsIgnoreCase("female") ? "0" : "1");
    }


    private void sendPredictionRequest(String age, String sex, String cp, String trestbps, String thalach, String exang,PredictionResultCallback callback) {
        OkHttpClient client = new OkHttpClient();
        clearErrorMessages();
        boolean isValid = true;

// Check if the age is empty
        if (age.trim().isEmpty()) {
            errorAge.setText("Age is required.");
            isValid = false;
        } else if (!isValidNumber(age, false)) {
            errorAge.setText("Invalid age. Please enter a valid number.");
            isValid = false;
        }

// Check if the resting blood pressure is empty
        if (trestbps.trim().isEmpty()) {
            errorTrestbps.setText("Resting Blood Pressure is required.");
            isValid = false;
        } else if (!isValidNumber(trestbps, false)) {
            errorTrestbps.setText("Invalid Resting Blood Pressure. Please enter a valid number.");
            isValid = false;
        }

// Check if the maximum heart rate is empty
        if (thalach.trim().isEmpty()) {
            errorThalach.setText("Maximum Heart Rate Achieved is required.");
            isValid = false;
        } else if (!isValidNumber(thalach, false)) {
            errorThalach.setText("Invalid Maximum Heart Rate Achieved. Please enter a valid number.");
            isValid = false;
        }

// Check if the sex is empty
        if (sex.trim().isEmpty()) {
            errorSex.setText("Sex is required.");
            isValid = false;
        } else if (!isValidGender(sex)) {
            errorSex.setText("Invalid gender. Must be 0 (female) or 1 (male).");
            isValid = false;
        }

// Check if the chest pain type is empty
        if (cp.trim().isEmpty()) {
            errorCp.setText("Chest Pain Type is required.");
            isValid = false;
        } else if (!isValidChestPain(cp)) {
            errorCp.setText("Chest pain type must be 0, 1, 2, or 3");
            isValid = false;
        }

// Check if the exercise induced angina is empty
        if (exang.trim().isEmpty()) {
            errorExang.setText("Exercise Induced Angina is required.");
            isValid = false;
        } else if (!isValidBinary(exang)) {
            errorExang.setText("Exercise Induced Angina must be 0 or 1");
            isValid = false;
        }

        if (!isValid) {
            // If not valid, do not proceed with sending the request
            return;
        }


        float ageFloat = Float.parseFloat(age);
        float sexFloat = Float.parseFloat(sex);
        float cpFloat = Float.parseFloat(cp);
        float trestbpsFloat = Float.parseFloat(trestbps);
        float thalachFloat = Float.parseFloat(thalach);
        float exangFloat = Float.parseFloat(exang);

        RequestBody requestBody = new FormBody.Builder()
                .add("age", String.valueOf(ageFloat))
                .add("sex", String.valueOf(sexFloat))
                .add("cp", String.valueOf(cpFloat))
                .add("trestbps", String.valueOf(trestbpsFloat))
                .add("thalach", String.valueOf(thalachFloat))
                .add("exang", String.valueOf(exangFloat))
                // 添加其他参数
                .build();


        Request request = new Request.Builder()
                .url("http://192.168.1.98:5000/heart_disease")
                .post(requestBody)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build();


        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(HeartDiseaseMainActivity.this, "request error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String result = response.body().string();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject resultJson = new JSONObject(result);
                                double prediction = resultJson.getDouble("prediction");
                                int predictionClass = resultJson.getInt("class");
                                callback.onResultReceived(predictionClass,prediction);

                                String formattedResult = String.format("the probability of getting heart disease is ：%.2f%%", prediction * 100);
                                resultTextView.setText(formattedResult);
                                storeHeartDiseaseData(cp, trestbps, thalach, exang, predictionClass,prediction);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                resultTextView.setText("results error");
                            }
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(HeartDiseaseMainActivity.this, "server error", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }


        });
    }
    // Utility method to validate number
    // Utility method to validate number, modified to check for empty strings
    private void clearErrorMessages() {
        errorAge.setText("");
        errorSex.setText("");
        errorCp.setText("");
        errorTrestbps.setText("");
        errorThalach.setText("");
        errorExang.setText("");
    }
    private boolean isValidNumber(String number, boolean allowDecimal) {
        if (number == null || number.trim().isEmpty()) {
            return false;
        }
        try {
            if (allowDecimal) {
                Float.parseFloat(number);
            } else {
                Integer.parseInt(number);
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }


    // Utility method to validate gender
    private boolean isValidGender(String gender) {
        return "0".equals(gender) || "1".equals(gender);
    }

    // Utility method to validate chest pain
    private boolean isValidChestPain(String cp) {
        if (!isValidNumber(cp, false)) {
            return false;
        }
        int cpNum = Integer.parseInt(cp);
        return cpNum >= 0 && cpNum <= 3;
    }

    // Utility method to validate binary inputs (exang)
    private boolean isValidBinary(String binary) {
        return "0".equals(binary) || "1".equals(binary);
    }

}
