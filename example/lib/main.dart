import 'dart:convert';
import 'dart:io';

import 'package:firebase_core/firebase_core.dart';
import 'package:flutter/material.dart';
import 'dart:async';

import 'package:bglocation/bglocation.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  if (Platform.isAndroid) {
    await Firebase.initializeApp(
        options: const FirebaseOptions(
            apiKey: "AIzaSyBpxaOc5SLtWJQ9eztLlM3RPZtn7K0Zbhs",
            authDomain: "angular-html-cfe31.firebaseapp.com",
            databaseURL: "https://angular-html-cfe31.firebaseio.com",
            projectId: "angular-html-cfe31",
            storageBucket: "angular-html-cfe31.appspot.com",
            messagingSenderId: "724649066042",
            appId: "1:724649066042:web:e38a5917eec8f6703abf03",
            measurementId: "G-2W3KDWV1SV"));
  } else {
    await Firebase.initializeApp();
  }
  await flutterLocalNotificationsPlugin
      .resolvePlatformSpecificImplementation<
          AndroidFlutterLocalNotificationsPlugin>()
      ?.createNotificationChannel(channel);

  await flutterLocalNotificationsPlugin
      .resolvePlatformSpecificImplementation<
          IOSFlutterLocalNotificationsPlugin>()
      ?.requestPermissions(alert: true, badge: true, sound: true);
  const android = AndroidInitializationSettings("@mipmap/ic_launcher");
  const ios = DarwinInitializationSettings();
  const settings = InitializationSettings(android: android, iOS: ios);

  await flutterLocalNotificationsPlugin.initialize(settings,
      onDidReceiveBackgroundNotificationResponse:
          onDidReceiveBackgroundNotificationResponse,
      onDidReceiveNotificationResponse: onDidReceiveNotificationResponse);
  runApp(const MaterialApp(
    home: MyApp(),
  ));
}

class MyApp extends StatefulWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  bool isRunning = false;

  StreamSubscription<Map<String, dynamic>>? subPosition;

  @override
  void initState() {
    super.initState();
    // initPlatformState();
    subPosition = Bglocation.getCurrentPosition().listen((event) {
      if (mounted) {
        //  showNotification("Notification", jsonEncode(event));

        setState(() {
          _platformVersion = jsonEncode(event);
        });
      }
    });
  }

  @override
  void dispose() {
    subPosition?.cancel();
    super.dispose();
  }

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    // initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    if (Platform.isIOS) {
      start();
      return;
    }
    await Bglocation.stopListenet();
    await Bglocation.stopForeground();
    Map r = await Bglocation.getStatus();
    isRunning = r["status"];
    debugPrint("$isRunning");
    if (!isRunning) {
      await Bglocation.onCreate("DASASDAS");

      subPosition = Bglocation.getCurrentPosition().listen((event) {
        if (mounted) {
          setState(() {
            _platformVersion = jsonEncode(event);
          });
        }
      });
      await Bglocation.start();
      await Bglocation.goForeground();
    } else {
      // await Bglocation.stopListenet();
      // await Bglocation.stopForeground();
      // await Future.delayed(const Duration(seconds: 3), () {});
    }

    if (!mounted) return;
    setState(() {
      _platformVersion = "$r";
    });
  }

  void start() async {
    try {
      final r = await Bglocation.start();
      showMsg("Start $r");
    } catch (e) {
      debugPrint("$e");
    }
  }

  Future<void> stopListen() async {
    bool r = await Bglocation.stopListenet();
    print(r);
  }

  void getStatus() async {
    final r = await Bglocation.getStatus();
    if (!mounted) {
      return;
    }
    setState(() {
      _platformVersion = "$r";
    });
  }

  void setInterval() async {
    bool r = await Bglocation.setIntervalNew(5000, 500);
  }

  void setTitle() async {
    await Bglocation.setTitle(
        title: "hola", subTitle: "como", textButton: "cancela");
  }

  void requestPermission() async {
    try {
      final result = await Bglocation.requestPermission();
      debugPrint("$result");
      showMsg("Permiso locarton result $result");
    } catch (e) {
      debugPrint("$e");
    }
  }

  void showMsg(String msg) async {
    try {
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(msg)));
    } catch (e) {
      debugPrint("$e");
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Plugin example app'),
      ),
      body: Column(
        crossAxisAlignment: CrossAxisAlignment.center,
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Center(
            child: Text('Running on: $_platformVersion\n'),
          ),
          ElevatedButton(
            onPressed: requestPermission,
            child: Text("Request Permission"),
          ),
          ElevatedButton(
            onPressed: initPlatformState,
            child: Text("Listen position"),
          ),
          ElevatedButton(
            onPressed: stopListen,
            child: Text("Stop listent"),
          ),
          ElevatedButton(
            onPressed: getStatus,
            child: Text("Get status"),
          ),
          ElevatedButton(
            onPressed: setInterval,
            child: Text("Set interval time"),
          ),
          ElevatedButton(
            onPressed: setTitle,
            child: Text("Set title"),
          ),
        ],
      ),
    );
  }
}

final FlutterLocalNotificationsPlugin flutterLocalNotificationsPlugin =
    FlutterLocalNotificationsPlugin();

const AndroidNotificationChannel channel = AndroidNotificationChannel(
    'high_importance_channel2', 'high_importance_channel',
    description: 'This channel is used for important notifications.',
    importance: Importance.max,
    enableLights: true,
    enableVibration: true,
    showBadge: true,
    playSound: true);

void onDidReceiveBackgroundNotificationResponse(NotificationResponse details) {}

void onDidReceiveNotificationResponse(NotificationResponse details) {}
void showNotification(String title, String msg) async {
  try {
    debugPrint("ENTRO EN NOTIFICATION");
    await flutterLocalNotificationsPlugin.show(
        450234,
        title,
        msg,
        NotificationDetails(
          android: AndroidNotificationDetails(channel.id, channel.name,
              color: Colors.blue, playSound: true, importance: Importance.high),
        ));
  } catch (e) {
    debugPrint("Error notification $e");
  }
}
