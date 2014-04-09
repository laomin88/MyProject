package com.charmenli.scalephone.util;

import android.os.Build;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;

public class CommandUtils {
    private static final String TAG = "CommandUtils";

    public static int exec(boolean root, boolean waitFor, String command) {
        return exec(root, waitFor, command, null, null);
    }

    public static int exec(boolean root, boolean waitFor, String[] commands) {
        return exec(root, waitFor, commands, null, null);
    }

    public static int exec(boolean root, boolean waitFor, String command, StringBuilder out, StringBuilder err) {
        return exec(root, waitFor, new String[]{command}, out, err);
    }

    public static int exec(boolean root, boolean waitFor, String[] commands, StringBuilder out, StringBuilder err) {
        Log.d(TAG, "exec root: " + root + ", waitFor: " + waitFor + ", command = " + Arrays.toString(commands));
        Runtime runtime = Runtime.getRuntime();
        Process process = null;
        int exitValue = -1;
        if (commands == null) {
            return -1;
        }

        try {
            if (root) {
                process = Runtime.getRuntime().exec("su");//获取root权限
                OutputStreamWriter localOutputStreamWriter = new OutputStreamWriter(process.getOutputStream(), "UTF-8");
                localOutputStreamWriter.write("export LD_LIBRARY_PATH=/vendor/lib:/system/lib\n");
                localOutputStreamWriter.flush();
                for(String command : commands) {
                    localOutputStreamWriter.write(command + "\n");//执行命令
                    localOutputStreamWriter.flush();
                }
                localOutputStreamWriter.write("exit\n");
                localOutputStreamWriter.flush();
                localOutputStreamWriter.close();
            } else {
                process = runtime.exec(commands);
                process.getOutputStream().write("exit\n".getBytes()); //退出process
            }

            if (waitFor) {
                process.waitFor();//等待process执行完成
            }

            byte[] outData = getData(process.getInputStream());//获取输出结果
            if (out != null) {
                out.append(new String(outData));
            }

            byte[] errorData = getData(process.getErrorStream());//获取异常结果
            if (err != null) {
                err.append(new String(errorData));
            }

            while(true) {
                try {
                    exitValue = process.exitValue();
                    break;
                } catch (Exception e) {
                    // TODO: handle exception
                    Thread.sleep(100);
                    Log.d(TAG, "exec wait to get exitValue!");
                }
            }

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            Log.e(TAG, e.toString());
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        Log.d(TAG, "exitValue: " + exitValue + ", out: " +  (out == null ? "":out.toString()) + ", err: " + (err == null ? "" : err.toString()));
        return exitValue;
    }

    private static byte[] getData(InputStream is) throws IOException {
        DataInputStream dis = new DataInputStream(is);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int read = -1;
        byte[] buffer = new byte[1024];
        while((read = dis.read(buffer)) > 0) {
            baos.write(buffer, 0, read);
            Arrays.fill(buffer, 0, buffer.length, (byte)0);
        }
        baos.close();
        return baos.toByteArray();
    }

    public static void gainRoot() {
        Log.d(TAG, "gainRoot start");
        Runtime runtime = Runtime.getRuntime();
        Process process = null;
        try {
            process = runtime.exec("su\n");
            process.getOutputStream().write("exit\n".getBytes());
            process.getOutputStream().write("exit\n".getBytes());
            process.waitFor();
        } catch (Exception e) {
            // TODO: handle exception
        } finally {
            if(process != null) {
                process.destroy();
            }
        }
        Log.d(TAG, "gainRoot finish");
    }

    public static boolean installApk(String path) {
        StringBuilder out = new StringBuilder();
        StringBuilder err = new StringBuilder();
        String cmd = "pm install -r " + path;
        exec(true, true, cmd, out, err);
        boolean ret = out.toString().replaceAll("\n", "").replaceAll("\r", "").endsWith("Success");
        return ret;
    }

    public static void forceStop(String pkgName) {
        exec(true, true, "am force-stop " + pkgName, new StringBuilder(), new StringBuilder());
    }

    public static void forceStop(String pkgName, int pid) {
        if (Build.VERSION.SDK_INT < 14) {
            exec(true, true, "kill -9 " + pid);
        } else {
            forceStop(pkgName);
        }
    }

    public static void forceStop(String[] pkgNames, Integer[] pids) {
        if (Build.VERSION.SDK_INT < 14) {
            String[] cmds = new String[pids.length];
            for (int i = 0;  i < pids.length; i++) {
                cmds[i] = "kill -9 " + pids[i];
            }
            exec(true, true, cmds);
        } else {
            String[] cmds = new String[pkgNames.length];
            for (int i = 0;  i < pkgNames.length; i++) {
                cmds[i] = "am force-stop " + pkgNames[i];
            }
            exec(true, true, cmds);
        }
    }
}
