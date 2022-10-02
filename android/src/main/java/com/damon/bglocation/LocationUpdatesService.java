package com.damon.bglocation;
/**
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Process;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A bound and started service that is promoted to a foreground service when location updates have
 * been requested and all clients unbind.
 *
 * For apps running in the background on "O" devices, location is computed only once every 10
 * minutes and delivered batched every 30 minutes. This restriction applies even to apps
 * targeting "N" or lower which are run on "O" devices.
 *
 * This sample show how to use a long-running service for location updates. When an activity is
 * bound to this service, frequent location updates are permitted. When the activity is removed
 * from the foreground, the service promotes itself to a foreground service, and location updates
 * continue. When the activity comes back to the foreground, the foreground service stops, and the
 * notification assocaited with that service is removed.
 */
public class LocationUpdatesService extends Service {

    private FirebaseFirestore collectionReference = FirebaseFirestore.getInstance();

    private static final String PACKAGE_NAME =
            "com.damon.bglocation";

    private static final String TAG = LocationUpdatesService.class.getSimpleName();

    /**
     * The name of the channel for notifications.
     */
    private static final String CHANNEL_ID = "tracking";

    static final String ACTION_BROADCAST = PACKAGE_NAME + ".broadcast";

    static final String EXTRA_LOCATION = PACKAGE_NAME + ".location";
    private static final String EXTRA_STARTED_FROM_NOTIFICATION = PACKAGE_NAME +
            ".started_from_notification";

    private final IBinder mBinder = new LocalBinder();

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private   long UPDATE_INTERVAL_IN_MILLISECONDS = 1000;

    /**
     * The fastest rate for active location updates. Updates will never be more frequent
     * than this value.
     */
    private   long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    /*
    * The value for update in firestore for users listen
     */
    private  long TIME_USER_UPDATE = 5000;
    private  long timeForUpdate= TIME_USER_UPDATE;
    /**
     * The identifier for the notification displayed for the foreground service.
     */
    private static final int NOTIFICATION_ID = 12345678;

    /**
     * Used to check whether the bound activity has really gone away and not unbound as part of an
     * orientation change. We create a foreground service notification only if the former takes
     * place.
     */
    private boolean mChangingConfiguration = false;

    private NotificationManager mNotificationManager;

    /**
     * Contains parameters used by {@link com.google.android.gms.location.FusedLocationProviderApi}.
     */
    private LocationRequest mLocationRequest;

    /**
     * Provides access to the Fused Location Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient;

    /**
     * Callback for changes in location.
     */
    private LocationCallback mLocationCallback;

    private Handler mServiceHandler;

    /**
     * The current location.
     */
    private Location mLocation;

    String id ="";
    String collection = "ruta";

    private  String titleNotify ="Plann momentos que marcan";
    private  String subtitleNotify ="En ruta";
    private  String textButtonNotify ="Dejar de notificar a los pasajeros";

    public LocationUpdatesService() {
    }

    @Override
    public void onCreate() {
        LocationServices.getGeofencingClient(this);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.setMockMode(false);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                onNewLocation(locationResult.getLastLocation());
            }
        };
        FirebaseApp.initializeApp(this);
        SharedPreferences sharedPreferences = getSharedPreferences(PACKAGE_NAME+"-data1",MODE_PRIVATE);
        id =  sharedPreferences.getString(PACKAGE_NAME+"-id","hola");
        collection = sharedPreferences.getString(PACKAGE_NAME+"-nameCollection","ruta");
      titleNotify = sharedPreferences.getString(PACKAGE_NAME+"-title","Plann momentos que marcan");
      subtitleNotify = sharedPreferences.getString(PACKAGE_NAME+"-subTitle","En ruta");
      textButtonNotify = sharedPreferences.getString(PACKAGE_NAME+"-textButton","Dejar de notificar a los pasajeros");

        UPDATE_INTERVAL_IN_MILLISECONDS = sharedPreferences.getLong("conductorInterval",1000);
        FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS/2;

        TIME_USER_UPDATE = sharedPreferences.getLong("interval",5000);

        createLocationRequest(UPDATE_INTERVAL_IN_MILLISECONDS,FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS,true);
        getLastLocation();

        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        mServiceHandler = new Handler(handlerThread.getLooper());
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            // Create the channel for the notification
            NotificationChannel mChannel =
                    new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH);
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mChannel.setAllowBubbles(true);
          }
          mChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            mChannel.setDescription("This channel using show notification for tracking");
            // Set the Notification Channel for the Notification Manager.
            mNotificationManager.createNotificationChannel(mChannel);
        }


    }

    public void goForeground(){
                new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startForeground(NOTIFICATION_ID, getNotification());
//              Intent intent = new Intent(getApplicationContext(), LocationUpdatesService.class);
////              intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
//              onUnbind(intent);

            }
        },1000);


    }

    public  void stopForeground(){
        stopForeground(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Service started");
        boolean startedFromNotification = intent.getBooleanExtra(EXTRA_STARTED_FROM_NOTIFICATION,
                false);

        // We got here because the user decided to remove location updates from the notification.
        if (startedFromNotification) {
            removeLocationUpdates();
            stopSelf(NOTIFICATION_ID);
            Process.killProcess(Process.myPid());
        }
        // Tells the system to not try to recreate the service after it has been killed.
        return START_NOT_STICKY;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mChangingConfiguration = true;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Called when a client (MainActivity in case of this sample) comes to the foreground
        // and binds with this service. The service should cease to be a foreground service
        // when that happens.
        Log.i(TAG, "in onBind()");
        stopForeground(true);
        mChangingConfiguration = false;


        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        // Called when a client (MainActivity in case of this sample) returns to the foreground
        // and binds once again with this service. The service should cease to be a foreground
        // service when that happens.
        Log.i(TAG, "in onRebind()");
        stopForeground(true);
        mChangingConfiguration = false;
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "Last client unbound from service");

        // Called when the last client (MainActivity in case of this sample) unbinds from this
        // service. If this method is called due to a configuration change in MainActivity, we
        // do nothing. Otherwise, we make this service a foreground service.
        if (!mChangingConfiguration && Utils.requestingLocationUpdates(this)) {
            Log.i(TAG, "Starting foreground service");
            /*
            // TODO(developer). If targeting O, use the following code.
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
                mNotificationManager.startServiceInForeground(new Intent(this,
                        LocationUpdatesService.kt.class), NOTIFICATION_ID, getNotification());
            } else {
                startForeground(NOTIFICATION_ID, getNotification());
            }
             */
            try{
                startForeground(NOTIFICATION_ID, getNotification());
            }catch(Exception e) {
                    Log.i(TAG,"ERROR "+e.getMessage());
            }
        }
        return true; // Ensures onRebind() is called when a client re-binds.
    }

    @Override
    public void onDestroy() {
        mServiceHandler.removeCallbacksAndMessages(null);
        removeLocationUpdates();
    }

    /**
     * Makes a request for location updates. Note that in this sample we merely log the
     * {@link SecurityException}.
     */
    public void requestLocationUpdates() {
        Log.i(TAG, "Requesting location updates");
        Utils.setRequestingLocationUpdates(this, true);
        startService(new Intent(getApplicationContext(), LocationUpdatesService.class));
        try {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback, Looper.myLooper());
        } catch (SecurityException unlikely) {
            Utils.setRequestingLocationUpdates(this, false);
            Log.e(TAG, "Lost location permission. Could not request updates. " + unlikely);
        }
    }

    /**
     * Removes location updates. Note that in this sample we merely log the
     * {@link SecurityException}.
     */
    @SuppressLint("WrongConstant")
    public void removeLocationUpdates() {
        Log.i(TAG, "Removing location updates");
        try {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            Utils.setRequestingLocationUpdates(this, false);
            stopForeground();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mNotificationManager.deleteNotificationChannel(CHANNEL_ID);
            }
          stopSelf();


        } catch (SecurityException unlikely) {
            Utils.setRequestingLocationUpdates(this, true);
            Log.e(TAG, "Lost location permission. Could not remove updates. " + unlikely);
        }
    }

    /**
     * Returns the {@link NotificationCompat} used as part of the foreground service.
     */
    private Notification getNotification() {
        Intent intent = new Intent(this, LocationUpdatesService.class);

        CharSequence text = Utils.getLocationText(mLocation);

        // Extra to help us figure out if we arrived in onStartCommand via the notification or not.
        intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, true);

        // The PendingIntent that leads to a call to onStartCommand() in this service.
        PendingIntent servicePendingIntent = PendingIntent.getService(this, 0, intent,
                PendingIntent.FLAG_MUTABLE);

        // The PendingIntent to launch activity.
        PendingIntent activityPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, BglocationPlugin.class), PendingIntent.FLAG_MUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,CHANNEL_ID)
//                .addAction(R.drawable.ic_launch, getString(R.string.launch_activity),
//                        activityPendingIntent)
                .addAction(R.drawable.ic_cancel, textButtonNotify,
                        servicePendingIntent)
                .setContentText(subtitleNotify)
                .setContentTitle(titleNotify)
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("En ruta...")
                .setSilent(true)
                .setWhen(System.currentTimeMillis());

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        builder.setCategory(Notification.CATEGORY_SERVICE);
      }

      // Set the Channel ID for Android O.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID); // Channel ID
        }

        return builder.build();
    }

    private void getLastLocation() {
        try {
            mFusedLocationClient.getLastLocation()
                    .addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                mLocation = task.getResult();
                            } else {
                                Log.w(TAG, "Failed to get location.");
                            }
                        }
                    });
        } catch (SecurityException unlikely) {
            Log.e(TAG, "Lost location permission." + unlikely);
        }
    }

    private void onNewLocation(Location location) {
        Log.i(TAG, "New location: " + location);

        mLocation = location;

        // Notify anyone listening for broadcasts about the new location.
        Intent intent = new Intent(ACTION_BROADCAST);
        intent.putExtra(EXTRA_LOCATION, location);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

        // Update notification content if running as a foreground service.
        if (serviceIsRunningInForeground(this)) {
            mNotificationManager.notify(NOTIFICATION_ID, getNotification());
            try {
                savedLocation(mLocation);
            }catch (Exception e){
                System.out.println(e);
            }

        }else {
            try {
                savedLocation(mLocation);
            }catch (Exception e){
                System.out.println(e);

            }
        }
    }

    void savedLocation(Location location){
        Date  date= new Date();
        Log.i(TAG,"ENTRO AQUI -1 "+date.toString());
        if(timeForUpdate>=0){
            timeForUpdate=timeForUpdate-UPDATE_INTERVAL_IN_MILLISECONDS;
        }
        if(timeForUpdate==0){

          Map<String,Object> data = new HashMap<>();
          data.put("accuracy",location.getAccuracy());
          data.put("altitude",location.getAltitude());
          data.put("bearing",location.getBearing());
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            data.put("bearingAccuracyDegrees",location.getBearingAccuracyDegrees());

            data.put("verticalAccuracyMeters",location.getVerticalAccuracyMeters());

          }
          if (Build.VERSION.SDK_INT >= 33) {
            data.put("complete",false);
          }
          data.put("elapsedRealtimeNanos",location.getElapsedRealtimeNanos());
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            data.put("elapsedRealtimeUncertaintyNanos",location.getElapsedRealtimeUncertaintyNanos());
          }
          data.put("fromMockProvider",location.isFromMockProvider());

          data.put("latitude",location.getLatitude());
          data.put("longitude",location.getLongitude());

          data.put("provider",location.getProvider());
          data.put("speed",location.getSpeed());
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            data.put("speedAccuracyMetersPerSecond",location.getSpeedAccuracyMetersPerSecond());

            data.put("speedAccuracy",location.getSpeedAccuracyMetersPerSecond());
          }

          data.put("time",location.getTime());
            timeForUpdate=TIME_USER_UPDATE;
            collectionReference
                    .collection(collection)
                    .document(id)
                    .set(data);
            Date  date2= new Date();
            Log.i(TAG,"ENTRO AQUI -2 "+date2.toString());
        }
        Date  date1= new Date();
        Log.i(TAG,"ENTRO AQUI -3 "+date1.toString());
    }


    /**
     * Sets the location request parameters.
     */
    private void createLocationRequest(long interval,long fastestInterval,boolean waitForAccurate) {
        mLocationRequest = LocationRequest.create()
        .setInterval(interval)
        .setFastestInterval(fastestInterval)
         .setWaitForAccurateLocation(waitForAccurate)
        .setPriority(Priority.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Class used for the client Binder.  Since this service runs in the same process as its
     * clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        LocationUpdatesService getService() {
            return LocationUpdatesService.this;
        }
    }

    /**
     * Returns true if this is a foreground service.
     *
     * @param context The {@link Context}.
     */
    public boolean serviceIsRunningInForeground(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(
                Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(
                Integer.MAX_VALUE)) {
            if (getClass().getName().equals(service.service.getClassName())) {
                if (service.foreground) {
                    return true;
                }
            }
        }
        return false;
    }
}
