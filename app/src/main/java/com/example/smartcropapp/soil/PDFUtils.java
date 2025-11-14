package com.example.smartcropapp.soil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PDFUtils {
    private static final String TAG = "PdfUtils";
    private final Context context;

    public PDFUtils(Context ctx) {
        this.context = ctx;
    }

    public File createPdf(String filename, Bitmap thumbnail, String headerText, String reportText) {
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create(); // A4-like
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();

        int x = 20, y = 30;
        paint.setTextSize(18);
        canvas.drawText("Soil Advisory Report", x, y, paint);
        paint.setTextSize(12);
        y += 24;
        canvas.drawText(headerText, x, y, paint);

        if (thumbnail != null) {
            int thumbW = 120, thumbH = 120;
            Bitmap scaled = Bitmap.createScaledBitmap(thumbnail, thumbW, thumbH, true);
            canvas.drawBitmap(scaled, 420, 30, paint);
        }

        y += 24;
        paint.setTextSize(11);
        int lineHeight = 16;
        String[] lines = reportText.split("\n");
        for (String line : lines) {
            if (y + lineHeight > pageInfo.getPageHeight() - 40) break;
            canvas.drawText(line, x, y, paint);
            y += lineHeight;
        }

        document.finishPage(page);

        File pdfDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "SoilReports");
        if (!pdfDir.exists()) pdfDir.mkdirs();
        File file = new File(pdfDir, filename);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            document.writeTo(fos);
            document.close();
            return file;
        } catch (IOException e) {
            Log.e(TAG, "Error writing PDF", e);
            document.close();
            return null;
        }
    }

    public File getLastPdf() {
        File pdfDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "SoilReports");
        if (!pdfDir.exists()) return null;
        File[] files = pdfDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));
        if (files == null || files.length == 0) return null;
        File last = files[0];
        for (File f : files) if (f.lastModified() > last.lastModified()) last = f;
        return last;
    }
}
