package com.proteus.sedemo;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.NonNull;
import android.widget.Toast;

public class MessengerService extends Service {

    static final int MSG_SAY_HELLO = 1;

    static class IncomingHandler extends Handler {
        private Context applicationContext;

        IncomingHandler(Context context) {
            applicationContext = context.getApplicationContext();
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MSG_SAY_HELLO:
                    Toast.makeText(applicationContext, "Hello", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    public MessengerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        Toast.makeText(this, "messenger binding", Toast.LENGTH_SHORT).show();
        return new Messenger(new IncomingHandler(this)).getBinder();
    }
}
