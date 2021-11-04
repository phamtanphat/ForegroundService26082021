package com.example.foregroundservice26082021;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MyService extends Service {

    MyHandlerThread handlerThread;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("BBB", "onCreate");
        handlerThread = new MyHandlerThread("HandlerThread");
        handlerThread.start();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (handlerThread.isAlive()) {
            handlerThread.setOnHandleMessage(new OnListenMessage() {
                @Override
                public void onHandleMessage(Message message) {
                    switch (message.what) {
                        case 1:
                            Log.d("BBB", message.obj + "");
                            break;
                        case 2:
                            Toast.makeText(getApplicationContext(), "Done", Toast.LENGTH_SHORT).show();
//                            Log.d("BBB",handlerThread.getState().name());
                            break;
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


    class MyHandlerThread extends HandlerThread {

        Handler handler;
        OnListenMessage onListenMessage;

        public MyHandlerThread(String name) {
            super(name);
        }

        @Override
        protected void onLooperPrepared() {
            super.onLooperPrepared();
            handler = new Handler(getLooper()) {
                @Override
                public void handleMessage(@NonNull Message msg) {
                    super.handleMessage(msg);
                    if (onListenMessage != null) {
                        onListenMessage.onHandleMessage(msg);
                    }
                }
            };
            for (int i = 0; i < 100; i++) {
                if (i == 50) {
                    quitSafely();
                }
                Message message = new Message();
                message.what = 1;
                message.obj = i;
                handler.sendMessage(message);

            }
            Message message2 = new Message();
            message2.what = 2;
            handler.sendMessage(message2);
        }

        public void setOnHandleMessage(OnListenMessage handleMessage) {
            onListenMessage = handleMessage;
        }
    }

    interface OnListenMessage {
        void onHandleMessage(Message message);
    }

}
