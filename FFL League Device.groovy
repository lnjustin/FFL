/**
 *  FFL League Device
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
    definition(name: "FFL League", namespace: "lnjustin", author: "lnjustin", importUrl: "")
    {
        capability "Actuator"
        capability "Switch"
        capability "PushableButton"
        
        attribute "tile1", "string"
        attribute "tile2", "string"
        attribute "tile3", "string"
        attribute "tile4", "string"
        attribute "tile5", "string"
        attribute "tile6", "string" 
        
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

        command(
             "setTileIconColor", 
             [
                [
                     "name":"Set Tile Text Color",
                     "description":"Set the color of the icon(s) on your tile(s). Hex format with leading #).",
                     "type":"ENUM",
                     "constraints": ["black","white"]
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

def on() {
    sendEvent(name: "switch", value: "on")
}

def off() {
    sendEvent(name: "switch", value: "off")
}

def updated()
{
    configure()
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
    parent.setTileTextColor(app.id, color)
}

def setTileIconColor(color) {
    parent.setTileIconColor(app.id, color)
}

def updateDevicesForLeague(leagueDeviceData, teamDevicesData) {
    if (leagueDeviceData.name) {
        logDebug("Setting league device label to " + leagueDeviceData.name)
        device.updateDataValue("label", leagueDeviceData.name)
        device.setName(leagueDeviceData.name)
        device.setDisplayName(leagueDeviceData.name)
    }
    else logDebug("League name is null")

    sendEvent(name: "tile1", value: leagueDeviceData.tile1)
    sendEvent(name: "tile2", value: leagueDeviceData.tile2)
    sendEvent(name: "tile3", value: leagueDeviceData.tile3)
    sendEvent(name: "tile4", value: leagueDeviceData.tile4)
    sendEvent(name: "tile5", value: leagueDeviceData.tile5)
    sendEvent(name: "tile6", value: leagueDeviceData.tile6) 

    teamDevicesData.each { teamId, teamDeviceData ->
        def teamDevice = getTeamDevice(teamDeviceData)
        if (teamDevice) teamDevice.updateDevicesForTeam(teamDeviceData)
    }
}

def createTeamDevices(leagueDeviceData, teamDevicesData) {
    state.leagueId = leagueDeviceData.id
    for (teamDeviceData in teamDevicesData) {
        createTeamDevice(teamDeviceData)
    }
    if (state.teamIDs != null) {
        def newTeamDeviceIds = teamDevicesData.collect {it.id}
        for (existingTeamDeviceId in state.teamIDs) {
            if (!existingTeamDeviceId in newTeamDeviceIds) deleteTeamDevice(existingTeamDeviceId)
        }
    }
    state.teamIDs = teamDevicesData.collect {it.id}
    
}

def getTeamDevice(teamDeviceData) {
    String networkID = getNetworkId(teamDeviceData.id, teamDeviceData.leagueId) 
    def teamDevice = getChildDevice(networkID)
    if (!teamDevice) teamDevice = createTeamDevice(teamDeviceData)
    return teamDevice
}

def createTeamDevice(teamDeviceData)
{
    String networkID = getNetworkId(teamDeviceData.id, teamDeviceData.leagueId) 
    def teamDevice = getChildDevice(networkID)
    if (!teamDevice) {
        teamDevice = addChildDevice("lnjustin", "FFL Team", networkID, [label:teamDeviceData.name, isComponent:true, name: teamDeviceData.name])
        if (teamDevice) {
            parent.updateSetting("parentID", device.id)
        }
        else log.error "Error Creating FFL Team Device"
    }
    if (teamDevice) {
        teamDevice.setLabel(teamDeviceData.name)
        teamDevice.setTeamId(teamDeviceData.id)
    }
    return teamDevice
}

def deleteTeamDevice(teamId)
{
    String networkID = getNetworkId(teamId) 
    deleteChildDevice(networkID)
}

def getNetworkId(teamId, leagueId = null) {
    def league = leagueId ?: state.leagueId
    String networkID = "fflTeamDevice" + league + "-" + teamId
    return networkID
}

def deleteAllTeamDevices() {
    for (child in getChildDevices())
    {
        deleteChildDevice(child.deviceNetworkId)
    }
}

def pushButton(buttonNum) {
    sendEvent(name: "pushed", value: buttonNum, isStateChange: true)
}

def push(buttonNum) {
    sendEvent(name: "pushed", value: buttonNum, isStateChange: true)
}

def refresh()
{

}
