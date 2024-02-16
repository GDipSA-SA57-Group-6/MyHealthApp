package iss.AD.myhealthapp;

import static java.lang.StrictMath.abs;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.PorterDuff;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {
/*
    private ImageView imageView;
    private Button buttonPickImage;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        buttonPickImage = findViewById(R.id.buttonPickImage);

        // Initialize the Activity Result Launcher
        new InitializeImagePickerLauncher().execute();

        buttonPickImage.setOnClickListener(view -> openImagePicker());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private Bitmap loadBitmapFromUri(Uri imageUri) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return ImageDecoder.decodeBitmap(ImageDecoder.createSource(getContentResolver(), imageUri));
        } else {
            return MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
        }
    }

    private void saveImageToInternalStorage(Bitmap bitmap) {
        try {
            // Create a file in internal storage
            String fileName = "profile_image.jpg";
            OutputStream outputStream = openFileOutput(fileName, MODE_PRIVATE);

            // Compress the bitmap to JPEG format
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

            // Close the output stream
            outputStream.close();

            // Display a success message or handle the file as needed
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class InitializeImagePickerLauncher extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            // Initialize the Activity Result Launcher on a background thread
            imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            Uri imageUri = result.getData().getData();
                            try {
                                Bitmap bitmap = loadBitmapFromUri(imageUri);
                                imageView.setImageBitmap(bitmap);

                                // Save the image to internal storage
                                saveImageToInternalStorage(bitmap);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
            return null;
        }
    }
    */

}




/*

public class MainActivity extends AppCompatActivity {
    private ImageView imageView;
    private Button buttonPickImage;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        buttonPickImage = findViewById(R.id.buttonPickImage);

        // Initialize the Activity Result Launcher
        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            Bitmap bitmap = loadBitmapFromUri(imageUri);
                            imageView.setImageBitmap(bitmap);

                            // Save the image to internal storage
                            saveImageToInternalStorage(bitmap);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

        buttonPickImage.setOnClickListener(view -> openImagePicker());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private Bitmap loadBitmapFromUri(Uri imageUri) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return ImageDecoder.decodeBitmap(ImageDecoder.createSource(getContentResolver(), imageUri));
        } else {
            return MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
        }
    }

    private void saveImageToInternalStorage(Bitmap bitmap) {
        try {
            // Create a file in internal storage
            String fileName = "profile_image.jpg";
            OutputStream outputStream = openFileOutput(fileName, MODE_PRIVATE);

            // Compress the bitmap to JPEG format
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

            // Close the output stream
            outputStream.close();

            // Display a success message or handle the file as needed
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
*/
/*
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:padding="16dp"
    tools:context=".MainActivity">

    <ImageView
        android:id="@+id/imageView"
        android:layout_width="match_parent"
        android:layout_height="200dp"
        android:scaleType="centerCrop"
        android:src="@drawable/ic_launcher_foreground" />

    <Button
        android:id="@+id/buttonPickImage"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_below="@id/imageView"
        android:layout_marginTop="16dp"
        android:text="Pick Image" />
</RelativeLayout>
 */