require 'json'

package = JSON.parse(File.read(File.join(__dir__, '..', 'package.json')))

Pod::Spec.new do |s|
  s.name           = 'ExpoItgVideo'
  s.version        = package['version']
  s.summary        = package['description']
  s.description    = package['description']
  s.license        = package['license']
  s.author         = package['author']
  s.homepage       = package['homepage']
  s.platforms      = {
    :ios => '15.1',
    :tvos => '15.1'
  }
  s.swift_version  = '5.9'
  s.source         = { :path => '.' }
  s.static_framework = true

  s.dependency 'ExpoModulesCore'
  # The ITG SDK is consumed from its Swift Package Manager repository.
  # URL and minimum version live in package.json ("itgAppleSdk") and are shared with app.plugin.js,
  # which links the same package into the app target so the SDK's dynamic frameworks get embedded.
  itg_sdk = package['itgAppleSdk']
  # The core SDK product is platform specific: InthegametviOS ships iOS slices only, Inthegametv tvOS only.
  # The Podfile is read as text on purpose: evaluating it again here would re-run React Native's Podfile setup.
  is_tvos = begin
    File.read(File.join(Pod::Config.instance.installation_root.to_s, 'Podfile')).match?(/^\s*platform\s+:tvos/)
  rescue StandardError
    false
  end
  spm_dependency(s,
    url: itg_sdk['url'],
    requirement: { kind: 'upToNextMajorVersion', minimumVersion: itg_sdk['minimumVersion'] },
    products: ['ITGPlayerViewController', is_tvos ? 'Inthegametv' : 'InthegametviOS']
  )

  # Swift/Objective-C compatibility
  s.pod_target_xcconfig = {
    'DEFINES_MODULE' => 'YES',
  }

  s.source_files = "**/*.{h,m,mm,swift,hpp,cpp}"
end
