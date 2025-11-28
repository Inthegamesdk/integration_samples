sub init()
    m.testFocusButton = m.top.findNode("testFocusButton")
    m.testFocusButton.setFocus(true)
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
    end if

     m.overlayViewController.accountRoute = {
        "accountId": "69230d1b5f7b3515524dd184", 'mandatory: your ITG accountId
        "channelSlug": channelSlug, 'mandatory: your channelSlug on our admin panel
        "virtualChannels": virtualChannels 'optional: virtual channels or categories, type: array of strings       
    }
end sub

sub handleFocus()
    m.testFocusButton.setFocus(true)
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
