# expo-itg-video

Expo module that renders an `AVPlayerViewController` (iOS / tvOS) or Media3 ExoPlayer view (Android) with the ITG overlay.

## iOS / tvOS: ITG Apple SDK via Swift Package Manager

The SDK comes from [`Inthegamesdk/itg-apple-sdk`](https://github.com/Inthegamesdk/itg-apple-sdk).
The URL and minimum version are set in this package's `package.json`:

```json
"itgAppleSdk": {
  "url": "https://github.com/Inthegamesdk/itg-apple-sdk",
  "minimumVersion": "2.7.36"
}
```

Two parts use that value:

1. `ios/ExpoItgVideo.podspec` adds the package to the pod with React Native's `spm_dependency`. This lets the module code compile with
   `import ItgPlayerViewController` and `import InthegametviOS` / `import Inthegametv`.
2. `app.plugin.js` is an Expo config plugin. It adds the same package products to the **app target** during `expo prebuild`.
   This is required because the SDK frameworks are dynamic: Xcode only embeds them into the `.app` when the app target links them.

Register the plugin in the app's `app.json`:

```json
"plugins": ["expo-itg-video"]
```

The core SDK product depends on the platform. `InthegametviOS` is used for iOS. `Inthegametv` is used for tvOS, which is detected the same way
`@react-native-tvos/config-tv` detects it (`EXPO_TV=1`, or `isTV: true` in that plugin's options).
