import 'dart:async';

import 'package:flutter/services.dart';

class Bglocation {
  static const MethodChannel _channel = MethodChannel('bglocation');
  static const EventChannel _eventChannel = EventChannel("bglocation/listen");

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Stream<Map<String, dynamic>> getCurrentPosition() {
    return _eventChannel
        .receiveBroadcastStream()
        .distinct()
        .map((event) => (Map.from(event)));
  }

  static Future<bool> stopListenet() async {
    try {
      return await _channel.invokeMethod("stop");
    } on PlatformException catch (e) {
      return false;
    }
  }

  static Future<Map> getStatus() async {
    try {
      return await _channel.invokeMethod("getStatus");
    } on PlatformException catch (e) {
      return {};
    }
  }

  static Future<bool> onCreate(String id,
      {String nameCollection = "ruta"}) async {
    try {
      return await _channel
          .invokeMethod("create", {"id": id, "nameCollection": nameCollection});
    } on PlatformException catch (e) {
      return false;
    }
  }

  static Future<bool> start() async {
    try {
      return await _channel.invokeMethod("start");
    } on PlatformException catch (e) {
      return false;
    }
  }

  static Future<bool> onResume() async {
    try {
      return await _channel.invokeMethod("onResume");
    } on PlatformException catch (e) {
      return false;
    }
  }

  static Future<bool> onPause() async {
    try {
      return await _channel.invokeMethod("onPause");
    } on PlatformException catch (e) {
      return false;
    }
  }

  static Future<bool> goForeground() async {
    try {
      return await _channel.invokeMethod("goForeground");
    } on PlatformException catch (e) {
      return false;
    }
  }

  static Future<bool> stopForeground() async {
    try {
      return await _channel.invokeMethod("stopForeground");
    } on PlatformException catch (e) {
      return false;
    }
  }

  static Future<bool> setIntervalNew(int interval) async {
    try {
      return await _channel.invokeMethod("intervalo", {
        "interval": interval,
      });
    } on PlatformException catch (e) {
      return false;
    }
  }
}
