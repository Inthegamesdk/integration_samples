import { ViewProps } from 'react-native';
import { requireNativeViewManager } from 'expo-modules-core';
import * as React from 'react';

export type Props = ViewProps;

const NativeView: React.ComponentType<Props> = requireNativeViewManager('ExpoItgVideoView');

export default function ExpoItgVideoView(props: Props) {
  return <NativeView {...props} />;
}
