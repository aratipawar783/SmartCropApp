package com.example.smartcropapp.soil;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.List;

public class SoilClassifier {
    private Interpreter interpreter;
    private List<String> labels;

    public SoilClassifier(Context context) throws IOException {
        ByteBuffer modelBuffer = loadModelFile(context, "soil_classifier.tflite");
        interpreter = new Interpreter(modelBuffer);
        labels = Utils.loadLabels(context, "labels.txt");
    }

    private ByteBuffer loadModelFile(Context context, String modelName) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(modelName);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public Prediction classify(Bitmap bitmap) {
        Bitmap resized = Bitmap.createScaledBitmap(bitmap, 224, 224, true);

        // Allocate buffer for UINT8 input
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(224 * 224 * 3);
        byteBuffer.order(ByteOrder.nativeOrder());

        int[] pixels = new int[224 * 224];
        resized.getPixels(pixels, 0, 224, 0, 0, 224, 224);

        // Put raw RGB bytes (0-255)
        for (int pixel : pixels) {
            byteBuffer.put((byte) ((pixel >> 16) & 0xFF)); // R
            byteBuffer.put((byte) ((pixel >> 8) & 0xFF));  // G
            byteBuffer.put((byte) (pixel & 0xFF));         // B
        }

        byteBuffer.rewind();

        // Model outputs 1x4 uint8 scores
        byte[][] output = new byte[1][labels.size()];
        interpreter.run(byteBuffer, output);

        // Convert to float probabilities
        float[] scores = new float[labels.size()];
        for (int i = 0; i < labels.size(); i++) {
            // scale = 0.00390625 (1/256), zeroPoint = 0 from your model
            scores[i] = (output[0][i] & 0xFF) * 0.00390625f;
        }

        // Find max
        int maxIdx = 0;
        for (int i = 1; i < scores.length; i++) {
            if (scores[i] > scores[maxIdx]) maxIdx = i;
        }

        return new Prediction(labels.get(maxIdx), scores[maxIdx]);
    }

    public static class Prediction {
        public String soilType;
        public float confidence;

        public Prediction(String soilType, float confidence) {
            this.soilType = soilType;
            this.confidence = confidence;
        }
    }
}
