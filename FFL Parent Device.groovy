/**
 *  FFL Parent
 *
 *  Copyright\u00A9 2024 lnjustin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Change History in Parent App
**/

metadata
{
    definition(name: "FFL", namespace: "lnjustin", author: "lnjustin", importUrl: "")
    {
        capability "Actuator"
        capability "Switch"
        
        attribute "tile", "string" 
        
        command(
             "setTileTextColor", 
             [
                [
                     "name":"Set Tile Text Color",
                     "description":"Set the color of the text on your tile(s). Hex format with leading #).",
                     "type":"text"
                ]
             ]
        )
    }
}

preferences
{
    section
    {
        input name: "logEnable", type: "bool", title: "Enable debug logging", defaultValue: true
    }
}

def logDebug(msg) 
{
    if (logEnable)
    {
        log.debug(msg)
    }
}    

def updated()
{
    configure()
}

def uninstalled()
{
    logDebug("FFL Parent Device: uninstalled()")
    deleteChildren()
}

def parse(String description)
{
    logDebug(description)
}

def configure()
{    
    refresh()
}

def setTileTextColor(color) {
    parent?.settingUpdate("textColor", color, "string") 
}

def setTileIconColor(color) {
    parent?.settingUpdate("iconColor", color, "string") 
}

def on() {
    sendEvent(name: "switch", value: "on")
}

def off() {
    sendEvent(name: "switch", value: "off")
}



def pushChildDeviceButton(appID, buttonNum) {
    def child = getChildDevice("fflChildDevice${appID}")
    if (child) {
        child.pushButton(buttonNum)
    }
    else log.error "No Child Device for app ${appID} found"    
}

def getSecondsBetweenDates(Date startDate, Date endDate) {
    try {
        def difference = endDate.getTime() - startDate.getTime()
        return Math.round(difference/1000)
    } catch (ex) {
        log.error "getSecondsBetweenDates Exception: ${ex}"
        return 1000
    }
}

def updateDevicesForLeague(leagueDeviceData, teamDevicesData) {
    def leagueDevice = getLeagueDevice(leagueDeviceData)
    if (leagueDevice) {
        leagueDevice.updateDevicesForLeague(leagueDeviceData, teamDevicesData)
    }
    else log.error "Error Updating FFL League Device"    
}

def createDevices(leagueDeviceData, teamDevicesData) {
    def leagueDevice = getLeagueDevice(leagueDeviceData)
    if (leagueDevice) {
        leagueDevice.createTeamDevices(leagueDeviceData, teamDevicesData)
    }
    else log.error "Error Creating FFL League Devices"
}

def getLeagueDevice(leagueDeviceData) {
    def leagueDevice = getChildDevice("fflLeagueDevice" + leagueDeviceData.id)
    if (!leagueDevice) leagueDevice = createLeagueDevice(leagueDeviceData)
    return leagueDevice
}

def createLeagueDevice(leagueDeviceData)
{
    String networkID = "fflLeagueDevice" + leagueDeviceData.id
    def leagueDevice = getChildDevice(networkID)
    if (!leagueDevice) {
        leagueDevice = addChildDevice("lnjustin", "FFL League", networkID, [label:leagueDeviceData.name, isComponent:true, name: leagueDeviceData.name])
        if (leagueDevice) {
            leagueDevice.updateSetting("parentID", device.id)
        }
        else log.error "Error Creating FFL League Device"
    }
    if (leagueDevice) {
        leagueDevice.setLabel(leagueDeviceData.name)
    }
    return leagueDevice
}

def deleteDevicesForLeague(leagueId) {
    def leagueDevice = getChildDevice("fflLeagueDevice" + leagueId)
    if (leagueDevice) {
        leagueDevice.deleteAllTeamDevices()
        deleteChildDevice("fflLeagueDevice" + leagueId)
    }  
}

def deleteAllDevices() {
    for(leagueDevice in getChildDevices())
    {
        leagueDevice.deleteAllTeamDevices()
        deleteChildDevice(leagueDevice.deviceNetworkId)
    }    
}

def deleteChild(appID)
{
    deleteChildDevice("fflChildDevice${appID}")
}

def deleteChildren()
{
    for(child in getChildDevices())
    {
        deleteChildDevice(child.deviceNetworkId)
    }
}

def refresh()
{

}

