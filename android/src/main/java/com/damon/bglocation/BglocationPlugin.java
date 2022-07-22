package com.damon.bglocation;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/** BglocationPlugin */
public class BglocationPlugin implements FlutterPlugin, MethodCallHandler,EventChannel.StreamHandler, ActivityAware {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private MethodChannel channel;
    private EventChannel.EventSink sink;
    private Tracking tracking;
    Activity activity;
    EventChannel eventChannel;
    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "bglocation");
        channel.setMethodCallHandler(this);
        eventChannel = new EventChannel(flutterPluginBinding.getBinaryMessenger(),"bglocation/listen");
        eventChannel.setStreamHandler(this);
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {

        switch (call.method) {

            case  "goForeground":
                if(tracking!=null){
                    tracking.goForeground();
                    result.success(true);
                }else{
                    result.success(false);
                }
                break;

            case "create":

                try {
                  Map<String,Object> s = (Map<String, Object>) call.arguments;
                  String id = (String) s.get("id");
                  String nameCollection = (String) s.get("nameCollection");
                  SharedPreferences sharedPreferences = activity.getSharedPreferences("com.damon.bglocation-data1",activity.MODE_PRIVATE);
                  SharedPreferences.Editor editor = sharedPreferences.edit();
                  editor.putString("com.damon.bglocation-id",id);
                  editor.putString("com.damon.bglocation-nameCollection",nameCollection);
                  editor.apply();

                  result.success(true);
                }catch (Exception e){
                  result.error("ERROR CREATE",e.getMessage(),false);
                }
                break;
            case "start":
              if(tracking ==null){
                tracking = new Tracking(activity,sink);
                if(!tracking.inListenet){
                  tracking.onCreate();
                  tracking.onStart();
                  tracking.onResume();
                }
//                new Handler().postDelayed(new Runnable() {
//                  @Override
//                  public void run() {
//                    if(!tracking.inListenet){
//                      tracking.requestLocationActulization();
//
//                    }
//                  }
//                },3000);
                if (tracking != null) {
                  new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                      tracking.requestLocationActulization();
                      result.success(true);

                    }
                  },3000);
                } else {
                  result.success(false);
                }
              }else {
                result.success(false);
              }

                break;
            case "stop":
                if (tracking != null) {

                    tracking.removeLocationActulizacion();
                    tracking.onStop();
                    result.success(true);
                } else {
                    result.success(false);

                }

                break;
            case "onResume":
                if (tracking != null) {
                    tracking.onResume();
                    result.success(true);

                }else {
                    result.success(false);
                }

                break;
            case "onPause":
                if (tracking != null) {
                    tracking.onPause();
                    result.success(true);

                }else {
                    result.success(false);

                }

                break;
            case "getStatus":
                Map<String, Object> data = new HashMap<>();
                if (tracking != null) {
                    data.put("status", tracking.inListenet);

                } else {
                    data.put("status", false);

                }
                result.success(data);
                break;
          case "stopForeground":
            if(tracking!=null){
              tracking.stopForeground();
              result.success(true);
            }else{
              Intent intent= new Intent(activity,LocationUpdatesService.class);
              activity.stopService(intent);
              result.success(false);
            }
            break;

          case "intervalo":
            if(tracking!=null){
              Map<String,Object> a = (Map<String, Object>) call.arguments;
              long interval = (long) a.get("interval");
              SharedPreferences sharedPreferences2 = activity.getSharedPreferences("com.damon.bglocation-data1",activity.MODE_PRIVATE);
              SharedPreferences.Editor editor2 = sharedPreferences2.edit();
              editor2.putLong("interval",interval);
              editor2.apply();
              result.success(true);
            }else {
              result.success(false);
            }

            break;
            default:
                result.notImplemented();
                break;
        }
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }


    @Override
    public void onListen(Object arguments, EventChannel.EventSink events) {
        sink = events;



    }

    @Override
    public void onCancel(Object arguments) {
        sink = null;
        if(tracking != null){
            tracking.removeLocationActulizacion();
            tracking = null;
        }
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        activity = binding.getActivity();
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
//      activity = null;
//      tracking = null;

    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
        activity = binding.getActivity();
    }

    @Override
    public void onDetachedFromActivity() {
//      activity = null;
//      tracking = null;
    }
}
