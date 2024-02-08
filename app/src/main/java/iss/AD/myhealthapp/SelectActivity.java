package iss.AD.myhealthapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SelectActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    Button mViewItemBtn;
    Button mAddCustomizedBtn;

    List<Food> foodList = new ArrayList<>();

//    private final List<Food> foodList = Arrays.asList(
//            new Food(1, "banana", "1 large"),
//            new Food(2, "bread", "1 regular slice"),
//            new Food(3, "carrot", "2 medium"),
//            new Food(4, "coke", "200ml"),
//            new Food(5, "cookie", "1 small"),
//            new Food(6, "rice", "½ cup"),
//            new Food(7, "seafood", "1 ounce"),
//            new Food(8, "steak", "1 ounce"),
//            new Food(9, "egg", "1"),
//            new Food(10, "pasta", "1 ounce")
//    );



    private final Map<Object, Integer> clickCountMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);


        OkHttpClient client = new OkHttpClient();


        Request request = new Request.Builder()
                .url("http://10.0.2.2:8080/api/food")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        String responseData = response.body().string();
                        if (responseData != null && responseData != ""){
                            foodList = parseJsonArray(responseData);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ListView listView = findViewById(R.id.listView);
                                    if (listView != null) {
                                        MyAdapter adapter = new MyAdapter(SelectActivity.this, foodList, clickCountMap);
                                        listView.setOnItemClickListener(SelectActivity.this);
                                        listView.setAdapter(adapter);
                                    }
                                }
                            });


                        } else {
                            Log.e("GetFoods", "Unexpected response code: " + response.code());
                        }
                    }} catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("GetFoods", "Error parsing JSON", e);
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e("GetFoods", "Network request failed: " + e.getMessage(), e);
            }
        });






        mViewItemBtn = findViewById(R.id.ViewItemBtn);
        mViewItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SelectActivity.this, ViewItemRecordedActivity.class);
                HashMap<Object, Integer> serializableMap = new HashMap<>(clickCountMap);
                intent.putExtra("data", serializableMap);
                startActivity(intent);

            }
        });

        mAddCustomizedBtn = findViewById(R.id.AddCustomizedBtn);
        mAddCustomizedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SelectActivity.this, AddCustomizedItemActivity.class));
            }
        });

/*
        ListView listView = findViewById(R.id.listView);
        if (listView != null) {
            MyAdapter adapter = new MyAdapter(this, foodList, clickCountMap);
            listView.setOnItemClickListener(this);
            listView.setAdapter(adapter);
        }
*/
    }




    private List<Food> parseJsonArray(String jsonArrayString) throws JSONException {

        JSONArray jsonArray = new JSONArray(jsonArrayString);

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);

            int id = jsonObject.getInt("id");
            String name = jsonObject.getString("name");
            String quantity_description = jsonObject.getString("quantity_description");

            Food food = new Food(id, name, quantity_description);
            foodList.add(food);
        }

        return foodList;
    }


    @Override
    public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
        TextView textView_1 = v.findViewById(R.id.textView_1);
        TextView textView_2 = v.findViewById(R.id.textView_2);

        Food food = foodList.get(pos);

        // Get the current click count for the item
        int clickCount = clickCountMap.getOrDefault(food, 0);
        // Increment the click count
        clickCount++;

        // Update the click count in the map
        clickCountMap.put(food, clickCount);

        // Display the click count in the Toast
        String display = "Recorded " + clickCount + " clicks for " + food.getName();
        Toast.makeText(this, display, Toast.LENGTH_SHORT).show();


        MyAdapter adapter = (MyAdapter) av.getAdapter();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }


    }


}
