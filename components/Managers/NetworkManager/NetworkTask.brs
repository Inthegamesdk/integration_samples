sub init()
    m.top.functionName = "setupRequest"
end sub

function setupRequest()
    try
        url = m.top.baseUrl
        request = m.top.request
        if url = invalid or request = invalid then return invalid
        pathURL = request.callFunc("getUrl")
        if Instr(0, pathURL, "http") > 0
            url = pathURL
        else
            url += pathURL
        end if
        body = request.callFunc("getBody")
        headers = request.callFunc("getHeaders")
        CachedUtil = CacheUtil(url, { ttl: request.cacheExpiredInterval })
        CachedResponse = CachedUtil.get()
        if isValid(request.cacheExpiredInterval) and IsValid(CachedResponse)
            response = {
                response: ParseJson(CachedResponse)
                error: false
            }
        else
            if request.method = "POST"
                if request.isUrlEncoded then body = getBody(body)
                response = postRequest(url, body, headers, request.timeout)
            else if request.method = "PUT"
                if isValid(request.bodyString) and request.bodyString <> ""
                    body = request.bodyString
                end if
                response = putRequest(url, body, headers, request.timeout)
            else if request.method = "PATCH"
                response = getPatchRequest(url, body, headers)
            else if request.method = "Headers"
                response = getHeader(url)
            else
                response = getRequest(url, headers, request.timeout)
            end if
            if isValid(request.cacheExpiredInterval) and request.cacheExpiredInterval > 0
                CachedUtil.put(FormatJson(response.response))
            end if
        end if
        
        responseModel = CreateObject("roSGNode", "URLResponse")
        responseModel.callFunc("initWithResponse", response)
      
        if IsInteger(response) then return invalid
        if IsValid(response)
            responseModel.headers = response.headers
            responseModel.context = request.context
            responseModel.responceCode = response.responceCode
            responseModel.completionInfo = request.completionInfo
            m.top.response = responseModel
        end if
    catch error
        ? getDataFromError(error)
    end try
end function

function getRequest(url, headers = invalid, timeout = 10000) as object
    try
        res = CreateObject("roUrlTransfer")
        port = CreateObject("roMessagePort")
        res.SetPort(port)
        res.setURL(url)
        h = getDefaultHeders()
        if headers <> invalid
            h.Append(headers)
        end if
        res.setRequest("GET")
        res.SetHeaders(h)
        res.EnableEncodings(true)
        res.SetCertificatesFile("common:/certs/ca-bundle.crt")
        res.InitClientCertificates()
        if res.AsyncGetToString()
            while true
                msg = Wait (timeout, port)
                if Type (msg) = "roUrlEvent"
                    resJson = invalid
                    if msg.GetResponseCode() = 200
                        pars = msg.GetString()
                        if msg.GetString() <> ""
                            response = ParseJson(pars)

                            if response = invalid
                                response = pars
                            end if
                        else
                            response = pars
                        end if
                        resJson = {
                            headers: msg.GetResponseHeaders()
                            response: response
                            responceCode: msg.GetResponseCode()
                            error: false
                        }
                        return resJson
                    else if msg.GetResponseCode() = 401
                        scene = m.top.getScene()
                        scene.callFunc("logOut", {})
                        res = msg.GetResponseCode()
                        resJson = {
                            response: "UNAUTORIZE"
                            responceCode: res
                            error: true
                        }

                        return resJson
                    else
                        serverResponse = invalid
                        if IsString(msg.GetString()) and Len(msg.GetString()) > 0
                            serverResponse = ParseJson(msg.GetString())
                        end if

                        if serverResponse = invalid
                            serverResponse = msg.GetFailureReason()
                        end if

                        resJson = {
                            response: serverResponse
                            responceCode: msg.GetResponseCode()
                            error: true
                        }
                    end if
                    if m.global.showLogs
                        ConsolLog().logGetRequest(url, h, msg)
                    end if
                    return resJson
                    exit while
                else if Type (msg) = "Invalid"
                    if m.global.showLogs
                        ' ConsolLog().logGetRequest(url, h, msg)
                    end if
                    resJson = {
                        response: invalid
                        responceCode: invalid
                        error: true
                    }
                    return resJson
                    res.AsyncCancel()
                    exit while
                end if
            end while
        end if
    catch error
        ? getDataFromError(error)
    end try
end function

function postRequest(url, body, headers = invalid, timeout = 10000)
    try
        http = CreateObject("roUrlTransfer")
        http.RetainBodyOnError(true)
        port = CreateObject("roMessagePort")
        http.SetPort(port)
        http.SetCertificatesFile("common:/certs/ca-bundle.crt")
        http.InitClientCertificates()
        http.setURL(url)
        http.EnableEncodings(true)

        h = getDefaultHeders()
        if headers <> invalid
            h.Append(headers)
        end if
        http.SetHeaders(h)

        if http.AsyncPostFromString(body) then
            event = Wait(timeout, http.GetPort())
            if Type(event) = "roUrlEvent" then
                resJson = invalid
                resCode = event.GetResponseCode()
                if resCode = 200
                    response = ParseJson(event.GetString())

                    resJson = {
                        responceCode: resCode
                        response: response
                        error: false
                    }
                else if resCode = 401
                    scene = m.top.getScene()
                    scene.callFunc("logOut", {})
                    resJson = {
                        responceCode: resCode
                        response: "UNAUTORIZE"
                        error: true
                    }
                    return resJson
                else
                    serverResponse = invalid
                    if IsString(event.GetString()) and Len(event.GetString()) > 0
                        serverResponse = ParseJson(event.GetString())
                    end if
                    if serverResponse = invalid
                        serverResponse = event.GetFailureReason()
                    end if
                    resJson = {
                        responceCode: resCode
                        response: serverResponse
                        error: true
                    }
                end if

                if m.global.showLogs
                    ConsolLog().logPOSTRequest(url, body, h, event)
                end if
                return resJson
            else if event = invalid then
                http.asynccancel()
                resJson = {
                    response: invalid
                    responceCode: invalid
                    error: true
                }
                return resJson
            else
                resJson = {
                    response: invalid
                    responceCode: invalid
                    error: true
                }
                return resJson
            end if
        end if
    catch error
        ? getDataFromError(error)
    end try
end function

function putRequest(url, body, headers, timeout = 10000)
    try
        http = CreateObject("roUrlTransfer")
        http.RetainBodyOnError(true)
        port = CreateObject("roMessagePort")
        http.SetPort(port)
        http.SetCertificatesFile("common:/certs/ca-bundle.crt")
        http.InitClientCertificates()
        http.setURL(url)
        http.setRequest("PUT")
        http.EnableEncodings(true)

        h = getDefaultHeders()
        h["Content-type"] = "application/json"

        if headers <> invalid
            h.Append(headers)
        end if
        http.SetHeaders(h)

        if http.AsyncPostFromString(body) then
            event = Wait(timeout, http.GetPort())
            if Type(event) = "roUrlEvent" then
                resJson = invalid
                resCode = event.GetResponseCode()

                if resCode = 201 or resCode = 200
                    resJson = {
                        responceCode: resCode
                        response: ParseJson(event.GetString())
                        error: false
                    }
                else if resCode = 401
                    scene = m.top.getScene()
                    scene.callFunc("logOut", {})
                    resJson = {
                        responceCode: resCode
                        response: "UNAUTORIZE"
                        error: true
                    }
                    return resJson
                else
                    serverResponse = invalid
                    if IsString(event.GetString()) and Len(event.GetString()) > 0
                        serverResponse = ParseJson(event.GetString())
                    end if
                    if serverResponse = invalid
                        serverResponse = event.GetFailureReason()
                    end if
                    resJson = {
                        responceCode: resCode
                        response: serverResponse
                        error: true
                    }
                end if
                if m.global.showLogs
                    ConsolLog().logPOSTRequest(url, body, h, event)
                end if
                return resJson
            else if event = invalid then
                http.asynccancel()
                resJson = {
                    response: invalid
                    responceCode: invalid
                    error: true
                }
                return resJson
            else
                resJson = {
                    response: invalid
                    responceCode: invalid
                    error: true
                }
                return resJson
            end if
        end if
    catch error
        ? getDataFromError(error)
    end try
end function

function getPatchRequest(url, body, headers = invalid, timeout = 10000) as object
    try
        res = CreateObject("roUrlTransfer")
        port = CreateObject("roMessagePort")
        res.SetPort(port)
        res.setURL(url)
        h = getDefaultHeders()
        if headers <> invalid
            h.Append(headers)
        end if
        res.setRequest("PATCH")
        res.SetHeaders(h)
        res.EnableEncodings(true)
        res.SetCertificatesFile("common:/certs/ca-bundle.crt")
        res.InitClientCertificates()
        if res.AsyncPostFromString(body)
            while true
                msg = Wait (timeout, port)
                if Type (msg) = "roUrlEvent"
                    resJson = invalid
                    if msg.GetResponseCode() = 200
                        pars = msg.GetString()
                        if msg.GetString() <> "" then res = ParseJson(pars)
                        resJson = {
                            headers: msg.GetResponseHeaders()
                            response: res
                            responceCode: msg.GetResponseCode()
                            error: false
                        }
                    else if msg.GetResponseCode() = 401
                        scene = m.top.getScene()
                        scene.callFunc("logOut", {})
                        resJson = {
                            response: "UNAUTORIZE"
                            responceCode: msg.GetResponseCode()
                            error: true
                        }
                        return resJson
                    else
                        serverResponse = invalid
                        if IsString(msg.GetString()) and Len(msg.GetString()) > 0
                            serverResponse = ParseJson(msg.GetString())
                        end if
                        if serverResponse = invalid
                            serverResponse = msg.GetFailureReason()
                        end if
                        resJson = {
                            response: serverResponse
                            responceCode: msg.GetResponseCode()
                            error: true
                        }
                    end if

                    if m.global.showLogs
                        ConsolLog().logGetRequest(url, h, msg)
                    end if
                    return resJson
                    exit while
                else if Type (msg) = "Invalid"
                    res.AsyncCancel()
                    resJson = {
                        response: invalid
                        responceCode: invalid
                        error: true
                    }
                    return resJson
                    exit while
                end if
            end while
        end if
    catch error
        ? getDataFromError(error)
    end try
end function

function getDefaultHeders()
    try
        headers = {}
        if m.top.request.isUrlEncoded
            headers["Content-type"] = "application/x-www-form-urlencoded"
        else
            headers["Content-type"] = "application/json"
        end if
        headers["X-ITG-SDK-VERSION"] = "v2-3"
        headers["Accept"] = "*/*"
        return headers
    catch error
        ? getDataFromError(error)
    end try
end function

function getBody(body)
    try
        http = CreateObject("roUrlTransfer")
        escapeBody = http.Escape(body)
        bodyEncode = "request=" + escapeBody
        return bodyEncode
    catch error
        ? getDataFromError(error)
    end try
end function

function getHeader(url) as object
    newUrl = url.replace(chr(10), "")
    newUrl = newUrl.replace(" ", "")

    try
        headers = {
            "Accept-Encoding": "identity;q=1, *;q=0",
            ' "Range": "bytes=0-"
        }

        res = CreateObject("roUrlTransfer")
        port = CreateObject("roMessagePort")
        res.SetPort(port)
        res.setURL(newUrl)
        res.SetHeaders(headers)
        res.setRequest("HEAD")
        res.EnableEncodings(true)
        res.SetCertificatesFile("common:/certs/ca-bundle.crt")
        res.InitClientCertificates()
        if res.AsyncHead()
            while true
                msg = Wait (100000, port)
                if Type (msg) = "roUrlEvent"
                    resJson = {
                        headers: msg.GetResponseHeaders()
                        response: res
                        responceCode: msg.GetResponseCode()
                        error: false
                    }
                    return resJson
                end if
            end while
        end if
    catch error
        ? getDataFromError(error)
    end try
end function
