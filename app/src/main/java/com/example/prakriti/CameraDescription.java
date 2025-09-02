package com.example.prakriti;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.ContentResolver;
import android.content.ContentValues;
import androidx.annotation.RequiresApi;
import android.provider.MediaStore;
import java.io.OutputStream;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CameraDescription extends AppCompatActivity {

    private static final String TAG = "CameraDescription";
    private static final int PERMISSION_REQUEST_CODE = 123;

    private ImageView headerImage;
    private TextView titleText;
    private TextView tabDescription, tabTreatment;
    private ScrollView scrollViewDescription, scrollViewTreatment;
    private View underlineIndicator;
    private Button savePDFButton;

    private TextView descriptionTextView, treatmentTextView;
    private DatabaseHelper dbHelper;

    // Store current data for PDF generation
    private String currentTitle = "";
    private String currentDescription = "";
    private String currentTreatment = "";
    private Bitmap currentImage = null;
    private String currentPrediction = "";
    private float currentConfidence = 0.0f;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_description);

        // Initialize database helper
        dbHelper = new DatabaseHelper(this);

        // Initialize views
        tabDescription = findViewById(R.id.tabDescription);
        tabTreatment = findViewById(R.id.tabTreatment);
        scrollViewDescription = findViewById(R.id.scrollViewDescription);
        scrollViewTreatment = findViewById(R.id.scrollViewTreatment);
        underlineIndicator = findViewById(R.id.underlineIndicator);
        headerImage = findViewById(R.id.headerImage);
        titleText = findViewById(R.id.titleText);
        descriptionTextView = findViewById(R.id.descriptionText);
        treatmentTextView = findViewById(R.id.treatmentText);
        savePDFButton = findViewById(R.id.PDFButton);

        // Set PDF button click listener
        savePDFButton.setOnClickListener(v -> {
            if (checkPermissions()) {
                generateAndSavePDF();
            } else {
                requestPermissions();
            }
        });

        // Handle incoming intent
        Intent intent = getIntent();
        if (intent != null) {
            String prediction = intent.getStringExtra("prediction");
            String photoPath = intent.getStringExtra("photo_path");
            float confidence = intent.getFloatExtra("confidence", 0.0f);

            currentPrediction = prediction != null ? prediction : "";
            currentConfidence = confidence;

            Log.d(TAG, "Received prediction: " + prediction + " with confidence: " + confidence);

            if (prediction != null) {
                loadDiseaseData(prediction, confidence);
            } else {
                // Fallback
                titleText.setText("No Prediction");
                descriptionTextView.setText("No disease prediction available.");
                treatmentTextView.setText("Please consult agricultural experts.");
                updateCurrentData();
            }

            // Load the captured image
            if (photoPath != null) {
                loadImageFromPath(photoPath);
            } else {
                // Use a default placeholder image
                headerImage.setImageResource(R.drawable.apple_leaf);
                currentImage = BitmapFactory.decodeResource(getResources(), R.drawable.apple_leaf);
            }
        }

        // Set underline indicator width after layout
        tabDescription.post(() -> {
            int tabWidth = tabDescription.getWidth();
            underlineIndicator.getLayoutParams().width = tabWidth;
            underlineIndicator.requestLayout();
        });

        // Tab switching
        tabDescription.setOnClickListener(v -> switchTab(true));
        tabTreatment.setOnClickListener(v -> switchTab(false));

        switchTab(true); // Show description tab by default
    }

    private void loadDiseaseData(String prediction, float confidence) {
        // Convert CropClassifier prediction format to database format
        String normalizedPrediction = normalizePredictionFormat(prediction);

        Log.d(TAG, "Normalized prediction: " + normalizedPrediction);

        // Get disease information from database
        DatabaseHelper.CropInfo diseaseInfo = dbHelper.getDiseaseInfo(normalizedPrediction);

        if (diseaseInfo != null) {
            // Format the title to show crop and condition with confidence
            String title = diseaseInfo.cropName + " - " + formatConditionName(diseaseInfo.condition);
            if (confidence > 0) {
                title += String.format(" (%.1f%% confidence)", confidence);
            }
            titleText.setText(title);

            // Set description and treatment
            String description = diseaseInfo.description;
            if (confidence > 0) {
                description += "\n\nModel Confidence: " + String.format("%.1f%%", confidence);
                if (confidence < 70) {
                    description += "\n\nNote: Low confidence prediction. Please verify with an expert.";
                }
            }
            descriptionTextView.setText(description);
            treatmentTextView.setText(diseaseInfo.treatment);
        } else {
            // Try alternative matching strategies
            diseaseInfo = tryAlternativeMatching(prediction);

            if (diseaseInfo != null) {
                String title = diseaseInfo.cropName + " - " + formatConditionName(diseaseInfo.condition);
                if (confidence > 0) {
                    title += String.format(" (%.1f%% confidence)", confidence);
                }
                titleText.setText(title);
                descriptionTextView.setText(diseaseInfo.description + "\n\nNote: Matched using alternative search.");
                treatmentTextView.setText(diseaseInfo.treatment);
            } else {
                // Fallback if disease not found in database
                handleUnknownPrediction(prediction, confidence);
            }
        }

        // Update current data after loading
        updateCurrentData();
    }

    private void updateCurrentData() {
        currentTitle = titleText.getText().toString();
        currentDescription = descriptionTextView.getText().toString();
        currentTreatment = treatmentTextView.getText().toString();
    }

    private String normalizePredictionFormat(String prediction) {
        if (prediction == null) return "";

        // Handle CropClassifier prediction format: "Tomato__Early_blight" -> "Early_blight"
        // Extract condition part after the crop name
        String normalized = prediction;

        // Split by double underscore first (CropClassifier format)
        if (prediction.contains("__")) {
            String[] parts = prediction.split("__");
            if (parts.length > 1) {
                normalized = parts[1]; // Take the condition part
            }
        }
        // Handle single underscore format: "Apple_Black_rot" -> "Black_rot"
        else if (prediction.contains("_")) {
            // Common crop prefixes to remove
            String[] cropPrefixes = {"Tomato_", "Apple_", "Grape_", "Cherry_", "Coffee_",
                    "Corn_", "Cotton_", "Jute_", "Potato_", "Rice_",
                    "Strawberry_", "Sugarcane_", "Wheat_"};

            for (String prefix : cropPrefixes) {
                if (prediction.startsWith(prefix)) {
                    normalized = prediction.substring(prefix.length());
                    break;
                }
            }
        }

        Log.d(TAG, "Prediction format conversion: " + prediction + " -> " + normalized);
        return normalized;
    }

    private DatabaseHelper.CropInfo tryAlternativeMatching(String prediction) {
        // Try searching with original prediction
        DatabaseHelper.CropInfo info = searchInDatabase(prediction);
        if (info != null) return info;

        // Try with spaces instead of underscores
        info = searchInDatabase(prediction.replace("_", " "));
        if (info != null) return info;

        // Try extracting keywords and searching
        String[] words = prediction.split("[_\\s]+");
        for (String word : words) {
            if (word.length() > 3) { // Skip short words
                info = searchInDatabase(word);
                if (info != null) return info;
            }
        }

        return null;
    }

    private DatabaseHelper.CropInfo searchInDatabase(String searchTerm) {
        // Use a simple cursor query to search
        return dbHelper.getDiseaseInfo(searchTerm);
    }

    private void handleUnknownPrediction(String prediction, float confidence) {
        titleText.setText("Unrecognized Condition");

        String description = "The detected condition '" + prediction + "' is not recognized in our database.\n\n";

        if (confidence > 0) {
            description += "Model Confidence: " + String.format("%.1f%%\n\n", confidence);

            if (confidence < 50) {
                description += "The model has low confidence in this prediction. ";
            }
        }

        description += "This might be:\n" +
                "• A new or rare condition not in our database\n" +
                "• A healthy plant with unusual lighting/angle\n" +
                "• An environmental factor affecting the image\n\n" +
                "Please consider taking another photo with better lighting and clarity.";

        descriptionTextView.setText(description);

        String treatment = "Recommendations:\n\n" +
                "1. Consult with local agricultural experts or extension services\n" +
                "2. Take additional photos from different angles\n" +
                "3. Check for common symptoms: discoloration, spots, wilting\n" +
                "4. Monitor the plant over several days for changes\n" +
                "5. Consider soil and environmental conditions";

        treatmentTextView.setText(treatment);
    }

    private String formatConditionName(String condition) {
        // Convert database condition names to readable format
        return condition.replace("_", " ")
                .replace("(", " (")
                .trim();
    }

    private void loadImageFromPath(String photoPath) {
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(photoPath);
            if (bitmap != null) {
                headerImage.setImageBitmap(bitmap);
                currentImage = bitmap;
            } else {
                // If bitmap is null, use default image
                headerImage.setImageResource(R.drawable.apple_leaf);
                currentImage = BitmapFactory.decodeResource(getResources(), R.drawable.apple_leaf);
            }
        } catch (Exception e) {
            // Handle any exceptions during image loading
            Log.e(TAG, "Error loading image: " + e.getMessage());
            headerImage.setImageResource(R.drawable.apple_leaf);
            currentImage = BitmapFactory.decodeResource(getResources(), R.drawable.apple_leaf);
        }
    }

    private void switchTab(boolean showDescription) {
        if (showDescription) {
            scrollViewDescription.setVisibility(View.VISIBLE);
            scrollViewTreatment.setVisibility(View.GONE);
            tabDescription.setTextColor(getResources().getColor(android.R.color.black));
            tabTreatment.setTextColor(getResources().getColor(android.R.color.darker_gray));
            underlineIndicator.animate().translationX(0).setDuration(200).start();
        } else {
            scrollViewDescription.setVisibility(View.GONE);
            scrollViewTreatment.setVisibility(View.VISIBLE);
            tabDescription.setTextColor(getResources().getColor(android.R.color.darker_gray));
            tabTreatment.setTextColor(getResources().getColor(android.R.color.black));
            underlineIndicator.animate().translationX(tabDescription.getWidth()).setDuration(200).start();
        }
    }

    private boolean checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ uses scoped storage, no permission needed for app-specific directories
            return true;
        } else {
            // For older versions, check write permission
            return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                generateAndSavePDF();
            } else {
                Toast.makeText(this, "Permission denied. Cannot save PDF.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void generateAndSavePDF() {
        try {
            // Show progress to user
            Toast.makeText(this, "Generating PDF...", Toast.LENGTH_SHORT).show();

            // Update current data before generating PDF
            updateCurrentData();

            // Create PDF document
            PdfDocument pdfDocument = new PdfDocument();

            // Create page info (A4 size)
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
            PdfDocument.Page page = pdfDocument.startPage(pageInfo);

            Canvas canvas = page.getCanvas();
            Paint paint = new Paint();
            paint.setTextSize(12);
            paint.setColor(android.graphics.Color.BLACK);
            paint.setAntiAlias(true); // Smoother text

            Paint titlePaint = new Paint();
            titlePaint.setTextSize(18);
            titlePaint.setColor(android.graphics.Color.BLACK);
            titlePaint.setTypeface(Typeface.DEFAULT_BOLD);
            titlePaint.setAntiAlias(true);

            Paint headerPaint = new Paint();
            headerPaint.setTextSize(14);
            headerPaint.setColor(android.graphics.Color.BLACK);
            headerPaint.setTypeface(Typeface.DEFAULT_BOLD);
            headerPaint.setAntiAlias(true);

            int yPosition = 50;
            int margin = 40;
            int pageWidth = pageInfo.getPageWidth() - (2 * margin);

            // Add app header
            canvas.drawText("Prakriti - Crop Disease Analysis Report", margin, yPosition, titlePaint);
            yPosition += 40;

            // Add date
            String currentDate = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
            canvas.drawText("Generated on: " + currentDate, margin, yPosition, paint);
            yPosition += 30;

            // Add separator line
            Paint linePaint = new Paint();
            linePaint.setColor(android.graphics.Color.GRAY);
            linePaint.setStrokeWidth(1);
            canvas.drawLine(margin, yPosition, pageWidth + margin, yPosition, linePaint);
            yPosition += 20;

            // Add image if available
            if (currentImage != null) {
                try {
                    // Scale image to fit width while maintaining aspect ratio
                    int maxImageWidth = 200;
                    int maxImageHeight = 150;

                    float scaleX = (float) maxImageWidth / currentImage.getWidth();
                    float scaleY = (float) maxImageHeight / currentImage.getHeight();
                    float scale = Math.min(scaleX, scaleY);

                    int scaledWidth = (int) (currentImage.getWidth() * scale);
                    int scaledHeight = (int) (currentImage.getHeight() * scale);

                    Bitmap scaledImage = Bitmap.createScaledBitmap(currentImage, scaledWidth, scaledHeight, true);

                    // Center the image
                    int imageX = margin + (pageWidth - scaledWidth) / 2;
                    canvas.drawBitmap(scaledImage, imageX, yPosition, null);
                    yPosition += scaledHeight + 20;

                    scaledImage.recycle(); // Free memory
                } catch (Exception e) {
                    Log.e(TAG, "Error adding image to PDF: " + e.getMessage());
                    yPosition += 20;
                }
            }

            // Check if we have enough space for content
            if (yPosition > 500) {
                Log.w(TAG, "PDF content might be truncated due to space constraints");
            }

            // Add title
            canvas.drawText("Analysis Result:", margin, yPosition, headerPaint);
            yPosition += 25;
            yPosition = drawWrappedText(canvas, currentTitle, margin, yPosition, pageWidth, paint);
            yPosition += 15;

            // Add description
            canvas.drawText("Description:", margin, yPosition, headerPaint);
            yPosition += 25;
            yPosition = drawWrappedText(canvas, currentDescription, margin, yPosition, pageWidth, paint);
            yPosition += 15;

            // Add treatment
            canvas.drawText("Treatment:", margin, yPosition, headerPaint);
            yPosition += 25;
            yPosition = drawWrappedText(canvas, currentTreatment, margin, yPosition, pageWidth, paint);

            // Add footer if space available
            if (yPosition < 780) {
                yPosition = 800; // Near bottom of page
                canvas.drawLine(margin, yPosition - 10, pageWidth + margin, yPosition - 10, linePaint);
                canvas.drawText("Generated by Prakriti App", margin, yPosition, paint);
            }

            pdfDocument.finishPage(page);

            // Save PDF
            savePDFToFile(pdfDocument);

        } catch (OutOfMemoryError e) {
            Log.e(TAG, "Out of memory while generating PDF: " + e.getMessage());
            Toast.makeText(this, "PDF generation failed: Out of memory. Try with a smaller image.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e(TAG, "Error generating PDF: " + e.getMessage());
            Toast.makeText(this, "Error generating PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private int drawWrappedText(Canvas canvas, String text, int x, int y, int maxWidth, Paint paint) {
        if (text == null || text.isEmpty()) {
            return y;
        }

        // Handle page overflow
        int maxY = 780; // Maximum Y position before page break needed

        String[] lines = text.split("\n");
        int currentY = y;

        for (String line : lines) {
            if (currentY > maxY) {
                // Would need page break logic here for multi-page PDFs
                break;
            }

            String[] words = line.split("\\s+");
            StringBuilder currentLine = new StringBuilder();

            for (String word : words) {
                String testLine = currentLine.toString() + (currentLine.length() > 0 ? " " : "") + word;
                float textWidth = paint.measureText(testLine);

                if (textWidth > maxWidth && currentLine.length() > 0) {
                    canvas.drawText(currentLine.toString(), x, currentY, paint);
                    currentY += 16; // Line height
                    currentLine = new StringBuilder(word);

                    if (currentY > maxY) break; // Prevent overflow
                } else {
                    currentLine.append(currentLine.length() > 0 ? " " : "").append(word);
                }
            }

            if (currentLine.length() > 0 && currentY <= maxY) {
                canvas.drawText(currentLine.toString(), x, currentY, paint);
                currentY += 16;
            }

            // Add extra space for paragraph breaks
            if (currentY <= maxY) {
                currentY += 8;
            }
        }

        return currentY;
    }


    private void savePDFToFile(PdfDocument pdfDocument) {
        try {
            // Create filename with disease name and timestamp
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String diseaseName = sanitizeFileName(currentTitle);
            String fileName = diseaseName + "_" + timestamp + ".pdf";

            File directory;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ - Use MediaStore for public Downloads folder
                savePDFUsingMediaStore(pdfDocument, fileName);
                return;
            } else {
                // Android 9 and below - Direct access to Downloads folder
                directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            }

            if (!directory.exists()) {
                directory.mkdirs();
            }

            File file = new File(directory, fileName);

            FileOutputStream fos = new FileOutputStream(file);
            pdfDocument.writeTo(fos);
            pdfDocument.close();
            fos.close();

            // Show success message and option to open/share
            Toast.makeText(this, "PDF saved to Downloads folder successfully!", Toast.LENGTH_LONG).show();

            // Optionally open the PDF
            openPDFLegacy(file);

        } catch (IOException e) {
            Log.e(TAG, "Error saving PDF: " + e.getMessage());
            Toast.makeText(this, "Error saving PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // Helper method to sanitize filename
    private String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "UnknownDisease";
        }

        // Remove invalid characters and spaces
        String sanitized = fileName
                .replaceAll("[\\\\/:*?\"<>|%\\s]", "")  // Remove invalid characters and spaces
                .replaceAll("[-().,]", "");             // Remove common punctuation

        // Limit length to avoid filesystem issues
        if (sanitized.length() > 50) {
            sanitized = sanitized.substring(0, 50);
        }

        // Ensure we have a valid name
        if (sanitized.isEmpty()) {
            sanitized = "CropAnalysis";
        }

        return sanitized;
    }

    // For Android 10+ using MediaStore
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void savePDFUsingMediaStore(PdfDocument pdfDocument, String fileName) {
        try {
            ContentResolver resolver = getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

            Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);

            if (uri != null) {
                OutputStream outputStream = resolver.openOutputStream(uri);
                pdfDocument.writeTo(outputStream);
                outputStream.close();
                pdfDocument.close();

                Toast.makeText(this, "PDF saved to Downloads folder successfully!", Toast.LENGTH_LONG).show();

                // Open the PDF using the URI
                openPDFModern(uri);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error saving PDF using MediaStore: " + e.getMessage());
            Toast.makeText(this, "Error saving PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // For opening PDF on older Android versions
    private void openPDFLegacy(File file) {
        try {
            Uri uri = FileProvider.getUriForFile(this, "com.example.prakriti.fileprovider", file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_GRANT_READ_URI_PERMISSION);

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, "No PDF viewer app found. File saved in Downloads folder.", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error opening PDF: " + e.getMessage());
            Toast.makeText(this, "PDF saved in Downloads folder but couldn't open it.", Toast.LENGTH_LONG).show();
        }
    }

    // For opening PDF on newer Android versions
    private void openPDFModern(Uri uri) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_GRANT_READ_URI_PERMISSION);

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, "No PDF viewer app found. File saved in Downloads folder.", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error opening PDF: " + e.getMessage());
            Toast.makeText(this, "PDF saved in Downloads folder but couldn't open it.", Toast.LENGTH_LONG).show();
        }
    }

    private void openPDF(File file) {
        try {
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_GRANT_READ_URI_PERMISSION);

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, "No PDF viewer app found. File saved at: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error opening PDF: " + e.getMessage());
            Toast.makeText(this, "PDF saved but couldn't open it. Location: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Close database helper if needed
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}