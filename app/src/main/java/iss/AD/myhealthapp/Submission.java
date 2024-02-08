package iss.AD.myhealthapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import java.time.LocalDate;
import java.util.List;

public class Submission {
    //public int id;

    public int userId;

    public LocalDate date;

    public List<SubmissionItem> submissionItems;

    // Default constructor
    public Submission() {
    }

    // Parameterized constructor
    public Submission(int userId, LocalDate date) {
        this.userId = userId;
        this.date = date;
    }

}