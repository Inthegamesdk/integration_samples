sub init()
    configureButtons()
    RegDelete()
end sub

sub configureButtons()
    m.typeIntegrationButtonsGroup = m.top.findNode("typeIntegrationButtonsGroup")
    m.typeIntegrationButtonsGroup.observeField("buttonSelected", "didSelectedButton")
    m.typeIntegrationButtonsGroup.buttons = ["Live stream", "Without video player", "With video player"]
    m.top.setFocus(true)
end sub

sub didSelectedButton(event)
    index = event.getData()
    if index = 0 then m.integrationController = m.top.createChild("IntegrationWithLiveStream")
    if index = 1 then m.integrationController = m.top.createChild("IntegrationWithoutVideoPlayer")
    if index = 2 then m.integrationController = m.top.createChild("IntegrationWithVideoPlayer")
end sub

function onKeyEvent(key as string, press as boolean) as boolean
    result = false

    if not press then return result

    if key = "back"
        m.top.removeChild(m.integrationController)
        m.integrationController = invalid
        m.typeIntegrationButtonsGroup.setFocus(true)
        result = true
    end if

    return result
end function

function RegDelete(key = invalid, section = "SDKData")
    if key = invalid
        sec = CreateObject("roRegistry")
        sec.Delete(section)
    else
        sec = CreateObject("roRegistrySection", section)
        sec.Delete(key)
    end if
end function