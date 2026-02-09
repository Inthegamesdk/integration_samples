import Blits from '@lightningjs/blits'

export default Blits.Component('VideoPlayer', {
  template: `
    <Element w="1920" h="1080">
      <Element  :src="$videoTexture" />
    </Element>
  `,
  state() {
    return {
      videoElement: null as HTMLVideoElement | null,
      videoTexture: null as HTMLVideoElement | null,
      isPlaying: false as boolean,
      itgInitialized: false as boolean,
    }
  },
  hooks: {
    ready() {
      this.createVideoElement()
      // this.loadInTheGameSDK()
    },
    focus() {
      if (this.videoElement && !this.isPlaying) {
        this.play()
      }
    },
    unfocus() {
      if (this.videoElement && this.isPlaying) {
        this.pause()
      }
    },
  },
  methods: {
    createVideoElement() {
      // Create video element
      const video = document.createElement('video')
      video.id = 'blitsVideoPlayer'
      video.crossOrigin = 'anonymous'
      video.loop = true
      video.muted = true
      video.src = 'https://assets.inthegame.io/admin-assets/black_screen_with_timer.mp4'

      // Style video element
      video.style.position = 'absolute'
      video.style.top = '0'
      video.style.left = '0'
      video.style.width = '1920px'
      video.style.height = '1080px'

      document.body.style.position = 'relative'
      document.body.style.width = '1920px'
      document.body.style.height = '1080px'

      // video.style.objectFit = 'contain'

      // Append to DOM
      document.body.appendChild(video)

      this.videoElement = video

      // Create video texture for Blits
      this.videoTexture = video

      // Auto-play
      this.play()
    },

    // to load sdk directly in component
    // loadInTheGameSDK() {
    //   // Check if SDK is already loaded
    //   if (window.inthegame) {
    //     this.initInTheGame()
    //     return
    //   }
    //
    //   // Load SDK script
    //   const script = document.createElement('script')
    //   script.src = 'https://html5.inthegame.io/v2-7/main.js'
    //   script.onload = () => {
    //     console.log('InTheGame SDK loaded')
    //     this.initInTheGame()
    //   }
    //   script.onerror = () => {
    //     console.error('Failed to load InTheGame SDK')
    //   }
    //   document.head.appendChild(script)
    // },

    initInTheGame() {
      if (window.inthegame && !this.itgInitialized) {
        const initParams = {
          videoPlayerId: 'blitsVideoPlayer',
          device: 'tv',
          accountId: '<YOUR_ACCOUNT_ID>',
          channelSlug: '<YOUR_CHANNEL_SLUG>',
        }

        try {
          window.inthegame.init(initParams)
          this.itgInitialized = true
          console.log('InTheGame SDK initialized successfully')
        } catch (error) {
          console.error('Failed to initialize InTheGame SDK:', error)
        }
      }
    },

    play() {
      if (this.videoElement) {
        this.videoElement.play()
          .then(() => {
            this.isPlaying = true
            this.initInTheGame()
          })
          .catch(err => {
            console.error('Video playback error:', err)
          })
      }
    },

    pause() {
      if (this.videoElement) {
        this.videoElement.pause()
        this.isPlaying = false
        console.log('Video paused')
      }
    },

    togglePlayPause() {
      if (this.isPlaying) {
        this.pause()
      } else {
        this.play()
      }
    },
  },
  input: {
    enter() {
      this.togglePlayPause()
    },
    space() {
      this.togglePlayPause()
    },
    playpause() {
      this.togglePlayPause()
    },
  },
})
