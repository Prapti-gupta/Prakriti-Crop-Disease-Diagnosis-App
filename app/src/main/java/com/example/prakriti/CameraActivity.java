package com.example.prakriti;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CameraActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_STORAGE_PERMISSION = 101;
    private static final int REQUEST_IMAGE_CAPTURE = 102;
    private static final int REQUEST_IMAGE_UPLOAD = 103;

    private ImageView imagePreview;
    private TextView placeholderText;
    private TextView statusText;
    private Button btnCamera;
    private Button btnUpload;
    private Button btnDone;
    private ProgressBar progressBar;

    private String currentPhotoPath;
    private File photoFile;
    private boolean hasImage = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        initializeViews();
        setupClickListeners();

        // Always enable buttons
        btnCamera.setEnabled(true);
        btnUpload.setEnabled(true);
        btnDone.setEnabled(true);
    }

    private void initializeViews() {
        imagePreview = findViewById(R.id.imagePreview);
        placeholderText = findViewById(R.id.placeholderText);
        statusText = findViewById(R.id.statusText);
        btnCamera = findViewById(R.id.btnCamera);
        btnUpload = findViewById(R.id.btnUpload);
        btnDone = findViewById(R.id.btnDone);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupClickListeners() {
        btnCamera.setOnClickListener(v -> {
            if (checkCameraPermission()) {
                openCamera();
            } else {
                requestCameraPermission();
            }
        });

        btnUpload.setOnClickListener(v -> {
            if (checkStoragePermission()) {
                openGallery();
            } else {
                requestStoragePermission();
            }
        });

        btnDone.setOnClickListener(v -> {
            if (hasImage) {
                navigateToCameraDescriptionPage();
            } else {
                Toast.makeText(this, "Please capture or upload a photo first", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean checkStoragePermission() {
        // For Android 13+ (Tiramisu), check READ_MEDIA_IMAGES instead
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            return true; // No permission needed from Android 10+
        }
    }

    private void requestStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                    REQUEST_STORAGE_PERMISSION);
        } else {
            // No need to request for Android 10 and above
            openGallery();
        }
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }



    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                REQUEST_CAMERA_PERMISSION);
    }



    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show();
                return;
            }

            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.yourapp.camera.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

                showProgress("Opening camera...");
            }
        } else {
            Toast.makeText(this, "No camera app available", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_UPLOAD);

        showProgress("Opening gallery...");
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        hideProgress();

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_IMAGE_CAPTURE:
                    handleCameraResult();
                    break;
                case REQUEST_IMAGE_UPLOAD:
                    handleGalleryResult(data);
                    break;
            }
        } else {
            statusText.setText("Action cancelled");
        }
    }

    private void handleCameraResult() {
        if (photoFile != null && photoFile.exists()) {
            showProgress("Processing image...");
            Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
            if (bitmap != null) {
                bitmap = scaleBitmapToFit(bitmap);
                imagePreview.setImageBitmap(bitmap);
                placeholderText.setVisibility(View.GONE);
                hasImage = true;
                statusText.setText("Photo captured successfully");
            } else {
                statusText.setText("Failed to load captured image");
            }
            hideProgress();
        } else {
            statusText.setText("Failed to capture image");
        }
    }

    private void handleGalleryResult(Intent data) {
        if (data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();
            showProgress("Loading image...");

            try {
                photoFile = createImageFile();
                copyUriToFile(selectedImageUri, photoFile);
                currentPhotoPath = photoFile.getAbsolutePath();

                Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
                if (bitmap != null) {
                    bitmap = scaleBitmapToFit(bitmap);
                    imagePreview.setImageBitmap(bitmap);
                    placeholderText.setVisibility(View.GONE);
                    hasImage = true;
                    statusText.setText("Photo uploaded successfully");
                } else {
                    statusText.setText("Failed to load uploaded image");
                }
            } catch (IOException e) {
                statusText.setText("Failed to process uploaded image");
                e.printStackTrace();
            }
            hideProgress();
        }
    }

    private void copyUriToFile(Uri uri, File file) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        FileOutputStream outputStream = new FileOutputStream(file);
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        inputStream.close();
        outputStream.close();
    }

    private Bitmap scaleBitmapToFit(Bitmap bitmap) {
        int maxWidth = 800;
        int maxHeight = 600;

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float scaleWidth = (float) maxWidth / width;
        float scaleHeight = (float) maxHeight / height;
        float scale = Math.min(scaleWidth, scaleHeight);

        if (scale < 1.0f) {
            int newWidth = Math.round(width * scale);
            int newHeight = Math.round(height * scale);
            return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
        }
        return bitmap;
    }

    private void showProgress(String message) {
        progressBar.setVisibility(View.VISIBLE);
        statusText.setText(message);
    }

    private void hideProgress() {
        progressBar.setVisibility(View.GONE);
    }

    private void navigateToCameraDescriptionPage() {
        Intent intent = new Intent(this, CameraDescription.class);
        if (currentPhotoPath != null) {
            intent.putExtra("photo_path", currentPhotoPath);
        }
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera();
                } else {
                    Toast.makeText(this, "Camera permission required to take photos",
                            Toast.LENGTH_SHORT).show();
                }
                break;

            case REQUEST_STORAGE_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery();
                } else {
                    Toast.makeText(this, "Storage permission required to upload photos",
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public String getCurrentPhotoPath() {
        return currentPhotoPath;
    }
}


