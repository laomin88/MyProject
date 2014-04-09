package com.charmenli.scalephone.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

/**
 * Created by charmenli on 2014/4/2.
 */
public class ImageUtils {

    public static Bitmap drawableToBitmap(Drawable drawable) // drawable 转换成 bitmap
    {
        int width = drawable.getIntrinsicWidth();   // 取 drawable 的长宽
        int height = drawable.getIntrinsicHeight();
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;         // 取 drawable 的颜色格式
        Bitmap bitmap = Bitmap.createBitmap(width, height, config);     // 建立对应 bitmap
        Canvas canvas = new Canvas(bitmap);         // 建立对应 bitmap 的画布
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);      // 把 drawable 内容画到画布中
        return bitmap;
    }

    public static Drawable zoomDrawable(Drawable drawable, int w, int h) {
        return new BitmapDrawable(imageScale(drawableToBitmap(drawable), w, h));       // 把 bitmap 转换成 drawable 并返回
    }

    public static Drawable zoomDrawable(Drawable drawable, float scale) {
        return new BitmapDrawable(imageScale(drawableToBitmap(drawable), scale));       // 把 bitmap 转换成 drawable 并返回
    }

    public static Bitmap imageScale(Bitmap bitmap, int dst_w, int dst_h) {

        int src_w = bitmap.getWidth();
        int src_h = bitmap.getHeight();
        float scale_w = ((float) dst_w) / src_w;
        float scale_h = ((float) dst_h) / src_h;
        return imageScale(bitmap, scale_w, scale_h);
    }

    public static Bitmap imageScale(Bitmap bitmap, float scale_w, float scale_h) {
        int src_w = bitmap.getWidth();
        int src_h = bitmap.getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale(scale_w, scale_h);
        Bitmap dstBitmap = Bitmap.createBitmap(bitmap, 0, 0, src_w, src_h, matrix, true);

        return dstBitmap;
    }

    public static Bitmap imageScale(Bitmap bitmap, float scale) {
        return imageScale(bitmap, scale, scale);
    }

    /**
     * 图片圆角处理
     */
    public static Bitmap getRCB(Bitmap bitmap, float roundPX) //RCB means Rounded Corner Bitmap
    {
        Bitmap dstbmp = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(dstbmp);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPX, roundPX, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return dstbmp;
    }
}

