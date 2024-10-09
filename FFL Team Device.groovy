/**
 *  FFL Team Device
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
    definition(name: "FFL Team", namespace: "lnjustin", author: "lnjustin", importUrl: "")
    {
        capability "Actuator"
        capability "Switch"
        capability "PushableButton"
        
        attribute "tile", "string"
        attribute "startingInjuredPlayer", "string" // bool
        attribute "minsLeft", "number"
        attribute "currentlyPlaying", "number"
        attribute "yetToPlay", "number"
        attribute "points", "number"
        attribute "projectedPoints", "number"

        attribute "opponentMinsLeft", "number"
        attribute "opponentCurrentlyPlaying", "number"
        attribute "opponentYetToPlay", "number"
        attribute "opponentPoints", "number"
        attribute "opponentProjectedPoints", "number"
        
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

def setTeamId(teamId) {
    state.teamId = teamId
}

def updateDevicesForTeam(teamDeviceData) {  
     if (teamDeviceData.team?.name) {
        device.setDisplayName(teamDeviceData.team?.name)
        device.setName(teamDeviceData.team?.name)
    }

    sendEvent(name: "tile", value: teamDeviceData.tile)
    sendEvent(name: "startingInjuredPlayer", value: teamDeviceData.team.startingInjuredPlayer)
    sendEvent(name: "tile", value: teamDeviceData.tile)

    def matchup = teamDeviceData.matchup
    if (matchup && matchup.home.teamId == state.teamId) {
        sendEvent(name: "minsLeft", value: matchup.home.minsLeft)
        sendEvent(name: "currentlyPlaying", value: matchup.home.numCurrentlyPlaying)
        sendEvent(name: "yetToPlay", value: matchup.home.numYetToPlay)
        sendEvent(name: "points", value: matchup.home.totalPointsLive ?: matchup.home.totalPoints)
        sendEvent(name: "projectedPoints", value: matchup.home.totalProjectedPointsLive ?: matchup.home.projectedScore)
        sendEvent(name: "opponentMinsLeft", value: matchup.away.minsLeft)
        sendEvent(name: "opponentCurrentlyPlaying", value: matchup.away.numCurrentlyPlaying)
        sendEvent(name: "opponentYetToPlay", value: matchup.away.numYetToPlay)
        sendEvent(name: "opponentPoints", value: matchup.away.totalPointsLive ?: matchup.away.totalPoints)
        sendEvent(name: "opponentProjectedPoints", value: matchup.away.totalProjectedPointsLive ?: matchup.away.projectedScore)

        if (matchup.home.numCurrentlyPlaying > 0) sendEvent(name: "switch", value: "on")
        else sendEvent(name: "switch", value: "off")
    }
    else if (matchup && matchup.away.teamId == state.teamId) {
        sendEvent(name: "minsLeft", value: matchup.away.minsLeft)
        sendEvent(name: "currentlyPlaying", value: matchup.away.numCurrentlyPlaying)
        sendEvent(name: "yetToPlay", value: matchup.away.numYetToPlay)
        sendEvent(name: "points", value: matchup.away.totalPointsLive ?: matchup.away.totalPoints)
        sendEvent(name: "projectedPoints", value: matchup.away.totalProjectedPointsLive ?: matchup.away.projectedScore)
        sendEvent(name: "opponentMinsLeft", value: matchup.home.minsLeft)
        sendEvent(name: "opponentCurrentlyPlaying", value: matchup.home.numCurrentlyPlaying)
        sendEvent(name: "opponentYetToPlay", value: matchup.home.numYetToPlay)
        sendEvent(name: "opponentPoints", value: matchup.home.totalPointsLive ?: matchup.home.totalPoints)
        sendEvent(name: "opponentProjectedPoints", value: matchup.home.totalProjectedPointsLive ?: matchup.home.projectedScore)

        if (matchup.away.numCurrentlyPlaying > 0) sendEvent(name: "switch", value: "on")
        else sendEvent(name: "switch", value: "off")
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
