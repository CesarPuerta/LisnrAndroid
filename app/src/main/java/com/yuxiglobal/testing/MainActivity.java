package com.yuxiglobal.testing;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import static com.yuxiglobal.testing.LisnrActivity.PERMISSIONS_REQUEST_RECORD_AUDIO;

public class MainActivity extends AppCompatActivity {
    @SuppressLint("StaticFieldLeak")
    EditText userInput, passwordInput, apiInput;
    Button b1;
    Intent in;

    public static final String MyPREFERENCES = "MyPrefs" ;
    public static final String User = "userKey";
    public static final String Passwd = "passwordKey";
    public static final String API = "apiKey";
    SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkLoginPermissions();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userInput=(EditText)findViewById(R.id.userData);
        passwordInput=(EditText)findViewById(R.id.passwordData);
        apiInput=(EditText)findViewById(R.id.apiUrl);

        userInput.setText("webiopi");
        passwordInput.setText("raspberry");
        apiInput.setText("http://10.3.9.96:8000/devices/mcp/0/value/1");

        b1=(Button)findViewById(R.id.button);
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        b1.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
            @Override
            public void onClick(View v) {
                String user = userInput.getText().toString();
                String password = passwordInput.getText().toString();
                String api = apiInput.getText().toString();

                SharedPreferences.Editor editor = sharedpreferences.edit();

                editor.putString(User, user);
                editor.putString(Passwd, password);
                editor.putString(API, api);
                editor.apply();

                in = new Intent(MainActivity.this, LisnrActivity.class);
                startActivity(in);
            }
        });
    }

    private void checkLoginPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Log.i(this.getClass().getSimpleName(), "Recording permissions not granted");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
        }
    }
}

