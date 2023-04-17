import Flutter
import UIKit


public class BglocationPlugin: NSObject, FlutterPlugin,FlutterStreamHandler {
   
    var eventSking:FlutterEventSink?;
    let utils:Utils = Utils();

    public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "bglocation", binaryMessenger: registrar.messenger())
    let instance = BglocationPlugin()
    let channel2 = FlutterEventChannel(name: "bglocation/listen", binaryMessenger: registrar.messenger())
        registrar.addMethodCallDelegate(instance, channel: channel)
        channel2.setStreamHandler(instance)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
      switch call.method{
      case "version":
          result("iOS " + UIDevice.current.systemVersion)
          break;
      case "permission":
          let r =  utils.requestPermission();
          result(r);
          break;
      case "start":
          var canStart = false;
          if(eventSking != nil){
              canStart = true;
              utils.start(sking: eventSking!);
          }
          result(canStart)
          break;
      case "stop":
          result(false)
          break;
      case "stopForeground":
          result(false);
          break;
      case "getStatus":
          var data:Dictionary = Dictionary<String,Bool>()
          data["status"]=false;
          result(data);
          break;
      case "create":
          result(false)
          break;
      case "goForeground":
          result(false)
          break;
      default:
          result("Method not found");
          break;
      }

  }

    public func onListen(withArguments arguments: Any?, eventSink events: @escaping FlutterEventSink) -> FlutterError? {
        eventSking=events;
        debugPrint("ON LISTEN SKIN ")
        return nil;
    }
    
    public func onCancel(withArguments arguments: Any?) -> FlutterError? {
        eventSking=nil;
        debugPrint("ERROR LISTEN ");
        return nil;
    }

}
