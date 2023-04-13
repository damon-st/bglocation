import 'dart:convert';

// ignore_for_file: public_member_api_docs, sort_constructors_first
class LocationModel {
  final double latitude;
  final double longitude;
  final double accuracy;
  final double altitude;
  final double bearing;
  double? verticalAccuracyMeters;
  double? bearingAccuracyDegrees;
  final double elapsedRealtimeNanos;
  final String provider;
  final double speed;
  double? speedAccuracy;
  final int time;
  LocationModel({
    required this.latitude,
    required this.longitude,
    required this.accuracy,
    required this.altitude,
    required this.bearing,
    this.bearingAccuracyDegrees,
    this.verticalAccuracyMeters,
    required this.elapsedRealtimeNanos,
    required this.provider,
    required this.speed,
    this.speedAccuracy,
    required this.time,
  });

  LocationModel copyWith({
    double? latitude,
    double? longitude,
    double? accuracy,
    double? altitude,
    double? bearing,
    double? verticalAccuracyMeters,
    double? bearingAccuracyDegrees,
    double? elapsedRealtimeNanos,
    String? provider,
    double? speed,
    double? speedAccuracy,
    int? time,
  }) {
    return LocationModel(
      latitude: latitude ?? this.latitude,
      longitude: longitude ?? this.longitude,
      accuracy: accuracy ?? this.accuracy,
      altitude: altitude ?? this.altitude,
      bearing: bearing ?? this.bearing,
      verticalAccuracyMeters:
          verticalAccuracyMeters ?? this.verticalAccuracyMeters,
      bearingAccuracyDegrees:
          bearingAccuracyDegrees ?? this.bearingAccuracyDegrees,
      elapsedRealtimeNanos: elapsedRealtimeNanos ?? this.elapsedRealtimeNanos,
      provider: provider ?? this.provider,
      speed: speed ?? this.speed,
      speedAccuracy: speedAccuracy ?? this.speedAccuracy,
      time: time ?? this.time,
    );
  }

  Map<String, dynamic> toMap() {
    return <String, dynamic>{
      'latitude': latitude,
      'longitude': longitude,
      'accuracy': accuracy,
      'altitude': altitude,
      'bearing': bearing,
      'verticalAccuracyMeters': verticalAccuracyMeters,
      'bearingAccuracyDegrees': bearingAccuracyDegrees,
      'elapsedRealtimeNanos': elapsedRealtimeNanos,
      'provider': provider,
      'speed': speed,
      'speedAccuracy': speedAccuracy,
      'time': time,
    };
  }

  factory LocationModel.fromMap(Map<String, dynamic> map) {
    return LocationModel(
      latitude: double.parse((map['latitude'] ?? 0).toString()),
      longitude: double.parse((map['longitude'] ?? 0).toString()),
      accuracy: double.parse((map['accuracy'] ?? 0).toString()),
      altitude: double.parse((map['altitude'] ?? 0).toString()),
      bearing: double.parse((map['bearing'] ?? 0).toString()),
      verticalAccuracyMeters: map['verticalAccuracyMeters'] != null
          ? double.parse((map['verticalAccuracyMeters'] ?? 0).toString())
          : null,
      bearingAccuracyDegrees: map['bearingAccuracyDegrees'] != null
          ? double.parse((map['bearingAccuracyDegrees'] ?? 0).toString())
          : null,
      elapsedRealtimeNanos:
          double.parse((map['elapsedRealtimeNanos'] ?? 0).toString()),
      provider: map['provider'] as String,
      speed: double.parse((map['speed'] ?? 0).toString()),
      speedAccuracy: map['speedAccuracy'] != null
          ? double.parse((map['speedAccuracy'] ?? 0).toString())
          : null,
      time: int.parse((map['time'] ?? 0).toString()),
    );
  }

  String toJson() => json.encode(toMap());

  factory LocationModel.fromJson(String source) =>
      LocationModel.fromMap(json.decode(source) as Map<String, dynamic>);
}
