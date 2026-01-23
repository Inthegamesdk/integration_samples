sub init()
    m.videoPlayer = m.top.findNode("videoPlayer")
end sub

sub onChangeTrakingData(event)
    trakingData = event.getData()
    m.mediaTailorBaseUrl = "https://dbfc60fb257a4fa69b8410fae7d4d3b6.mediatailor.us-west-2.amazonaws.com"
    showStream(m.mediaTailorBaseUrl + trakingData.manifestUrl)
    initITGLibrary("demo_mediatailor")
    trackingUrl = m.mediaTailorBaseUrl + trakingData.trackingUrl
    interval = 5
    initMediaTailor(trackingUrl, interval)
end sub

sub showStream(url)
    content = createObject("roSGNode", "ContentNode")
    content.url = url
    content.streamformat = "hls"
    m.videoPlayer.content = content
    m.videoPlayer.control = "play"
    m.videoPlayer.setFocus(true)
end sub

'Init ITGLibrary
sub initITGLibrary(channelSlug, virtualChannels = [])
    if m.overlayViewController = invalid
        m.overlayViewController = m.top.createChild("ITGLibrary:OverlayViewController")
        m.overlayViewController.videoPlayer = m.videoPlayer
    end if

     m.overlayViewController.accountRoute = {
        "accountId": "69230d1b5f7b3515524dd184", 'mandatory: your ITG accountId
        "channelSlug": channelSlug, 'mandatory: your channelSlug on our admin panel
        "virtualChannels": virtualChannels 'optional: virtual channels or categories, type: array of strings       
    }
end sub

'Init MediaTailor plugin
sub initMediaTailor(trackingUrl, interval)
  m.MediaTailor = CreateObject("roSGNode", "ITGLibrary:MediaTailor")
  m.MediaTailor.observeField("didReceiveTrackingData", "onDidReceiveTrackingData")
  m.MediaTailor.delegate = m.overlayViewController
  m.MediaTailor.callFunc("startMediaTailor", trackingUrl, interval)
end sub

sub onDidReceiveTrackingData(event)
  trackingData = event.getData()
  ? "trackingData: " trackingData
end sub

' ------------------ Extras ------------------
function onKeyEvent(key as string, press as boolean) as boolean
    result = false

    if m.overlayViewController <> invalid
        result = m.overlayViewController.callFunc("onKeyEvent", key, press)
    end if

    if not result
        'Handle your logic
    end if

    return result
end function

