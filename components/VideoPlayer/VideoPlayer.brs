' fast integration
sub init()
  m.top.setFocus(true)
  m.background = m.top.findNode("background")
  m.videoPlayer = m.top.findNode("videoPlayer")
end sub

sub createMediaTailorService(streamUrl)
  m.MediaTailorService = CreateObject("roSGNode", "MediaTailorService")
  m.MediaTailorService.observeField("manifestUrl", "handleResponceManifest")
  m.MediaTailorService.observeField("creative", "handleResponceCreative")
  m.MediaTailorService.baseUrl = "https://3e763f5a2cb64a869ea9bb83d5f933d7.mediatailor.us-west-2.amazonaws.com"
  m.MediaTailorService.streamUrl = streamUrl
  m.MediaTailorService.callFunc("start")
end sub

sub handleResponceCreative(event)
  url = event.getData()
  if isValid(m.overlayViewController)
    m.overlayViewController.callFunc("injectFlexi", url)
  end if
end sub

sub handleResponceManifest(event)
  manifestUrl = event.getData()
  content = CreateObject("roSGNode", "ContentNode")
  content.url = manifestUrl
  content.streamformat = "hls"

  m.videoPlayer.content = content
  m.videoPlayer.control = "play"
end sub

sub configureVideoContentNode()
  RegDelete()
  createMediaTailorService("/v1/session/7c8ce5ad5bcc5198ca301174a2ead89b25915ca4/demo_page_for_client_testing/index.m3u8")
  m.videoPlayer.setFocus(true)
  m.videoPlayer.width = getSize(1920)
  m.videoPlayer.height = getSize(1080)
  configureSDKPanel()
end sub

function stopVideo()
    m.videoPlayer.control = "stop"
    m.videoPlayer = invalid
end function

sub configureSDKPanel()
  RegDelete()
  m.overlayViewController = m.top.findNode("overlayViewController")
  m.overlayViewController.environment = m.top.environment
  m.overlayViewController.showLogs = true
  m.overlayViewController.videoPlayer = m.videoPlayer
  m.overlayViewController.accountRoute = m.top.accountRoute
end sub


function onKeyEvent(key as string, press as boolean) as boolean
  ? "function VideoPlayer onKeyEvent("key" as string, "press" as boolean) as boolean"
  ? "currentContent " m.overlayViewController.callFunc("currentContent")
  m.overlayViewController.callFunc("closeOverlayIfNeeded", key)

  result = m.overlayViewController.callFunc("onKeyEvent", key, press)

  if not press and result then return false

  return result
end function
