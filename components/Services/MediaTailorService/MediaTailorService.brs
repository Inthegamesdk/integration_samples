sub init()
    m.availsIDs = []
    m.MediaTailorTasks = []
    m.trackingUrl = ""
    m.pingTimer = CreateObject("roSGNode", "Timer")
    m.pingTimer.duration = 5
    m.pingTimer.repeat = true
    m.pingTimer.observeField("fire", "trackingPing")
end sub

function start()
    top = m.top
    MediaTailorTask = CreateObject("roSGNode", "MediaTailorTask")
    MediaTailorTask.id = Str(Rnd(10000))
    MediaTailorTask.observeField("response", "handleResponseManifest")
    MediaTailorTask.url = top.baseUrl + top.streamUrl
    MediaTailorTask.functionName = "postRequest"
    m.MediaTailorTasks.push(MediaTailorTask)
    MediaTailorTask.control = "Run"
end function

sub trackingPing()
    MediaTailorTask = CreateObject("roSGNode", "MediaTailorTask")
    MediaTailorTask.id = Str(Rnd(10000))
    MediaTailorTask.observeField("response", "handleResponseTraking")
    MediaTailorTask.url = m.trackingUrl
    MediaTailorTask.functionName = "getRequest"
    m.MediaTailorTasks.push(MediaTailorTask)
    MediaTailorTask.control = "Run"
end sub

sub handleResponseTraking(event)
    data = event.getData()
    clearTask(data.selfid)
    if data.response.avails.count() > 0
        avail = data.response.avails[0]
        if not contains(m.availsIds, avail.id)
            nonLinearAds = avail.nonLinearAdsList[0].nonLinearAdList[0]
            m.top.creative = nonLinearAds.staticResource
        end if
        m.availsIds.push(avail.availId)
    end if
end sub

sub handleResponseManifest(event)
    data = event.getData()
    clearTask(data.selfid)
    top = m.top
    m.top.manifestUrl = top.baseUrl + data.response.manifestUrl
    m.trackingUrl = top.baseUrl + data.response.trackingUrl
    trackingPing()
    m.pingTimer.control = "start"
end sub

sub clearTask(id)
    for i = m.MediaTailorTasks.count() - 1 to 0 step -1
        if m.MediaTailorTasks[i].id = id
            m.MediaTailorTasks[i] = invalid
            m.MediaTailorTasks.Delete(i)
        end if
    end for
end sub