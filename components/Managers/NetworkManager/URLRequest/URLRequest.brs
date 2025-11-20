sub init()
    m.top.headerParameters = {}
    m.top.querryParams = {}
    m.top.arrayBody = []
    m.top.context = {}
    m.top.body = {}
end sub

function getUrl() as string
    try
        return m.top.url
    catch error
        ? getDataFromError(error)
    end try
end function

function getBody()
    try
        if m.top.arrayBody.count() > 0
            bodyString = FormatJson(m.top.arrayBody)
        else
            bodyString = FormatJson(m.top.body)
        end if
        return bodyString
    catch error
        ? getDataFromError(error)
    end try
end function

function getHeaders()
    try
        header = {}
        if isValid(m.top.headerParameters)
            header = m.top.headerParameters
        end if
        return header
    catch error
        ? getDataFromError(error)
    end try
end function

function getQuerryString()
    try
        string = "?"
        querryParams = getDefaultQuerryParams()
        if isValid(m.top.querryParams)
            querryParams.append(m.top.querryParams)
        end if
        for each parmsPair in querryParams.Items()
            string += parmsPair.key + "=" + parmsPair.value + "&"
        end for
        if string = "?"
            return ""
        end if
        return string
    catch error
        ? getDataFromError(error)
    end try
end function

function getDefaultQuerryParams()
    try
        querry = {}
        return querry
    catch error
        ? getDataFromError(error)
    end try
end function
