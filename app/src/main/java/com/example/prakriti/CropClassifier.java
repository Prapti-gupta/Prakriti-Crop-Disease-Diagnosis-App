package com.example.prakriti;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class CropClassifier {

    private static final String TAG = "CropClassifier";
    private Module model;
    private Context context;

    private String[] cropClasses = {
            "Tomato__Early_blight", "Tomato__Bacterial_spot",
            "Tomato__Healthy", "Tomato_Late_blight", "Tomato__Leaf_Mold",
            "Tomato__Septoria_leaf_spot", "Tomato_Spider_mites Two-spotted_spider_mite", "Tomato__Target_Spot",
            "Tomato__Tomato_mosaic_virus", "Tomato_Tomato_Yellow_Leaf_Curl_Virus", "Apple_Apple_scab",
            "Apple_Black_rot", "Apple_Cedar_apple_rust", "Apple__Healthy"
    };

    public CropClassifier(Context context) {
        this.context = context;
        loadModel();
    }

    // --- Load TorchScript model (.pt) ---
    private void loadModel() {
        try {
            File modelFile = new File(context.getFilesDir(), "quantized_model.pt");
            if (!modelFile.exists()) {
                InputStream is = context.getResources().openRawResource(R.raw.quantized_model);
                FileOutputStream fos = new FileOutputStream(modelFile);
                byte[] buffer = new byte[is.available()];
                is.read(buffer);
                fos.write(buffer);
                fos.close();
                is.close();
            }

            // Load the TorchScript model
            model = Module.load(modelFile.getAbsolutePath());
            Log.d(TAG, "Model loaded successfully");
        } catch (IOException e) {
            Log.e(TAG, "Error loading model: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // --- Preprocess (must match training transforms) ---
    private Tensor preprocess(Bitmap bitmap) {
        bitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float[] imgData = new float[3 * width * height];
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        float mean[] = {0.485f, 0.456f, 0.406f};
        float std[] = {0.229f, 0.224f, 0.225f};

        for (int i = 0; i < pixels.length; i++) {
            int val = pixels[i];
            int r = ((val >> 16) & 0xFF);
            int g = ((val >> 8) & 0xFF);
            int b = (val & 0xFF);

            imgData[i] = (r / 255.0f - mean[0]) / std[0];
            imgData[i + width * height] = (g / 255.0f - mean[1]) / std[1];
            imgData[i + 2 * width * height] = (b / 255.0f - mean[2]) / std[2];
        }

        return Tensor.fromBlob(imgData, new long[]{1, 3, height, width});
    }

    // --- Simple Prediction Method ---
    public String predict(Bitmap bitmap) {
        if (model == null) {
            Log.e(TAG, "Model not loaded");
            return "Unknown";
        }

        try {
            Tensor inputTensor = preprocess(bitmap);
            Tensor outputTensor = model.forward(IValue.from(inputTensor)).toTensor();
            float[] scores = outputTensor.getDataAsFloatArray();

            int maxIdx = -1;
            float maxScore = -Float.MAX_VALUE;
            for (int i = 0; i < scores.length; i++) {
                if (scores[i] > maxScore) {
                    maxScore = scores[i];
                    maxIdx = i;
                }
            }

            if (maxIdx >= 0 && maxIdx < cropClasses.length) {
                String prediction = cropClasses[maxIdx];
                Log.d(TAG, "Prediction: " + prediction);
                return prediction;
            } else {
                Log.w(TAG, "Invalid prediction index: " + maxIdx);
                return "Unknown";
            }
        } catch (Exception e) {
            Log.e(TAG, "Error during prediction: " + e.getMessage());
            e.printStackTrace();
            return "Error";
        }
    }

    // --- Enhanced Prediction Method with Confidence ---
    public CropPrediction predictCrop(Bitmap bitmap) {
        if (model == null) {
            Log.e(TAG, "Model not loaded");
            return new CropPrediction("Unknown", 0.0f);
        }

        try {
            // Preprocess the image using TensorImageUtils for better consistency
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true);

            // Convert to tensor with normalization for ImageNet pretrained models
            Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(
                    resizedBitmap,
                    new float[]{0.485f, 0.456f, 0.406f}, // ImageNet mean
                    new float[]{0.229f, 0.224f, 0.225f}  // ImageNet std
            );

            // Run inference
            Tensor outputTensor = model.forward(IValue.from(inputTensor)).toTensor();

            // Get prediction results
            float[] scores = outputTensor.getDataAsFloatArray();

            // Find the class with highest score
            int maxScoreIdx = 0;
            float maxScore = scores[0];
            for (int i = 1; i < scores.length; i++) {
                if (scores[i] > maxScore) {
                    maxScore = scores[i];
                    maxScoreIdx = i;
                }
            }

            // Apply softmax to get probability
            float[] probabilities = softmax(scores);
            float confidence = probabilities[maxScoreIdx] * 100;

            String predictedCrop = maxScoreIdx < cropClasses.length ?
                    cropClasses[maxScoreIdx] : "Unknown Crop";

            Log.d(TAG, "Predicted: " + predictedCrop + " with confidence: " + confidence + "%");

            return new CropPrediction(predictedCrop, confidence);

        } catch (Exception e) {
            Log.e(TAG, "Error during prediction: " + e.getMessage());
            e.printStackTrace();
            return new CropPrediction("Error", 0.0f);
        }
    }

    private float[] softmax(float[] input) {
        float[] output = new float[input.length];
        float sum = 0.0f;

        // Find max value to prevent overflow
        float maxVal = input[0];
        for (int i = 1; i < input.length; i++) {
            if (input[i] > maxVal) {
                maxVal = input[i];
            }
        }

        // Calculate exponentials and sum
        for (int i = 0; i < input.length; i++) {
            output[i] = (float) Math.exp(input[i] - maxVal);
            sum += output[i];
        }

        // Normalize
        for (int i = 0; i < output.length; i++) {
            output[i] /= sum;
        }

        return output;
    }

    // Utility method to copy raw resource to file
    private void copyRawResourceToFile(int resourceId, File outFile) throws IOException {
        InputStream inputStream = context.getResources().openRawResource(resourceId);
        FileOutputStream outputStream = new FileOutputStream(outFile);

        byte[] buffer = new byte[1024];
        int read;
        while ((read = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, read);
        }

        inputStream.close();
        outputStream.close();
    }

    // Utility method to copy asset to file
    private void copyAssetToFile(String assetName, File outFile) throws IOException {
        InputStream inputStream = context.getAssets().open(assetName);
        FileOutputStream outputStream = new FileOutputStream(outFile);

        byte[] buffer = new byte[1024];
        int read;
        while ((read = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, read);
        }

        inputStream.close();
        outputStream.close();
    }

    // Method to check if model is loaded
    public boolean isModelLoaded() {
        return model != null;
    }

    // Method to get available crop classes
    public String[] getCropClasses() {
        return cropClasses.clone();
    }

    // Inner class for prediction results
    public static class CropPrediction {
        private String cropName;
        private float confidence;

        public CropPrediction(String cropName, float confidence) {
            this.cropName = cropName;
            this.confidence = confidence;
        }

        public String getCropName() {
            return cropName;
        }

        public float getConfidence() {
            return confidence;
        }

        public String getFormattedConfidence() {
            return String.format("%.1f%%", confidence);
        }

        @Override
        public String toString() {
            return cropName + " (" + getFormattedConfidence() + ")";
        }
    }
}