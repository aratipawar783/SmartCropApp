package com.example.smartcropapp.userprofile;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class CropHelper {

    public enum CropShape {CIRCLE, SQUARE, RECTANGLE}

    // Returns cropped bitmap with finger gestures already applied in ImageView
    public static Bitmap cropImage(ImageView imageView, CropShape shape) {
        imageView.setDrawingCacheEnabled(true);
        Bitmap original = imageView.getDrawingCache();
        if (original == null) return null;

        int width = original.getWidth();
        int height = original.getHeight();
        Bitmap cropped;

        switch (shape) {
            case CIRCLE:
                int size = Math.min(width, height);
                cropped = Bitmap.createBitmap(original, (width - size)/2, (height - size)/2, size, size);
                break;
            case SQUARE:
                int minSide = Math.min(width, height);
                cropped = Bitmap.createBitmap(original, (width - minSide)/2, (height - minSide)/2, minSide, minSide);
                break;
            case RECTANGLE:
            default:
                cropped = Bitmap.createBitmap(original, 0, 0, width, height);
                break;
        }

        // Optional: If circle, mask outside area
        if (shape == CropShape.CIRCLE) {
            Bitmap output = Bitmap.createBitmap(cropped.getWidth(), cropped.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(output);
            canvas.drawARGB(0,0,0,0);
            android.graphics.Paint paint = new android.graphics.Paint();
            paint.setAntiAlias(true);
            float radius = cropped.getWidth() / 2f;
            canvas.drawCircle(radius, radius, radius, paint);
            paint.setXfermode(new android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(cropped, 0, 0, paint);
            cropped = output;
        }

        imageView.setDrawingCacheEnabled(false);
        return cropped;
    }

    // Simple touch listener to move & scale image inside ImageView
    public static void enableMoveAndScale(ImageView imageView) {
        imageView.setScaleType(ImageView.ScaleType.MATRIX);
        final Matrix matrix = new Matrix();
        final Matrix savedMatrix = new Matrix();
        final float[] startPoint = new float[2];
        final float[] midPoint = new float[2];
        final float[] lastEvent = new float[2];
        final float[] matrixValues = new float[9];
        final float[] prevDist = new float[1];
        final int NONE = 0, DRAG = 1, ZOOM = 2;
        final int[] mode = {NONE};

        imageView.setOnTouchListener((v, event) -> {
            ImageView view = (ImageView)v;
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    savedMatrix.set(matrix);
                    startPoint[0] = event.getX();
                    startPoint[1] = event.getY();
                    mode[0] = DRAG;
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    prevDist[0] = spacing(event);
                    if (prevDist[0] > 10f) {
                        savedMatrix.set(matrix);
                        midPoint(midPoint, event);
                        mode[0] = ZOOM;
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mode[0] == DRAG) {
                        matrix.set(savedMatrix);
                        matrix.postTranslate(event.getX() - startPoint[0], event.getY() - startPoint[1]);
                    } else if (mode[0] == ZOOM && event.getPointerCount() == 2) {
                        float newDist = spacing(event);
                        if (newDist > 10f) {
                            matrix.set(savedMatrix);
                            float scale = newDist / prevDist[0];
                            matrix.postScale(scale, scale, midPoint[0], midPoint[1]);
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                    mode[0] = NONE;
                    break;
            }
            view.setImageMatrix(matrix);
            return true;
        });
    }

    private static float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float)Math.sqrt(x*x + y*y);
    }

    private static void midPoint(float[] point, MotionEvent event) {
        point[0] = (event.getX(0) + event.getX(1))/2;
        point[1] = (event.getY(0) + event.getY(1))/2;
    }
}
