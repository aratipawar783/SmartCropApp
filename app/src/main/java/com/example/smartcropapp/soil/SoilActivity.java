package com.example.smartcropapp.soil;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.smartcropapp.R;
import com.example.smartcropapp.SettingsActivity;

import java.io.IOException;
import java.io.InputStream;

public class SoilActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSIONS = 100;
    private static final float CONFIDENCE_THRESHOLD = 0.60f; // require >= 60%
    private static final float MARGIN_THRESHOLD = 0.15f;     // top - second >= 15%

    ImageView ivSoilPreview;
    Button btnCamera, btnGallery, btnClassify, btnGeneratePdf, btnSeePdf;
    TextView tvResult, tvReport;

    Uri photoUri;
    Bitmap selectedBitmap;

    TFLiteClassifier classifier;
    ReportGenerator reportGenerator;
    PDFUtils pdfUtils;

    ActivityResultLauncher<Intent> cameraLauncher;
    ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soil);

        ivSoilPreview = findViewById(R.id.imgView);
        btnCamera = findViewById(R.id.btnCamera1);
        btnGallery = findViewById(R.id.btnGallery1);
        btnClassify = findViewById(R.id.btnClassify);
        btnGeneratePdf = findViewById(R.id.btnGeneratePdf);
        btnSeePdf = findViewById(R.id.btnSeePdf);
        tvResult = findViewById(R.id.tvResult);
        tvReport = findViewById(R.id.tvReport);
        Toolbar toolbar = findViewById(R.id.toolbar_soil);
        setSupportActionBar(toolbar);

// Set the title text
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Soil Advisory");
        }

// Set the title color to white
        toolbar.setTitleTextColor(Color.WHITE);

        // Initially disable PDF button
        btnGeneratePdf.setEnabled(false);


        try {
            classifier = new TFLiteClassifier(this, "soil_classifier.tflite", "labels.txt");
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to load model", Toast.LENGTH_LONG).show();
        }

        reportGenerator = new ReportGenerator();
        pdfUtils = new PDFUtils(this);

        checkAndRequestPermissions();

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        try {
                            if (photoUri != null) {
                                selectedBitmap = getBitmapFromUri(photoUri);
                                ivSoilPreview.setImageBitmap(selectedBitmap);
                                btnGeneratePdf.setEnabled(false);
                                tvResult.setText("");
                                tvReport.setText("");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        try {
                            selectedBitmap = getBitmapFromUri(uri);
                            ivSoilPreview.setImageBitmap(selectedBitmap);
                            btnGeneratePdf.setEnabled(false);
                            tvResult.setText("");
                            tvReport.setText("");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

        btnCamera.setOnClickListener(v -> openCamera());
        btnGallery.setOnClickListener(v -> openGallery());

        btnClassify.setOnClickListener(v -> {
            if (selectedBitmap == null) {
                Toast.makeText(this, "Please select or take a photo first", Toast.LENGTH_SHORT).show();
                return;
            }

            // Optional heuristic: skip obvious non-soil photos quickly
            boolean likelySoil = ImageUtils.isLikelySoil(selectedBitmap);

            TFLiteClassifier.Result res = classifier.classify(selectedBitmap);

            // Decide whether to accept model's top class
            boolean highEnough = res.confidence >= CONFIDENCE_THRESHOLD;
            boolean marginOk = (res.confidence - res.secondConfidence) >= MARGIN_THRESHOLD;

            if (!likelySoil) {
                // heuristic says not soil
                tvResult.setText("Unknown (image doesn't look like soil)");
                tvReport.setText("The image does not appear to be soil. Please capture a clear soil surface photo and try again.");
                btnGeneratePdf.setEnabled(false);
                return;
            }

            if (!highEnough || !marginOk) {
                tvResult.setText(String.format("Unknown (low confidence: %d%%) - Top: %s, 2nd: %s",
                        Math.round(res.confidence * 100), res.label, res.secondLabel));
                tvReport.setText("Model confidence is low or ambiguous. Please take another photo (plain soil surface, no hand/objects) or ensure better lighting.");
                btnGeneratePdf.setEnabled(false);
                return;
            }

            // GOOD: accept result
            String display = String.format("%s (%.0f%%)", res.label, res.confidence * 100f);
            tvResult.setText(display);
            Toast.makeText(this, "Soil classified..!", Toast.LENGTH_SHORT).show();

            String report = reportGenerator.generateReport(res.label, res.confidence);
            tvReport.setText(report);

            // Enable PDF generation now that we have an acceptable report
            btnGeneratePdf.setEnabled(true);
        });

        btnGeneratePdf.setOnClickListener(v -> {
            if (tvReport.getText().toString().isEmpty()) {
                Toast.makeText(this, "No report to generate. Classify first.", Toast.LENGTH_SHORT).show();
                return;
            }
            String filename = "soil_report_" + System.currentTimeMillis() + ".pdf";
            java.io.File pdfFile = pdfUtils.createPdf(filename, selectedBitmap,
                    tvResult.getText().toString(), tvReport.getText().toString());
            if (pdfFile != null) {
                Toast.makeText(this, "PDF saved: " + pdfFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Failed to create PDF", Toast.LENGTH_SHORT).show();
            }
        });

        btnSeePdf.setOnClickListener(v -> {
            java.io.File lastPdf = pdfUtils.getLastPdf();
            if (lastPdf == null) {
                Toast.makeText(this, "No PDF found. Generate one first.", Toast.LENGTH_SHORT).show();
                return;
            }

            Uri pdfUri = androidx.core.content.FileProvider.getUriForFile(
                    this,
                    getApplicationContext().getPackageName() + ".provider",
                    lastPdf
            );

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(pdfUri, "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_GRANT_READ_URI_PERMISSION);

            try {
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "No PDF viewer app found. Please install Google Drive or Adobe Reader.", Toast.LENGTH_LONG).show();
            }
        });

    }
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.soil_menu, menu);
        return true;
    }

    // Handle ActionBar item clicks
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.gen_pdf) {
            if (tvReport.getText().toString().isEmpty()) {
                Toast.makeText(this, "No report to generate. Classify first.", Toast.LENGTH_SHORT).show();

            }
            String filename = "soil_report_" + System.currentTimeMillis() + ".pdf";
            java.io.File pdfFile = pdfUtils.createPdf(filename, selectedBitmap,
                    tvResult.getText().toString(), tvReport.getText().toString());
            if (pdfFile != null) {
                Toast.makeText(this, "PDF saved: " + pdfFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Failed to create PDF", Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (id == R.id.see_pdf) {
            java.io.File lastPdf = pdfUtils.getLastPdf();
            if (lastPdf == null) {
                Toast.makeText(this, "No PDF found. Generate one first.", Toast.LENGTH_SHORT).show();

            }

            Uri pdfUri = androidx.core.content.FileProvider.getUriForFile(
                    this,
                    getApplicationContext().getPackageName() + ".provider",
                    lastPdf
            );

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(pdfUri, "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_GRANT_READ_URI_PERMISSION);

            try {
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "No PDF viewer app found. Please install Google Drive or Adobe Reader.", Toast.LENGTH_LONG).show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        Bitmap bitmap;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.Source src = ImageDecoder.createSource(getContentResolver(), uri);
            bitmap = ImageDecoder.decodeBitmap(src, (decoder, info, s) -> {
                decoder.setMutableRequired(true);
                decoder.setAllocator(ImageDecoder.ALLOCATOR_SOFTWARE);
            });
        } else {
            InputStream is = getContentResolver().openInputStream(uri);
            bitmap = android.graphics.BitmapFactory.decodeStream(is);
            if (is != null) is.close();
        }
        // ensure software ARGB_8888 for pixel access
        return bitmap.copy(Bitmap.Config.ARGB_8888, true);
    }

    // ... openCamera(), openGallery(), permissions code (unchanged) ...
    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "soil_photo");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From camera");
        photoUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        cameraLauncher.launch(intent);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private void checkAndRequestPermissions() {
        String[] permissions = new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        boolean need = false;
        for (String p : permissions) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                need = true;
                break;
            }
        }
        if (need) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
