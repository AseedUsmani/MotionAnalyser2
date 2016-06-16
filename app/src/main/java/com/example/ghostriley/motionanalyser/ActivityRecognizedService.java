package com.example.ghostriley.motionanalyser;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.List;

/*
 * Created by GhostRiley on 28/05/2016.
 */


public class ActivityRecognizedService extends IntentService {

    AnalysingActivity mObject = new AnalysingActivity();
    int confidence;

    public ActivityRecognizedService() {
        super("ActivityRecognizedService");
    }

    public ActivityRecognizedService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            handleDetectedActivities(result.getProbableActivities());
        }
    }

    private void handleDetectedActivities(List<DetectedActivity> probableActivities) {
        confidence = mObject.confidence;
        mObject.mServiceCount++;

        for (DetectedActivity activity : probableActivities) {
            switch (activity.getType()) {
                case DetectedActivity.IN_VEHICLE: {
                    if (activity.getConfidence() >= confidence) {
                        mObject.mCount[0]++;
                        mObject.flag_d++;
                        mObject.flag_w = 0;
                        if (mObject.flag_d == 2) {
                        }
                    }
                    mObject.mActivity[0] = "In Vehicle: " + Integer.toString(activity.getConfidence()) + " " + Integer.toString(mObject.mCount[0]);
                    Log.e("ActivityRecognition", "In Vehicle: " + activity.getConfidence() + " " + Integer.toString(mObject.mCount[0]));
                    break;
                }
                case DetectedActivity.ON_BICYCLE: {
                    if (activity.getConfidence() >= confidence) {
                        mObject.mCount[1]++;
                    }
                    mObject.mActivity[1] = "Cycling: " + Integer.toString(activity.getConfidence()) + " " + Integer.toString(mObject.mCount[1]);
                    Log.e("ActivityRecognition", "Cycling: " + activity.getConfidence() + " " + Integer.toString(mObject.mCount[1]));
                    break;
                }
                case DetectedActivity.ON_FOOT: {
                    if (activity.getConfidence() >= confidence) {
                        mObject.mCount[2]++;
                    }
                    mObject.mActivity[2] = "On Foot: " + Integer.toString(activity.getConfidence()) + " " + Integer.toString(mObject.mCount[2]);
                    Log.e("ActivityRecognition", "On foot: " + activity.getConfidence() + " " + Integer.toString(mObject.mCount[2]));
                    break;
                }
                case DetectedActivity.RUNNING: {
                    if (activity.getConfidence() >= confidence) {
                        mObject.mCount[3]++;
                    }
                    mObject.mActivity[3] = "Running: " + Integer.toString(activity.getConfidence()) + " " + Integer.toString(mObject.mCount[3]);
                    Log.e("ActivityRecognition", "Running: " + activity.getConfidence() + " " + Integer.toString(mObject.mCount[3]));
                    break;
                }

                case DetectedActivity.STILL: {
                    if (activity.getConfidence() >= confidence) {
                        mObject.mCount[4]++;
                    }
                    mObject.mActivity[4] = "Still: " + Integer.toString(activity.getConfidence()) + " " + Integer.toString(mObject.mCount[4]);
                    Log.e("ActivityRecognition", "Still: " + activity.getConfidence() + " " + Integer.toString(mObject.mCount[4]));
                    break;
                }

                case DetectedActivity.WALKING: {
                    if (activity.getConfidence() >= confidence) {
                        mObject.mCount[5]++;

                        if (mObject.flag_d >= 2 || mObject.flag_w == 1) {
                            mObject.flag_w++;

                            if (mObject.flag_w == 2) {
                                mObject.flag_w = 0;
                                mObject.flag_d = 0;
                                mObject.flag = 1;
                            }
                        }
                    }
                    mObject.mActivity[5] = "Walking: " + Integer.toString(activity.getConfidence()) + " " + Integer.toString(mObject.mCount[5]);
                    Log.e("ActivityRecognition", "Walking: " + activity.getConfidence() + " " + Integer.toString(mObject.mCount[5]));
                    break;
                }

                case DetectedActivity.TILTING: {
                    if (activity.getConfidence() >= confidence) {
                        mObject.mCount[6]++;
                    }
                    mObject.mActivity[6] = "Tilting: " + Integer.toString(activity.getConfidence()) + " " + Integer.toString(mObject.mCount[6]);
                    Log.e("ActivityRecognition", "Tilting: " + activity.getConfidence() + " " + Integer.toString(mObject.mCount[6]));
                    break;
                }

                case DetectedActivity.UNKNOWN: {
                    if (activity.getConfidence() >= confidence) {
                        mObject.mCount[7]++;
                    }
                    mObject.mActivity[7] = "Unknown: " + Integer.toString(activity.getConfidence()) + " " + Integer.toString(mObject.mCount[7]);
                    Log.e("ActivityRecognition", "Unknown: " + activity.getConfidence() + " " + Integer.toString(mObject.mCount[7]));
                    break;
                }
            }
        }
    }
}