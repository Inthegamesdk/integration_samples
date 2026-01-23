sub init()
    loadITGLibrary()
end sub

' ------------------ Loading SDK library ------------------
sub loadITGLibrary()
    m.componentLibrary = CreateObject("roSGNode", "ComponentLibrary")
    m.componentLibrary.uri = "https://assets.inthegame.io/roku/itg-flosports-roku-v2_7/test.pkg"
    m.componentLibrary.observeField("loadStatus", "onLoadStatusLibraryChanged")
end sub

sub onLoadStatusLibraryChanged(event)
    status = event.getData()
    if status <> "ready" then return
    getTreckingData()
end sub

sub getTreckingData()
    NetworkTask = CreateObject("roSGNode", "NetworkTask")
    NetworkTask.observeField("response", "handleResponseTrakingData")
    NetworkTask.url = "https://dbfc60fb257a4fa69b8410fae7d4d3b6.mediatailor.us-west-2.amazonaws.com/v1/session/7c8ce5ad5bcc5198ca301174a2ead89b25915ca4/Flosport27/index.m3u8"
    NetworkTask.functionName = "postRequest"
    NetworkTask.control = "Run"
end sub

sub handleResponseTrakingData(event)
    data = event.getData()
    m.integrationController = m.top.createChild("Integration")
    m.integrationController.trakingData = data.response
end sub