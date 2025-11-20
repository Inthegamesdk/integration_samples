sub init()
    m.loadingLibraryLabelStatus = m.top.findNode("loadingLibraryLabelStatus")
    loadITGLibrary()

    m.networkLayerManager = CreateObject("roSGNode", "NetworkLayerManager")

    m.envList = m.top.findNode("envList")
    m.chennelList = m.top.findNode("chennelList")
    m.accountList = m.top.findNode("accountList")
    m.envList.observeField("rowItemFocused", "handleItemFocused")
    m.chennelList.observeField("itemSelected", "onItemSelected")
    m.accountList.observeField("rowItemFocused", "handleFocusedAccountList")
    m.networkLayerManager.observeField("infoResponce", "infoApplication")
    m.networkLayerManager.observeField("channelsInfo", "handleResponceChannels")
    m.networkLayerManager.observeField("accounts", "handleAccounts")
    m.networkLayerManager.observeField("accountDetail", "handleAccountDetail")
    m.channelsInfo = {}
    m.horizontalFocusedElements = [m.envList, m.accountList]
    m.currentFocusedElementIndex = 0
    m.accountID = invalid
    m.environment = "v2-7"
end sub

sub loadITGLibrary()
    m.componentLibrary = CreateObject("roSGNode", "ComponentLibrary")
    m.componentLibrary.observeField("loadStatus", "onLoadStatusLibraryChanged")
    m.componentLibrary.uri = "https://assets.inthegame.io/roku/ITGLibrary_v2-7.pkg"
end sub

 sub onLoadStatusLibraryChanged(event)
    status = event.getData()
    m.loadingLibraryLabelStatus.text = "Library loading status: " + status
    if status <> "ready" then return
    m.networkLayerManager.callFunc("fetchAccounts", "https://assets.internal.inthegame.io/general/list_demos_ott.json")
 end sub

sub handleAccounts(event)
    data = event.getData()
    m.envData = {}
    for each item in data
        if m.envData.doesExist(item.environment)
            m.envData[item.environment].push(item)
        else
            m.envData[item.environment] = [item]
        end if
    end for
    configureEnvList()
end sub

sub handleAccountDetail(event)
    data = event.getData()
    if IsInvalid(data) then return
    configureChannelsList(data.channels)
end sub

sub onItemSelected(event)
    index = event.getData()
    content = m.chennelList.content.getChild(index).getChild(0)
    m.accountRoute = { "channelSlug": content.slug, "accountID": m.accountID }
    m.videoPlayer = m.top.createChild("VideoPlayer")
    m.videoPlayer.environment = m.environment
    m.videoPlayer.videoUrl = content.video
    m.videoPlayer.accountRoute = m.accountRoute
    m.videoPlayer.setFocus(true)
end sub

sub infoApplication(event)
    data = event.getData()
    if IsInvalid(data) then return
    m.videoPlayer = m.top.createChild("VideoPlayer")
    m.videoPlayer.videoUrl = data.streamUrl
    m.videoPlayer.accountRoute = m.accountRoute
    m.videoPlayer.setFocus(true)
end sub

sub handleItemFocused(event)
    indexPath = event.getData()
    envKey = m.envList.content.getChild(indexPath[0]).getChild(indexPath[1]).title
    m.environment = envKey
    configureAccountsList(m.envData[envKey])
end sub

sub handleFocusedAccountList(event)
    indexPath = event.getData()
    content = m.accountList.content.getChild(indexPath[0]).getChild(indexPath[1])
    m.accountID = content.accountId
    m.networkLayerManager.callFunc("fetchAccountDetail", getAccountDetailUrl(content.environment, content.accountId))
end sub

sub handleResponceChannels(event)
    data = event.getData()
    m.channelsInfo[data[0].environment] = data
    configureChannelsList(data)
end sub

sub configureAccountsList(data)
    content = CreateObject("roSGNode", "ContentNode")

    for each channel in data
        rowContent = content.createChild("ContentNode")
        elementContent = rowContent.createChild("ContentNode")
        elementContent.addFields(channel)
        elementContent.title = channel.accountName
    end for

    m.accountList.content = content
    m.accountList.itemSize = [400, 100]
    m.accountList.rowItemSize = [[400, 100]]
    m.accountList.itemSpacing = [0, 10]
    m.accountList.rowItemSpacing = [[0, 10]]
end sub

sub configureChannelsList(data)
    if IsInvalid(m.chennelList.content) then m.horizontalFocusedElements.push(m.chennelList)
    content = CreateObject("roSGNode", "ContentNode")

    for each channel in data
        rowContent = content.createChild("ContentNode")
        elementContent = rowContent.createChild("ContentNode")
        elementContent.addFields(channel)
        elementContent.title = channel.label
    end for

    m.chennelList.content = content
    m.chennelList.itemSize = [500, 100]
    m.chennelList.rowItemSize = [[500, 100]]
    m.chennelList.itemSpacing = [0, 10]
    m.chennelList.rowItemSpacing = [[0, 10]]
end sub

sub configureEnvList()
    content = CreateObject("roSGNode", "ContentNode")

    for each env in m.envData.items()
        rowContent = content.createChild("ContentNode")
        elementContent = rowContent.createChild("ContentNode")
        elementContent.title = env.key
    end for

    m.envList.content = content
    m.envList.itemSize = [250, 50]
    m.envList.rowItemSize = [[250, 50]]
    m.envList.itemSpacing = [0, 10]
    m.envList.rowItemSpacing = [[0, 10]]
    m.envList.setFocus(true)
end sub

function onKeyEvent(key as string, press as boolean) as boolean
    result = false
    ? "ChannelList function onKeyEvent("key" as string, "press" as boolean) as boolean"
    if not press then return result

    if key = "left" and m.horizontalFocusedElements[m.currentFocusedElementIndex].hasFocus()
        m.currentFocusedElementIndex -= 1
        if m.currentFocusedElementIndex < 0 then 
            m.currentFocusedElementIndex += 1
            return false
        end if
        m.horizontalFocusedElements[m.currentFocusedElementIndex].setFocus(true)
    else if key = "right" and m.horizontalFocusedElements[m.currentFocusedElementIndex].hasFocus()
        m.currentFocusedElementIndex += 1
        if m.currentFocusedElementIndex > m.horizontalFocusedElements.count() - 1
            m.currentFocusedElementIndex = m.horizontalFocusedElements.count() - 1
            return false
        end if
        m.horizontalFocusedElements[m.currentFocusedElementIndex].setFocus(true)
    else if key = "back" and IsValid(m.videoPlayer)
        m.videoPlayer.callFunc("stopVideo")
        m.top.removeChild(m.videoPlayer)
        m.videoPlayer = invalid
        m.horizontalFocusedElements[m.currentFocusedElementIndex].setFocus(true)
    end if

    return true
end function

sub getAccountDetailUrl(env, accountID) as object
    return "https://openapi.internal.inthegame.io/" + env + "/api/v2/account?accountId=" + accountID
end sub