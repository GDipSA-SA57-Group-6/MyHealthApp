package iss.AD.myhealthapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class RecordExerciseDisplay extends AppCompatActivity {

    Button btnReturn;

    TextView exerciseInfoTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_exercise_display);

        exerciseInfoTextView = findViewById(R.id.exerciseInfoTextView);

        String exerciseInfo = "Running: 360 calories per 30 minutes\n" +
                "Swimming: 250 calories per 30 minutes\n" +
                "Walking: 150 calories per 30 minutes\n" +
                "Badminton: 200 calories per 30 minutes\n" +
                "Cycling: 200 calories per 30 minutes";

        exerciseInfoTextView.setText(exerciseInfo);


        btnReturn = findViewById(R.id.btnReturn); // Assuming you have a button with id infoButton
        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RecordExerciseDisplay.this, RecordExercise.class);
                startActivity(intent);
            }
        });

    }
}