package com.example.prakriti;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
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

import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CameraActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_STORAGE_PERMISSION = 101;
    private static final int REQUEST_IMAGE_CAPTURE = 102;
    private static final int REQUEST_IMAGE_UPLOAD = 103;

    private ImageView imagePreview;
    private TextView placeholderText, statusText;
    private Button btnCamera, btnUpload, btnDone;
    private ProgressBar progressBar;

    private String currentPhotoPath;
    private File photoFile;
    private boolean hasImage = false;

    private Module module;
    private final int INPUT_SIZE = 224;
    private List<String> classNames;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera); // Make sure this matches your XML file

        initializeViews();
        setupClickListeners();

        // Load model and class names
        try {
            module = Module.load(assetFilePath("quantized_edgevit_dynamic_final.pt"));
            classNames = loadClassNames("classes.txt");
            statusText.setText("Model loaded successfully ✅");
        } catch (Exception e) {
            statusText.setText("Failed to load model ❌");
            e.printStackTrace();
        }
    }

    private void initializeViews() {
        imagePreview = findViewById(R.id.imagePreview);
        placeholderText = findViewById(R.id.placeholderText);
        statusText = findViewById(R.id.statusText);
        btnCamera = findViewById(R.id.btnCamera);
        btnUpload = findViewById(R.id.btnUpload);
        btnDone = findViewById(R.id.btnDone);
        progressBar = findViewById(R.id.progressBar);

        // Safety check for nulls
        if (btnCamera == null || btnUpload == null || btnDone == null) {
            Toast.makeText(this, "Button IDs do not match XML!", Toast.LENGTH_LONG).show();
        }
    }

    private void setupClickListeners() {
        btnCamera.setOnClickListener(v -> {
            if (checkCameraPermission()) openCamera();
            else requestCameraPermission();
        });

        btnUpload.setOnClickListener(v -> {
            if (checkStoragePermission()) openGallery();
            else requestStoragePermission();
        });

        btnDone.setOnClickListener(v -> {
            if (hasImage) runModelOnImage(currentPhotoPath);
            else Toast.makeText(this, "Please capture or upload a photo first", Toast.LENGTH_SHORT).show();
        });
    }

    private boolean checkStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    private void requestStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                    REQUEST_STORAGE_PERMISSION);
        } else openGallery();
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
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show();
                return;
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.prakriti.fileprovider", photoFile); // Correct authority
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                showProgress("Opening camera...");
            }
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
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile("JPEG_" + timeStamp + "_", ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        hideProgress();
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) handleCameraResult();
            else if (requestCode == REQUEST_IMAGE_UPLOAD) handleGalleryResult(data);
        } else statusText.setText("Action cancelled");
    }

    private void handleCameraResult() {
        if (photoFile != null && photoFile.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
            if (bitmap != null) {
                bitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true);
                imagePreview.setImageBitmap(bitmap);
                placeholderText.setVisibility(View.GONE);
                hasImage = true;
                btnDone.setVisibility(View.VISIBLE);
                btnDone.setEnabled(true);
                statusText.setText("Photo captured successfully");
            } else statusText.setText("Failed to load captured image");
        } else statusText.setText("Failed to capture image");
    }

    private void handleGalleryResult(Intent data) {
        if (data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();
            try {
                photoFile = createImageFile();
                InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                FileOutputStream outputStream = new FileOutputStream(photoFile);
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1)
                    outputStream.write(buffer, 0, bytesRead);
                inputStream.close();
                outputStream.close();

                currentPhotoPath = photoFile.getAbsolutePath();
                Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
                if (bitmap != null) {
                    bitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true);
                    imagePreview.setImageBitmap(bitmap);
                    placeholderText.setVisibility(View.GONE);
                    hasImage = true;
                    btnDone.setVisibility(View.VISIBLE);
                    btnDone.setEnabled(true);
                    statusText.setText("Photo uploaded successfully");
                } else statusText.setText("Failed to load uploaded image");
            } catch (IOException e) {
                statusText.setText("Failed to process uploaded image");
                e.printStackTrace();
            }
        }
    }
    private void showPredictionDialog(String predictedClass, float score) {
        // Inflate custom layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_prediction_result, null);

        TextView tvPredictedCrop = dialogView.findViewById(R.id.tvPredictedCrop);
        TextView tvConfidence = dialogView.findViewById(R.id.tvConfidence);
        ProgressBar progressBarConfidence = dialogView.findViewById(R.id.progressBarConfidence);
        Button btnYes = dialogView.findViewById(R.id.btnYes);
        Button btnNo = dialogView.findViewById(R.id.btnNo);

        // Set values
        tvPredictedCrop.setText(predictedClass);
        tvConfidence.setText(String.format(Locale.getDefault(), "Confidence: %.2f%%", score * 100));
        progressBarConfidence.setProgress((int)(score * 100));

        // Build dialog
        final androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        btnYes.setOnClickListener(v -> {
            // Go to CameraDescription page
            // Increment crop count
            SharedPreferences prefs = getSharedPreferences("PrakritiData", MODE_PRIVATE);
            int count = prefs.getInt("cropCount", 0);
            prefs.edit()
                    .putInt("cropCount", count + 1)
                    .putLong("lastDiagnosisTime", System.currentTimeMillis())
                    .apply();
            Intent intent = new Intent(CameraActivity.this, CameraDescription.class);
            intent.putExtra("prediction", predictedClass);       // send predicted class
            intent.putExtra("photo_path", currentPhotoPath);     // send image path
            startActivity(intent);
            dialog.dismiss();
            finish(); // optional
        });




        btnNo.setOnClickListener(v -> {
            // Just dismiss and allow user to try again
            dialog.dismiss();
            Toast.makeText(CameraActivity.this, "You can capture/upload another image.", Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }


    private void runModelOnImage(String imagePath) {
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            bitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true);

            Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(bitmap,
                    TensorImageUtils.TORCHVISION_NORM_MEAN_RGB,
                    TensorImageUtils.TORCHVISION_NORM_STD_RGB);

            IValue output = module.forward(IValue.from(inputTensor));
            float[] scores = output.toTensor().getDataAsFloatArray();

            int maxIndex = 0;
            float maxScore = -Float.MAX_VALUE;
            for (int i = 0; i < scores.length; i++) {
                if (scores[i] > maxScore) {
                    maxScore = scores[i];
                    maxIndex = i;
                }
            }

            String predictedClass = (classNames != null && maxIndex < classNames.size()) ?
                    classNames.get(maxIndex) : "Class " + maxIndex;

            // Show dialog for user confirmation
            showPredictionDialog(predictedClass, maxScore);

        } catch (Exception e) {
            statusText.setText("Model inference failed ❌");
            e.printStackTrace();
        }
    }

    private List<String> loadClassNames(String fileName) {
        List<String> classes = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open(fileName)))) {
            String line;
            while ((line = reader.readLine()) != null) classes.add(line);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return classes;
    }

    private String assetFilePath(String assetName) throws IOException {
        File file = new File(getFilesDir(), assetName);
        if (file.exists() && file.length() > 0) return file.getAbsolutePath();

        try (InputStream is = getAssets().open(assetName);
             FileOutputStream os = new FileOutputStream(file)) {
            byte[] buffer = new byte[4 * 1024];
            int read;
            while ((read = is.read(buffer)) != -1) os.write(buffer, 0, read);
            os.flush();
        }
        return file.getAbsolutePath();
    }

    private void showProgress(String message) {
        progressBar.setVisibility(View.VISIBLE);
        statusText.setText(message);
    }

    private void hideProgress() {
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) openCamera();
        else if (requestCode == REQUEST_STORAGE_PERMISSION && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) openGallery();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(CameraActivity.this, HomeScreen.class);
        // Clear all activities above TargetActivity from the back stack
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish(); // optional, closes CurrentActivity
    }

}
