// Expo config plugin for expo-itg-video.
//
// The ITG Apple SDK is distributed through Swift Package Manager
// (https://github.com/Inthegamesdk/itg-apple-sdk) as dynamic xcframeworks.
// ExpoItgVideo.podspec declares the package for the pod so the module compiles,
// but dynamic frameworks are only embedded when the *app target* depends on the
// package products. This plugin adds that dependency during `expo prebuild`.
//
// The core SDK product is platform specific, so the plugin picks it the same way
// @react-native-tvos/config-tv decides whether to build for TV:
//   - iOS:  ITGPlayerViewController + InthegametviOS
//   - tvOS: ITGPlayerViewController + Inthegametv   (EXPO_TV=1, or config-tv's `isTV: true`)

const { withXcodeProject, createRunOncePlugin } = require('expo/config-plugins');
const pkg = require('./package.json');

const SDK_URL = pkg.itgAppleSdk.url;
const SDK_MIN_VERSION = pkg.itgAppleSdk.minimumVersion;
const TV_PLUGIN = '@react-native-tvos/config-tv';

function isTV(config) {
  const env = String(process.env.EXPO_TV ?? '').trim().toLowerCase();
  if (env === '1' || env === 'true') return true;
  const tvPlugin = (config.plugins ?? []).find(
    (p) => Array.isArray(p) && p[0] === TV_PLUGIN
  );
  return tvPlugin?.[1]?.isTV === true;
}

const unquote = (value) => String(value ?? '').replace(/^"(.*)"$/, '$1');

function addSwiftPackageToAppTarget(project, { url, minimumVersion, products }) {
  const objects = project.hash.project.objects;
  objects.XCRemoteSwiftPackageReference ??= {};
  objects.XCSwiftPackageProductDependency ??= {};

  const [targetId, target] = Object.entries(objects.PBXNativeTarget).find(
    ([id, t]) =>
      !id.endsWith('_comment') &&
      unquote(t.productType) === 'com.apple.product-type.application'
  );

  // Package reference on the project.
  const packageName = url.split('/').pop();
  let packageId = Object.keys(objects.XCRemoteSwiftPackageReference).find(
    (id) =>
      !id.endsWith('_comment') &&
      unquote(objects.XCRemoteSwiftPackageReference[id].repositoryURL) === url
  );
  if (!packageId) {
    packageId = project.generateUuid();
    objects.XCRemoteSwiftPackageReference[packageId] = {
      isa: 'XCRemoteSwiftPackageReference',
      repositoryURL: `"${url}"`,
      requirement: { kind: 'upToNextMajorVersion', minimumVersion },
    };
    objects.XCRemoteSwiftPackageReference[`${packageId}_comment`] =
      `XCRemoteSwiftPackageReference "${packageName}"`;
    const rootProject = project.getFirstProject().firstProject;
    rootProject.packageReferences ??= [];
    rootProject.packageReferences.push({
      value: packageId,
      comment: `XCRemoteSwiftPackageReference "${packageName}"`,
    });
  } else {
    objects.XCRemoteSwiftPackageReference[packageId].requirement = {
      kind: 'upToNextMajorVersion',
      minimumVersion,
    };
  }

  // Product dependencies on the app target, linked in its Frameworks phase
  // (Xcode then embeds the dynamic binary frameworks automatically).
  const frameworksPhase = target.buildPhases
    .map((phase) => phase.value)
    .find((id) => objects.PBXFrameworksBuildPhase?.[id]);
  target.packageProductDependencies ??= [];

  for (const productName of products) {
    const alreadyLinked = target.packageProductDependencies.some((dep) => {
      const existing = objects.XCSwiftPackageProductDependency[dep.value];
      return existing && existing.package === packageId && unquote(existing.productName) === productName;
    });
    if (alreadyLinked) continue;

    const dependencyId = project.generateUuid();
    objects.XCSwiftPackageProductDependency[dependencyId] = {
      isa: 'XCSwiftPackageProductDependency',
      package: packageId,
      package_comment: `XCRemoteSwiftPackageReference "${packageName}"`,
      productName,
    };
    objects.XCSwiftPackageProductDependency[`${dependencyId}_comment`] = productName;
    target.packageProductDependencies.push({ value: dependencyId, comment: productName });

    const buildFileId = project.generateUuid();
    objects.PBXBuildFile[buildFileId] = {
      isa: 'PBXBuildFile',
      productRef: dependencyId,
      productRef_comment: productName,
    };
    objects.PBXBuildFile[`${buildFileId}_comment`] = `${productName} in Frameworks`;
    objects.PBXFrameworksBuildPhase[frameworksPhase].files.push({
      value: buildFileId,
      comment: `${productName} in Frameworks`,
    });
  }

  return targetId;
}

const withItgAppleSdk = (config) =>
  withXcodeProject(config, (config) => {
    addSwiftPackageToAppTarget(config.modResults, {
      url: SDK_URL,
      minimumVersion: SDK_MIN_VERSION,
      products: ['ITGPlayerViewController', isTV(config) ? 'Inthegametv' : 'InthegametviOS'],
    });
    return config;
  });

module.exports = createRunOncePlugin(withItgAppleSdk, pkg.name, pkg.version);
