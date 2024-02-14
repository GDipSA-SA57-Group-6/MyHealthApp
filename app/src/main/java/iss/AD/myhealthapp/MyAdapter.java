package iss.AD.myhealthapp;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class MyAdapter extends ArrayAdapter<Object> {
    private final Context context;
    private final Map<Object, Integer> clickCountMap;

    protected List<Food> foodList;

    public MyAdapter(Context context, List<Food> foodList, Map<Object, Integer> clickCountMap) {
        super(context, R.layout.row);
        this.context = context;
        this.foodList= foodList;
        this.clickCountMap = clickCountMap;

        addAll(new Object[foodList.size()]);
        //addAll(this.foodList);

    }
    @Override
    public void clear(){
        //this.foodList.clear();
        //notifyDataSetChanged();
        super.clear();
    }

    public void setData(List<Food> foodList)
    {
        //this.foodList= null; //foodList;

        //this.clear();
        //this.addAll(new Object[0]);
//        this.addAll(new Object[foodList.size()]);
        //notifyDataSetChanged();


    }


    @SuppressLint("ResourceAsColor")
    @NonNull
    @Override
    public View getView(int pos, View view, @NonNull ViewGroup parent) {
        /*
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                    Activity.LAYOUT_INFLATER_SERVICE);

            // if we are not responsible for adding the view to the parent,
            // then attachToRoot should be 'false' (which is in our case)
            view = inflater.inflate(R.layout.row, parent, false);
        }
        */

        if (pos >= foodList.size())
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                    Activity.LAYOUT_INFLATER_SERVICE);

            // if we are not responsible for adding the view to the parent,
            // then attachToRoot should be 'false' (which is in our case)
            view = inflater.inflate(R.layout.row_empty  , parent, false);

            return view;
        }
        else
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                    Activity.LAYOUT_INFLATER_SERVICE);

            // if we are not responsible for adding the view to the parent,
            // then attachToRoot should be 'false' (which is in our case)
            view = inflater.inflate(R.layout.row, parent, false);

        }

        /*
        // set the image for ImageView
        ImageView imageView = view.findViewById(R.id.imageView);
        int id = context.getResources().getIdentifier(foodList.get(pos).getName(),
                "drawable", context.getPackageName());
        imageView.setImageResource(id);
        */
        Food food = foodList.get(pos);
        // set the image for ImageView
        ImageView imageView = view.findViewById(R.id.imageView);

        if (food != null) {
            int id = context.getResources().getIdentifier(food.getName(),
                    "drawable", context.getPackageName());

            if (id != 0) {
                // Image resource found, set it
                imageView.setImageResource(id);
            } else {
                // Image resource not found, load and display saved image
                loadAndDisplaySavedImage(imageView, food.getName());
            }
        }




        // set the text for TextView
        TextView textView_1 = view.findViewById(R.id.textView_1);
        textView_1.setText(foodList.get(pos).getName());

        TextView textView_2 = view.findViewById(R.id.textView_2);
        textView_2.setText(foodList.get(pos).getQuantityDescription());

        TextView clickCountTextView = view.findViewById(R.id.clickCountTextView);
        //Food food = foodList.get(pos);

        int clickCount = clickCountMap.getOrDefault(food, 0);
        clickCountTextView.setText("You’ve consumed "  + clickCount +" times");
        clickCountTextView.setTextColor(Color.WHITE);

        //view.setBackgroundColor(context.getResources().getColor(android.R.color.white));

        Button btnAction = view.findViewById(R.id.btnAction);
        btnAction.setOnClickListener(null);

        btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int clickCount_new = clickCountMap.getOrDefault(food, 0) - 1;
                clickCountMap.put(food, clickCount_new); // Update the map regardless of the count

                if (clickCount_new <= 0) {
                    btnAction.setVisibility(View.GONE);

                    View linearLayoutView = (View)(view.getParent().getParent());
                    linearLayoutView.setBackgroundColor(context.getResources().getColor(android.R.color.white));

                    TextView textView_1 = linearLayoutView.findViewById(R.id.textView_1);
                    TextView textView_2 = linearLayoutView.findViewById(R.id.textView_2);
                    //((View)textView_1.getParent()).setBackgroundColor(context.getResources().getColor(R.color.white));

                    textView_1.setTextColor(Color.BLACK);
                    textView_2.setTextColor(Color.BLACK);
                }

                clickCountTextView.setText("You’ve consumed " + clickCount_new +" times");
                clickCountTextView.setTextColor(Color.WHITE);
            }
        });



        if (clickCount <= 0) {
            btnAction.setVisibility(View.GONE);
            view.setBackgroundColor(context.getResources().getColor(R.color.white));

            textView_1.setTextColor(context.getResources().getColor(android.R.color.black));
            textView_2.setTextColor(context.getResources().getColor(android.R.color.black));
        } else {
            btnAction.setVisibility(View.VISIBLE);
            view.setBackgroundColor(context.getResources().getColor(R.color.my_primary));

            textView_1.setTextColor(context.getResources().getColor(android.R.color.white));
            textView_2.setTextColor(context.getResources().getColor(android.R.color.white));
        }
/*
        if (clickCount == 0) {
            //v.setBackgroundColor(getResources().getColor(android.R.color.white));
            ((View)textView_1.getParent()).setBackgroundColor(getResources().getColor(R.color.white));
        } else {
            //v.setBackgroundColor(getResources().getColor(R.color.my_primary));
            ((View)textView_1.getParent()).setBackgroundColor(getResources().getColor(R.color.my_primary));

            textView_1.setTextColor(getResources().getColor(android.R.color.white));
            textView_2.setTextColor(getResources().getColor(android.R.color.white));
        }
*/


        return view;
    }

    private void loadAndDisplaySavedImage(ImageView imageView, String name) {
        try {
            // Get user ID from SharedPreferences
            SharedPreferences pref = context.getSharedPreferences("user_credentials", Context.MODE_PRIVATE);
            int userId = pref.getInt("userId", -1);

            // Create a file name with user ID and food name tag
            String fileName = "food_image_" + userId + "_" + name + ".jpg";

            Log.d("MyAdapter", "Loading image: " + fileName); // Add this line for debugging

            // Load the previously saved image from internal storage
            FileInputStream inputStream = context.openFileInput(fileName);
            Bitmap savedBitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            // Display the saved image
            imageView.setImageBitmap(savedBitmap);

        } catch (IOException e) {
            e.printStackTrace();
            Log.e("MyAdapter", "Error loading saved image: " + e.getMessage()); // Add this line for debugging
        }
    }
}


/*
package iss.AD.myhealthapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.Map;

public class MyAdapter extends ArrayAdapter<Object> {
    private final Context context;
    private final Map<Object, Integer> clickCountMap;

    protected List<Food> foodList;

    public MyAdapter(Context context, List<Food> foodList, Map<Object, Integer> clickCountMap) {
        super(context, R.layout.row);
        this.context = context;
        this.foodList= foodList;
        this.clickCountMap = clickCountMap;

        addAll(new Object[foodList.size()]);
    }

    @SuppressLint("ResourceAsColor")
    @NonNull
    @Override
    public View getView(int pos, View view, @NonNull ViewGroup parent) {
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                    Activity.LAYOUT_INFLATER_SERVICE);

            // if we are not responsible for adding the view to the parent,
            // then attachToRoot should be 'false' (which is in our case)
            view = inflater.inflate(R.layout.row, parent, false);
        }

        // set the image for ImageView
        ImageView imageView = view.findViewById(R.id.imageView);
        int id = context.getResources().getIdentifier(foodList.get(pos).getName(),
                "drawable", context.getPackageName());
        imageView.setImageResource(id);

        // set the text for TextView
        TextView textView_1 = view.findViewById(R.id.textView_1);
        textView_1.setText(foodList.get(pos).getName());

        TextView textView_2 = view.findViewById(R.id.textView_2);
        textView_2.setText(foodList.get(pos).getQuantityDescription());

        TextView clickCountTextView = view.findViewById(R.id.clickCountTextView);
        Food food = foodList.get(pos);

        int clickCount = clickCountMap.getOrDefault(food, 0);
        clickCountTextView.setText("You’ve consumed "  + clickCount +" times");
        clickCountTextView.setTextColor(Color.WHITE);

        //view.setBackgroundColor(context.getResources().getColor(android.R.color.white));

        Button btnAction = view.findViewById(R.id.btnAction);
        btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int clickCount_new = clickCountMap.getOrDefault(food, 0) - 1;
                clickCountMap.put(food, clickCount_new); // Update the map regardless of the count

                if (clickCount_new <= 0) {
                    btnAction.setVisibility(View.GONE);

                    View linearLayoutView = (View)(view.getParent().getParent());
                    linearLayoutView.setBackgroundColor(context.getResources().getColor(android.R.color.white));

                    TextView textView_1 = linearLayoutView.findViewById(R.id.textView_1);
                    TextView textView_2 = linearLayoutView.findViewById(R.id.textView_2);
                    //((View)textView_1.getParent()).setBackgroundColor(context.getResources().getColor(R.color.white));

                    textView_1.setTextColor(Color.BLACK);
                    textView_2.setTextColor(Color.BLACK);
                }

                clickCountTextView.setText("You’ve consumed " + clickCount_new +" times");
                clickCountTextView.setTextColor(Color.WHITE);
            }
        });

        if (clickCount <= 0) {
            btnAction.setVisibility(View.GONE);
            view.setBackgroundColor(context.getResources().getColor(R.color.white));

            textView_1.setTextColor(context.getResources().getColor(android.R.color.black));
            textView_2.setTextColor(context.getResources().getColor(android.R.color.black));
        } else {
            btnAction.setVisibility(View.VISIBLE);
            view.setBackgroundColor(context.getResources().getColor(R.color.my_primary));

            textView_1.setTextColor(context.getResources().getColor(android.R.color.white));
            textView_2.setTextColor(context.getResources().getColor(android.R.color.white));
        }
/*
        if (clickCount == 0) {
            //v.setBackgroundColor(getResources().getColor(android.R.color.white));
            ((View)textView_1.getParent()).setBackgroundColor(getResources().getColor(R.color.white));
        } else {
            //v.setBackgroundColor(getResources().getColor(R.color.my_primary));
            ((View)textView_1.getParent()).setBackgroundColor(getResources().getColor(R.color.my_primary));

            textView_1.setTextColor(getResources().getColor(android.R.color.white));
            textView_2.setTextColor(getResources().getColor(android.R.color.white));
        }
*//*


        return view;
                }
                }
 */
