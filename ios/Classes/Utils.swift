//
//  Utils.swift
//  bglocation
//
//  Created by user on 4/17/23.
//

import Foundation
import CoreLocation
import Flutter

public class Utils:NSObject, CLLocationManagerDelegate {
    var skin:FlutterEventSink?;
    let locationManager:CLLocationManager=CLLocationManager();
    
    override init(){
        super.init()
    locationManager.delegate=self;
    locationManager.showsBackgroundLocationIndicator=true;
    locationManager.desiredAccuracy=kCLLocationAccuracyBestForNavigation;
    debugPrint("INIT CLASS UTILS")
    }

     func  requestPermission()->Bool{
         locationManager.requestWhenInUseAuthorization();
          return true;
    }
    
    func start(sking skink:@escaping FlutterEventSink){
        skin = skink;
        locationManager.startUpdatingLocation();
       debugPrint("START LOCATION")
    }
    
    public func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
          debugPrint("ENTRO EN LOCATION MANAGER")
           guard let lastLocation = locations.last else { return } // obtener la última ubicación
           // hacer algo con la ubicación
        debugPrint("Latitud: \(lastLocation.coordinate.latitude), Longitud: \(lastLocation.coordinate.longitude)")

        if(skin != nil){
            var data:Dictionary = Dictionary<String,Any>();
            data["latitude"]=lastLocation.coordinate.latitude;
            data["longitude"]=lastLocation.coordinate.longitude;
            if #available(iOS 13.4, *) {
                data["accuracy"]=lastLocation.courseAccuracy;
                data["bearingAccuracyDegrees"]=lastLocation.courseAccuracy;
                
            } else {
                data["accuracy"]=0;
            };
            data["altitude"]=lastLocation.altitude;
            data["bearing"]=lastLocation.course;
            data["verticalAccuracyMeters"]=lastLocation.verticalAccuracy;
            data["provider"] = ""
            data["speed"]=lastLocation.speed;
            data["speedAccuracy"]=lastLocation.speedAccuracy;
            data["time"]=Int(lastLocation.timestamp.timeIntervalSince1970 * 1_000)
            skin!(data)
        }
       }

      public func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
           // manejar el error
          debugPrint("Error al obtener la ubicación: \(error.localizedDescription)")
       }
}
