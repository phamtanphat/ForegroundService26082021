package com.example.foregroundservice26082021;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.util.Random;

public class MyService extends Service {

    NotificationManager notificationManager;
    MyDownLoadThread myDownLoadThread;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("BBB", "onCreate");
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        startForeground(1, createNotification("Loading", 10, 0));
        myDownLoadThread = new MyDownLoadThread("Thread-1");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!myDownLoadThread.isAlive()) {
            myDownLoadThread.start();
            myDownLoadThread.setOnListenerMessage(new OnListenerMessage() {
                @Override
                public void onHandleMessage(Message message) {
                    switch (message.what) {
                        case 1:
                            Log.d("BBB", message.obj.toString());
                            notificationManager.notify(1, createNotification("Loading", 10, (Integer) message.obj));
                            break;
                        case 2:
                            Toast.makeText(getApplicationContext(), "Down load finish", Toast.LENGTH_SHORT).show();
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    stopForeground(true);
                                    stopSelf();
                                }
                            }, 2000);
                    }

                }
            });
        }
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("BBB", "onDestroy");
        Toast.makeText(this, "onDestroy", Toast.LENGTH_SHORT).show();
    }

    public Notification createNotification(String message, int max, int min) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "CHANNEL_SERVICE");
        builder.setSmallIcon(android.R.drawable.ic_dialog_email);
        builder.setShowWhen(true);
        builder.setContentTitle("Down load");
        builder.setContentText(message);
        builder.setProgress(max, min, false);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel("CHANNEL_SERVICE", "CHANNEL", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        return builder.build();
    }

    class MyDownLoadThread extends HandlerThread {
        Handler handler;
        OnListenerMessage onListenerMessage;

        public MyDownLoadThread(String name) {
            super(name);
        }

        @Override
        protected void onLooperPrepared() {
            super.onLooperPrepared();
            handler = new Handler(getLooper()) {
                @Override
                public void handleMessage(@NonNull Message msg) {
                    super.handleMessage(msg);
                    onListenerMessage.onHandleMessage(msg);
                }
            };
            for (int i = 1; i <= 10; i++) {
                Message message = new Message();
                message.what = 1;
                message.obj = i;
                handler.dispatchMessage(message);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Message message2 = new Message();
            message2.what = 2;
            handler.sendMessage(message2);


        }

        public void setOnListenerMessage(OnListenerMessage onListenerMessage) {
            this.onListenerMessage = onListenerMessage;
        }
    }

    interface OnListenerMessage {
        void onHandleMessage(Message message);
    }

}
