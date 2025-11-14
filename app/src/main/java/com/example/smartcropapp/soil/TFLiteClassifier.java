package com.example.smartcropapp.soil;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class TFLiteClassifier {
    private static final String TAG = "TFLiteClassifier";

    private static final int IMAGE_SIZE = 224; // model input
    private static final int NUM_CHANNELS = 3; // RGB

    private Interpreter interpreter;
    private List<String> labels;

    public static class Result {
        public final String label;
        public final float confidence;       // normalized 0..1
        public final String secondLabel;
        public final float secondConfidence; // normalized 0..1
        public final int topIndex;
        public final int secondIndex;
        public Result(String label, float confidence, String secondLabel, float secondConfidence, int topIndex, int secondIndex) {
            this.label = label;
            this.confidence = confidence;
            this.secondLabel = secondLabel;
            this.secondConfidence = secondConfidence;
            this.topIndex = topIndex;
            this.secondIndex = secondIndex;
        }
    }

    public TFLiteClassifier(Context context, String modelAssetPath, String labelsAssetPath) throws IOException {
        interpreter = new Interpreter(loadModelFile(context.getAssets(), modelAssetPath));
        labels = loadLabels(context.getAssets(), labelsAssetPath);
    }

    private MappedByteBuffer loadModelFile(AssetManager assetManager, String modelPath) throws IOException {
        AssetFileDescriptor afd = assetManager.openFd(modelPath);
        FileInputStream fis = new FileInputStream(afd.getFileDescriptor());
        FileChannel channel = fis.getChannel();
        long start = afd.getStartOffset();
        long len = afd.getDeclaredLength();
        return channel.map(FileChannel.MapMode.READ_ONLY, start, len);
    }

    private List<String> loadLabels(AssetManager assetManager, String labelPath) throws IOException {
        List<String> out = new ArrayList<>();
        BufferedReader br = new BufferedReader(new InputStreamReader(assetManager.open(labelPath)));
        String line;
        while ((line = br.readLine()) != null) out.add(line.trim());
        br.close();
        return out;
    }

    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        // Ensure correct size
        Bitmap resized = Bitmap.createScaledBitmap(bitmap, IMAGE_SIZE, IMAGE_SIZE, true);

        // For UINT8 input the model expects values 0..255 per channel
        ByteBuffer bb = ByteBuffer.allocateDirect(IMAGE_SIZE * IMAGE_SIZE * NUM_CHANNELS);
        bb.order(ByteOrder.nativeOrder());

        int[] pixels = new int[IMAGE_SIZE * IMAGE_SIZE];
        resized.getPixels(pixels, 0, IMAGE_SIZE, 0, 0, IMAGE_SIZE, IMAGE_SIZE);

        for (int px : pixels) {
            int r = (px >> 16) & 0xFF;
            int g = (px >> 8) & 0xFF;
            int b = px & 0xFF;
            bb.put((byte) r);
            bb.put((byte) g);
            bb.put((byte) b);
        }
        bb.rewind();
        return bb;
    }

    /**
     * Run inference and returns Result (label, confidence [0..1], secondLabel, secondConfidence)
     */
    public Result classify(Bitmap bitmap) {
        if (interpreter == null) {
            Log.e(TAG, "Interpreter not initialized");
            return new Result("Error", 0f, "Error", 0f, -1, -1);
        }

        // Defensive: copy hardware-backed bitmaps into ARGB_8888 software bitmap if needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (bitmap.getConfig() == Bitmap.Config.HARDWARE) {
                bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            }
        }

        ByteBuffer input = convertBitmapToByteBuffer(bitmap);

        // Output: model returns UINT8 probabilities (0..255) shaped [1, N]
        int outSize = interpreter.getOutputTensorCount() > 0
                ? interpreter.getOutputTensor(0).shape()[1] // [1,N] -> index 1 is N
                : labels.size();
        byte[][] output = new byte[1][outSize];
        interpreter.run(input, output);

        // find top 2
        int topIdx = -1, secondIdx = -1;
        int topVal = -1, secondVal = -1;
        for (int i = 0; i < output[0].length; ++i) {
            int val = output[0][i] & 0xFF; // unsigned
            if (val > topVal) {
                secondVal = topVal; secondIdx = topIdx;
                topVal = val; topIdx = i;
            } else if (val > secondVal) {
                secondVal = val; secondIdx = i;
            }
        }

        float topConf = topVal / 255.0f;
        float secondConf = secondVal >= 0 ? (secondVal / 255.0f) : 0f;

        String topLabel = (topIdx >= 0 && topIdx < labels.size()) ? labels.get(topIdx) : "Unknown";
        String secLabel = (secondIdx >= 0 && secondIdx < labels.size()) ? labels.get(secondIdx) : "Unknown";

        return new Result(topLabel, topConf, secLabel, secondConf, topIdx, secondIdx);
    }

    public void close() {
        if (interpreter != null) {
            interpreter.close();
            interpreter = null;
        }
    }
}
