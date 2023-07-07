# API documentation

## HyperTrack object

### getDeviceId()

Returns the device ID.

```javascript
HyperTrack.getDeviceId()
```

### setIsTracking(isTracking: Boolean)

Starts or stops tracking.

```javascript
HyperTrack.setIsTracking(true)
```

### addGeotag

Adds a geotag.

```javascript
HyperTrack.addGeotag(
    JSON.stringify(
        {
            "test_object": {
                "test_key1": "test_value1"
            }
        }
    )
);
```

#### Parameters

| Name | Type        | Description |
| --- |-------------|-------------|
| geotagData | JSON String | Geotag data |

#### Returns

JSON String of result object
Result<Result<Location, LocationError>, JsError>

### addGeotagWithExpectedLocation

Adds a geotag with expected location.

```javascript
HyperTrack.addGeotagWithExpectedLocation(
    JSON.stringify(
        {
            "test_object": {
                "test_key1": "test_value1"
            }
        }
    ),
    JSON.stringify(
        {
            "latitude": 37.33182,
            "longitude": -122.03118
        }
    )
);
```

#### Parameters

| Name | Type        | Description              |
| --- |-------------|--------------------------|
| geotagData | JSON String | Geotag data              |
| expectedLocation | JSON String | Expected location object |

Expected location object:
```
{
  "latitude": Number,
  "longitude": Number,
}
```

#### Returns

JSON String of result object
Result<Result<LocationWithDeviation, LocationError>, JsError>

## Data types

### Result<Success, Failure>
```
{
  "type": "success",
  "value": Success
}

{
  "type": "failure",
  "value": Failure
}
```

### Location
```
{
  "latitude": Double,
  "longitude": Double,
}
```

### LocationError

Tracking is not started (adding geotags is not possible)
```
{
  "type": "notRunning
}
```

SDK is not initialized yet (no location data to add geotag)
```
{
    "type": "starting
}
```

There was an outage while getting the location data
```
{
    "type": "hyperTrackError",
    "value": HyperTrackError
}
```

### HyperTrackError

```javascript
enum HyperTrackError {
    /**
    * GPS satellites are not in view.
    */
    gpsSignalLost = 'gpsSignalLost',
    
    /**
    * The user enabled mock location app while mocking locations is prohibited.
    */
    locationMocked = 'locationMocked',
    
    /**
    * The user denied location permissions.
    */
    locationPermissionsDenied = 'locationPermissionsDenied',
    
    /**
    * Can’t start tracking in background with When In Use location permissions.
    * SDK will automatically start tracking when app will return to foreground.
    */
    locationPermissionsInsufficientForBackground = 'locationPermissionsInsufficientForBackground',
    
    /**
    * [iOS only] The user has not chosen whether the app can use location services.
    */
    locationPermissionsNotDetermined = 'locationPermissionsNotDetermined',
    
    /**
    * The user didn’t grant precise location permissions or downgraded permissions to imprecise.
    */
    locationPermissionsReducedAccuracy = 'locationPermissionsReducedAccuracy',
    
    /**
    * [iOS only] The app is in Provisional Always authorization state, which stops sending locations when app is in background.
    */
    locationPermissionsProvisional = 'locationPermissionsProvisional',
    
    /**
    * [iOS only] The app is not authorized to use location services.
    */
    locationPermissionsRestricted = 'locationPermissionsRestricted',
    
    /**
    * The user disabled location services systemwide.
    */
    locationServicesDisabled = 'locationServicesDisabled',
    
    /**
    * [Android only] The device doesn't have location services.
    */
    locationServicesUnavailable = 'locationServicesUnavailable',
    
    /**
    * [iOS only] The user has not chosen whether the app can use motion activity services.
    */
    motionActivityPermissionsNotDetermined = 'motionActivityPermissionsNotDetermined',
    
    /**
    * The user denied motion activity permissions.
    */
    motionActivityPermissionsDenied = 'motionActivityPermissionsDenied',
    
    /**
    * [iOS only] The user has restricted motion activity services.
    */
    motionActivityServicesDisabled = 'motionActivityServicesDisabled',
    
    /**
    * [iOS only] The device doesn't have motion activity services.
    */
    motionActivityServicesUnavailable = 'motionActivityServicesUnavailable',
    
    /**
    * [iOS only] The app is not authorized to use motion activity services.
    */
    motionActivityPermissionsRestricted = 'motionActivityPermissionsRestricted',
    
    /**
    *  [Android only] The user denied notification permissions needed to display persistent notification needed for foreground location tracking.
    */
    invalidPublishableKey = 'invalidPublishableKey',
    
    /**
    * The SDK is not collecting locations because it’s neither tracking nor available.
    */
    blockedFromRunning = 'blockedFromRunning',
}
```

### Location

```javascript
{
    "latitude": Double,
    "longitude": Double,
}
```

### LocationWithDeviation

```javascript
{
    "location": Location,
    "deviation": Double,
}
```

### JsError

```javascript
{
    "error": String,
}
```
