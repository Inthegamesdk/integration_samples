sub init()
    m.videoPlayer = m.top.findNode("videoPlayer")
    content = createObject("roSGNode", "ContentNode")
    content.url = "https://assets.inthegame.io/uploads/v2-3/TestingsQA/videos/Allweknow,CaliforniaAdventure_480eul4476.mp4"
    content.streamformat = "mp4"
    m.videoPlayer.content = content
    m.videoPlayer.control = "play"
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
    m.videoPlayer.setFocus(true)
    if m.overlayViewController = invalid
        m.overlayViewController = m.top.createChild("ITGLibrary:OverlayViewController")
        m.overlayViewController.videoPlayer = m.videoPlayer
        m.overlayViewController.showLogs = true
    end if

     m.overlayViewController.accountRoute = {
        "accountId": "69230d1b5f7b3515524dd184", 'mandatory: your ITG accountId
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
