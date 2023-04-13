import 'dart:async';

import 'package:bglocation/models/location_model.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/services.dart';

class Bglocation {
  static const MethodChannel _channel = MethodChannel('bglocation');
  static const EventChannel _eventChannel = EventChannel("bglocation/listen");

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<dynamic> useFirebase({required bool useFirebase}) async {
    try {
      return await _channel
          .invokeMethod("useFirebase", {"useFirebase": useFirebase});
    } catch (e) {
      debugPrint("$e");
      return false;
    }
  }

  static Stream<LocationModel> getListenPosition() {
    return _eventChannel
        .receiveBroadcastStream()
        .distinct()
        .map((event) => LocationModel.fromMap(Map.from(event)));
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

  ///[pasajeroInterval]Time in millisecons 5*1000 for update location for user
  ///[conductorInterval]Time in millisecons 1*1000 for update conductor
  ///conductor time divide 2 in the fastUpdate
  static Future<bool> setIntervalNew(
      int pasajeroInterval, int conductorInterval) async {
    try {
      return await _channel.invokeMethod("intervalo", {
        "interval": pasajeroInterval,
        "conductorInterval": conductorInterval,
      });
    } on PlatformException catch (e) {
      return false;
    }
  }

  static Future<bool> setTitle({
    required String title,
    required String subTitle,
    required String textButton,
  }) async {
    try {
      return await _channel.invokeMethod("changeTitle", {
        "title": title,
        "subTitle": subTitle,
        "textButton": textButton,
      });
    } on PlatformException catch (e) {
      debugPrint("$e");
      return false;
    }
  }
}
