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
