/**
 *  FFL Child
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
    definition(name: "FFL Child", namespace: "lnjustin", author: "lnjustin", importUrl: "")
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

def updateDevice(appID, data) {
    state.appID = appID    
    sendEvent(name: "tile", value: "")
    sendEvent(name: "tile1", value: data.tile1)
    sendEvent(name: "tile2", value: data.tile2)
    sendEvent(name: "tile3", value: data.tile3)
    sendEvent(name: "tile4", value: data.tile4)
    sendEvent(name: "tile5", value: data.tile5)
    sendEvent(name: "tile6", value: data.tile6)
    sendEvent(name: "switch", value: data.switchValue)
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
