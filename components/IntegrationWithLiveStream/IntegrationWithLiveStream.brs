sub init()
    m.videoPlayer = m.top.findNode("videoPlayer")
    content = createObject("roSGNode", "ContentNode")
    content.url = "https://media.inthegame.io/uploads/videos/streamers/7b4a122aea060830d2070fae9aa74442.bd4ec5b14d63d39eadf733b39529c6eb.mp4"
    content.streamformat = "mp4"
    m.videoPlayer.content = content
    m.videoPlayer.control = "play"
    m.videoPlayer.setFocus(true)
    loadSDK()
end sub

' ------------------ Loading SDK library ------------------
sub loadSDK()
    m.componentLibrary = CreateObject("roSGNode", "ComponentLibrary")
    m.componentLibrary.uri = "https://assets.inthegame.io/roku/ITGLibrary_v2-7.pkg"
    m.componentLibrary.observeField("loadStatus", "onLoadStatusLibraryChanged")
end sub

sub onLoadStatusLibraryChanged(event)
    status = event.getData()
    if status <> "ready" then return
    initSDK("demo")
end sub

sub initSDK(channelSlug, virtualChannels = [])
    if m.overlayViewController = invalid
        m.overlayViewController = m.top.createChild("ITGLibrary:OverlayViewController")
        m.overlayViewController.environment = "dev"
        m.overlayViewController.videoPlayer = m.videoPlayer
    end if

     m.overlayViewController.accountRoute = {
        "accountId": "62a73d850bcf95e08a025f82", 'mandatory: your ITG accountId
        "channelSlug": channelSlug, 'mandatory: your channelSlug on our admin panel
        "virtualChannels": virtualChannels 'optional: virtual channels or categories, type: array of strings       
    }
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

