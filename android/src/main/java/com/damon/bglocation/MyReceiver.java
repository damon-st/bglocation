package com.damon.bglocation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;

import java.util.HashMap;
import java.util.Map;

import io.flutter.plugin.common.EventChannel;

/**
 * Receiver for broadcasts sent by {@link LocationUpdatesService}.
 */
public class MyReceiver extends BroadcastReceiver {

    MyReceiver(){}
    EventChannel.EventSink  result;
    MyReceiver(EventChannel.EventSink result){
        this.result = result;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Location location = intent.getParcelableExtra(LocationUpdatesService.EXTRA_LOCATION);
        if (location != null) {

            Map<String,Object> data = new HashMap<>();
            data.put("latitude",location.getLatitude());
            data.put("longitude",location.getLongitude());
            data.put("accuracy",location.getAccuracy());
            data.put("altitude",location.getAltitude());
            data.put("bearing",location.getBearing());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                data.put("verticalAccuracyMeters",location.getVerticalAccuracyMeters());

                data.put("bearingAccuracyDegrees",location.getBearingAccuracyDegrees());
            }
            data.put("elapsedRealtimeNanos",location.getElapsedRealtimeNanos());
            data.put("provider",location.getProvider());
            data.put("speed",location.getSpeed());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                data.put("speedAccuracy",location.getSpeedAccuracyMetersPerSecond());
            }

            data.put("time",location.getTime());

//                Utils2.sendNotification(context,location.getLatitude()+"");
            result.success(data);
        }
    }
}