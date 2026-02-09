# Integrate InTheGame (ITG) SDK in a Lightning (Blits) project

This guide shows a minimal, production-friendly way to add ITG to a Lightning/Blits app.

## 1) Add the SDK script to index.html

Place the ITG SDK right before your app entry script.

If you are running a dev server that sets COOP/COEP headers (common in Canvas/Workers setups), loading third‑party
scripts can fail with:
ERR_BLOCKED_BY_RESPONSE.NotSameOriginAfterDefaultedToSameOriginByCoep (status 200).

To fix during development, remove or relax COOP/COEP on the dev server so the SDK can be embedded:

- Cross-Origin-Opener-Policy: unset or same-origin-allow-popups
- Cross-Origin-Embedder-Policy: unset or credentialless
  Restart your dev server after changing headers. For production, keep your security model, but ensure the SDK host can
  be embedded under your COEP policy or load the SDK from a page that does not enforce COEP.

Tip: If you can’t change server headers quickly, open the app in a Chrome instance with web security disabled for
testing only.

## 2) Initialize ITG when the player is ready (or on first play)

Ensure your video element has a stable id. Then call `window.inthegame.init()` once, when the player is ready or right
after the first successful `play()`.

### Example with a Blits component that creates a video element and initializes ITG on first play (also in ./example/src/components/VideoPlayer.ts):

```typescript
ts // VideoPlayer.ts import Blits from '@lightningjs/blits'
export default Blits.Component('VideoPlayer', {
  template: `<Element w="1920" h="1080"> <Element :src="$videoTexture" /> </Element> `,
  state() {
    return {videoElement: null as HTMLVideoElement | null, isPlaying: false, itgInitialized: false,}
  },
  hooks: {
    ready() {
      const video = document.createElement('video')
      video.id = 'blitsVideoPlayer'
      video.loop = true
      video.muted = true
      video.src = '[https://assets.inthegame.io/admin-assets/black_screen_with_timer.mp4](https://assets.inthegame.io/admin-assets/black_screen_with_timer.mp4)'
      video.style.position = 'absolute'
      video.style.top = '0'
      video.style.left = '0'
      video.style.width = '1920px'
      video.style.height = '1080px'
      document.body.appendChild(video)
      this.videoElement = video // Use the HTMLVideoElement as the Blits texture source this.videoTexture = video
      // Start playback; init ITG after the first successful play
      this.play()
    },
  },
  methods: {
    initITG() {
      if (!this.itgInitialized && (window as any).inthegame) {
        (window as any).inthegame.init({
          videoPlayerId: 'blitsVideoPlayer',
          device: 'tv',
          accountId: '<YOUR_ACCOUNT_ID>',
          channelSlug: '<YOUR_CHANNEL_SLUG>',
        })
        this.itgInitialized = true
      }
    },
    play() {
      if (!this.videoElement) return this.videoElement.play().then(() => {
        this.isPlaying = true
        this.initITG() // init once, after the first play
      }).catch(err => console.error('Video playback error:', err))
    },
    pause() {
      if (!this.videoElement) return this.videoElement.pause()
      this.isPlaying = false
    },
  },
})
```


Notes:
- Only call `init` once per page lifecycle.
- If your player exposes a “ready” event, you can call `init` there instead of “first play”.

## 3) Shaka player example (check ./blits-example/src/PlayerWithITG.js)

If you use a player manager (e.g., Shaka or your own abstraction), initialize ITG once the player has loaded and you know the video element id. The referenced example demonstrates:
- Loading the stream via the manager
- Calling `window.inthegame.init({ videoPlayerId, accountId, channelSlug, device: 'tv' })` when the player is ready
- Continuing normal player controls and telemetry after initialization

This pattern keeps your player logic separate and makes ITG initialization predictable and idempotent.

## Troubleshooting

- SDK script not found:
  Ensure the script tag is present and not blocked by COEP/COOP during development.
- init throws or overlays not visible:
  Confirm the `videoPlayerId` matches the actual HTML video element id that’s in the DOM at init time.
- Initialize only once:
  Guard with a boolean flag so re-renders or refocus events don’t re-init.

That’s it. Add the script, relax COEP for dev if needed, and init on player ready/first play.
