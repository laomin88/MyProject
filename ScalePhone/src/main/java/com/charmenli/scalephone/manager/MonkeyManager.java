package com.charmenli.scalephone.manager;

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import com.charmenli.scalephone.util.CommandUtils;
import com.charmenli.scalephone.util.Waiter;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Monker服务
 * 启动Monkey、停止Monkey、对接系统的Monkey、通过MonkeyBridge开放接口
 *
 * @author charmenli
 */
public class MonkeyManager {
    private static final String TAG = "MonkeyManager";

    private static final long TIME_OUT = 120000;
    private static final String MONKEY_SERVER = "com.android.commands.monkey";
    private static final String MONKEY_HOST = "127.0.0.1";
    private static final int MONKEY_PORT = 37894;
    private static final MonkeyManager SERVER = new MonkeyManager();

    private Socket mSocket;
    private OutputStreamWriter mMonkeyWriter;
    private MonkeyActionExecutor mMonkeyActionExecutor;

    private MonkeyManager() {
    }

    public final static MonkeyManager get() {
        return SERVER;
    }

    public boolean isRunning() {
        return bStarted.get();
    }

    private AtomicBoolean bStarted = new AtomicBoolean();

    public MonkeyActionExecutor startMonkeyServer() throws IOException {
        synchronized (bStarted) {
            if (bStarted.get()) {
                return mMonkeyActionExecutor;
            }
            stopMonkeyServer();
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    CommandUtils.exec(true, true, "monkey --port " + MONKEY_PORT);
                }
            };
            ThreadExecutor.execute(r);
            bStarted.set(true);
            if (waitMonkeyServer()) {
                initMonkeyServer();
            } else {
                throw new IllegalStateException("Start monkey server time out!");
            }
            mMonkeyActionExecutor = new MonkeyActionExecutor();
            return mMonkeyActionExecutor;
        }
    }

    public MonkeyActionExecutor getMonkeyActionExecutor() {
        if (bStarted.get()) return mMonkeyActionExecutor;
        return null;
    }

    private void initMonkeyServer() throws IOException {
        // TODO Auto-generated method stub
        mSocket = new Socket();
        mSocket.connect(new InetSocketAddress(MONKEY_HOST, MONKEY_PORT));
        mMonkeyWriter = new OutputStreamWriter(mSocket.getOutputStream());
        Log.d(TAG, "initMonkeyServer finished!");
    }

    public void stopMonkeyServer() {
        if (!bStarted.get()) {
            return;
        }
        Runnable r = new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                CommandUtils.exec(true, true, "killall " + MONKEY_SERVER);
            }
        };
        ThreadExecutor.execute(r);
        bStarted.set(false);
        mMonkeyActionExecutor = null;
    }

    private boolean waitMonkeyServer() {
        long timeout = SystemClock.uptimeMillis() + TIME_OUT;
        boolean ready = false;
        while (timeout > SystemClock.uptimeMillis() && !ready) {
            StringBuilder out = new StringBuilder();
            CommandUtils.exec(true, true, "ps | grep commands.monkey", out, null);
            Log.i(TAG, "waitMonkeyServer ps: " + out);
            if (out.toString().contains(MONKEY_SERVER)) {
                ready = true;
            }
            Waiter.sleep(300);
        }
        return ready;

    }

    private static class ThreadExecutor {
        private static Executor executor = Executors.newFixedThreadPool(10);

        public static void execute(Runnable r) {
            executor.execute(r);
        }
    }

    public class MonkeyActionExecutor {
        private Handler mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                final ArrayList<String> actions = msg.getData().getStringArrayList("actions");
                final boolean showIndicator = msg.getData().getBoolean("show", true);
                new Thread(){
                    @Override
                    public void run() {
                        try {
                            execute(actions, showIndicator);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        };
        private String execute(String action, boolean showIndicator) {
            synchronized (bStarted) {
                try {
                    Log.d(TAG, "execute action: " + action);
                    action = action.endsWith("\n") ? action : action+"\n";
                    mMonkeyWriter.write(action);
                    mMonkeyWriter.flush();
                    byte[] buffer = new byte[1024];
                    int read = mSocket.getInputStream().read(buffer);
                    return new String(buffer, 0, read);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    bStarted.set(false);
                } finally {
                    if (showIndicator) IndicatorManager.getInstance().showIndicator();
                }
            }
            return "false";
        }

        private void execute(ArrayList<String> actions) throws IOException {
            execute(actions, true);
        }

        private void execute(ArrayList<String> actions, boolean show) throws IOException {
            for (String action : actions) {
                String result = execute(action, false);
                Log.d(TAG, "execute action " + action + ", result:" + result);
            }
            if (show){
                IndicatorManager.getInstance().showIndicator();
            }
        }

        public void submit(ArrayList<String> actions) {
            submit(actions, true);
        }

        public void submit(ArrayList<String> actions, boolean show) {
            Message msg = Message.obtain();
            Bundle data = new Bundle();
            data.putStringArrayList("actions", actions);
            data.putBoolean("show", show);
            msg.setData(data);
            mHandler.sendMessageDelayed(msg, 300);
        }

        public void submit(String action) {
            Message msg = Message.obtain();
            Bundle data = new Bundle();
            ArrayList actions = new ArrayList(Arrays.asList(action));
            data.putStringArrayList("actions", actions);
            msg.setData(data);
            mHandler.sendMessageDelayed(msg, 300);
        }

        public static final int STEP_LEN = 15;
        public void turnLeft(Context context) {
            int width = (int) (getScreenWidth(context)*0.7);
            int y = getScreenHeight(context)/2;
            Point start = new Point(width, y);
            Point end = new Point(0, y);
            int steps = width/STEP_LEN;
            submit(getActions(start, end, steps), false);
        }

        public void turnRight(Context context) {
            int width = (int) (getScreenWidth(context)*0.7);
            int y = getScreenHeight(context)/2;
            Point start = new Point(10, y);
            Point end = new Point(width+10, y);
            int steps = width/STEP_LEN;
            submit(getActions(start, end, steps), false);
        }

        public void turnDown(Context context) {
            int x = getScreenWidth(context)/2;
            int height = (int) (getScreenHeight(context)*0.6);
            int offset = (getScreenHeight(context) - height) / 2;
            Point start = new Point(x, height+offset);
            Point end = new Point(x, offset);
            int steps = height/STEP_LEN;
            submit(getActions(start, end, steps), false);
        }

        public void turnUp(Context context) {
            int x = getScreenWidth(context)/2;
            int height = (int) (getScreenHeight(context)*0.6);
            int offset = (getScreenHeight(context) - height) / 2;
            Point end = new Point(x, height+offset);
            Point start = new Point(x, offset);
            int steps = height/STEP_LEN;
            submit(getActions(start, end, steps), false);
        }

        private int getScreenWidth(Context context) {
            return context.getResources().getDisplayMetrics().widthPixels;
        }

        private int getScreenHeight(Context context) {
            return context.getResources().getDisplayMetrics().heightPixels;
        }

        private ArrayList<String> getActions(Point start, Point end, int steps) {
            int sx = start.x, sy = start.y;
            int ex = end.x, ey = end.y;
            int stepLenX = (int) Math.ceil((ex - sx)/(float)steps);
            int stepLenY = (int) Math.ceil((ey - sy)/(float)steps);
            ArrayList<String> actions = new ArrayList<String>();
            final int intervalTime = 6;
            actions.add("touch down " + sx + " " + sy);
            actions.add("sleep " + intervalTime);
            for (int i = 1; i < steps; i++) {
                int x = sx + i * stepLenX;
                int y = sy + i * stepLenY;
                actions.add("touch move " + x + " " + y);
                actions.add("sleep " + intervalTime);
            }
            actions.add("touch up " + ex + " " + ey);
            return actions;
        }

    }

}
