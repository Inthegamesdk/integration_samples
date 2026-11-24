# TV Example

Creates a project that can build for Apple TV and Android TV targets.

This project uses

- the [React Native TV fork](https://github.com/react-native-tvos/react-native-tvos), which supports both phone (Android and iOS) and TV (Android TV and Apple TV) targets
- the [React Native TV config plugin](https://github.com/react-native-tvos/config-tv/tree/main/packages/config-tv) to allow Expo prebuild to modify the project's native files for TV builds

## ITG SDK integration (Swift Package Manager)

The video player with the ITG overlay lives in the local Expo module [`expo-itg-video`](./expo-itg-video).
On iOS and tvOS it uses the ITG Apple SDK from its Swift Package Manager repository,
[`Inthegamesdk/itg-apple-sdk`](https://github.com/Inthegamesdk/itg-apple-sdk). The current minimum version is **2.7.36**.

- **SDK version**: set in one place, `itgAppleSdk` in [`expo-itg-video/package.json`](./expo-itg-video/package.json). The requirement is "up to next major" from `minimumVersion`.
- **Module code**: [`ExpoItgVideo.podspec`](./expo-itg-video/ios/ExpoItgVideo.podspec) declares the package with React Native's `spm_dependency`, so `pod install` adds it to the `ExpoItgVideo` pod.
- **App target**: the SDK ships as dynamic xcframeworks, so the app target also has to link the package for Xcode to embed them. The config plugin [`expo-itg-video/app.plugin.js`](./expo-itg-video/app.plugin.js), registered in `app.json`, does this during `expo prebuild`.
- **iOS vs tvOS**: both sides pick the platform-specific core product:

| Target | SPM products |
| ------ | ------------ |
| iOS (`npm run prebuild`) | `ITGPlayerViewController`, `InthegametviOS` |
| tvOS (`npm run prebuild:tv`, `EXPO_TV=1`) | `ITGPlayerViewController`, `Inthegametv` |

To move to a newer SDK, bump `itgAppleSdk.minimumVersion`. Then run `npm run prebuild` or `npm run prebuild:tv`,
or run `pod install` in `ios/` and update packages in Xcode (File → Packages → Update to Latest Package Versions).
The resolved version is pinned in `ios/MyTVProject.xcworkspace/xcshareddata/swiftpm/Package.resolved`.

## 🚀 How to use

#### Creating a new project

- Create a project: `npx create-expo-app -e with-tv`
- `cd` into the project

- For TV development:

```sh
yarn
yarn prebuild:tv # Executes clean Expo prebuild with TV modifications
yarn ios # Build and run for Apple TV
yarn android # Build for Android TV
yarn web # Run the project on web from localhost
```
- For mobile development:

```sh
yarn
yarn prebuild # Executes Expo prebuild with no TV modifications
yarn ios # Build and run for iOS
yarn android # Build for Android mobile
yarn web # Run the project on web from localhost
```
> **_NOTE:_**
> Setting the environment variable `EXPO_TV=1` enables the `@react-native-tvos/config-tv` plugin to modify the project for TV.
> This can also be done by setting the parameter `isTV` to true in the `app.json`.

#### TV specific file extensions

This project contains an [example Metro configuration](./metro.config.js) that allows Metro to resolve application source files with TV-specific code, indicated by specific file extensions (`*.ios.tv.tsx`, `*.android.tv.tsx`, `*.tv.tsx`). This config is not enabled by default, since it will impact bundling performance, but is available for developers who need this capability.

#### TV specific app icons and banners

This project contains placeholder images for the Android TV banner and for Apple TV brand assets (app icon and top shelf images). The `config-tv` plugin will use these images to construct the required native image files and make the right modifications in project files. You can simply replace these images with your own app images. Note that for Apple TV, the images must be the exact sizes indicated.
