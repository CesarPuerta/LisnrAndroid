package com.yuxiglobal.testing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;
import com.lisnr.sdk.LisnrContentManager;
import com.lisnr.sdk.LisnrManager;
import com.lisnr.sdk.LisnrTextTone;
import com.lisnr.sdk.exceptions.InvalidConfigurationException;
import com.lisnr.sdk.exceptions.RecordingPermissionMissingException;
import com.lisnr.sdk.exceptions.RecordingUnavailableException;
import android.widget.TextView;

import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class LisnrActivity extends MainActivity{
    LisnrManager lisnr;
    LisnrContentManager contentManager;
    TextView listeningStatus;
    String key = "e998ec8e-9ab9-4932-aef4-5e4d0603e44c";

    static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lisnr);

        listeningStatus = (TextView)findViewById(R.id.listeningStatus);
        lisnr = LisnrManager.getConfiguredInstance(key, getApplication());
        contentManager = new LisnrContentManager(lisnr);
        LocalBroadcastManager.getInstance(this).registerReceiver(contentRetrieved, new IntentFilter(LisnrManager.ACTION_TEXT_TONE_HEARD));
        listenForTone();
    }

    BroadcastReceiver contentRetrieved = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Parcelable result = intent.getParcelableExtra(LisnrManager.EXTRA_TEXT_TONE_HEARD);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
                new SendPostRequest().execute(context, result);
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                try {
                    lisnr.startListening();
                    //lisnr.startBroadcastingTone();
                } catch (RecordingPermissionMissingException | InvalidConfigurationException | RecordingUnavailableException e) {
                    e.printStackTrace();
                }
            } else {
                Log.w(this.getClass().getSimpleName(), "Recording permissions denied. Please enable them in app settings");
            }
        }
    }

    private void listenForTone() {
        if(lisnr.getCurrentStatus() != LisnrManager.LISNR_STATUS.LISTENING) {
            try {
                lisnr.startListening();
            } catch (RecordingPermissionMissingException e) {
                Toast.makeText(this,"Recording Permission is missing.",Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (InvalidConfigurationException e) {
                e.printStackTrace();
            } catch (RecordingUnavailableException e) {
                Toast.makeText(this,"Recording is unavailable, likely another app is recording.",Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    private class SendPostRequest extends AsyncTask<Object, Void, String> {
        Object[] FinalData;
        protected void onPreExecute(){}

        protected String doInBackground(Object... arguments) {
            try {
                FinalData = arguments;
                URL url = new URL(sharedpreferences.getString(API, ""));

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);


                String userName = String.valueOf(sharedpreferences.getString(User, ""));
                String password = String.valueOf(sharedpreferences.getString(Passwd, ""));


                byte[] data = (userName + ":" + password).getBytes("US-ASCII");

                String encodedAuth = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.FROYO) {
                    encodedAuth = Base64.encodeToString(data, Base64.DEFAULT  );
                }

                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Authorization", "Basic " + encodedAuth);

                int responseCode= conn.getResponseCode();
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    return "True";
                }
                else {
                    return "False";
                }
            }
            catch(Exception e){
                return "Exception: " + e.getMessage();
            }
        }

        protected void onPostExecute(String  response) {
            if (Boolean.valueOf(response)) {
                listeningStatus.setText("Access granted");
            } else {
                listeningStatus.setText("Incorrect params");
            }
        }
    }
}