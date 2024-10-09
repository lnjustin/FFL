/**
 *  FFL
 *
 *  Copyright 2024 lnjustin
 *
 *  Licensed Virtual the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Change History:
 *  v1.0.0 - initial alpha release
 *  v1.0.1 - update bug fix
 *  v1.0.2 - ingame update bug fix
 *  v1.1.0 - Added league/team child devices, in-play stats
 *  v1.1.1 - Fix tile color commands; add advanceWeek() call to initialize
 */
import java.text.SimpleDateFormat
import groovy.transform.Field

definition(
    name: "FFL",
    namespace: "lnjustin",
    author: "lnjustin",
    description: "Fantasy Football Integration",
    category: "My Apps",
    oauth: [displayName: "FFL", displayLink: ""],
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"
)

@Field String checkMark = "https://raw.githubusercontent.com/lnjustin/App-Images/master/checkMark.svg"
@Field String xMark = "https://raw.githubusercontent.com/lnjustin/App-Images/master/xMark.svg"

preferences {
    page name: "mainPage", title: "", install: true, uninstall: false
    page name: "removePage", title: "", install: false, uninstall: true
}

def mainPage() {
    dynamicPage(name: "mainPage") {
    	    installCheck()
		    if(state.appInstalled == 'COMPLETE'){   
                section() {
                    header()
                }    
                section(getInterface("header", " FFL Instances")) {
                    app(name: "anyOpenApp", appName: "FFL Instance", namespace: "lnjustin", title: "<b>Add a new FFL Instance</b>", multiple: true)
                }
                section("") { 
                    paragraph getInterface("note", txt="After installing or updating your team(s) above, be sure to click the DONE button below.")
                }
			    section (getInterface("header", " General Settings")) {
                    input("refreshUponDST", "bool", title: "Refresh Upon Daylight Savings Time?", defaultValue: true, required: false)
                    input("debugOutput", "bool", title: "Enable debug logging?", defaultValue: true, displayDuringSetup: false, required: false)
		        }
            }
            section("") {
                href(name: "removePage", title: getInterface("boldText", "Remove FFL"), description: "", required: false, page: "removePage")
                
                footer()
            }
    }
}

String logo(String width='75') {
    return '<img width="' + width + 'px" style="display: block;margin-left: auto;margin-right: auto;margin-top:0px;" border="0" src="' + getLogoPath() + '">'
}

def header() {
    paragraph logo('90')
}

def getLogoPath() {
    return "https://github.com/lnjustin/App-Images/blob/master/GameTime/GameTime.png?raw=true"
}

def footer() {
    paragraph getInterface("line", "") + '<div style="display: block;margin-left: auto;margin-right: auto;text-align:center"><img width="25px" border="0" src="' + getLogoPath() + '"> &copy; 2024 lnjustin.<br>'
}

def removePage() {
	dynamicPage(name: "removePage", title: "Remove FFL", install: false, uninstall: true) {
		section ("WARNING!\n\nRemoving FFL will remove all FFL Devices\n") {
            header() 
		}
	}
}

def settingUpdate(settingName, value, type) {
    app.updateSetting(settingName, value)
}

def getTextColorSetting() {
    return (textColor) ? textColor : "#000000"
}

def getFontSizeSetting() {
    return fontSize != null ? fontSize : 100
}

def installed() {
	initialize()
}

def updated() {
    unschedule()
	unsubscribe()
	initialize()
}

def uninstalled() {
    deleteAllDevices()
	logDebug "Uninstalled app"
}

def initialize() {
    createParentDevice()
    childApps.each { child ->
        child.updated()                
    }
}

def updateTileTextColor(color) {
    childApps.each { child ->
     //   child.settingUpdate("textColor", color, "string")   
        child.updateSetting("textColor", [value: color, type: "string"]) 
        child.update()             
    }
}

def updateTileIconColor(color) {
    childApps.each { child ->
        child.updateSetting("iconColor", [value: color, type: "enum"]) 
        child.update() 
    } 
}

def refreshChildApps() {
    childApps.each { child ->
        child.update()                
    }
}

def fullUpdate(appID) {
    childApps.each { child ->
        if (child.id == appID) {
            child.update()                
        }
    }
}

def installCheck(){
	state.appInstalled = app.getInstallationState() 
	if(state.appInstalled != 'COMPLETE'){
		section{paragraph "Please hit 'Done' to install '${app.label}' parent app "}
  	}
  	else{
    	log.info "Parent Installed"
  	}
}

def getParentDevice() {
    def parent = getChildDevice("fflParentDevice${app.id}")
    if (!parent) parent = createParentDevice()
    return parent
}

def createParentDevice()
{
    def parent = getChildDevice("fflParentDevice${app.id}")
    if (!parent) {
        String parentNetworkID = "fflParentDevice${app.id}"
        parent = addChildDevice("lnjustin", "FFL", parentNetworkID, [label:"FFL", isComponent:true, name:"FFL"])
        if (parent) {
            parent.updateSetting("parentID", app.id)
            logDebug("Created FFL Parent Device")
        }
        else log.error "Error Creating FFL Parent Device"
    }
    return parent
}

def deleteAllDevices() 
{
    def parent = getParentDevice()
    parent?.deleteAllDevices()
    deleteChildDevice("fflParentDevice${app.id}")
}

def deleteDevicesForLeague(leagueId) {
    def parent = getParentDevice()
    parent?.deleteDevicesForLeague(leagueId)
}

def deleteChildrenDevicess()
{
    def parent = getChildDevice("fflParentDevice${app.id}")
    if (parent) {
        parent.deleteChildren()
    }
    else log.error "No Parent Device Found. No child devices deleted."    
}

def deleteChildDevice(appID = null) {
    def parent = getChildDevice("fflParentDevice${app.id}")
    if (parent) {
        parent.deleteChild(appID)
    }
    else log.error "No Parent Device Found."
}

def createDevices(leagueDeviceData, teamDevicesData) {

    def parent = getParentDevice()
    parent.createDevices(leagueDeviceData, teamDevicesData)
}

def setTileTextColor(appId, color) {
    childApps.each { child ->
        if (child.id == appID) {
            child.settingUpdate("textColor", color, "string")     
            child.update()           
        }
    }
}

def setTileIconColor(appId, color) {
    childApps.each { child ->
        if (child.id == appID) {
            child.settingUpdate("iconColor", color, "string")     
            child.update()           
        }
    }
}

def updateDevicesForLeague(leagueDeviceData, teamDevicesData) {
    def parent = getParentDevice()
    if (parent) {
        parent.updateDevicesForLeague(leagueDeviceData, teamDevicesData)
    }
    else log.error "No Parent Device Found with ID=I" + "fflParentDevice${app.id}" + " When Attempting to Update Devices for League. "    
}

def pushDeviceButton(appID, buttonNum) {
    def parent = getChildDevice("fflParentDevice${app.id}")
    if (parent) {
        parent.pushChildDeviceButton(appID, buttonNum)
    }
    else log.error "No Parent Device Found."    
}

def logDebug(msg) {
    if (settings?.debugOutput) {
		log.debug msg
	}
}

def getInterface(type, txt="", link="") {
    switch(type) {
        case "line": 
            return "<hr style='background-color:#555555; height: 1px; border: 0;'></hr>"
            break
        case "header": 
            return "<div style='color:#ffffff;font-weight: bold;background-color:#555555;border: 1px solid;box-shadow: 2px 3px #A9A9A9'> ${txt}</div>"
            break
        case "error": 
            return "<div style='color:#ff0000;font-weight: bold;'>${txt}</div>"
            break
        case "note": 
            return "<div style='color:#333333;font-size: small;'>${txt}</div>"
            break
        case "subField":
            return "<div style='color:#000000;background-color:#ededed;'>${txt}</div>"
            break     
        case "subHeader": 
            return "<div style='color:#000000;font-weight: bold;background-color:#ededed;border: 1px solid;box-shadow: 2px 3px #A9A9A9'> ${txt}</div>"
            break
        case "subSection1Start": 
            return "<div style='color:#000000;background-color:#d4d4d4;border: 0px solid'>"
            break
        case "subSection2Start": 
            return "<div style='color:#000000;background-color:#e0e0e0;border: 0px solid'>"
            break
        case "subSectionEnd":
            return "</div>"
            break
        case "boldText":
            return "<b>${txt}</b>"
            break
        case "link":
            return '<a href="' + link + '" target="_blank" style="color:#51ade5">' + txt + '</a>'
            break
    }
} 

