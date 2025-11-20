function findMaxNumber(arr as object) as object
    try
        if arr.count() = 0 then
            return 0
        end if

        maxNumber = arr[0]
        for each num in arr
            if num > maxNumber then
                maxNumber = num
            end if
        end for
        return maxNumber
    catch error
        ? getDataFromError(error)
    end try
end function

function summArrayNumbers(arr as object) as object
    try
        if arr.count() = 0 then
            return 0
        end if

        summ = 0
        for each num in arr
            summ += num
        end for
        return summ
    catch error
        ? getDataFromError(error)
    end try
end function

sub validateUrl(url) as object
    try
        regex = CreateObject("roRegex", "https?://[^ ]+", "i")
        if regex.IsMatch(url)
            resultArray = regex.Match(url)
            if resultArray.count() > 0
                return resultArray[0]
            end if
        end if
        return url
    catch error
        ? getDataFromError(error)
    end try
end sub

function getNowTimestamp() as object
    try
        date = createObject("roDateTime")
        seconds = date.asSeconds()
        longIntSecond = CreateObject("roLongInteger")
        longIntSecond.SetLongInt(seconds)
        longIntMilisecond = CreateObject("roLongInteger")
        longIntMilisecond.SetLongInt(1000)
        longInt = longIntSecond * longIntMilisecond
        return longInt
    catch error
        ? getDataFromError(error)
    end try
end function

function getNowTimestampSeconds() as object
    try
        date = createObject("roDateTime")
        seconds = date.asSeconds()

        return seconds
    catch error
        ? getDataFromError(error)
    end try
end function

sub getRndNumberString() as object
    try
        numberString = ""
        for i = 0 to 15
            numberString += Rnd(9).toSTR()
        end for
        return numberString
    catch error
        ? getDataFromError(error)
    end try
end sub

function getUnixTimestamp() as string
    try
        currentDateTime = CreateObject("roDateTime")
        rolongInt = getLongInt(currentDateTime.AsSeconds()) * getLongInt(1000)
        unixTimestamp = rolongInt.toSTR()
        return unixTimestamp
    catch error
        ? getDataFromError(error)
    end try
end function

'''''*********************************************
' Logging Helper Functions
' ******************************************************

function ConsolLog() as object
    try
        console = {
            logPOSTRequest: function(url, body, requestHeaders, urlEvent)
                logPOSTRequest(url, body, requestHeaders, urlEvent)
            end function
            logGetRequest: function(url, requestHeaders, urlEvent)
                logGetRequest(url, requestHeaders, urlEvent)
            end function
            logObject: function(logObject, logerName = invalid)
                logObjectWithName(logerName, logObject)
            end function
        }
        return console
    catch error
        ? getDataFromError(error)
    end try
end function

sub logObjectWithName(logerName, logObject)
    try
        log = ""
        if IsString(logerName)
            log += ">>> " + logerName + ": "
        end if
        if IsString(logObject)
            log += chr(10) + chr(10) + logObject
            ? log
        else if IsValid(objectToPrintable(logObject))
            ? log
            ? objectToPrintable(logObject)
        else
            ? log
            ? "Cant print this object!!!"
        end if
    catch error
        ? getDataFromError(error)
    end try
end sub

function objectToTheString(obj)
    try
        stringObj = ""
        if IsAssociativeArray(obj)
            return FormatJson(obj)
        else if IsArray(obj)
            for each item in obj
                stringObj += chr(10) + objectToTheString(item) + chr(10)
            end for
        else if IsSGNode(obj)
            fields = obj.getFields()
            return objectToTheString(fields)
        end if
        return stringObj
    catch error
        ? getDataFromError(error)
    end try
end function

function objectToPrintable(obj)
    stringObj = ""
    try
        if IsAssociativeArray(obj) or IsArray(obj)
            return obj
        else if IsSGNode(obj)
            fields = obj.getFields()
            return fields
        else
            return invalid
        end if
    catch error
        ? getDataFromError(error)
    end try
    return stringObj
end function

' =====================================================

function logRequest(url, body, requestHeaders, method, roURLEvent)
    try
        resJson = invalid
        responseString = roURLEvent.GetString()

        if isValid(responseString) and Len(responseString) > 0
            resJson = ParseJson(responseString)
        end if

        data = "method: " + method + "/n" + "URL: " + url + "/n" + "HEADERS: " + FormatJson(requestHeaders) + "/n" + "RESPONSE CODE: " + roURLEvent.GetResponseCode().toStr()
        ? ""
        ? "======================("method")========================== "
        ? "URL: " url
        ? ""
        if method = "POST"
            ? "BODY: " body
            ? ""
            data = data + "/n" + "BODY: " + body
        end if
        ? "HEADERS: " requestHeaders
        ? "=================================================== "
        ? ""
        ? "RESPONSE CODE: " roURLEvent.GetResponseCode().toStr()
        ? "=================================================== "
        ? ""
        ? "RESPONSE: " resJson
        ? "=================================================== "
        ? ""

        appendInGlobal("logData", data)
    catch error
        ? getDataFromError(error)
    end try
end function

function logGetRequest(url, requestHeaders, roURLEvent)
    logRequest(url, invalid, requestHeaders, "GET", roURLEvent)
end function

function logPOSTRequest(url, body, requestHeaders, roURLEvent)
    logRequest(url, body, requestHeaders, "POST", roURLEvent)
end function

function logPUTRequest(url, body, requestHeaders, roURLEvent)
    logRequest(url, body, requestHeaders, "PUT", roURLEvent)
end function

' ******************************************************
' Registry Helper Functions
' ******************************************************

function RegRead(key, section = invalid)
    try
        if section = invalid then section = "SDKData"
        if not IsString(key) then return invalid
        sec = CreateObject("roRegistrySection", section)
        if IsInvalid(key) then return invalid
        if sec.Exists(key)
            return sec.Read(key)
        end if
    catch error
        ? getDataFromError(error)
    end try
    return invalid
end function

function RegWrite(key, val, section = invalid)
    try
        if section = invalid then section = "SDKData"
        sec = CreateObject("roRegistrySection", section)
        sec.Write(key, val)
    catch error
        ? getDataFromError(error)
    end try
end function

function RegDelete(key = invalid, section = "SDKData")
    try
        if key = invalid
            sec = CreateObject("roRegistry")
            sec.Delete(section)
        else
            sec = CreateObject("roRegistrySection", section)
            sec.Delete(key)
        end if
    catch error
        ? getDataFromError(error)
    end try
end function

sub appendInGlobal(key, data)
    try
        if m.global[key] <> invalid
            dataSaved = m.global[key]
            dataSaved += data
            m.global[key] = dataSaved
        else
            obj = {}
            obj[key] = data
            m.global.addFields(obj)
        end if
    catch error
        ? getDataFromError(error)
    end try
end sub

sub getPercentIntWithStr(percent) as object
    try
        return convertStrToInt(percent.replace("%", ""))
    catch error
        ? getDataFromError(error)
    end try
end sub


' ******************************************************
' Array Helper Functions
' ******************************************************

function ReplaceAssocArray(initialArray as object, defaultArray as object) as object
    try
        for each key in defaultArray
            if initialArray.doesExist(key) = false or initialArray.doesExist(key)
                initialArray[key] = defaultArray[key]
            else
                if type(initialArray[key]) = "roAssociativeArray" and type(defaultArray[key]) = "roAssociativeArray"
                    initialArray[key] = ReplaceAssocArray(initialArray[key], defaultArray[key])
                end if
            end if
        end for
        return initialArray
    catch error
        ? getDataFromError(error)
    end try
end function

function ReplaceDetailAssocArray(initialArray as object, defaultArray as object) as object
    try
        if IsInvalid(initialArray) then initialArray = {}
        for each key in defaultArray
            if type(initialArray[key]) = "roAssociativeArray" and type(defaultArray[key]) = "roAssociativeArray"
                initialArray[key] = ReplaceDetailAssocArray(initialArray[key], defaultArray[key])
            else if initialArray.doesExist(key) = false or initialArray.doesExist(key) and isInvalid(initialArray[key])
                initialArray[key] = defaultArray[key]
            else if initialArray.doesExist(key) and IsString(initialArray[key]) and initialArray[key] = ""
                initialArray[key] = defaultArray[key]
            end if
        end for
        return initialArray
    catch error
        ? getDataFromError(error)
    end try
end function

function findArrIndex(arr, key, value, key2 = invalid)
    try
        if arr <> invalid
            if key2 <> invalid
                for i = 0 to arr.Count() - 1
                    if arr[i][key][key2] = value
                        return i
                    end if
                end for
            else
                for i = 0 to arr.Count() - 1
                    if arr[i][key] = value
                        return i
                    end if
                end for
            end if
        end if
        return invalid
    catch error
        ? getDataFromError(error)
    end try
end function

function findAllArrIndex(arr, key, value, key2 = invalid)
    try
        indexes = []
        if arr <> invalid
            if key2 <> invalid
                for i = 0 to arr.Count() - 1
                    if arr[i][key][key2] = value
                        indexes.push(i)
                    end if
                end for
            else
                for i = 0 to arr.Count() - 1
                    if arr[i][key] = value
                        indexes.push(i)
                    end if
                end for
            end if
        end if
        return indexes
    catch error
        ? getDataFromError(error)
    end try
end function

function contains(arr as object, value) as boolean
    try
        for each entry in arr
            if entry = value
                return true
            end if
        end for
        return false
    catch error
        ? getDataFromError(error)
    end try
end function

function getIndex(arr, value)
    try
        i = 0
        for each item in arr
            if item = value
                return i
            end if
            i++
        end for
        return invalid
    catch error
        ? getDataFromError(error)
    end try
end function

' ******************************************************
' Type check
' ******************************************************

function IsInteger(value as dynamic) as boolean
    try
        return IsValid(value) and GetInterface(value, "ifInt") <> invalid and (Type(value) = "roInt" or Type(value) = "roInteger" or Type(value) = "Integer")
    catch error
        ? getDataFromError(error)
    end try
end function

function IsBoolean(value as dynamic) as boolean
    try
        return IsValid(value) and GetInterface(value, "ifBoolean") <> invalid and (Type(value) = "Bool" or Type(value) = "roBoolean" or Type(value) = "Boolean")
    catch error
        ? getDataFromError(error)
    end try
end function

function IsVideoPlayer(value as dynamic) as boolean
    try
        return IsValid(value) and (Type(value) = "Video" or Type(value) = "roVideo")
    catch error
        ? getDataFromError(error)
    end try
end function

function IsLongInteger(value as dynamic) as boolean
    try
        return IsValid(value) and GetInterface(value, "ifLongInt") <> invalid and (Type(value) = "roLongInteger" or Type(value) = "roLongInteger" or Type(value) = "LongInteger")
    catch error
        ? getDataFromError(error)
    end try
end function

function IsFloat(value as dynamic) as boolean
    try
        return IsValid(value) and (GetInterface(value, "ifFloat") <> invalid or (Type(value) = "roFloat" or Type(value) = "Float"))
    catch error
        ? getDataFromError(error)
    end try
end function

function IsArray(value as dynamic) as boolean
    try
        return IsValid(value) and Type(value) = "roArray"
    catch error
        ? getDataFromError(error)
    end try
end function

function IsRoXMLList(value as dynamic) as boolean
    try
        return IsValid(value) and Type(value) = "roXMLList"
    catch error
        ? getDataFromError(error)
    end try
end function

function IsAssociativeArray(value as dynamic) as boolean
    try
        return IsValid(value) and Type(value) = "roAssociativeArray"
    catch error
        ? getDataFromError(error)
    end try
end function

function IsString(value as dynamic) as boolean
    try
        return IsValid(value) and GetInterface(value, "ifString") <> invalid
    catch error
        ? getDataFromError(error)
    end try
end function

function IsSGNode(value as dynamic) as boolean
    try
        return IsValid(value) and (GetInterface(value, "ifSGNodeField") <> invalid or Type(value) = "roSGNode")
    catch error
        ? getDataFromError(error)
    end try
end function

function IsValid(value as dynamic) as boolean
    try
        return Type(value) <> "<uninitialized>" and value <> invalid
    catch error
        ? getDataFromError(error)
    end try
end function

function IsInvalid(value as dynamic) as boolean
    try
        return Type(value) = "<uninitialized>" or value = invalid
    catch error
        ? getDataFromError(error)
    end try
end function

function IsNumeric(value) as boolean
    try
        if IsInteger(value) or IsFloat(value) return true
        if not IsString(value) then return false
        regex = CreateObject("roRegex", "^-?\d+(\.\d+)?$", "i")
        return regex.IsMatch(value)
    catch error
        ? getDataFromError(error)
    end try
end function

function StrToInt(strin as string) as integer
    try
        return val(strin)
    catch error
        ? getDataFromError(error)
    end try
end function

function StrToFloat(strin as string) as float
    try
        return val(strin)
    catch error
        ? getDataFromError(error)
    end try
end function

' ******************************************************
' Device Info
' ******************************************************

function getDeviceModel()
    try
        di = CreateObject("roDeviceInfo")
        return di.getModel()
    catch error
        ? getDataFromError(error)
    end try
end function

function isVersionOSSupported(version) as object
    try
        deviceInfo = CreateObject("roDeviceInfo")
        deviceVersionInfo = deviceInfo.GetOSVersion()
        deviceVersionParts = [deviceVersionInfo.major, deviceVersionInfo.minor, deviceVersionInfo.revision]
        supportedVersionParts = version.split(".")

        while supportedVersionParts.count() < deviceVersionParts.count()
            supportedVersionParts.push("0")
        end while

        for i = 0 to deviceVersionParts.count() - 1
            devicePart = deviceVersionParts[i].toInt()
            supportedPart = supportedVersionParts[i].toInt()

            if devicePart > supportedPart then
                return true
            else if devicePart < supportedPart then
                return false
            end if
        end for

        return true
    catch error
        ? getDataFromError(error)
    end try
end function

function generateUUID() as string
    try
        timestamp = getNowTimestamp().toStr()

        uuid = ""
        for i = 1 to 32
            randomDigit = Int(Rnd(0) * 16)
            uuid = uuid + Mid("0123456789abcdef", randomDigit + 1, 1)
        end for

        uuid = Left(timestamp, 8) + "-" + Mid(timestamp, 9, 4) + "-" + Mid(timestamp, 13, 4) + "-" + Mid(timestamp, 17, 4) + "-" + Right(uuid, 12)

        return uuid
    catch error
        ? getDataFromError(error)
    end try
end function

function getSize(size) as object
    try
        deviceInfo = CreateObject("roDeviceInfo")
        resolution = deviceInfo.GetUIResolution()
        resolutionName = resolution.name

        if isValid(m.global.resolution)
            if m.global.resolution <> ""
                resolutionName = m.global.resolution
            end if
        end if

        if resolutionName = "HD"
            size = size * 2 / 3
            return size
        end if
        return size
    catch error
        ? getDataFromError(error)
    end try
end function

sub getConfigForLevel(config, level, posterWidth, posterHeight) as object
    try
        if level = "low"
            config = {
                "loadDisplayMode": "limitSize"
                "fps": config.fps
                "width": posterWidth / 2
                "height": posterHeight / 2
                "incrementLevel": 1
                "videoUrl": config.videoUrl
                "maxThreds": config.maxThreds
                "baseManifestUrl": config.baseManifestUrl
            }
        else if level = "critical"
            config = {
                "loadDisplayMode": "limitSize"
                "fps": config.fps
                "width": posterWidth / 4
                "height": posterHeight / 4
                "incrementLevel": 1
                "videoUrl": config.videoUrl
                "baseManifestUrl": config.baseManifestUrl
                "maxThreds": config.maxThreds
            }
        else if level = "normal"
            config["incrementLevel"] = 1
            if IsInvalid(config.loadDisplayMode)
                config.loadDisplayMode = "limitSize"
                config.width = posterWidth
                config.height = posterHeight
            end if
        end if

        return config
    catch error
        ? getDataFromError(error)
    end try
end sub

sub getConfigForDownloadingFrame(frames, maxSizes) as object
    try
        scaleFactorWidth = 1
        scaleFactorHeight = 1

        if frames.width > maxSizes.width then scaleFactorWidth = frames.width / maxSizes.width
        if frames.height > maxSizes.height then scaleFactorHeight = frames.height / maxSizes.height

        scaleFactor = max(scaleFactorWidth, scaleFactorHeight)

        frames.width = Fix(frames.width / scaleFactor)
        frames.height = Fix(frames.height / scaleFactor)

        return frames
    catch error
        ? getDataFromError(error)
    end try
end sub

function max(a, b)
    try
        if a > b then return a
        return b
    catch error
        ? getDataFromError(error)
    end try
end function

function min(a as integer, b as integer) as integer
    try
        if a < b
            return a
        else
            return b
        end if
    catch error
        ? getDataFromError(error)
    end try
end function

sub calculateSizeWithPercent(size, percent) as object
    try
        if IsInteger(percent) or IsFloat(percent) then return percent
        percentInt = percent.replace("%", "").toFloat()
        return (size / 100) * percentInt
    catch error
        ? getDataFromError(error)
    end try
end sub

sub getLanguage() as string
    try
        di = CreateObject("roDeviceInfo")
        locale = di.GetCurrentLocale().split("_")
        return locale[0]
    catch error
        ? getDataFromError(error)
    end try
end sub

sub getMediumFont(size = 25) as object
    try
        label = CreateObject("roSGNode", "Label")
        label.font = "font:MediumSystemFont"
        label.font.size = size
        return label.font
    catch error
        ? getDataFromError(error)
    end try
end sub

sub getBoldFont(size = 25) as object
    try
        label = CreateObject("roSGNode", "Label")
        label.font = "font:MediumBoldSystemFont"
        label.font.size = size
        return label.font
    catch error
        ? getDataFromError(error)
    end try
end sub

sub getLargeBoldFont(size = 25) as object
    try
        label = CreateObject("roSGNode", "Label")
        label.font = "font:LargeBoldSystemFont"
        label.font.size = size
        return label.font
    catch error
        ? getDataFromError(error)
    end try
end sub

sub getLongInt(number) as longinteger
    try
        milisecondsInjection = CreateObject("roLongInteger")
        milisecondsInjection.SetLongInt(number)
        return milisecondsInjection
    catch error
        ? getDataFromError(error)
    end try
end sub

sub convertPercent(percent) as object
    try
        percent = (fix((percent * 10) + 0.5)) / 10
        return str(percent)
    catch error
        ? getDataFromError(error)
    end try
end sub

function findInArray(array, findKey, findElement)
    try
        if IsInvalid(array) then return invalid
        if array.count() = 0 then return invalid
        for each element in array
            if element <> invalid and element[findKey] = findElement
                return element
            end if
        end for

        return invalid
    catch error
        ? getDataFromError(error)
    end try
end function

function findInChildren(node, findKey, findElement)
    try
        if IsInvalid(node) or node.getChildCount() = 0 then return invalid

        for i = 0 to node.getChildCount() - 1
            child = node.getChild(i)
            if isValid(child[findKey]) and child[findKey] = findElement
                return child
            end if
        end for

        return invalid
    catch error
        ? getDataFromError(error)
    end try
end function

function containts(array, findKey, findElement)
    try
        if IsInvalid(array) then return invalid
        if array.count() = 0 then return invalid
        for each element in array
            if element <> invalid and element[findKey] = findElement
                return element
            end if
        end for

        return invalid
    catch error
        ? getDataFromError(error)
    end try
end function

function isContaints(array, findKey, findElement)
    try
        if IsInvalid(array) then return false
        if array.count() = 0 then return false
        for each element in array
            if element <> invalid and element[findKey] = findElement
                return true
            end if
        end for

        return false
    catch error
        ? getDataFromError(error)
    end try
end function

function containtsInArray(array, findElement)
    try
        if IsInvalid(array) then return false
        if array.count() = 0 then return false
        for each element in array
            if element <> invalid and LCase(element) = LCase(findElement)
                return true
            end if
        end for

        return false
    catch error
        ? getDataFromError(error)
    end try
end function

function findAllInArray(array, findKey, findElement)
    try
        if IsInvalid(array) then return invalid
        if array.count() = 0 then return invalid
        finded = []
        for each element in array
            if element <> invalid and element[findKey] = findElement
                finded.push(element)
            end if
        end for

        return finded
    catch error
        ? getDataFromError(error)
    end try
end function

function FilterItemsByTs(items as object, key, threshold as longinteger) as object
    try
        ' Ensure the input is an array
        if Type(items) <> "roArray" then
            print "Error: Input is not an array"
            return invalid
        end if

        ' Create a new array to hold the filtered items
        relevantItems = CreateObject("roArray", items.Count(), True)

        ' Iterate through each item in the array
        For Each item In items
            ' Ensure the item is an associative array and has a "ts" key
            if Type(item) = "roAssociativeArray" and item.DoesExist(key) then
                tsValue = item[key]
                ' Perform the comparison and add the item to relevantItems if it meets the criteria

                if tsValue > threshold then
                    relevantItems.Push(item)
                end if
            else
                print "Warning: Item is not an associative array or 'ts' key does not exist"
            end if
        next

        return relevantItems
    catch error
        ? getDataFromError(error)
    end try
end function

function CompareByKey(a as object, b as object, key as string, ascending as boolean) as integer
    try
        if not a.DoesExist(key) or not b.DoesExist(key) then
            return 0
        end if

        valueA = a[key]
        valueB = b[key]

        if valueA < valueB then
            if ascending then
                return -1
            else
                return 1
            end if
        else if valueA > valueB then
            if ascending then
                return 1
            else
                return -1
            end if
        else
            return 0
        end if
    catch error
        ? getDataFromError(error)
    end try
end function

function SortAssociativeArrayByKey(arr as object, key as string, ascending as boolean) as object
    try
        if arr = invalid or arr.Count() = 0 then return arr

        for i = 0 to arr.Count() - 2
            for j = i + 1 to arr.Count() - 1
                if CompareByKey(arr[i], arr[j], key, ascending) > 0 then
                    temp = arr[i]
                    arr[i] = arr[j]
                    arr[j] = temp
                end if
            end for
        end for

        return arr
    catch error
        ? getDataFromError(error)
    end try
end function

sub isExitElement(element, array) as object
    try
        for each item in array
            if item = element then return true
        end for
        return false
    catch error
        ? getDataFromError(error)
    end try
end sub

function RemoveDuplicates(array as object) as object
    try
        uniqueArray = []
        encounteredValues = {}

        for each item in array
            if encounteredValues.doesExist(item) = false
                uniqueArray.push(item)
                encounteredValues[item] = true
            end if
        end for

        return uniqueArray
    catch error
        ? getDataFromError(error)
    end try
end function

sub isFileExists(filePath) as object
    try
        reletivePath = filePath.replace("tmp:/", "")
        isExist = MatchFiles("tmp:/", reletivePath)
        return isExist.count() > 0
    catch error
        ? getDataFromError(error)
    end try
end sub

sub configurePath(name, style, path) as object
    try
        return "tmp:/" + name + "-" + style + "." + getExtensionFont(path)
    catch error
        ? getDataFromError(error)
    end try
end sub

sub configureFilePath() as object
    try
        return "tmp:/" + Rnd(100).toStr() + ".gif"
    catch error
        ? getDataFromError(error)
    end try
end sub

sub getExtensionFont(url) as object
    try
        if IsValid(url) and url <> "" then return right(url, 3)
        return ""
    catch error
        ? getDataFromError(error)
    end try
end sub

sub getVersionApp() as object
    try
        appInfo = CreateObject("roAppInfo")
        return appInfo.GetVersion()
    catch error
        ? getDataFromError(error)
    end try
end sub

sub convertStrToInt(value) as object
    try
        if IsString(value) then return value.toInt()
        return value
    catch error
        ? getDataFromError(error)
    end try
end sub

sub convertIntToStr(value) as object
    try
        if IsInteger(value) or IsLongInteger(value) then return value.toStr()
        return value
    catch error
        ? getDataFromError(error)
    end try
end sub

sub getFont(typeFont) as object
    try
        if isInvalid(m.global.itg_fonts) then return invalid

        for each font in m.global.itg_fonts
            if IsValid(font.type) and font.type = typeFont
                if font.uri = "pkg:/<font path>.otf" or font.uri = "" then return invalid
                return font
            end if
        end for

        return invalid
    catch error
        ? getDataFromError(error)
    end try
end sub

sub getRegularFont(size = 25) as object
    try
        return getMediumFont(size)
    catch error
        ? getDataFromError(error)
    end try
end sub

sub validationUsername(username) as object
    try
        regexUsername = CreateObject("roRegex", "[`!@#$%^&*()+\=\[\]{};:\\|,.<>\/?~]", "i")
        isMatchUsername = regexUsername.isMatch(username)

        if username.len() < 30 and username.len() > 3 then isMatchUsername = true
        if username.len() < 3 then isMatchUsername = false
        return isMatchUsername
    catch error
        ? getDataFromError(error)
    end try
end sub

sub getMediaITG(name) as object
    try
        if IsInvalid(name) then name = ""
        isAbsoluteUrl = Instr(0, name, "https://") > 0 or Instr(0, name, "http://") > 0

        if isAbsoluteUrl then return name
        if (name.Instr(".png") > 0 or name.Instr(".jpg") > 0 or name.Instr(".jpeg") > 0) and name.Instr(".webp") = 0
            name += ".webp"
        end if

        return "https://assets.inthegame.io" + name
    catch error
        ? getDataFromError(error)
    end try
end sub

sub configureTimer(duration, repeat) as object
    try
        if IsInvalid(duration) then duration = 0
        timer = CreateObject("roSGNode", "Timer")
        timer.duration = Str(duration).toFloat()
        timer.repeat = repeat
        return timer
    catch error
        ? getDataFromError(error)
    end try
end sub

sub getRndWithRange(num1, num2) as object
    try
        rndNumber = rnd(num2)
        while rndNumber > num1
            return rndNumber
        end while
    catch error
        ? getDataFromError(error)
    end try
end sub

sub replaceValue(value, allowFewLines = true) as object
    try
        if IsInvalid(value) or not IsString(value) then return value
        if IsInvalid(m.global.userData) then return value
        value = value.replace("{user.email}", m.global.userData.email)
        value = value.replace("{user.name}", m.global.userData.name)
        value = value.replace("{video.currentTime}", m.global.positionVideo.toStr())
        value = value.replace("{video.length}", m.global.durationVideo.toStr())
        value = value.replace("{channel.slug}", m.global.infoSDK.slug)
        value = value.replace("{channel.name}", m.global.infoSDK.name)
        value = value.replace("{screen.width}", getSize(1920).toSTR())
        value = value.replace("{screen.height}", getSize(1080).toSTR())
        value = value.replace("{sdk.version}", getVersionApp())
        value = value.replace("[cachebuster]", getRndNumberString())
        value = value.replace("[timestamp]", getUnixTimestamp())
        value = value.replace("{screenHeight}", getSize(1920).toSTR())
        value = value.replace("{screenWidth}", getSize(1080).toSTR())

        accountStats = getStatsInfo("account", m.global.accountStats, "")
        channelStats = getStatsInfo("channel", m.global.accountStats, m.global.infoSDK.slug)

        if isValid(accountStats)
            value = value.replace("{user.accountExpoints}", accountStats.expoints)
            value = value.replace("{user.accountCoins}", accountStats.coins)
        end if

        if isValid(channelStats)
            value = value.replace("{user.channelExpoints}", channelStats.expoints)
            value = value.replace("{user.channelCoins}", channelStats.coins)
        end if

        if allowFewLines
            value = value.replace("<br>", chr(10))
        end if

        if isValid(m.global.userData.phone) and m.global.userData.phone <> ""
            value = value.replace("{user.phone}", m.global.userData.phone)
        else
            value = value.replace("{user.phone}", "")
        end if

        if isValid(m.global.userData.foreignId) and m.global.userData.foreignId <> ""
            value = value.replace("{user.foreignId}", m.global.userData.foreignId)
        else
            value = value.replace("{user.foreignId}", "")
        end if

        value = value.replace("[cachebuster]", getRndNumberString())
        value = value.replace("[timestamp]", getUnixTimestamp())

        return value
    catch error
        ? getDataFromError(error)
    end try
end sub

sub getInternalVars() as object
    try
        foriegn_id = ""

        if isValid(m.global.userData.foreignId) and m.global.userData.foreignId <> ""
            foriegn_id = m.global.userData.foreignId
        end if

        return {
            "screen_width": getSize(1920).toSTR(),
            "screen_height": getSize(1080).toSTR(),
            "foriegn_id": foriegn_id,
            "video_length": m.global.durationVideo.toStr(),
            "current_time": m.global.positionVideo.toStr(),
            "channel_name": m.global.infoSDK.name,
            "channel_slug": m.global.infoSDK.slug,
            "sdk_version": getVersionApp(),
            "user_account_xp": "",
            "user_account_points": "",
            "user_channel_xp": ""
        }
    catch error
        ? getDataFromError(error)
    end try
end sub

sub replaceUserAgent(url) as object
    url = url.replace("{userAgent}", getStringUserAgent())
    return url
end sub

sub getStringUserAgent() as object
    if isOSVersionSupport(12, 5)
        urlTransfer = CreateObject("roUrlTransfer")
        userAgent = urlTransfer.GetUserAgent()
        return userAgent
    else
        userAgent = "Roku/" + GetVersionOS() + " (" + getDeviceModel() + ")"
        return userAgent
    end if

    return ""
end sub

sub isOSVersionSupport(major, minor) as object
    di = CreateObject("roDeviceInfo")
    osVersion = di.GetOSVersion()
    currentMajorVersion = osVersion.major.toInt()
    currentMinorVersion = osVersion.minor.toInt()

    if currentMajorVersion > major 
        return true
    else currentMajorVersion = major 
        if currentMinorVersion >= minor then return true
    end if

    return false
end sub

function GetVersionOS() as object
    di = CreateObject("roDeviceInfo")
    version = di.GetOSVersion()
    return version.major + "." + version.minor
end function

sub getStatsInfo(typeStats, accountStats, slug) as object
    try
        coins = 0
        expoints = 0
        if typeStats = "account"
            for each userStats in accountStats
                coins += userStats.coins
                expoints += userStats.expoints
            end for
        else if typeStats = "channel"
            channelStats = findInArray(accountStats, "channelSlug", slug)
            if IsValid(channelStats)
                coins = channelStats.coins
                expoints = channelStats.expoints
            end if
        end if

        return {
            "coins": coins.toSTR()
            "expoints": expoints.toSTR()
        }
    catch error
        ? getDataFromError(error)
    end try
end sub

sub convertStrToMD5(value) as string
    try
        byte = CreateObject("roByteArray")
        byte.FromAsciiString(value)
        digest = CreateObject("roEVPDigest")
        digest.Setup("md5")
        digest.Update(byte)
        result = digest.Final()
        return result
    catch error
        ? getDataFromError(error)
    end try
end sub

function timeStringToSeconds(timeString as string) as object
    try
        if timeString = invalid or Len(timeString) < 8 then
            return invalid
        end if

        timeParts = timeString.split(":")

        if timeParts.count() <> 3 then
            return invalid
        end if

        hours = timeParts[0].toInt()
        minutes = timeParts[1].toInt()
        seconds = timeParts[2].toFloat()

        totalTimeInSeconds = hours * 3600 + minutes * 60 + seconds

        return totalTimeInSeconds
    catch error
        ? getDataFromError(error)
    end try
end function

function parseStringToAssocArray(str) as object
    try
        ' "@event='start' or @njfs='start'"
        splitSeparator = [" or ", " and "]
        separator = ""
        strConponents = []
        for each sep in splitSeparator
            strConponents = Split(str, sep)
            if strConponents.count() > 1
                separator = sep
                exit for
            end if
        end for
        params = {}
        removeSubstingsArr = ["'", "@", " ", "="]

        for each item in strConponents
            keyValueComponents = Split(item, "=")
            params[removeSubstrings(keyValueComponents[0], removeSubstingsArr)] = removeSubstrings(keyValueComponents[1], removeSubstingsArr)
        end for
        params["sign"] = removeSubstrings(separator, removeSubstingsArr)
        return params
    catch error
        ? getDataFromError(error)
    end try
end function

function removeSubstrings(str, substrings) as string
    try
        for each substring in substrings
            str = str.replace(substring, "")
        end for
        return str
    catch error
        ? getDataFromError(error)
    end try
end function

function Split(str as string, delimiter as string) as object
    try
        result = []
        startPos = 0
        while true
            endPos = Instr(startPos, str, delimiter)
            if endPos = 0 then
                result.push(Mid(str, startPos))
                exit while
            end if
            result.push(Mid(str, startPos, endPos - startPos))
            startPos = endPos + Len(delimiter)
        end while
        return result
    catch error
        ? getDataFromError(error)
    end try
end function

function InstrITG(str as string, subStr as string) as integer
    try
        ' The function starts looking from the first character
        start = 1
        ' Loop through the string
        for i = start to Len(str)
            ' Check if the substring is found starting at position i
            if Mid(str, i, Len(subStr)) = subStr then
                return i
            end if
        end for
        return 0
    catch error
        ? getDataFromError(error)
    end try
end function

function ExtractPatterns(text as string) as object
    try
        result = []
        openBracePos = Instr(0, text, "{")
        while openBracePos <> 0
            closeBracePos = Instr(openBracePos, text, "}")
            if closeBracePos <> 0
                patternLength = closeBracePos - openBracePos
                pattern = Mid(text, openBracePos + 1, patternLength)
                pattern = pattern.replace("{", "")
                pattern = pattern.replace("}", "")
                result.push(pattern)
                openBracePos = Instr(closeBracePos, text, "{")
            else
                openBracePos = 0
            end if
        end while
        return result
    catch error
        ? getDataFromError(error)
    end try
end function

sub getDataFromError(error) as object
    try
        stingError = FormatJson(error)

        if IsInvalid(m.global.infoSDK.extraSettings) then return "SYSTEM ERROR: -> " + stingError
        if IsString(m.global.infoSDK.extraSettings) and m.global.infoSDK.extraSettings = "" then return "SYSTEM ERROR: -> " + stingError
        settings = ParseJson(m.global.infoSDK.extraSettings)
        if IsInvalid(settings) then return "SYSTEM ERROR: -> " + stingError
        if IsInvalid(settings.logs.level) then return "SYSTEM ERROR: -> " + stingError

        isContains = contains(settings.logs.level, 3)
        if not checkUserID(settings.logs) then return ""
        if isContains or settings.logs.level.count() = 0
            m.global.logsManager.callFunc("sendErrorLog", stingError)
        end if
        return "SYSTEM ERROR: -> " + stingError
    catch error
        print "fail formatted error"
    end try
end sub

sub checkUserID(logs) as object
    try
        allowSendLogs = false
        if isValid(logs.users)
            if isValid(m.global.userData) and isValid(m.global.userData.foreignId)
                for each item in logs.users
                    if item = m.global.userData.foreignId
                        allowSendLogs = true
                    end if
                end for
            end if
            if logs.users.count() = 0 then allowSendLogs = true
        else
            allowSendLogs = true
        end if
        return allowSendLogs
    catch error
        ? getDataFromError(error)
    end try
end sub

sub sendMappingError(error) as object
    try
        if IsInvalid(m.global.infoSDK.extraSettings) or m.global.infoSDK.extraSettings = "" then return "Mapping ERROR: -> " + stingError
        settings = ParseJson(m.global.infoSDK.extraSettings)
        if IsInvalid(settings) then return "Mapping ERROR: -> " + stingError
        if IsInvalid(settings.logs) then return "Mapping ERROR: -> " + stingError
        if IsInvalid(settings.logs.level) then return "Mapping ERROR: -> " + stingError

        stingError = FormatJson(error)
        isContains = contains(settings.level, 2)

        stingError = FormatJson(error)
        if isContains or settings.logs.level.count() = 0
            m.global.logsManager.callFunc("sendMappingErrorLog", stingError)
        end if
        return "Mapping ERROR: -> " + stingError
    catch error
        print "fail formatted error"
    end try
end sub

function GetHostFromUrl(url as string) as string
    try
        startPos = Instr(0, url, "://")
        if startPos = 0 then
            startPos = 1
        else
            startPos = startPos + 3
        end if

        endPos = Instr(startPos, url, "/")
        if endPos = 0 then
            endPos = Len(url) + 1
        end if

        host = Mid(url, startPos, endPos - startPos)
        return host
    catch error
        ? getDataFromError(error)
    end try
end function

function GetFileNameFromUrl(url as string) as string
    try
        lastSlashPosition = 0
        for i = Len(url) to 1 step -1
            if Mid(url, i, 1) = "/" then
                lastSlashPosition = i
                exit for
            end if
        end for

        if lastSlashPosition > 0 then
            fileName = Mid(url, lastSlashPosition + 1)
        else
            fileName = url
        end if

        return fileName
    catch error
        ? getDataFromError(error)
    end try
end function

sub removeTabAndSpacings(value) as object
    try
        if isValid(value) and IsString(value) and isValidUrl(value)
            value = value.replace(chr(10), "")
            value = value.replace(" ", "")
        end if
        return value
    catch error
        ? getDataFromError(error)
    end try
end sub

sub isValidUrl(url) as object
    try
        regex = CreateObject("roRegex", "(?:https?):\/\/(\w+:?\w*)?(\S+)(:\d+)?(\/|\/([\w#!:.?+=&%!\-\/]))?", "i")
        return regex.IsMatch(url)
    catch error
        ? getDataFromError(error)
    end try
end sub

sub getMask(rect) as string
    try
        imageWidth = 1920
        imageHeight = 1080

        bm = CreateObject("roBitmap", { width: imageWidth, height: imageHeight, AlphaEnable: false })
        bm.Clear(&hFFFFFFFF)
        bm.DrawRect(rect.x, rect.y, rect.width, rect.height, &hFF000000)

        ba = bm.GetPng(0, 0, imageWidth, imageHeight)
        ba.WriteFile("tmp:/flexiMask.png")

        return "tmp:/flexiMask.png"
    catch error
        ? getDataFromError(error)
    end try
end sub

sub isGif(uri) as object
    try
        if IsInvalid(uri) then return false
        return uri <> "nil" and Instr(0, uri, ".gif") > 0
    catch error
        ? getDataFromError(error)
    end try
end sub

sub isVideo(uri) as object
    try
        if IsInvalid(uri) then return false
        return uri <> "nil" and (Instr(0, uri, ".mp4") > 0 or Instr(0, uri, ".m3u8") > 0 or Instr(0, uri, ".mov") > 0)
    catch error
        ? getDataFromError(error)
    end try
end sub

sub isDeviceSupportMC() as object
    try
        devices = ["N1000", "N1100", "N1101", "3000X", "3050X", "3100X", "2400X", "4200X", "2710X", "2720X", "3700X", "3710X", "3800X", "3900X", "3930X", "3930EU"]
        deviceModel = getDeviceModel()
        return not containtsInArray(devices, deviceModel)
    catch error
        ? getDataFromError(error)
    end try
end sub

function RemoveLastAmpersand(str as String) as String
    lastIndex = -1
    tempIndex = Instr(1, str, "&")

    while tempIndex > 0
        lastIndex = tempIndex
        tempIndex = Instr(lastIndex + 1, str, "&")
    end while

    if lastIndex > 0 then
        return Left(str, lastIndex - 1) + Mid(str, lastIndex + 1)
    else
        return str 
    end if
end function