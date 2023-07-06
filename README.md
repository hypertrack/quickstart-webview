# Android Webview Quickstart for HyperTrack SDK

[HyperTrack](https://www.hypertrack.com/) lets you add live location tracking to your mobile app. Live location is made available along with ongoing activity, tracking controls and tracking outage with reasons.

This repo contains an example Android app with a WebView that demonstrates you how to integrate your website with HyperTrack SDK.

## API Reference

Reference for the JS API that you can call from a WebView
[API Reference](API_REFERENCE.md)

## How to get started?

### Create HyperTrack Account

[Sign up](https://dashboard.hypertrack.com/signup) for HyperTrack and get your publishable key from the [Setup page](https://dashboard.hypertrack.com/setup).

### Clone Quickstart app

### Update the publishable key

Insert your HyperTrack publishable key to `const PUBLISHABLE_KEY` in `HyperTrackJsApi.kt`

### [Set up silent push notifications](https://hypertrack.com/docs/install-sdk-react-native/#set-up-silent-push-notifications)

HyperTrack SDK needs Firebase Cloud Messaging to manage on-device tracking as well as enable using HyperTrack cloud APIs from your server to control the tracking.

### Run the app

### Grant permissions

Grant location and activity permissions (choose "Always Allow" for location).

### Start tracking

Press `Start tracking` button.

To see the device on a map, open the [HyperTrack dashboard](https://dashboard.hypertrack.com/).

## Support

Join our [Slack community](https://join.slack.com/t/hypertracksupport/shared_invite/enQtNDA0MDYxMzY1MDMxLTdmNDQ1ZDA1MTQxOTU2NTgwZTNiMzUyZDk0OThlMmJkNmE0ZGI2NGY2ZGRhYjY0Yzc0NTJlZWY2ZmE5ZTA2NjI) for instant responses. You can also email us at help@hypertrack.com
