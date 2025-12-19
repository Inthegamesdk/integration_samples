# ITG Integration Samples — Branch Guide (Platforms + Use Cases)

This repository is organized as **one integration per branch**.  
Choose your **platform** and **use case**, then switch to the matching branch.

---

## Platforms & branch mapping

## Apple (iOS) — SwiftUI

Use these branches if you’re integrating ITG into a **SwiftUI** iOS app.  
Pick a dependency manager (SPM or CocoaPods) and then choose **Fast** vs **Full**.

### Swift Package Manager (SPM)
- **Fast path (minimal wiring / quickest validation):** `apple/fastIntegrationSwiftUISPM`
- **Full path (more complete / production-like):** `apple/fullIntegrationSwiftUISPM`

### CocoaPods
- **Fast path (minimal wiring / quickest validation):** `apple/fastIntegrationSwiftUICocoapods`
- **Full path (more complete / production-like):** `apple/fullIntegrationSwiftUICocoapods`

**Use case hints**
- Choose **Fast** if you want to validate the integration quickly (POC/regression check).
- Choose **Full** if you need a more complete example to mirror real app integration.
- Choose **SPM** for modern dependency management; choose **CocoaPods** if your app already uses Pods.

---

## Android 

Choose a branch based on your **player stack** and your **integration focus**.

### Media3
- `android/itg-2.7-media3`  
  **Use case:** new Android projects or projects already migrated to Media3.

### Legacy ExoPlayer stack
- `android/itg-2.7-exoplayer`  
  **Use case:** apps still using older ExoPlayer APIs (not yet migrated to Media3).

### Third-party players
- `android/itg-2.7-bitmovin`  
  **Use case:** apps built around the Bitmovin SDK.
- `android/itg-2.7-kaltura-player`  
  **Use case:** apps built around the Kaltura SDK.

### SSAI — AWS MediaTailor
- `android/itg-2.7-mediatailor`  
  **Use case:** server-side ad insertion / ad stitching workflows driven by MediaTailor.

### SSAI — DataZoom AWS MediaTailor
- `android/itg-2.7-datazoom`  
  **Use case:** DataZoom-focused integration and measurement workflow.

---

## Android TV (Leanback library)

Use these branches if you’re building a TV experience (DPAD navigation + Leanback UI patterns).

- `android/itg-2.7-media3-leanback`  
  **Use case:** Android TV / Fire TV using **Media3** + Leanback.
- `android/itg-2.7-exoplayer-leanback`  
  **Use case:** Android TV / Fire TV using **legacy ExoPlayer** + Leanback.

---

## Roku

- `roku/itg-2.7-video-player`  
  **Use case:** standard Roku player integration sample.
- `roku/itg-2.7-mediatailor`  
  **Use case:** Roku integration for AWS MediaTailor (SSAI) workflows.

---

## Flutter

- `flutter`  
  **Use case:** Flutter integration sample / cross-platform path.

---

## React Native (Expo)

- `react-native-expo`  
  **Use case:** React Native + Expo friendly integration path.

---

## Naming conventions used

- `apple/<fast|full>Integration<UI><SPM|Cocoapods>`
- `android/itg-<version>-<player-or-usecase>`
- `roku/itg-<version>-<usecase>`
- `flutter`, `react-native-expo`

`


