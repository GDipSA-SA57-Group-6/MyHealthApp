package iss.AD.myhealthapp;


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

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DiabetesMainActivity extends AppCompatActivity {


    private EditText highBpInput, bmiInput, heartDiseaseInput, genHlthInput, physHlthInput, diffWalkInput, ageInput, incomeInput;
    private TextView errorHighBp, errorBmi, errorHeartDisease, errorGenHlth, errorPhysHlth, errorDiffWalk, errorAge, errorIncome;
    private Button submitBtn;
    private TextView resultTextView,dateTextView;
    private Button clearBtn;
    private Button backToMainBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diabetes_main);

        highBpInput = findViewById(R.id.highBpInput);
        bmiInput = findViewById(R.id.bmiInput);
        heartDiseaseInput = findViewById(R.id.heartDiseaseInput);
        genHlthInput = findViewById(R.id.genHlthInput);
        physHlthInput = findViewById(R.id.physHlthInput);
        diffWalkInput = findViewById(R.id.diffWalkInput);
        ageInput = findViewById(R.id.ageInput);
        incomeInput = findViewById(R.id.incomeInput);
        submitBtn = findViewById(R.id.submitBtn);
        resultTextView = findViewById(R.id.resultTextView);
        errorHighBp = findViewById(R.id.errorHighBp);
        errorBmi = findViewById(R.id.errorBmi);
        errorDiffWalk = findViewById(R.id.errorDiffWalk);
        errorAge = findViewById(R.id.errorAge);
        errorGenHlth = findViewById(R.id.errorGenHlth);
        errorHeartDisease = findViewById(R.id.errorHeartDiseaseorAttack);
        errorIncome = findViewById(R.id.errorIncome);
        errorPhysHlth = findViewById(R.id.errorPhysHlth);
        dateTextView = findViewById(R.id.dateDisplay);

        String formattedDate = getCurrentDate();
        dateTextView.setText("Date: " + formattedDate);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String highBP = highBpInput.getText().toString();
                String bmi = bmiInput.getText().toString();
                String heartDisease = heartDiseaseInput.getText().toString();
                String genHlth = genHlthInput.getText().toString();
                String physHlth = physHlthInput.getText().toString();
                String diffWalk = diffWalkInput.getText().toString();
                String age = ageInput.getText().toString();
                String income = incomeInput.getText().toString();
                sendPredictionRequest(highBP, bmi, heartDisease, genHlth, physHlth, diffWalk, age, income, new DiabetesMainActivity.ResultCallback() {
                    @Override
                    public void onResultReceived(int predictionClass,double predictionProbability) {

                        storeDiabetesData(diffWalk,income, highBP, bmi, heartDisease, genHlth, physHlth,predictionClass,predictionProbability);
                    }
                });

            }
        });
        backToMainBtn = findViewById(R.id.backToMainBtn);
        backToMainBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 返回主界面
                finish();
            }
        });
        clearBtn = findViewById(R.id.clearBtn);
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 清除所有EditText的内容
                highBpInput.setText("");
                bmiInput.setText("");
                heartDiseaseInput.setText("");
                genHlthInput.setText("");
                physHlthInput.setText("");
                diffWalkInput.setText("");
                
                incomeInput.setText("");
                resultTextView.setText("");
                errorHighBp.setText("");
                errorBmi.setText("");
                errorPhysHlth.setText("");
                errorIncome.setText("");
                errorAge.setText("");
                errorDiffWalk.setText("");
                errorHeartDisease.setText("");
                errorGenHlth.setText("");
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

    public String getCurrentDate() {
        ZoneId zoneId = ZoneId.of("Asia/Singapore");
        LocalDate currentDate = LocalDate.now(zoneId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return currentDate.format(formatter);
    }
    private void storeDiabetesData(String diffWalk,String income, String highBP, String bmi,String heartDisease, String genHlth, String physHlth ,int predictionClass,double predictionProbability) {
        int userId = getUserId();
        String currentDate = getCurrentDate();

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("userId", userId);
            jsonObject.put("date", currentDate);
            jsonObject.put("diffWalk", diffWalk);
            jsonObject.put("income", income);
            jsonObject.put("highBP", highBP);
            jsonObject.put("bmi", bmi);
            jsonObject.put("heartDisease", heartDisease);
            jsonObject.put("genHlth", genHlth);
            jsonObject.put("physHlth", physHlth);
            jsonObject.put("predictionClass", predictionClass);
            jsonObject.put("predictionProbability", predictionProbability);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonObject.toString());

        Request request = new Request.Builder()
                .url("http://192.168.1.98:8080/api/diabetesData")
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
                        Toast.makeText(DiabetesMainActivity.this, "错误：" + response.code(), Toast.LENGTH_SHORT).show();
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
        String apiUrl = "http://192.168.1.98:8080/api/user/get/" + String.valueOf(userId);

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
    private interface ResultCallback {
        void onResultReceived(int predictionClass,double predictionProbability);
    }
    private void handleUserInfoResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            String birthDate = jsonObject.getString("birthDate");


            int age = calculateAge(birthDate);
            // 更新UI，这里只是示例，您可能需要根据实际情况调整
            updateUIWithUserInfo(age);
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

    private void updateUIWithUserInfo(int age) {

        ageInput.setText(String.valueOf(age));



    }
    private void sendPredictionRequest(String highBP, String bmi, String heartDisease, String genHlth, String physHlth, String diffWalk, String age, String income, DiabetesMainActivity.ResultCallback callback) {
        OkHttpClient client = new OkHttpClient();
        clearErrorMessages();

        boolean isValid = true;

        if (highBP.trim().isEmpty()) {
            errorHighBp.setText("High blood pressure is required.");
            isValid = false;
        } else if (!isValidBinary(highBP)) {
            errorHighBp.setText("HighBP must be 0 or 1");
            isValid = false;
        }
        if (bmi.trim().isEmpty()) {
            errorBmi.setText("BMI is required.");
            isValid = false;
        } else if (!isValidNumber(bmi,true)) {
            errorBmi.setText("BMI must be a valid number");
            isValid = false;
        }
        if (heartDisease.trim().isEmpty()) {
            errorHeartDisease.setText("Heart Disease is required.");
            isValid = false;
        } else if (!isValidBinary(heartDisease)) {
            errorHeartDisease.setText("Heart Disease must be 0 or 1");
            isValid = false;
        }
        if (genHlth.trim().isEmpty()) {
            errorGenHlth.setText("General health is required.");
            isValid = false;
        } else if (!isValidGenHlth(genHlth)) {
            errorGenHlth.setText("General Health must be a number between 1 and 5");
            isValid = false;
        }
        if (physHlth.trim().isEmpty()) {
            errorPhysHlth.setText("Physical health is required.");
            isValid = false;
        } else if (!isValidNumber(physHlth, true)) {
            errorPhysHlth.setText("Physical Health must be a valid number");
            isValid = false;
        }
        if (diffWalk.trim().isEmpty()) {
            errorDiffWalk.setText("Difficulty walking is required.");
            isValid = false;
        } else if (!isValidBinary(diffWalk)) {
            errorDiffWalk.setText("DiffWalk must be 0 or 1");
            isValid = false;
        }

        if (age.trim().isEmpty()) {
            errorAge.setText("Age is required.");
            isValid = false;
        } else if (!isValidNumber(age, true)) {
            errorAge.setText("Invalid age. Please enter a valid number.");
            isValid = false;
        }
        if (income.trim().isEmpty()) {
            errorIncome.setText("Income is required.");
            isValid = false;
        } else if (!isValidIncome(income)) {
            errorIncome.setText("Income must be a number between 1 and 8");
            isValid = false;
        }


        if (!isValid) {
            // 如果验证失败，不发送请求
            return;
        }
// ... 发送请求的代码
        float highBPFloat = Float.parseFloat(highBP);
        float bmiFloat = Float.parseFloat(bmi);
        float heartDiseaseFloat = Float.parseFloat(heartDisease);
        float genHlthFloat = Float.parseFloat(genHlth);
        float physHlthFloat = Float.parseFloat(physHlth);
        float diffWalkFloat = Float.parseFloat(diffWalk);
        float ageFloat = Float.parseFloat(age);
        float incomeFloat = Float.parseFloat(income);



        RequestBody requestBody = new FormBody.Builder()
                .add("HighBP", String.valueOf(highBPFloat))
                .add("BMI", String.valueOf(bmiFloat))
                .add("HeartDiseaseorAttack", String.valueOf(heartDiseaseFloat))
                .add("GenHlth", String.valueOf(genHlthFloat))
                .add("PhysHlth", String.valueOf(physHlthFloat))
                .add("DiffWalk", String.valueOf(diffWalkFloat))
                .add("Age", String.valueOf(ageFloat))
                .add("Income", String.valueOf(incomeFloat))
                .build();
        Request request = new Request.Builder()
                .url("http://192.168.1.98:5000/diabetes")
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
                        Toast.makeText(DiabetesMainActivity.this, "request fail: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                                String formattedResult = String.format("the probability of diabetes is ：%.2f%%", prediction * 100);
                                resultTextView.setText(formattedResult);
                                storeDiabetesData(diffWalk,income, highBP, bmi, heartDisease, genHlth, physHlth,predictionClass,prediction);
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
                            Toast.makeText(DiabetesMainActivity.this, "server error", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private boolean isValidBinary(String binary) {
        return "0".equals(binary) || "1".equals(binary);
    }


    private boolean isValidGenHlth(String value) {
        if (!isValidNumber(value, false)) {
            return false;
        }
        int number = Integer.parseInt(value);
        return number >= 1 && number <= 5;
    }

    private boolean isValidIncome(String value) {
        if (!isValidNumber(value, false)) {
            return false;
        }
        int number = Integer.parseInt(value);
        return number >= 1 && number <= 8;
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

    // ... (其他验证方法)
    private void clearErrorMessages() {
        errorHighBp.setText("");
        errorBmi.setText("");
        errorPhysHlth.setText("");
        errorIncome.setText("");
        errorAge.setText("");
        errorDiffWalk.setText("");
        errorHeartDisease.setText("");
        errorGenHlth.setText("");

    }
}
