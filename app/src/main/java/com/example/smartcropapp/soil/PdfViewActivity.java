package com.example.smartcropapp.soil;

import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;

public class PdfViewActivity extends AppCompatActivity {

    ImageView ivPdfPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ivPdfPage = new ImageView(this);
        setContentView(ivPdfPage);

        String path = getIntent().getStringExtra("pdf_path");
        if (path == null) {
            Toast.makeText(this, "No PDF path provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        File file = new File(path);
        try (ParcelFileDescriptor fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)) {
            PdfRenderer renderer = new PdfRenderer(fd);
            if (renderer.getPageCount() > 0) {
                PdfRenderer.Page page = renderer.openPage(0);
                Bitmap bmp = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
                page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                ivPdfPage.setImageBitmap(bmp);
                page.close();
            }
            renderer.close();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Unable to open PDF", Toast.LENGTH_SHORT).show();
        }
    }
}
