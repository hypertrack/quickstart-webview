try {
    let deviceId = HyperTrack.getDeviceId();
    document.getElementById("device-id").innerText = deviceId;
} catch (e) {
    alert(e);
}

function startTracking() {
    try {
        HyperTrack.setIsTracking(true);
    } catch (e) {
        alert(e);
    }
}

function stopTracking() {
    try {
        HyperTrack.setIsTracking(false);
    } catch (e) {
        alert(e);
    }
}

function addGeotag() {
    try {
        let addGeotagResult = HyperTrack.addGeotag(
            JSON.stringify(
                {
                    "test_object": {
                        "test_key1": "test_value1"
                    }
                }
            )
        );
        alert(JSON.stringify(JSON.parse(addGeotagResult), null, 2));
    } catch (e) {
        alert(e);
    }
}

function addGeotagWithExpectedLocation() {
    try {
        let addGeotagWithExpectedLocationResult = HyperTrack.addGeotagWithExpectedLocation(
            JSON.stringify(
                {
                    "with_expected_location": "true",
                    "test_object": {
                        "test_key1": "test_value1"
                    }
                }
            ),
            JSON.stringify(
                {
                    "latitude": 37.7758,
                    "longitude": -122.435,
                }
            )
        );
        alert(JSON.stringify(JSON.parse(addGeotagWithExpectedLocationResult), null, 2));
    } catch (e) {
        alert(e);
    }
}

function setMetadata() {
    try {
        HyperTrack.setMetadata(
            JSON.stringify(
                {
                    "metadata_field": "test_metadata_value"
                }
            )
        );
    } catch (e) {
        alert(e);
    }
}

function setName() {
     try {
          HyperTrack.setName("test_name");
     } catch (e) {
          alert(e);
     }
}

function askForPermissions() {
    try {
        HyperTrack.requestPermissionsIfNecessary();
    } catch (e) {
        alert(e);
    }
}
