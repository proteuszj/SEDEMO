package com.proteus.sedemo;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.proteus.utils.ByteArray;

import org.simalliance.openmobileapi.Channel;
import org.simalliance.openmobileapi.Reader;
import org.simalliance.openmobileapi.SEService;
import org.simalliance.openmobileapi.Session;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SEService.CallBack, ActivityCompat.OnRequestPermissionsResultCallback {
    public static final String TAG = "SEDEMO";
    public static final int MY_PERMISSIONS_REQUEST = 10001;
    public static final String MY_PERMISSION = "com.proteus.permission.SEDEMO";

    private static final int COLOR_SEND = 0xFF0033AA;
    private static final int COLOR_RECV = Color.BLUE;


    private static final String aidHex = "A0 00 00 05 59 10 10 FF FF FF FF 89 00 00 01 00";
    private static final String getEID = "80E2910006BF3E035C015A";
    private static final String getProfiles = "80E2910003BF2D00";
    private final Object obj_se = new Object();
    public List<String> dataString = new ArrayList<>();
    private SEService seService = null;
    private Session session = null;
    private Channel channel = null;
    private String capdu;
    private String rapdu;

    private LinearLayout logLayout;

    private StringBuffer logBuffer = new StringBuffer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        logLayout = findViewById(R.id.linearLayout);
        synchronized (obj_se) {
            new SEService(this, this);
        }
    }

    private void Log(String title, int color_title, String value) {
        LinearLayout linearLayout = new LinearLayout(this);
        TextView textView = new TextView(this);
        textView.setTextColor(color_title);
        textView.setText(title + "\t");
        linearLayout.addView(textView);
        if (null != value) {
            textView = new TextView(this);
            textView.setText(value);
            linearLayout.addView(textView);
        }
        logLayout.addView(linearLayout);
    }

    private void LogAPDU() {
        Log("send: ", COLOR_SEND, capdu);
        Log("recv: ", COLOR_RECV, rapdu);
    }

    public void send(View view) {
//        checkAndRequestPermission(MY_PERMISSION, MY_PERMISSIONS_REQUEST);
        capdu = ((EditText) findViewById(R.id.editText_capdu)).getText().toString();
        if (!capdu.isEmpty()) {
            rapdu = sendApdu(capdu);
            LogAPDU();
        }
    }

    public void getEID(View view) {
        capdu = getEID;
        rapdu = sendApdu(capdu);
        LogAPDU();
    }

    public void getProfiles(View view) {
        capdu = getProfiles;
        rapdu = sendApdu(capdu);
        LogAPDU();
    }

    public void showATR(View view) {
        Log("reset ", COLOR_SEND, null);
        Log("atr: \t", COLOR_RECV, new ByteArray(session.getATR()).toString());
    }

    public void clear(View view) {
        logLayout.removeAllViews();
    }

//    private void checkAndRequestPermission(String permission, int requestID) {
//        if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, permission)) {
//            log(permission + " granted");
//        } else {
//            log(permission + " denied");
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
//                log("should request rationale");
//                ActivityCompat.requestPermissions(this, new String[]{permission}, requestID);
//                log("request " + permission);
//            } else {
//                ActivityCompat.requestPermissions(this, new String[]{permission}, requestID);
//                log("request " + permission);
//            }
//        }
//    }

    private String sendApdu(String c_apdu) {
        if (null != channel && !channel.isClosed()) {
            ByteArray ba = new ByteArray(c_apdu);
            byte[] response;
            try {
                response = channel.transmit(ba.data());
                return new ByteArray(response).toString();
            } catch (IOException e) {
                e.printStackTrace();
                return "9FFF";
            }
        }
        return "FFFF";
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (MY_PERMISSIONS_REQUEST == requestCode) {
            if (grantResults.length == 1 && permissions.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "onRequestPermissionsResult: " + permissions[0] + " granted by user");
            } else {
                Log.d(TAG, "onRequestPermissionsResult: " + permissions[0] + " denied by user");
            }
        }
    }

    @Override
    public void serviceConnected(SEService seService) {
        Log.d(TAG, "serviceConnected: connected");
        synchronized (obj_se) {
            if (null != seService && seService.isConnected()) {
                this.seService = seService;
                Log.d(TAG, "serviceConnected: " + String.format("readers: %d", seService.getReaders().length));
                if (seService.getReaders().length > 0) {
                    for (int i = 0; i < seService.getReaders().length; i++)
                        Log.d(TAG, "serviceConnected: " + seService.getReaders()[i].getName());
                }
                Reader readerSIM = seService.getReaders()[0];
                if (readerSIM.isSecureElementPresent()) {
                    Log.d(TAG, "serviceConnected: " + readerSIM.getName() + " is present");
                    try {
                        session = readerSIM.openSession();
                        channel = session.openLogicalChannel(new ByteArray(aidHex).data());
                        Log("AID: \t", COLOR_SEND, aidHex);
                        Log.d(TAG, "serviceConnected: " + (channel.isClosed() ? "closed" : "open"));
                        if (!channel.isClosed()) {
                            Log.d(TAG, "serviceConnected: " + (channel.isBasicChannel() ? "basic" : "other channel"));
                            byte[] response = channel.getSelectResponse();
                            Log("recv: ", COLOR_RECV, new ByteArray(response).toString());
                            Log.d(TAG, "serviceConnected: " + new ByteArray(response).toString());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            obj_se.notifyAll();
        }
    }

}
