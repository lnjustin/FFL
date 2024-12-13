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
        
        attribute "matchup1", "string"
        attribute "matchup2", "string"
        attribute "matchup3", "string"
        attribute "matchup4", "string"
        attribute "matchup5", "string"
        attribute "matchup6", "string" 

        attribute "awards", "string" 
        attribute "ranking", "string" 
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

def updateDevicesForLeague(leagueDeviceData, teamDevicesData) {
    if (leagueDeviceData.name) {
        logDebug("Setting league device label to " + leagueDeviceData.name)
        device.updateDataValue("label", leagueDeviceData.name)
        device.setName(leagueDeviceData.name)
        device.setDisplayName(leagueDeviceData.name)
    }
    else logDebug("League name is null")

    sendEvent(name: "matchup1", value: leagueDeviceData.matchup1)
    sendEvent(name: "matchup2", value: leagueDeviceData.matchup2)
    sendEvent(name: "matchup3", value: leagueDeviceData.matchup3)
    sendEvent(name: "matchup4", value: leagueDeviceData.matchup4)
    sendEvent(name: "matchup5", value: leagueDeviceData.matchup5)
    sendEvent(name: "matchup6", value: leagueDeviceData.matchup6) 

    sendEvent(name: "awards", value: leagueDeviceData.awards) 
    sendEvent(name: "ranking", value: leagueDeviceData.ranking) 

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
