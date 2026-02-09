import Blits from '@lightningjs/blits'

import VideoPlayer from '../components/VideoPlayer'

export default Blits.Component('Home', {
  components: {
    VideoPlayer
  },
  template: `
    <Element w="1920" h="1080" color="#000000">
      <VideoPlayer ref="videoPlayer" />
    </Element>
  `,
  state() {
    return {
    }
  },
  hooks: {
    ready() {
      this.$focus(this.$ref('videoPlayer'))
    },
  },
})
