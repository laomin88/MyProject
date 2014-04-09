package com.charmenli.scalephone.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by charmenli on 2014/3/31.
 */
public class ScreenCaptureUtils {
    private static final String TAG = ScreenCaptureUtils.class.getSimpleName();

    private static void exec(String[] cmds, OutputStream outputStream) {
        Runtime runtime = Runtime.getRuntime();
        Process process = null;
        if (cmds == null) {
            return;
        }
        try {
            process = runtime.exec("su\n");//获取root权限
            OutputStreamWriter writer = new OutputStreamWriter(process.getOutputStream(), "UTF-8");
            writer.write("export LD_LIBRARY_PATH=/vendor/lib:/system/lib\n");
            writer.flush();
            //skip header
            byte[] header = new byte[process.getInputStream().available()];
            process.getInputStream().read(header);
            Log.d(TAG, "exec header = " + new String(header));
            for (String cmd : cmds) {
                Log.d(TAG, "exec cmd: " + cmd);
                writer.write(cmd + "\n");//执行命令
                writer.flush();
//                process.waitFor();//等待process执行完成
                Log.d(TAG, "exec cmd: " + cmd + " finish");
            }
            fillResult(process, outputStream);
            writer.write("exit\n");//退出su
            writer.flush();
            writer.write("exit\n"); //退出process
            writer.flush();
        } catch (Exception e) {
            Log.d(TAG, "exec " + Arrays.toString(cmds) + " failed");
        } finally {
            try {
                process.destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "exec all cmds: " + Arrays.toString(cmds) + " finished!");
    }

    private static void fillResult(Process process, OutputStream outputStream) {
        InputStream inputStream = process.getInputStream();
        int allread = 0;
        if (inputStream != null) {
            byte[] buf = new byte[4096];
            int read;
            try {
                while ((read = inputStream.read(buf))>0) {
    //                Log.d(TAG, "fillResult read = " + read);
                    allread += read;
                    outputStream.write(buf, 0, read);
                    if (read < buf.length)break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "fillResult all read = " + allread);
    }

    private static void exec(String cmd, OutputStream outputStream) {
        exec(new String[]{cmd}, outputStream);
    }

    private static InputStream change(ByteArrayOutputStream outputStream) {
        ByteArrayInputStream bais = new ByteArrayInputStream(outputStream.toByteArray());
        return bais;
    }

    public static Bitmap getScreenBitmap() {
        String cmd = "screencap -p";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        exec(cmd, baos);
        Bitmap bitmap = BitmapFactory.decodeStream(change(baos));
        IOUtils.closeQuietly(baos);
        return bitmap;
    }


}
