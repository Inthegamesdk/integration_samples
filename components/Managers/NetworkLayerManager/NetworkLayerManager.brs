function fetchAccounts(url)
    try
        request = CreateObject("roSGNode", "URLRequest")
        request.url = url
        request.method = "GET"
        request.withBody = false
        networkManager = CreateObject("roSGNode", "NetworkTask")
        networkManager.request = request
        networkManager.observeField("response", "onResponseAccounts")
        networkManager.control = "RUN"
    catch error
        ? getDataFromError(error)
    end try
end function

function fetchAccountDetail(url)
    try
        request = CreateObject("roSGNode", "URLRequest")
        request.url = url
        request.method = "GET"
        request.withBody = false
        networkManager = CreateObject("roSGNode", "NetworkTask")
        networkManager.request = request
        networkManager.observeField("response", "onResponseAccountDetail")
        networkManager.control = "RUN"
    catch error
        ? getDataFromError(error)
    end try
end function

sub onResponseAccounts(event)
    data = event.getData()
    m.top.accounts = data.arrayData
end sub

sub onResponseAccountDetail(event)
    data = event.getData()
    m.top.accountDetail = data.data
end sub