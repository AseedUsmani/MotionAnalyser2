package com.example.ghostriley.motionanalyser;

import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class AnalysingActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public GoogleApiClient mApiClient;
    public static final String TAG = MainActivity.class.getSimpleName();


    protected String mFileName;
    protected String mConfidence;
    protected String mDelay;
    public Button mStartButton;
    public Button mFinishButton;
    public static int flag;
    public static int confidence;
    public static int[] mCount = {0, 0, 0, 0, 0, 0, 0, 0};
    public static String mActivity[] = {
            "Driving 0 0",
            "Cycling 0 0",
            "On Foot 0 0",
            "Running 0 0",
            "Still 0 0",
            "Walking 0 0",
            "Tilting 0 0",
            "Unknown 0 0"
    };
    public static int delayD;
    public static int delayW;
    public static int mServiceCount;
    public TextView textView0, textView1, textView2, textView3, textView4, textView5, textView6, textView7, textServiceCount;
    public static TextView mLatitude, mLongitude, lastUpdateText;
    public static int flag_d, flag_w;
    public static int mDelayTime;

    //For timer
    TextView time;
    long starttime = 0L;
    long timeInMilliseconds = 0L;
    long timeSwapBuff = 0L;
    long updatedtime = 0L;
    int t = 1;
    int secs = 0;
    int mins = 0;
    int milliseconds = 0;
    Handler handler = new Handler();

    private LocationManager locManager;
    private LocationListener locListener = new MyLocationListener();

    private boolean gps_enabled = false;
    private boolean network_enabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysing);

        time = (TextView) findViewById(R.id.timer);
        textView0 = (TextView) findViewById(R.id.textView0);
        textView1 = (TextView) findViewById(R.id.textView1);
        textView2 = (TextView) findViewById(R.id.textView2);
        textView3 = (TextView) findViewById(R.id.textView3);
        textView4 = (TextView) findViewById(R.id.textView4);
        textView5 = (TextView) findViewById(R.id.textView5);
        textView6 = (TextView) findViewById(R.id.textView6);
        textView7 = (TextView) findViewById(R.id.textView7);
        textServiceCount = (TextView) findViewById(R.id.serviceCount);
        mLatitude = (TextView) findViewById(R.id.latitudeText);
        mLongitude = (TextView) findViewById(R.id.longitudeText);
        lastUpdateText = (TextView) findViewById(R.id.lastUpdateText);

        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mStartButton = (Button) findViewById(R.id.startButton);
        mFinishButton = (Button) findViewById(R.id.finishButton);
        //locManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        //Retrieving information
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                mFileName = null;
                mConfidence = null;
                mDelay = null;

            } else {
                mFileName = extras.getString("fileName");
                mConfidence = extras.getString("confidence");
                mDelay = extras.getString("delayTime");
                confidence = Integer.parseInt(mConfidence);
                mDelayTime = Integer.parseInt(mDelay)*1000;
            }
        } else {
            mFileName = (String) savedInstanceState.getSerializable("fileName");
            mConfidence = (String) savedInstanceState.getSerializable("confidence");
            mDelay = (String) savedInstanceState.getSerializable("delayTime");
        } //information retrieved*/
        confidence = Integer.parseInt(mConfidence);

        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //resetting counter
                for (int j = 0; j < 8; j++) {
                    mCount[j] = 0;
                }
                flag = 0;
                mServiceCount = 0;
                flag_d = 0;
                flag_w = 0;
                mDelayTime = 10 * 60 * 1000;
                delayD = 10000;
                delayW = mDelayTime;

                mApiClient.connect();
                mStartButton.setVisibility(View.INVISIBLE);
                mFinishButton.setVisibility(View.VISIBLE);

                if (t == 1) {
                    starttime = SystemClock.uptimeMillis();
                    handler.postDelayed(updateTimer, 0);
                    t = 0;
                } else {
                    time.setTextColor(Color.BLUE);
                    timeSwapBuff += timeInMilliseconds;
                    handler.removeCallbacks(updateTimer);
                    t = 1;
                }
            }
        });

        mFinishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mApiClient.disconnect();

                //Saving file
                Toast.makeText(AnalysingActivity.this, "Saving file...", Toast.LENGTH_SHORT
                ).show();
                try {
                    saveFile();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(AnalysingActivity.this, "Failed to save file!", Toast.LENGTH_LONG).show();
                }

                //Resetting timer
                starttime = 0L;
                timeInMilliseconds = 0L;
                timeSwapBuff = 0L;
                updatedtime = 0L;
                t = 1;
                secs = 0;
                mins = 0;
                milliseconds = 0;
                handler.removeCallbacks(updateTimer);

                //restarting application
                Intent intent = new Intent(AnalysingActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Intent intent = new Intent(this, ActivityRecognizedService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(mApiClient, 5000, pendingIntent);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(AnalysingActivity.this, "Connection to Google Services suspended!", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(AnalysingActivity.this, "Connection to Google Services failed!", Toast.LENGTH_LONG).show();
    }


    public Runnable updateTimer = new Runnable() {
        public void run() {
            timeInMilliseconds = SystemClock.uptimeMillis() - starttime;
            updatedtime = timeSwapBuff + timeInMilliseconds;
            secs = (int) (updatedtime / 1000);
            mins = secs / 60;
            secs = secs % 60;
            milliseconds = (int) (updatedtime % 1000);
            time.setText("" + mins + ":" + String.format("%02d", secs) + ":"
                    + String.format("%03d", milliseconds));
            time.setTextColor(Color.RED);

            textView0.setText(mActivity[0]);
            textView1.setText(mActivity[1]);
            textView2.setText(mActivity[2]);
            textView3.setText(mActivity[3]);
            textView4.setText(mActivity[4]);
            textView5.setText(mActivity[5]);
            textView6.setText(mActivity[6]);
            textView7.setText(mActivity[7]);
            textServiceCount.setText("Service Count: " + Integer.toString(mServiceCount));

            if (flag == 1) {
                savingLocation();
                flag = 0;
            }

            handler.postDelayed(this, 0);
        }
    };

    public void saveFile() throws IOException {
        String appName = getString(R.string.app_name);

        if (isExternalStorageAvailable()) {
            String externalPath = Environment.getExternalStorageDirectory().toString();
            File mediaStorageDir = new File(externalPath, appName);

            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdir()) {
                    Log.e(TAG, "Failed to create directory");
                }
            }
            File file;
            java.util.Date now = new java.util.Date();
            String timestamp = new SimpleDateFormat("ddMM_HHmm", Locale.US).format(now);
            String path = mediaStorageDir.getPath() + File.separator;
            String fileName = mFileName + "_" + timestamp;
            file = new File(path + fileName + ".txt");

            PrintWriter out = new PrintWriter(new FileWriter(file));

            //Putting confidence as heading
            out.println("Confidence Level: " + Integer.toString(confidence));
            out.println("Time: " + time.getText().toString());
            out.println("");
            out.println("");
            time.setText("00:00:00");

            // Write each string in the array on a separate line
            for (int i = 0; i < 8; i++) {
                out.println(mActivity[i] + "\n");
            }
            out.println("Service count: " + Integer.toString(mServiceCount));

            out.close();
            Toast.makeText(AnalysingActivity.this, "File saved.", Toast.LENGTH_LONG).show();
        }
    }

    private boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED))
            return true;
        else return false;
    }

    public void savingLocation() {
        try {
            gps_enabled = locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }
        try {
            network_enabled = locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        // don't start listeners if no provider is enabled
        if (!gps_enabled && !network_enabled) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getBaseContext());
            builder.setTitle("Attention!");
            builder.setMessage("Sorry, location is not determined. Please enable location providers");
            builder.setPositiveButton("OK", (DialogInterface.OnClickListener) this);
            builder.setNeutralButton("Cancel", (DialogInterface.OnClickListener) this);
            builder.create().show();
            mLatitude.setText("Failed to get location");
            mLongitude.setText("Turn on location services");
        }

        if (gps_enabled) {
            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locListener);
        } else if (network_enabled) {
            locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locListener);
        }
    }

    class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                // This need
                //
                // s to stop getting the location data and save the battery power.
                locManager.removeUpdates(locListener);

                String longitude = "Longitude: " + location.getLongitude();
                String latitude = "Latitude: " + location.getLatitude();

                mLatitude.setText(latitude);
                mLongitude.setText(longitude);
                lastUpdateText.setText("" + mins + ":" + String.format("%02d", secs) + ":"
                        + String.format("%03d", milliseconds));
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO Auto-generated method stub
        }
    }

    /*@Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_NEUTRAL) {
            Toast.makeText(AnalysingActivity.this, "Sorry, location is not determined. To fix this please enable location providers",
                    Toast.LENGTH_SHORT).show();
        } else if (which == DialogInterface.BUTTON_POSITIVE) {
            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }
    }*/
}

