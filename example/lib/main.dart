import 'dart:math';

import 'package:firebase_core/firebase_core.dart';
import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:bglocation/bglocation.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
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
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  bool isRunning = false;
  @override
  void initState() {
    super.initState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    Map r = await Bglocation.getStatus();
    isRunning = r["status"];

    if (!isRunning) {
      await Bglocation.onCreate("DASASDAS");

      Bglocation.getCurrentPosition().listen((event) {
        print(event);
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

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
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
          ],
        ),
      ),
    );
  }
}
