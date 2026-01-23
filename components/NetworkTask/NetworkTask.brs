
function getRequest() as object
    try
        res = CreateObject("roUrlTransfer")
        port = CreateObject("roMessagePort")
        res.SetPort(port)
        res.setURL(m.top.url)
 
        res.setRequest("GET")
        res.EnableEncodings(true)
        res.SetCertificatesFile("common:/certs/ca-bundle.crt")
        res.InitClientCertificates()
        if res.AsyncGetToString()
            while true
                msg = Wait (10000, port)
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
                            selfID: m.top.id
                            responceCode: msg.GetResponseCode()
                            error: false
                        }

                        m.top.response = resJson
                    else if msg.GetResponseCode() = 401
                        scene = m.top.getScene()
                        scene.callFunc("logOut", {})
                        res = msg.GetResponseCode()
                        resJson = {
                            response: "UNAUTORIZE"
                            responceCode: res
                            selfID: m.top.id
                            error: true
                        }
                        
                        m.top.response =  resJson
                    else
                        serverResponse = invalid
                        if Len(msg.GetString()) > 0
                            serverResponse = ParseJson(msg.GetString())
                        end if

                        if serverResponse = invalid
                            serverResponse = msg.GetFailureReason()
                        end if

                        resJson = {
                            response: serverResponse
                            responceCode: msg.GetResponseCode()
                            error: true
                            selfID: m.top.id
                        }
                    end if

                    m.top.response =  resJson
                    exit while
                else if Type (msg) = "Invalid"
                    
                    resJson = {
                        response: invalid
                        responceCode: invalid
                        error: true
                        selfID: m.top.id
                    }
                    m.top.response =  resJson
                    res.AsyncCancel()
                    exit while
                end if
            end while
        end if
    catch error
        ? error
    end try
end function

function postRequest()
    try
        http = CreateObject("roUrlTransfer")
        http.RetainBodyOnError(true)
        port = CreateObject("roMessagePort")
        http.SetPort(port)
        http.SetCertificatesFile("common:/certs/ca-bundle.crt")
        http.InitClientCertificates()
        http.setURL(m.top.url)
        http.EnableEncodings(true)

        if http.AsyncPostFromString(m.top.body) then
            event = Wait(10000, http.GetPort())
            if Type(event) = "roUrlEvent" then
                resJson = invalid
                resCode = event.GetResponseCode()
                if resCode = 200
                    response = ParseJson(event.GetString())

                    resJson = {
                        responceCode: resCode
                        response: response
                        error: false
                        selfID: m.top.id
                    }
                else if resCode = 401
                    scene = m.top.getScene()
                    scene.callFunc("logOut", {})
                    resJson = {
                        responceCode: resCode
                        response: "UNAUTORIZE"
                        error: true
                        selfID: m.top.id
                    }
                    m.top.response = resJson
                else
                    serverResponse = invalid
                    if Len(event.GetString()) > 0
                        serverResponse = ParseJson(event.GetString())
                    end if

                    if serverResponse = invalid
                        serverResponse = event.GetFailureReason()
                    end if
                    resJson = {
                        responceCode: resCode
                        response: serverResponse
                        error: true
                        selfID: m.top.id
                    }
                end if

                m.top.response =  resJson
            else if event = invalid then
                http.asynccancel()
                resJson = {
                    response: invalid
                    responceCode: invalid
                    error: true
                    selfID: m.top.id
                }
                m.top.response =  resJson
            else
                resJson = {
                    response: invalid
                    responceCode: invalid
                    error: true
                    selfID: m.top.id
                }
                m.top.response = resJson
            end if
        end if
    catch error
        ? error
    end try
end function