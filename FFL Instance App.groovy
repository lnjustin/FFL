/**
 *  FFL Instance
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
 *  Change History in Parent App
 */
import java.text.SimpleDateFormat
import groovy.transform.Field
import groovy.time.TimeCategory
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

definition(
    name: "FFL Instance",
    parent: "lnjustin:FFL",
    namespace: "lnjustin",
    author: "lnjustin",
    description: "FFL Instance",
    category: "My Apps",
    oauth: [displayName: "FFL", displayLink: ""],
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"
)

@Field String checkMark = "https://raw.githubusercontent.com/lnjustin/App-Images/master/checkMark.svg"
@Field String xMark = "https://raw.githubusercontent.com/lnjustin/App-Images/master/xMark.svg"
@Field String logo = "https://raw.githubusercontent.com/lnjustin/App-Images/master/FFL/ffllogo.png"
@Field String injuryIcon = "https://raw.githubusercontent.com/lnjustin/App-Images/master/FFL/first-aid-box.png"
@Field String projectedIcon = "https://raw.githubusercontent.com/lnjustin/App-Images/master/FFL/projectedIcon.png"
@Field String projectedIconLight = "https://raw.githubusercontent.com/lnjustin/App-Images/master/FFL/projectedIcon_light.png"
@Field String inPlayIcon = "https://github.com/lnjustin/App-Images/raw/refs/heads/master/FFL/Button-Green.svg"
@Field String minsLeftIconLight = "https://raw.githubusercontent.com/lnjustin/App-Images/master/FFL/stopwatchWhite.png"
@Field String minsLeftIcon = "https://raw.githubusercontent.com/lnjustin/App-Images/master/FFL/stopwatchBlack.png"
@Field String byeWeekIconLight = "https://raw.githubusercontent.com/lnjustin/App-Images/master/FFL/byeWeekLight.png"
@Field String byeWeekIcon = "https://raw.githubusercontent.com/lnjustin/App-Images/master/FFL/byeWeek.png"

@Field daysOfWeekList = ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"]
@Field daysOfWeekShortMap = ["Sunday":"SUN", "Monday":"MON", "Tuesday":"TUE", "Wednesday":"WED", "Thursday":"THU", "Friday":"FRI", "Saturday":"SAT"]
@Field daysOfWeekMap = ["Sunday":1, "Monday":2, "Tuesday":3, "Wednesday":4, "Thursday":5, "Friday":6, "Saturday":7]
@Field months = ["JAN","FEB","MAR","APR","MAY","JUN","JUL","AUG","SEP","OCT","NOV","DEC"]
@Field MINS_PER_PRO_GAME = 60
@Field MINS_PER_PRO_GAME_PERIOD = 15

mappings
{
    path("/ffl/leagueMatchupTile/:appId/:tileNum") { action: [ GET: "fetchLeagueMatchupTile"] }
    path("/ffl/teamMatchupTile/:appId/:teamId") { action: [ GET: "fetchTeamMatchupTile"] }
    path("/ffl/teamRosterTile/:appId/:teamId") { action: [ GET: "fetchTeamRosterTile"] }
}

def getLeagueMatchupTileEndpoint(tileNum) {
    return getFullApiServerUrl() + "/ffl/leagueMatchupTile/${app.id}/${tileNum}?access_token=${state.accessToken}"    
}

def getTeamMatchupTileEndpoint(teamId) {
    return getFullApiServerUrl() + "/ffl/teamMatchupTile/${app.id}/${teamId}?access_token=${state.accessToken}"    
}

def getTeamRosterTileEndpoint(teamId) {
    return getFullApiServerUrl() + "/ffl/teamRosterTile/${app.id}/${teamId}?access_token=${state.accessToken}"    
}

def getUpdateInterval() {
    return settings['updateInterval'] != null ? settings['updateInterval']*60 : 600
}

def instantiateToken() {
     if(!state.accessToken){	
         //enable OAuth in the app settings or this call will fail
         createAccessToken()	
     }   
}

preferences {
    page name: "mainPage", title: "", install: true, uninstall: true
}

def mainPage() {
    dynamicPage(name: "mainPage") {
            section {            
                header()
                paragraph getInterface("header", " League Setup")

                input(name:"leagueId", type: "text", title: "ESPN League ID", required:true, submitOnChange: true)
                input(name:"swidCookie", type: "text", title: "SWID Cookie", required:true, submitOnChange: true)
                input(name:"espnS2Cookie", type: "text", title: "espn_S2 Cookie", required:true, submitOnChange: true)

            }
            if (leagueId && swidCookie && espnS2Cookie) {
                def teamsEnum = getTeamNamesEnum()           
                section (getInterface("header", " Teams Setup")) {
                    input("followedTeams", "enum", title: "Select Team(s) to Follow", options: teamsEnum, required: true, multiple: true, submitOnChange: true)
                    
                }
                section (getInterface("header", " Week Setup")) {
                    input name: "dayOfWeekToAdvanceWeek", title: "Day on which to Advance FFL Week", type: "enum", options: ["Tuesday", "Wednesday", "Thursday"], width: 6
                    input name: "timeOfDayToAdvanceWeek", title: "Time of Day at which to Advance FFL Week", type: "time", width: 6
                }
                section (getInterface("header", " Matchup Tile Setup")) {     
                    input("showTeamName", "bool", title: "Show Team Name?", defaultValue: false, displayDuringSetup: false, required: false)
                    input("showTeamOwnerFirstName", "bool", title: "Show Team Owner First Name?", defaultValue: false, displayDuringSetup: false, required: false)
                    input("showTeamOwnerLastName", "bool", title: "Show Team Owner Last Name?", defaultValue: false, displayDuringSetup: false, required: false)
                    input("showMinsLeft", "bool", title: "Show Minutes Left?", defaultValue: false, displayDuringSetup: false, required: false)
                    input("showTeamRecord", "bool", title: "Show Team Record After Matchup Final?", defaultValue: true, displayDuringSetup: false, required: false)
                    input(name:"scoreFontSize", type: "number", title: "Matchup Tile Font Size for Earned Score (%)", required:true, defaultValue:100, width: 6)
                    input(name:"projectedScoreFontSize", type: "number", title: "Matchup Tile Font Size for Projected Score (%)", required:true, defaultValue:100, width: 6)
                    input(name:"teamInfoFontSize", type: "number", title: "Matchup Tile Font Size for Team Info (%)", required:true, defaultValue:100, width: 6)
                    input("textColor", "text", title: "Matchup Tile Text Color (Hex format with leading #)", defaultValue: '#000000', displayDuringSetup: false, required: false, width: 6)
                    input("iconColor", "enum", title: "Matchup Tile Icon Color (Hex format with leading #)", defaultValue: 'black', options: ["black", "white"], displayDuringSetup: false, required: false, width: 6)
                }
                section (getInterface("header", " Roster Tile Setup")) {     
                    input(name:"rosterFontSize", type: "number", title: "Roster Tile Font Size (%)", required:true, defaultValue:100, width: 6)
                    input("rosterTextColor", "text", title: "Roster Tile Text Color (Hex format with leading #)", defaultValue: '#000000', displayDuringSetup: false, required: false, width: 6)
                    input("rosterRowColor1", "text", title: "Roster Tile Row Color 1 (Hex format with leading #)", defaultValue: '#989898', displayDuringSetup: false, required: false, width: 6)
                    input("rosterRowColor2", "text", title: "Roster Tile Row Color 2 (Hex format with leading #)", defaultValue: '#767676', displayDuringSetup: false, required: false, width: 6)
                }
            }
            section (getInterface("header", " General Settings")) {                
                label title: "FFL Instance Name", required:false, submitOnChange:true
                input name: "updateFrequencyOutOfGame", title: "How Often to Update When No Game Is Ongoing (minutes)", type: "number", required: true, description: "30+ Minutes Recommended" 
                input name: "updateFrequencyInGame", title: "How Often to Update When Game Is Ongoing For Teams Selected To Show On Matchup Tile (minutes)", type: "number", required: true, description: "5-10 Minutes Acceptable" 
                input name: "decimalPlaces", title: "Decimal Places", type: "enum", required: true, options: [0 , 1, 2]
			    input("debugOutput", "bool", title: "Enable debug logging?", defaultValue: true, displayDuringSetup: false, required: false)
                input name: "disabled", title:"Manually Disable?", type:"bool", required:false, submitOnChange:false
		    }
            section("") {
                
                footer()
            }
    }
}

String logo(String width='75') {
    return '<img width="' + width + 'px" style="display: block;margin-left: auto;margin-right: auto;margin-top:0px;" border="0" src="' + getLogoPath() + '">'
}

Integer getdecimalPlaceSetting() {
    return decimalPlaces != null ? (decimalPlaces as Integer) : 1
}

def header() {
    paragraph logo('120')
}

def getTextColorSetting(type) {
    if (type == "teamInfo") return (teamInfoTextColor) ? teamInfoTextColor : "#000000"
    else if (type == "score") return (scoreTextColor) ? scoreTextColor : "#000000"
    else if (type == "projectedScore") return (projectedScoreTextColor) ? projectedScoreTextColor : "#000000"
}

def getRosterTextColorSetting() {
    return rosterTextColor ?: "#000000"
}

def getTextColorSetting() {
    return textColor ?: "#000000"
}

def getRosterFontSizeSetting() {
    return rosterFontSize ?: 100
}

def getFontSizeSetting(type) {
    if (type == "teamInfo") return teamInfoFontSize != null ? teamInfoFontSize : 100
    else if (type == "score") return scoreFontSize != null ? scoreFontSize : 100
    else if (type == "projectedScore") return projectedScoreFontSize != null ? projectedScoreFontSize : 100
}

def getLogoPath() {
    return logo
}

def footer() {
    paragraph getInterface("line", "") + '<div style="display: block;margin-left: auto;margin-right: auto;text-align:center"><img width="25px" border="0" src="' + getLogoPath() + '"> &copy; 2024 lnjustin.<br>'
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
    deleteDevices()
	logDebug "Uninstalled app"
}

def initialize() {
    instantiateToken()
    if (!settings["disabled"] && leagueId && swidCookie && espnS2Cookie) {
        createDevices()
        advanceWeek()
        updateOutOfGame()
        scheduleWeekAdvance()
        scheduleUpdateAtGametimes()
    }
}

def scheduleWeekAdvance() {
    if (timeOfDayToAdvanceWeek && dayOfWeekToAdvanceWeek) {
        def advanceTime = toDateTime(timeOfDayToAdvanceWeek)
        def hour = advanceTime.format("H")
        def min = advanceTime.format("m")
        def advanceDay = daysOfWeekShortMap[dayOfWeekToAdvanceWeek]
        def advanceChron = "0 ${min} ${hour} ? * ${advanceDay}"
        schedule(advanceChron, advanceWeek)
    }
}

def updateOutOfGame() {
    update()
    runIn(updateFrequencyOutOfGame*60, updateOutOfGame)
}

def scheduleUpdateAtGametimes() {
    def now = new Date()

    def allProTeams = []
    def selectedTeamIDs = followedTeams.collect {it as Integer}
    for (teamId in selectedTeamIDs) {
        def team = state.teams[teamId]
        def proTeams = team.roster.collect {it.proTeamId}
        allProTeams.addAll(proTeams)
    }

    def gameStartTimesEpoch = []
    def proSchedules = fetchProSchedules()
    if (proSchedules) {
        def proTeams = proSchedules?.settings?.proTeams
        for (team in proTeams) {
            def gamesByTeam = team.proGamesByScoringPeriod
            for (teamGames in gamesByTeam) {
                teamGames.eachWithIndex { scoringPeriodId, gameDataArray, index ->
                def gameData = gameDataArray[0]
                    def date = new Date(gameData.date)
                    if (date.after(now) && gameData.scoringPeriodId as Integer == state.scoringPeriod && gameData.startTimeTBD == false && (gameData.awayProTeamId in allProTeams || gameData.homeProTeamId in allProTeams)) {
                        gameStartTimesEpoch.add(gameData.date)
                        def home = PRO_TEAM_MAP[gameData.homeProTeamId]
                        def away = PRO_TEAM_MAP[gameData.awayProTeamId]
                    }
                }
            }
        }
    }
    else logDebug("Warning: no proSchedules fetched")

    gameStartTimesEpoch = gameStartTimesEpoch.unique()

    def gameStartTimes = []
    for (startTime in gameStartTimesEpoch) {
        def date = new Date(startTime)
        def offsetDate = adjustDateByMins(date, 1)  // update 1 minute after game time, to ensure updated
        gameStartTimes.add(offsetDate)
    }
   // logDebug("game start times: " + gameStartTimes)

    for (startTime in gameStartTimes) {
        if (startTime.after(now)) runOnce(startTime, update, [overwrite: false])
    }
}

def getProTeamGame(proTeams, proTeamId, scoringPeriod) {
    def team = proTeams.find { it.id == proTeamId }
    if (team) {
        if (team.byeWeek == scoringPeriod) return null
        def games = team.proGamesByScoringPeriod
        def game = games.find { it.scorePeriodId == scoringPeriod }
        return game
    }
    else log.debug("No Game Data Found for proTeamId = " + proTeamId)
}

def update() {
    logDebug("Update()...")
    def leagueData = fetchLeague()
    if (leagueData) {
        state.leagueName = leagueData.settings?.name
        state.teamCount = leagueData.size
        state.scoringType = leagueData.scoringSettings?.scoringType
        state.lineupSlotCounts = leagueData.settings?.rosterSettings?.lineupSlotCounts

        state.scoringPeriod = leagueData.scoringPeriodId as Integer
        state.finalScoringPeriod = leagueData.status?.finalScoringPeriod as Integer
        state.week = (state.scoringPeriod <= state.finalScoringPeriod) ? state.scoringPeriod : state.finalScoringPeriod
        state.seasonId = leagueData.seasonId
        state.currentMatchupPeriod = leagueData.status?.currentMatchupPeriod
        

        def members = [:]
        leagueData.members.each { member ->
            def thisMember = [:]
            thisMember.id = member.id 
            thisMember.firstName = titleCase(member.firstName)
            thisMember.lastName = titleCase(member.lastName)
            thisMember.email = member.displayName
            members[member.id] = thisMember
        }
        state.members = members

        def teams = []
        leagueData.teams.each { team ->
            def thisTeam = [:]
            thisTeam.id = team.id
            thisTeam.name = team.name
            thisTeam.abbrev = team.abbrev
            thisTeam.logo = team.logo
            thisTeam.divisionId = team.divisionId
            thisTeam.record = [wins: team.record?.overall?.wins, losses: team.record?.overall?.losses, ties: team.record?.overall?.ties]
            thisTeam.totalPointsFor = team.record?.overall?.pointsFor
            thisTeam.totalPointsAgainst = team.record?.overall?.pointsAgainst
            thisTeam.playoffSeed = team.playoffSeed
            thisTeam.rankFinal = team.rankFinal
            thisTeam.currentProjectedRank = team.currentProjectedRank
            thisTeam.startingInjuredPlayer = false

            def owners = [:]
            thisTeam.owners.each { memberID ->
                owners[memberID] = members[memberID]
            }
            thisTeam.primaryOwner = members[team.primaryOwner]

            thisTeam.roster = []
            leagueData.teams.each { rosterTeam ->
                if (rosterTeam.id && rosterTeam.id == team.id) {
                    def rosterEntries = rosterTeam.roster.entries
                    rosterEntries.each { player ->
                        def thisPlayer = [:]
                        thisPlayer.injuryStatus = player.injuryStatus
                        thisPlayer.lineupSlotId = player.lineupSlotId
                        thisPlayer.slot = SLOT_MAP[thisPlayer.lineupSlotId]
                        thisPlayer.lineupLocked = player.playerPoolEntry.lineupLocked
                        thisPlayer.rank = [ppr: player.playerPoolEntry.player.draftRanksByRankType.PPR.rank, standard: player.playerPoolEntry.player.draftRanksByRankType.STANDARD.rank]
                        thisPlayer.fullName = player.playerPoolEntry.player.fullName
                        thisPlayer.firstName = player.playerPoolEntry.player.firstName
                        thisPlayer.lastName = player.playerPoolEntry.player.lastName
                        thisPlayer.id = player.playerId
                        thisPlayer.positionId = player.playerPoolEntry.player.defaultPositionId
                        thisPlayer.position = POSITION_MAP[thisPlayer.positionId]
                        thisPlayer.headshot = getPlayerHeadshotUrl(player.playerId)
                        thisPlayer.proTeamId = player.playerPoolEntry.player.proTeamId
                        thisPlayer.proTeamName = PRO_TEAM_MAP[thisPlayer.proTeamId]
                        thisPlayer.injured = player.playerPoolEntry?.player?.injured
                        thisPlayer.injuryStatus2 = player.playerPoolEntry?.player?.injuryStatus
                        thisPlayer.stats = [:]
                        def stats = player.playerPoolEntry?.player?.stats
                        for (stat in stats) {
                            thisPlayer.stats[stat.scoringPeriodId] = [:]
                            if (stat.statSourceId == 1) thisPlayer.stats[stat.scoringPeriodId].projectedPoints = stat.appliedTotal
                            else if (stat.statSourceId == 0) thisPlayer.stats[stat.scoringPeriodId].actualPoints = stat.appliedTotal
                        }
                        thisTeam.roster.add(thisPlayer)

                         if (thisPlayer.slot != 'BE' && thisPlayer.slot != 'IR') {
                            if (thisPlayer.injuryStatus == "OUT") thisTeam.startingInjuredPlayer = true
                            if (thisPlayer.injuryStatus2 == "OUT") thisTeam.startingInjuredPlayer = true
                            if (thisPlayer.injured == true && thisPlayer.stats && thisPlayer.stats[state.scoringPeriod]?.projectedPoints == 0) thisTeam.startingInjuredPlayer = true
                         }
                    }
                }
            }
            
            teams[team.id] = thisTeam
        }
        state.teams = teams
    }
    else logDebug("Warning: No League Data Retrieved")

    def teamsInPlay = []

    def liveScoringResult = fetchLiveScoring()
    def scoreboardData = fetchScoreboard()
    if (liveScoringResult && scoreboardData) {
        def scoreboard = []
        def matchupIdMap = liveScoringResult.schedule
        scoreboardData.schedule.eachWithIndex { matchup, index ->
            def thisMatchup = [:]
            def matchupPeriodId = matchupIdMap[index]?.matchupPeriodId
            thisMatchup.matchupPeriod = matchupPeriodId

            if (matchup.away) {
                thisMatchup.away = [:]
                thisMatchup.away.teamId = matchup.away.teamId
                thisMatchup.away.totalPoints = matchup.away.totalPoints
                if (matchup.away.totalPointsLive) thisMatchup.away.totalPointsLive = matchup.away.totalPointsLive
                if (matchup.away.totalProjectedPointsLive) thisMatchup.away.totalProjectedPointsLive = formatDecimal(matchup.away.totalProjectedPointsLive)
                if (matchup.away.tiebreak) thisMatchup.away.tiebreak = matchup.away.tiebreak
                if (matchup.away.rosterForCurrentScoringPeriod) {
                    thisMatchup.away.lineup = getLineup(matchup.away.rosterForCurrentScoringPeriod.entries)

                    thisMatchup.away.minsLeft = 0
                    thisMatchup.away.numCurrentlyPlaying = 0
                    thisMatchup.away.numYetToPlay = 0
                    for (player in thisMatchup.away.lineup) {
                        if (player.slot != 'BE' && player.slot != 'IR') {
                            if (player.game?.minsLeft != null) thisMatchup.away.minsLeft += (player.game?.minsLeft as Integer)
                            if (player.game?.status == "in") thisMatchup.away.numCurrentlyPlaying += 1
                            else if (player.game?.status == "pre") thisMatchup.away.numYetToPlay += 1
                        }
                    }
                    if (thisMatchup.away.numCurrentlyPlaying > 0) teamsInPlay.add(thisMatchup.away.teamId)

                    if (matchup.winner == "UNDECIDED") {
                        for (player in thisMatchup.away.lineup) {
                            if (player.projectedPoints && player.slot != 'BE' && player.slot != 'IR') {
                                if (thisMatchup.away.projectedScore == null) thisMatchup.away.projectedScore = player.projectedPoints
                                else thisMatchup.away.projectedScore += player.projectedPoints
                            }
                        }
                    }
                    else thisMatchup.away.projectedScore = matchup.away.totalPoints
                }
            }
            
            if (matchup.home) {
                thisMatchup.home = [:]
                thisMatchup.home.teamId = matchup.home.teamId
                thisMatchup.home.totalPoints = matchup.home.totalPoints
                if (matchup.home.totalPointsLive) thisMatchup.home.totalPointsLive = matchup.home.totalPointsLive
                if (matchup.home.totalProjectedPointsLive) thisMatchup.home.totalProjectedPointsLive = formatDecimal(matchup.home.totalProjectedPointsLive)
                if (matchup.home.tiebreak) thisMatchup.home.tiebreak = matchup.home.tiebreak
                if (matchup.home.rosterForCurrentScoringPeriod) {
                    thisMatchup.home.lineup = getLineup(matchup.home.rosterForCurrentScoringPeriod.entries)

                    thisMatchup.home.minsLeft = 0
                    thisMatchup.home.numCurrentlyPlaying = 0
                    thisMatchup.home.numYetToPlay = 0
                    for (player in thisMatchup.home.lineup) {
                        if (player.slot != 'BE' && player.slot != 'IR') {
                            if (player.game?.minsLeft != null) thisMatchup.home.minsLeft += (player.game?.minsLeft as Integer)
                            if (player.game?.status == "in") thisMatchup.home.numCurrentlyPlaying += 1
                            else if (player.game?.status == "pre") thisMatchup.home.numYetToPlay += 1
                        }
                    }
                    if (thisMatchup.home.numCurrentlyPlaying > 0) teamsInPlay.add(thisMatchup.home.teamId)

                    if (matchup.winner == "UNDECIDED") {
                        for (player in thisMatchup.home.lineup) {
                            if (player.projectedPoints && player.slot != 'BE' && player.slot != 'IR') {
                                if (thisMatchup.home.projectedScore == null) thisMatchup.home.projectedScore = formatDecimal(player.projectedPoints)
                                else thisMatchup.home.projectedScore += player.projectedPoints
                            }
                        }
                    }
                    else thisMatchup.home.projectedScore = matchup.home.totalPoints
                }
            }
            thisMatchup.winner = matchup.winner
            if (matchup.playoffTierType) thisMatchup.playoffTierType = matchup.playoffTierType

            scoreboard.add(thisMatchup)
        }
        state.scoreboard = scoreboard
    }
    else logDebug("Warning: No liveScoring data and/or scoreboard data Retrieved")

    def selectedTeamIDs = followedTeams.collect {it as Integer}
    def anySelectedTeamInPlay = selectedTeamIDs.any { teamsInPlay.contains( it ) }
    logDebug("selectedTeamIDs = " + selectedTeamIDs + " teamsInPlay = " + teamsInPlay + " anySelectedTeamInPlay = " + anySelectedTeamInPlay)
    if (anySelectedTeamInPlay) {
        runIn(updateFrequencyInGame * 60, update, [overwrite: false])
    }

    updateDevices()
}

def getPlayerHeadshotUrl(playerId) {
    return "https://a.espncdn.com/i/headshots/nfl/players/full/" + playerId + ".png"
}

def getDayOfWeek(Date date) {
    Calendar cal = Calendar.getInstance()
    cal.setTimeZone(location.timeZone)
    cal.setTime(date)
    def dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)    
    return dayOfWeek
}

def advanceWeek() {
    def leagueData = fetchLeague()
    if (leagueData) {
        state.scoringPeriod = leagueData.scoringPeriodId as Integer
        state.finalScoringPeriod = leagueData.status?.finalScoringPeriod as Integer
        state.week = (state.scoringPeriod <= state.finalScoringPeriod) ? state.scoringPeriod : state.finalScoringPeriod
        state.seasonId = leagueData.seasonId
        state.currentMatchupPeriod = leagueData.status?.currentMatchupPeriod
    }
    state.matchupPeriodToDisplay = state.currentMatchupPeriod  

    scheduleUpdateAtGametimes()
}

def getLineup(rosterEntries) {
    def proGames = fetchGames()
    def lineup = []
    rosterEntries.each { player ->
        def thisPlayer = [:]
      //  logDebug("Player: " + player)
        thisPlayer.lineupSlotId = player.lineupSlotId
        thisPlayer.slot = SLOT_MAP[thisPlayer.lineupSlotId]
        thisPlayer.playerId = player.playerId
        thisPlayer.firstName = player.playerPoolEntry?.player.firstName
        thisPlayer.lastName = player.playerPoolEntry?.player.lastName
        thisPlayer.eligibleSlots = player.playerPoolEntry?.player.eligibleSlots
        thisPlayer.positionId = player.playerPoolEntry.player.defaultPositionId
        thisPlayer.position = POSITION_MAP[thisPlayer.positionId]
        thisPlayer.injuryStatus = player.playerPoolEntry?.player?.injuryStatus
        thisPlayer.proTeamId = player.playerPoolEntry.player.proTeamId
        thisPlayer.game = getPlayerGameData(proGames.events, thisPlayer.proTeamId)
        def stats = player.playerPoolEntry?.player?.stats
        for (stat in stats) {
            if (stat.statSplitTypeId == 1) {
                if (stat.statSourceId == 1) thisPlayer.projectedPoints = stat.appliedTotal
                else if (stat.statSourceId == 0) {
                    thisPlayer.actualPoints = stat.appliedTotal
                    thisPlayer.stats = stat.stats
                }
            }
        }    
        lineup.add(thisPlayer)
    }
    return lineup
}

def getPlayerGameData(proGames, proTeamId) {
    def game = [:]
    for (proGame in proGames) {
        def home = null
        def away = null
        for (competitor in proGame.competitors) {
            if (competitor.homeAway == "home") home = competitor
            else if (competitor.homeAway == "away") away = competitor
        }
        if ((home && ((home.id as Integer) == proTeamId)) || (away && ((away.id as Integer) == proTeamId)))  {
            game.date = proGame.date
            game.status = proGame.status
            game.summary = proGame.summary
            game.home = home
            game.away = away
            game.clock = proGame.clock
            game.period = proGame.period
            game.percentComplete = proGame.percentComplete

            if (game.status == "pre") game.minsLeft = MINS_PER_PRO_GAME
            else if (game.status == "post") game.minsLeft = 0
            else if (game.status == "in") {
                def period = proGame.fullStatus.period as Integer
                def secsLeft = (MINS_PER_PRO_GAME_PERIOD*60)*(4 - period) + proGame.fullStatus.clock
                game.minsLeft = (secsLeft / 60).setScale(0, java.math.RoundingMode.DOWN)
            }
            return game
        }
    }
}

def getSecsFromClock(clock) {
    def clockParts = clock.split(":")
    def mins = clockParts[0] as Integer
    def secs = clockParts[1] as Integer
    return (mins*60) + secs
}

def getTeamNamesEnum() {
    def teamsData = fetchTeams()
    def teams = [:]
    if (teamsData) {
        teamsData.teams.each { team ->
            teams[team.id] = team.name
        }
    }
    state.teamInfo = teams
    return teams
}

def getCurrentScoringPeriodId() {
    return state.myTeam?.currentScoringPeriodId ?: -1
}

def setMatchupScores() {
    def matchupScores = getMatchupScore(teamId)

}

def setCurrentBoxScores() {
    def result = fetchBoxScore()
    def boxScores = []
    if (result) {
        result.schedule?.each { matchup ->
            def boxScore = [:]
            boxScore.matchupPeriodId = matchup.matchupPeriodId

            boxScore.away = [:]
            boxScore.away.teamId = matchup.away.teamId
            boxScore.away.totalPoints = matchup.away.totalPoints
            if (matchup.away && matchup.away.rosterForCurrentScoringPeriod) boxScore.away.roster = getLineup(matchup.away.rosterForCurrentScoringPeriod.entries)

            boxScore.home = [:]
            boxScore.home.teamId = matchup.home.teamId
            boxScore.home.totalPoints = matchup.home.totalPoints
            if (matchup.home && matchup.home.rosterForCurrentScoringPeriod) boxScore.home.roster = getLineup(matchup.home.rosterForCurrentScoringPeriod.entries)
            
            boxScores.add(boxScore)
        }
        state.boxScores = boxScores
    }
}

def getMatchupForTeam(teamId) {
    for (matchup in state.scoreboard) {
        if (matchup.matchupPeriod == state.matchupPeriodToDisplay) {
            if ((matchup.home && matchup.home.teamId == teamId as Integer) || (matchup.away && matchup.away.teamId == teamId as Integer)) {
                return matchup
            }
        }
    }   
    logDebug("No Matchup Found for Team " + teamId + " for matchup period " + state.matchupPeriodToDisplay)
}

def getTeamRosterTile(teamId) {  
    if (!state.refreshNum) state.refreshNum = 0
    state.refreshNum++
    def rosterTileUrl = getTeamRosterTileEndpoint(teamId) + '&version=' + state.refreshNum   
    def rosterTile =     "<div style='height:100%;width:100%'><iframe src='${rosterTileUrl}' style='height:100%;width:100%;border:none'></iframe></div>"
    return rosterTile
}

def getTeamMatchupTile(teamId) {  
    if (!state.refreshNum) state.refreshNum = 0
    state.refreshNum++
    def matchupTileUrl = getTeamMatchupTileEndpoint(teamId) + '&version=' + state.refreshNum   
    def matchupTile =     "<div style='height:100%;width:100%'><iframe src='${matchupTileUrl}' style='height:100%;width:100%;border:none'></iframe></div>"
    return matchupTile
}

def getLeagueMatchupTile(tileNum) {  
    if (!state.refreshNum) state.refreshNum = 0
    state.refreshNum++
    def matchupTileUrl = getLeagueMatchupTileEndpoint(tileNum) + '&version=' + state.refreshNum   
    def matchupTile =     "<div style='height:100%;width:100%'><iframe src='${matchupTileUrl}' style='height:100%;width:100%;border:none'></iframe></div>"
    return matchupTile
}

def getLeagueMatchupForTile(tileNum) {
    def tileCount = 0
    def selectedTeamIDs = followedTeams.collect {it as Integer}
    for (matchup in state.scoreboard) {
        if (matchup.matchupPeriod == state.matchupPeriodToDisplay) {
            if (matchup && (matchup.home.teamId as Integer) in selectedTeamIDs && matchup.away && (matchup.away.teamId as Integer) in selectedTeamIDs) {
                tileCount++
                if (tileCount == tileNum) return matchup
            }
            else if ((matchup.home && (matchup.home.teamId as Integer) in selectedTeamIDs) || (matchup.away && (matchup.away.teamId as Integer) in selectedTeamIDs)) {
                tileCount++
                if (tileCount == tileNum) return matchup
            }
        }
    }
}

def fetchLeagueMatchupTile() {
    if(params.appId.toInteger() != app.id) {
        logDebug("Returning null since app ID received at endpoint is ${params.appId.toInteger()} whereas the app ID of this app is ${app.id}")
        return null    // request was not for this app/team, so return null
    }
    def tileNum = params.tileNum.toInteger()
    def matchup = getLeagueMatchupForTile(tileNum)
    def matchupTile = getTileForMatchup(matchup)
    render contentType: "text/html", data: matchupTile, status: 200
}

def fetchTeamMatchupTile() {
    if(params.appId.toInteger() != app.id) {
        logDebug("Returning null since app ID received at endpoint is ${params.appId.toInteger()} whereas the app ID of this app is ${app.id}")
        return null    // request was not for this app/team, so return null
    }
    def teamId = params.teamId
    def matchup = getMatchupForTeam(teamId)
    def matchupTile = getTileForMatchup(matchup)
    render contentType: "text/html", data: matchupTile, status: 200
}

def fetchTeamRosterTile() {
    if(params.appId.toInteger() != app.id) {
        logDebug("Returning null since app ID received at endpoint is ${params.appId.toInteger()} whereas the app ID of this app is ${app.id}")
        return null    // request was not for this app/team, so return null
    }
    def teamId = params.teamId
    def matchup = getMatchupForTeam(teamId)
    def rosterTile = getRosterForMatchup(matchup, teamId)
    render contentType: "text/html", data: rosterTile, status: 200    
}

def getTileForMatchup(matchup) {
    def defaultTextColor = getTextColorSetting()
    def defaultFontSize = getFontSizeSetting("teamInfo")
    def matchupTile = ""
    
    matchupTile += "<script>"
    matchupTile += "function handleContentClick(e) { window.parent.postMessage('childContentClicked', '*') }"
    matchupTile += "</script>"

    matchupTile += "<div id='matchupDiv' onClick='handleContentClick();' style='height:100%;'>"
    if (matchup != null) {
        def awayTeamId = matchup.away ? matchup.away.teamId : null
        def homeTeamId = matchup.home ? matchup.home.teamId : null
        def isGameFinished = matchup.winner == "UNDECIDED" ? false : true
        def homeTeamInjuryDisplay = (homeTeamId != null && state.teams[homeTeamId]?.startingInjuredPlayer) ? "<img src='" + injuryIcon + "' width='30%' style='position:absolute; right: 0; vertical-align: top'>" : ""
        def awayTeamInjuryDisplay = (awayTeamId != null && state.teams[awayTeamId]?.startingInjuredPlayer) ? "<img src='" + injuryIcon + "' width='30%' style='position:absolute; left: 0; vertical-align: top'>" : ""
        def homeTeamInPlayDisplay = (matchup.home && matchup.home.numCurrentlyPlaying > 0) ? "<img src='" + inPlayIcon + "' width='30%' style='position:absolute; left: 0; vertical-align: top'>" : ""
        def awayTeamInPlayDisplay = (matchup.away && matchup.away.numCurrentlyPlaying > 0) ? "<img src='" + inPlayIcon + "' width='30%' style='position:absolute; right: 0; vertical-align: top'>" : ""
        
        def byeIcon = iconColor == "black" ? byeWeekIcon : byeWeekIconLight
        def homeTeamLogo = homeTeamId != null ? state.teams[homeTeamId]?.logo : byeIcon
        def awayTeamLogo = awayTeamId != null ? state.teams[awayTeamId]?.logo : byeIcon

        matchupTile += "<table width='100%' style='border-collapse: collapse;font-size:${defaultFontSize}%;color:${defaultTextColor};font-family: 'Oswald, sans-serif'>"
        matchupTile += "<tr>"
        matchupTile += "<td width='40%' align=center style='vertical-align: top'><div style='position:relative;display:inline'><img src='" + awayTeamLogo + "' width='80%' style='vertical-align: bottom'>" + awayTeamInjuryDisplay + awayTeamInPlayDisplay + "</div></td>"
        matchupTile += "<td width='10%' align=center>at</td>"
        matchupTile += "<td width='40%' align=center style='vertical-align: top'><div style='position:relative;display:inline'><img src='" + homeTeamLogo + "' width='80%' style='vertical-align: bottom'>" + homeTeamInjuryDisplay + homeTeamInPlayDisplay + "</div></td>"
        matchupTile += "</tr>"
        if (showTeamName || showTeamRecord) {

            def textColor = getTextColorSetting()
            def fontSize = getFontSizeSetting("teamInfo")

            def awayName = (showTeamName && awayTeamId != null ? state.teams[awayTeamId]?.name : "")
            def awayRecord = ""
            if (isGameFinished && showTeamRecord && awayTeamId != null) awayRecord = (showTeamName ? " " : "") + '(' + state.teams[awayTeamId]?.record?.wins + '-' + state.teams[awayTeamId]?.record?.losses + (state.teams[awayTeamId]?.record?.ties ?: "") + ')'

            def homeName = (showTeamName && homeTeamId != null ? state.teams[homeTeamId]?.name : "")
            def homeRecord = ""
            if (isGameFinished && showTeamRecord && homeTeamId != null) homeRecord = (showTeamName ? " " : "") + '(' + state.teams[homeTeamId]?.record?.wins + '-' + state.teams[homeTeamId]?.record?.losses + (state.teams[homeTeamId]?.record?.ties ?: "") + ')'

            matchupTile += "<tr style='padding-bottom: 0em;font-size:${fontSize}%;color:${textColor};font-weight: bold;'>"
            matchupTile += "<td width='40%' style='vertical-align: top;' align=center>" + awayName + awayRecord + "</td>" 
            matchupTile += "<td width='10%' align=center></td>"
            matchupTile += "<td width='40%' style='vertical-align: top;' align=center>" + homeName + homeRecord + "</td>"
            matchupTile += "</tr>"
        }
        if (showTeamOwnerFirstName || showTeamOwnerLastName) {

            def textColor = getTextColorSetting()
            def fontSize = getFontSizeSetting("teamInfo")

            def awayOwner = awayTeamId != null ? state.teams[awayTeamId]?.primaryOwner : null
            def homeOwner = homeTeamId != null ? state.teams[homeTeamId]?.primaryOwner : null
            def awayOwnerName = ""
            if (awayOwner) awayOwnerName = (showTeamOwnerFirstName ? awayOwner.firstName : "") + (showTeamOwnerFirstName && showTeamOwnerLastName ? " " : "") + (showTeamOwnerLastName ? awayOwner.lastName : "")
            def homeOwnerName = ""
            if (homeOwner) homeOwnerName = (showTeamOwnerFirstName ? homeOwner.firstName : "") + (showTeamOwnerFirstName && showTeamOwnerLastName ? " " : "") + (showTeamOwnerLastName ? homeOwner.lastName : "")
            matchupTile += "<tr style='padding-bottom: 0em; font-size:${fontSize}%; color:${textColor};font-weight: bold;'>"
            matchupTile += "<td width='40%' style='vertical-align: top;' align=center>" + awayOwnerName + "</td>" 
            matchupTile += "<td width='10%' align=center></td>"
            matchupTile += "<td width='40%' style='vertical-align: top;' align=center>" + homeOwnerName + "</td>" 
            matchupTile += "</tr>"
        }
        def awayScore = null
        def awayScoreColor = null
        def awayProjectedScore = null
        def homeScore = null
        def homeScoreColor = null
        def homeProjectedScore = null
        
        if (!isGameFinished) {
            if ((matchup.away && matchup.away.totalPointsLive != null) || (matchup.home && matchup.home.totalPointsLive != null)) {
                awayScore = (matchup.away && matchup.away.totalPointsLive) ? matchup.away.totalPointsLive : 0
                homeScore = (matchup.home && matchup.home.totalPointsLive) ? matchup.home.totalPointsLive : 0
            }
            if ((matchup.home && matchup.home.totalProjectedPointsLive != null) || (matchup.away && matchup.away.totalProjectedPointsLive)) {
                awayProjectedScore = (matchup.away && matchup.away.totalProjectedPointsLive) ? matchup.away.totalProjectedPointsLive : 0
                homeProjectedScore = (matchup.home && matchup.home.totalProjectedPointsLive) ? matchup.home.totalProjectedPointsLive : 0
            }
            else {
                awayProjectedScore = matchup.away ? matchup.away.projectedScore : 0
                homeProjectedScore = matchup.home ? matchup.home.projectedScore : 0
            }
        }
        else {
            awayScore = matchup.away ? matchup.away.totalPoints : 0
            homeScore = matchup.home ? matchup.home.totalPoints : 0
            if (matchup.winner == "HOME") {
                homeScoreColor = "#059936" // green
                awayScoreColor = "#C33414" // red
            }
            else if (matchup.winner == "AWAY") {
                awayScoreColor = "#059936" // green
                homeScoreColor = "#C33414" // red
            }
        }
        if (homeScore != null && awayScore != null) {
            def textColor = getTextColorSetting()
            def fontSize = getFontSizeSetting("score")
            matchupTile += "<tr style='padding-bottom: 0em;font-size:${fontSize}%;color:${textColor};font-weight: bold;'>"
            matchupTile += "<td width='40%' align=center" + (awayScoreColor ? " bgcolor='${awayScoreColor}'" : "") +  ">" + formatDecimal(awayScore) + "</td>"
            matchupTile += "<td width='10%' align=center></td>"
            matchupTile += "<td width='40%' align=center" + (homeScoreColor ? " bgcolor='${homeScoreColor}'" : "") +  ">" + formatDecimal(homeScore) + "</td>"
            matchupTile += "</tr>" 
        }
        if (homeProjectedScore != null && awayProjectedScore != null) {
            def textColor = getTextColorSetting()
            def fontSize = getFontSizeSetting("projectedScore")
            def icon = iconColor == "black" ? projectedIcon : projectedIconLight
            matchupTile += "<tr style='padding-bottom: 0em;font-size:${fontSize}%;color:${textColor};font-weight: bold;'>"
            matchupTile += "<td width='40%' align=center>" + "<img src='" + icon + "' width='25%' style='vertical-align: center'> " + formatDecimal(awayProjectedScore) + "</td>"
            matchupTile += "<td width='10%' align=center></td>"
            matchupTile += "<td width='40%' align=center>" + "<img src='" + icon + "' width='25%' style='vertical-align: center'> " + formatDecimal(homeProjectedScore) + "</td>"
            matchupTile += "</tr>"                    
        }
        if (showMinsLeft) {
            def textColor = getTextColorSetting()
            def fontSize = getFontSizeSetting("teamInfo")
            def icon = iconColor == "black" ? minsLeftIcon : minsLeftIconLight
            matchupTile += "<tr style='padding-bottom: 0em;font-size:${fontSize}%;color:${textColor};font-weight: bold;'>"
            matchupTile += "<td width='40%' align=center>" + "<img src='" + icon + "' width='25%' align=center style='vertical-align: center'> " + (matchup.away ? matchup.away.minsLeft : 0) + "</td>"
            matchupTile += "<td width='10%' align=center></td>"
            matchupTile += "<td width='40%' align=center>" + "<img src='" + icon + "' width='25%' align=center style='vertical-align: center'> " + (matchup.home ? matchup.home.minsLeft : 0) + "</td>"
            matchupTile += "</tr>"   
        }
        matchupTile += "</table>" 
    }
    matchupTile += "</div>" 
    return matchupTile
}

def sortLineupBySlot(lineup) {
    def sortedLineup = lineup?.sort { it.lineupSlotId }
    def flexPlayers = sortedLineup.findAll { it.lineupSlotId == 23 }
    def finalLineup = []
    for (player in sortedLineup) {
        if (player.lineupSlotId <= 6 ) finalLineup.add(player)
    }
    for (player in flexPlayers) {
        finalLineup.add(player)
    }
    for (player in sortedLineup) {
        if (player.lineupSlotId > 6 && player.lineupSlotId != 23) finalLineup.add(player)
    }
    return finalLineup
}

def getInjuryStatusStr(injuryStatus) {
    def status = INJURY_STATUS_MAP[injuryStatus]
    if (status) return "<span style='color: #ff0000; font-weight:bold'>" + status + "</span>"
    else return ""
}

def processLineup(slotTypeIndex, lineup) {
    def playerIndex = lineup.findIndexOf{ it.lineupSlotId == (slotTypeIndex as Integer) }
    def player = null
    if (playerIndex != -1) player = lineup.removeAt(playerIndex)
    return [player: player, lineup: lineup]
}

def getRosterForMatchup(matchup, teamId) {
    def defaultTextColor = getRosterTextColorSetting()
    def defaultFontSize = getRosterFontSizeSetting()
    def styleText = "border-collapse: collapse;font-size:${defaultFontSize}%;color:${defaultTextColor};font-family: 'Oswald, sans-serif"

    def rosterTile = "<div style='height:100%;'>"
    if (matchup != null) {
        def team = null
        
        if (matchup.away && matchup.away.teamId == teamId.toInteger()) team = matchup.away
        else if (matchup.home && matchup.home.teamId == teamId.toInteger()) team = matchup.home

        if (team.lineup != null) {
            def lineup = sortLineupBySlot(team.lineup)

            rosterTile += "<table width='100%' height='100%' style='" + styleText + "'>"
            rosterTile += "<tr bgcolor='" + rosterRowColor1 + "'>"
            rosterTile += "<th width='10%' align=left>SLOT</th>"
            rosterTile += "<th width='35%' align=left>PLAYER</th>"
            rosterTile += "<th width='25%' align=left>OPP</th>"
            rosterTile += "<th width='15%' align=center>PROJ</th>"
            rosterTile += "<th width='15%' align=right>SCORE</th>"
            rosterTile += "</tr>"
            
            def numRows = 0
            def projectionTotal = 0
            def scoreTotal = 0
            def bench = false
            SLOT_MAP.each { slotTypeIndex, slotTypeAbbr ->
                def slotTypeCount = state.lineupSlotCounts[slotTypeIndex as String]
                for (i = 0; i < slotTypeCount; i++) {
                    numRows++
                    def result = processLineup(slotTypeIndex, lineup)
                    def player = result.player
                    lineup = result.lineup
                    
                    def bgcolor = rosterRowColor1
                    if (numRows % 2 > 0) bgcolor = rosterRowColor2
                    rosterTile += "<tr bgcolor='" + bgcolor + "' style='font-weight:bold'>"

                    def slotDescription = SLOT_MAP[slotTypeIndex as Integer]

                    if (bench == false && player != null && (slotDescription == "BE" || slotDescription == "IR")) {
                        rosterTile += "<td width = '70%' align=right colspan='3'>" + "TOTALS" + "</td>"
                        rosterTile += "<td width = '15%' align=center>" + formatDecimal(projectionTotal) + "</td>"
                        rosterTile += "<td width = '15%' align=right>" + formatDecimal(scoreTotal) + "</td>"
                        rosterTile += "</tr>"

                        numRows++
                        bgcolor = rosterRowColor1
                        if (numRows % 2 > 0) bgcolor = rosterRowColor2

                        rosterTile += "<tr bgcolor='" + bgcolor + "' style='font-weight:bold'>"
                        bench = true
                    }
                    rosterTile += "<td width='10%' align=left>" + slotDescription + "</td>"
                    rosterTile += "<td width = '35%' align=left>"
                        rosterTile += "<table width='100%' style='" + styleText + "'>"
                        rosterTile += "<tr>"
                            def img = null
                            if (player != null && player.position != "D/ST" ) img = getPlayerHeadshotUrl(player.playerId)
                            else if (player != null) img = getTeamLogo(player.proTeamId)
                            def imgStr = img ? ("<img src='" + img + "' width='100%' style='vertical-align: top'>") : ""
                            rosterTile += "<td rowspan='2' width='30%' align=left style='vertical-align: top'>" + imgStr + "</td>"

                            def playerText = player ? (player.firstName + " " + player.lastName + " " + getInjuryStatusStr(player.injuryStatus)) : "EMPTY"
                            rosterTile += "<td width='70%' align=left style='vertical-align: top'>" + playerText + "</td>"
                        
                        def teamStr = ""
                        if (player != null) teamStr = PRO_TEAM_MAP[player.proTeamId] + " " + "<span style='font-weight:bold'>" + player.position + "</span>"
                        rosterTile += "</tr>"
                        rosterTile += "<tr>"
                            rosterTile += "<td width='70%' align=left style='vertical-align: top'>" + teamStr + "</td>"
                        rosterTile += "</tr>"
                        rosterTile += "</table>" 
                    rosterTile += "</td>"

                    rosterTile += "<td width = '25%' align=left>"
                        rosterTile += "<table width='100%' style='" + styleText + ";'>"
                            rosterTile += "<tr>"
                                rosterTile += "<td width='100%' align=left style='vertical-align: top'>" + getOppFirstLine(player) + "</td>"
                            rosterTile += "</tr>"
                            rosterTile += "<tr>"
                                rosterTile += "<td width='100%' align=left style='vertical-align: top'>" + getOppSecondLine(player) + "</td>"
                            rosterTile += "</tr>"
                        rosterTile += "</table>" 
                    rosterTile += "</td>"
                    if (player && player.projectedPoints != null && player.position != "BE" && player.position != "IR") projectionTotal += player.projectedPoints
                    if (player && player.actualPoints != null && player.position != "BE" && player.position != "IR") scoreTotal += player.actualPoints
                    rosterTile += "<td width = '15%' align=center>" + ((player && player.projectedPoints != null) ? formatDecimal(player.projectedPoints) : "--") + "</td>"
                    rosterTile += "<td width = '15%' align=right>" + (player && player.actualPoints != null ? formatDecimal(player.actualPoints) : "--") + "</td>"
                    rosterTile += "</tr>"
                }
            }
            rosterTile += "</table>" 
        }
    }
    rosterTile += "</div>" 
    return rosterTile
}

def getOppFirstLine(player) {
    def oppFirstLine = ""
    if (player && player.game?.status == "pre") {
        if ((player.game?.home?.id as Integer) == (player.proTeamId as Integer)) {
            if (player.game?.away?.id == null) oppFirstLine = "BYE"
            else oppFirstLine = PRO_TEAM_MAP[(player.game?.away?.id as Integer)]
        }
        else if ((player.game?.away?.id as Integer) == (player.proTeamId as Integer)) {
            if (player.game?.home?.id == null) oppFirstLine = "BYE"
            else oppFirstLine = "@" + PRO_TEAM_MAP[(player.game?.home?.id as Integer)]
        }
    }
    else if (player && (player.game?.status == "post" || player.game?.status == "in")) {
        oppFirstLine = getPlayerStats(player)
    }
    return oppFirstLine
}

def getOppSecondLine(player) {
    def secondLine = ""
    if (player && player.game?.status == "pre") {
        secondLine = formatGametime(player.game?.date)
    }
    else if (player && (player.game?.status == "post" || player.game?.status == "in")) { 
        def opp = ""
        if ((player.game?.home?.id as Integer) == (player.proTeamId as Integer)) {
            if (player.game?.away?.id == null) opp = "BYE"
            else opp = PRO_TEAM_MAP[(player.game?.away?.id as Integer)] + ", "
        }
        else if ((player.game?.away?.id as Integer) == (player.proTeamId as Integer)) {
            if (player.game?.home?.id == null) opp = "BYE"
            else opp = PRO_TEAM_MAP[(player.game?.home?.id as Integer)] + ", "
        }

        def scoreStr = ""
        if ((player.game?.home?.id as Integer) == (player.proTeamId as Integer) && player.game?.away?.id != null) {
            // player on home team
            if (player.game?.status == "post") scoreStr += (player.game?.home?.winner == true) ? "W " : "L "
            scoreStr += removeDecimal(player.game?.home?.score) + "-" + removeDecimal(player.game?.away?.score)
        }
        else if ((player.game?.away?.id as Integer) == (player.proTeamId as Integer) && player.game?.home?.id != null) {
            // player on away team
            if (player.game?.status == "post") scoreStr += (player.game?.away?.winner == true) ? "W " : "L "
            scoreStr += removeDecimal(player.game?.away?.score) + "-" + removeDecimal(player.game?.home?.score)
        }

        def statusStr = ""
        if (player.game?.status == "post") statusStr = " Final"
        else if (player.game?.status == "in") {
            statusStr = " " + QUARTER_MAP[player.game?.period] + " " + player.game?.clock
        }

        secondLine = opp + scoreStr + statusStr
    }
    return secondLine
}

def getPlayerStats(player) {
    logDebug("Getting stats for player " + player)
    def stats = ""
    if (player.position == "QB") {
        def passingYards = removeDecimal(player.stats['3'] ?: 0)
        def rushingYards = removeDecimal(player.stats['24'] ?: 0)
        def totalYards = passingYards + rushingYards

        def passingTDs = removeDecimal(player.stats['4'] ?: 0)
        def rushingTDs = removeDecimal(player.stats['25'] ?: 0)
        def totalTDs = passingTDs + rushingTDs

        def interceptions = removeDecimal(player.stats['20'] ?: 0)

        stats = totalYards + " YDS"
        if (rushingTDs == 1 && passingTDs == 0) stats += ", RUSH TD"
        else if (rushingTDs > 1 && passingTDs == 0) stats += ", " rushingTDs + " RUSH TD"
        else if (rushingTDs > 0 && passingTDs > 0) stats += ", " + totalTDs + " TOT TD"
        else if (rushingTDs == 0 && passingTDs == 1) stats += ", TD"
        else if (rushingTDs == 0 && passingTDs > 1) stats += ", " + passingTDs + " TD"
        if (interceptions > 0) stats += ", " + interceptions + " INT"
    }
    else if (player.position == "RB") {
        def rushingYards = removeDecimal(player.stats['24'] ?: 0)

        def receivingTDs = removeDecimal(player.stats['43'] ?: 0)
        def rushingTDs = removeDecimal(player.stats['25'] ?: 0)
        def totalTDs = receivingTDs + rushingTDs

        def fumbles = removeDecimal(player.stats['68'] ?: 0)

        stats += rushingYards + " YDS"
        if (receivingTDs > 0 && rushingTDs == 0) stats += ", REC TD"
        else if (receivingTDs > 0 && rushingTDs > 0) stats += ", " + totalTDs + " TOT TD"
        else if (rushingTDs == 1 && receivingTDs == 0) stats += ", TD"
        else if (rushingTDs > 1 && receivingTDs == 0) stats += ", " + receivingTDs + " TD"
        if (fumbles > 0) stats += ", " + fumbles + " FUM"
    }
    else if (player.position == "WR" || player.position == "TE") {
        def receptions = removeDecimal(player.stats['41'] ?: 0)
        def receivingYards = removeDecimal(player.stats['42'] ?: 0)

        def receivingTDs = removeDecimal(player.stats['43'] ?: 0)
        def rushingTDs = removeDecimal(player.stats['25'] ?: 0)
        def totalTDs = receivingTDs + rushingTDs

        def fumbles = removeDecimal(player.stats['68'] ?: 0)

        if (receptions > 0) stats += receptions + " REC, "
        stats += receivingYards + " YDS"
        if (rushingTDs == 1 && receivingTDs == 0) stats += ", RUSH TD"
        else if (rushingTDs > 1 && receivingTDs == 0) stats += ", " rushingTDs + " RUSH TD"
        else if (rushingTDs > 0 && receivingTDs > 0) stats += ", " + totalTDs + " TOT TD"
        else if (rushingTDs == 0 && receivingTDs == 1) stats += ", TD"
        else if (rushingTDs == 0 && receivingTDs > 1) stats += ", " + receivingTDs + " TD"
        if (fumbles > 0) stats += ", " + fumbles + " FUM"
    }
    else if (player.position == "D/ST") {
        def touchdowns = removeDecimal(player.stats['105'] ?: 0)
        def interceptions = removeDecimal(player.stats['95'] ?: 0)
        def fumbleRecoveries = removeDecimal(player.stats['96'] ?: 0)
        def safeties = removeDecimal(player.stats['98'] ?: 0)
        def pointsAllowed = removeDecimal(player.stats['120'] ?: 0)
        def blocks = removeDecimal(player.stats['97'] ?: 0)


        def numStats = 0
        if (touchDowns > 0) {
            numStats ++
            if (touchDowns == 1) stats += "TD"
            else if (touchDowns > 1) stats += touchDowns += " TD"
        }
        if (interceptions > 0) {
            if (numStats > 0) stats += ", "
            numStats++
            if (interceptions == 1) stats += "INT"
            else if (interceptions > 1) stats += interceptions + " INT"
        }
        if (fumbleRecoveries > 0) {
             if (numStats > 0) stats += ", "
            numStats++
            if (fumbleRecoveries == 1) stats += "FR"
            else if (fumbleRecoveries > 1) stats += fumbleRecoveries + " FR"           
        }
        if (safeties > 0) {
             if (numStats > 0) stats += ", "
            numStats++
            if (safeties == 1) stats += "SFTY"
            else if (safeties > 1) stats += safeties + " SFTY"           
        }
        if (numStats < 4 && pointsAllowed > 0) {
             if (numStats > 0) stats += ", "
            numStats++
            stats += pointsAllowed + " PA"           
        }  
        if (numStats < 4 && blocks > 0) {
             if (numStats > 0) stats += ", "
            numStats++
            if (blocks == 1) stats += "BLK"
            else if (blocks > 1) stats += blocks + " BLK"           
        }      
    }
    else if (player.position == "K") {
        def madeFGs = removeDecimal(player.stats['83'] ?: 0)
        def attemptedFGs = removeDecimal(player.stats['84'] ?: 0)
        def madeXP = removeDecimal(player.stats['86'] ?: 0)
        def attemptedXP = removeDecimal(player.stats['87'] ?: 0)   

        if (attemptedFGs > 0) stats += madeFGs + "/" + attemptedFGs + " FG"
        if (attemptedFGs > 0 && attemptedXP > 0) stats += ", "
        if (attemptedXP > 0) stats += madeXP + "/" + attemptedXP + " XP"
    }
    return stats
}

def formatGametime(date) {
    def result = ""
    Date gametime = getDateObjectFromUTCDt(date)
    result = formatDt(gametime)
    return result
}

def formatDt(dt) {
    def tf = new SimpleDateFormat("EEE h:mm a")
    if(location?.timeZone) { tf?.setTimeZone(location?.timeZone) }
    else {
        LOG("Hubitat TimeZone is not found or is not set... Please Try to open your Hubitat location and Press Save...", 1, "warn")
        return null
    }
    return tf.format(dt)
}

def getDateObjectFromUTCDt(utcDt) {
    def inFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
    inFormat.setTimeZone(TimeZone.getTimeZone("UTC"))
    return inFormat.parse(utcDt)
}

def titleCase(str) {
    def lower = str.toLowerCase()
    def capitalized = lower.capitalize()
    return capitalized
}

def formatDecimal(number) {
    if (number instanceof Integer) return number
    else if (number instanceof BigDecimal) return number.setScale(getdecimalPlaceSetting(), java.math.RoundingMode.UP)
    else return number.round(getdecimalPlaceSetting())
}

def removeDecimal(number) {
    if (number instanceof Integer) return number
    else if (number instanceof BigDecimal) return number.setScale(0, java.math.RoundingMode.DOWN) 
}

def fetchTeams(forTeamId = null) {
    sendApiRequest(["mTeam"], forTeamId)
}

def fetchBoxScore(forTeamId = null, forScoringPeriodId = null, forMatchupPeriodId = null) {
    def result = sendApiRequest(["mBoxscore"], forTeamId, forScoringPeriodId, forMatchupPeriodId)
    return result
}

def fetchMatchupScores(forTeamId = null, forScoringPeriodId = null, forMatchupPeriodId = null) {
    def matchupScores = sendApiRequest(["mMatchupScore"], forTeamId, forScoringPeriodId, forMatchupPeriodId)
    return matchupScores
}

def fetchLiveScoring() {
    def result = sendApiRequest(["mLiveScoring"])
    return result
}

def fetchScoreboard() {
    def result = sendApiRequest(["mScoreboard"])
    return result
}

def fetchRoster(forTeamId = null) {
    def result = sendApiRequest(["mRoster"], forTeamId)
    return result
}

def fetchLeague() {
    def result = sendApiRequest(["mTeam", "mRoster", "mMatchup", "mStandings", "mSettings", "mLiveScoring"])
    return result
}

def fetchProSchedules() {
    return sendPublicApiRequest("https://lm-api-reads.fantasy.espn.com/apis/v3/games/ffl/seasons/" + getFFLYearStart() + "?view=proTeamSchedules_wl")
}

def getTeamLogo(proTeamId) {
    return "https://a.espncdn.com/i/teamlogos/nfl/500/scoreboard/" + PRO_TEAM_MAP[proTeamId] + ".png"
}

def fetchGames() {
    // fetches all game data for the current week. Add "?dates=yyyyMMdd-yyyyMMdd" as url parameter to limit to certain dates
    return sendPublicApiRequest("https://site.api.espn.com/", "apis/fantasy/v2/games/ffl/games")
}

def createDevices()
{
    def leagueDeviceData = [id: leagueId, name: (state.leagueName ?: ("FFL" + leagueId))]

    def teamDevicesData = []
    def selectedTeamIDs = followedTeams.collect {it as Integer}
    for (teamId in selectedTeamIDs) {
        def teamName = (state.teams != null) ? state.teams[teamId]?.name : state.teamInfo[teamId]
        def device = [id: teamId, name: teamName, leagueId: getLeagueId()]
        teamDevicesData.add(device)
    }
    
    parent.createDevices(leagueDeviceData, teamDevicesData)
}

def deleteDevices()
{
    parent.deleteDevicesForLeague(leagueId)
}

def getFFLYearStart() {
    def fflYearStart = null

    ZoneId localZoneId = location.timeZone.toZoneId()
    ZonedDateTime now = ZonedDateTime.now(localZoneId)
    Integer year = now.getYear()
    Integer month = now.getMonth().getValue()

   // logDebug("Year: ${year} Month: ${month}")
    if (month >= 5) fflYearStart = year
    else if (month < 5) fflYearStart = year - 1   // ffl starts in August or September each year

    return fflYearStart
}

def getLeagueId() {
    return settings["leagueId"]
}


def updateDevices() {
    def leagueDeviceData = [:]
    leagueDeviceData.id = getLeagueId()
    leagueDeviceData.name = state.leagueName ?: ("FFL" + getLeagueId())
    leagueDeviceData.matchup1 = getLeagueMatchupTile(1)
    leagueDeviceData.matchup2 = getLeagueMatchupTile(2)
    leagueDeviceData.matchup3 = getLeagueMatchupTile(3)
    leagueDeviceData.matchup4 = getLeagueMatchupTile(4)
    leagueDeviceData.matchup5 = getLeagueMatchupTile(5)
    leagueDeviceData.matchup6 = getLeagueMatchupTile(6)

    def teamDevicesData = [:]
    def selectedTeamIDs = followedTeams.collect {it as Integer}
    for (teamId in selectedTeamIDs) {
        teamDevicesData[teamId] = [:]
        teamDevicesData[teamId].id = teamId
        teamDevicesData[teamId].leagueId = getLeagueId()
        teamDevicesData[teamId].team = state.teams[teamId]
        teamDevicesData[teamId].matchup = getTeamMatchupTile(teamId)
        teamDevicesData[teamId].matchupData = getMatchupForTeam(teamId)
        teamDevicesData[teamId].roster = getTeamRosterTile(teamId)
    }
    
    parent.updateDevicesForLeague(leagueDeviceData, teamDevicesData)
}

def pushDeviceButton(buttonNum) {
    parent.pushDeviceButton(app.id, buttonNum)
}

def sendApiRequest(viewList = null, teamId = null, scoringPeriodId = null, matchupPeriodId = null)
{
    def viewInlineString = "?view="
    def viewHeaderString = ""
    for (i=0; i < viewList.size(); i++) {
        if (i>0) viewInlineString += "&view="
        viewInlineString += viewList[i]
        viewHeaderString += viewList[i] + ";"
    }
    def params = [
		uri: "https://lm-api-reads.fantasy.espn.com/apis/v3/games/ffl/seasons/" + getFFLYearStart() + "/segments/0/leagues/" + getLeagueId() + "/" + viewInlineString + (teamId != null ? "&forTeamId=" + teamId : "") + (scoringPeriodId != null ? "&scoringPeriodId=" + scoringPeriodId : "") + (matchupPeriodId != null ? "&matchupPeriodId=" + matchupPeriodId : ""),
        path: "",
		contentType: "application/json",
		headers: [
                'Cookie': "SWID=" + swidCookie + ";espn_s2=" + espnS2Cookie,
                'view': viewList
            ],
		timeout: 1000
	]

    if (body != null)
        params.body = body

    def result = null
    logDebug(viewList + " Api Call: ${params}")
    try
    {
        httpGet(params) { resp ->
            result = resp.data
        }                
    }
    catch (Exception e)
    {
        log.warn "sendApiRequest() failed: ${e.message}"
        return null
    }   
    return result
}

def sendPublicApiRequest(uri, path = "") {
    def params = [
        uri: uri,
        path: path,
		contentType: "application/json",
		timeout: 1000
	]

    if (body != null)
        params.body = body

    def result = null
    try
    {
        httpGet(params) { resp ->
            result = resp.data
        }                
    }
    catch (Exception e)
    {
        log.warn "sendPublicApiRequest() for ${uri}${path} failed: ${e.message}"
        return null
    }   
    return result
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

def adjustDateByMins(Date date, Integer mins) {
    Calendar cal = Calendar.getInstance()
    cal.setTimeZone(location.timeZone)
    cal.setTime(date)
    cal.add(Calendar.MINUTE, mins)
    Date newDate = cal.getTime()
    return newDate
}

def getTimeMapFromDateTime(dateTime) {
    Calendar cal = Calendar.getInstance()
    cal.setTimeZone(location.timeZone)
    cal.setTime(dateTime)
    def hour = cal.get(Calendar.HOUR_OF_DAY)
    def minutes = cal.get(Calendar.MINUTE)
    return [hour: hour, minutes: minutes]
}

def getOrdinal(num) {
    // get ordinal number for num range 1-30
    def ord = null
    if (num == 1 || num == 21) ord = "st"
    else if (num == 2 || num == 22) ord = "nd"
    else if (num == 3 || num == 23) ord = "rd"
    else ord = "th"
    return ord
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

@Field Map SLOT_MAP = [
    0: 'QB',
    1: 'TQB',
    2: 'RB',
    3: 'RB/WR',
    4: 'WR',
    5: 'WR/TE',
    6: 'TE',
    23: 'FLEX',
    7: 'OP',
    8: 'DT',
    9: 'DE',
    10: 'LB',
    11: 'DL',
    12: 'CB',
    13: 'S',
    14: 'DB',
    15: 'DP',
    16: 'D/ST',
    17: 'K',
    18: 'P',
    19: 'HC',
    20: 'BE',
    21: 'IR',
    22: '',
    24: 'ER',
    25: 'Rookie',
]


@Field Map QUARTER_MAP = [
    1: '1st',
    2: '2nd',
    3: '3rd',
    4: '4th'
]
@Field Map POSITION_MAP = [
    1: 'QB',
    2: 'RB',
    3: 'WR',
    4: 'TE',
    5: 'K',
    7: 'P', // punter
    9: 'DT',
    10: 'DE',
    11: 'LB',
    12: 'CB',
    13: 'S',
    14: 'HC',
    16: 'D/ST'
]

@Field Map PRO_TEAM_MAP = [
    0 : 'None',
    1 : 'ATL',
    2 : 'BUF',
    3 : 'CHI',
    4 : 'CIN',
    5 : 'CLE',
    6 : 'DAL',
    7 : 'DEN',
    8 : 'DET',
    9 : 'GB',
    10: 'TEN',
    11: 'IND',
    12: 'KC',
    13: 'LV',
    14: 'LAR',
    15: 'MIA',
    16: 'MIN',
    17: 'NE',
    18: 'NO',
    19: 'NYG',
    20: 'NYJ',
    21: 'PHI',
    22: 'ARI',
    23: 'PIT',
    24: 'LAC',
    25: 'SF',
    26: 'SEA',
    27: 'TB',
    28: 'WSH',
    29: 'CAR',
    30: 'JAX',
    33: 'BAL',
    34: 'HOU'
]

@Field Map INJURY_STATUS_MAP = [
    'INJURY_RESERVE' : 'IR',
    'QUESTIONABLE' : 'Q',
    'DOUBTFUL' : 'D',
    'OUT' : 'O',
    'PROBABLE' : 'P'
]

@Field Map ACTIVITY_MAP = [
    178: 'FA ADDED',
    180: 'WAIVER ADDED',
    179: 'DROPPED',
    181: 'DROPPED',
    239: 'DROPPED',
    244: 'TRADED',
    'FA': 178,
    'WAIVER': 180,
    'TRADED': 244
]

@Field Map PLAYER_STATS_MAP = [
    // Passing Stats
    0: 'passingAttempts', // PA
    1: 'passingCompletions', // PC
    2: 'passingIncompletions', // INC
    3: 'passingYards', // PY
    4: 'passingTouchdowns', // PTD
    // 5-14 appear for passing players
    // 5-7: 6 is half of 5 (integer divide by 2), 7 is half of 6 (integer divide by 2)
    // 8-10: 9 is half of 8 (integer divide by 2), 10 is half of 9 (integer divide by 2)
    // 11-12: 12 is half of 11 (integer divide by 2)
    // 13-14: 14 is half of 13 (integer divide by 2)
    15: 'passing40PlusYardTD', // PTD40
    16: 'passing50PlusYardTD', // PTD50
    17: 'passing300To399YardGame', // P300
    18: 'passing400PlusYardGame', // P400
    19: 'passing2PtConversions', // 2PC
    20: 'passingInterceptions', // INT
    21: 'passingCompletionPercentage',
    22: 'passingYards', // PY - TODO: figure out what the difference is between 22 and 3

    // Rushing Stats
    23: 'rushingAttempts', // RA
    24: 'rushingYards', // RY
    25: 'rushingTouchdowns', // RTD
    26: 'rushing2PtConversions', // 2PR
    // 27-34 appear for rushing players
    // 27-29: 28 is half of 27 (integer divide by 2), 29 is half of 28 (integer divide by 2)
    // 30-32: 31 is half of 30 (integer divide by 2), 32 is half of 31 (integer divide by 2)
    // 33-34: 34 is half of 33 (integer divide by 2)
    35: 'rushing40PlusYardTD', // RTD40
    36: 'rushing50PlusYardTD', // RTD50
    37: 'rushing100To199YardGame', // RY100
    38: 'rushing200PlusYardGame', // RY200
    39: 'rushingYardsPerAttempt',
    40: 'rushingYards', // RY - TODO: figure out what the difference is between 40 and 24

    // Receiving Stats
    41: 'receivingReceptions', // REC
    42: 'receivingYards', // REY
    43: 'receivingTouchdowns', // RETD
    44: 'receiving2PtConversions', // 2PRE
    45: 'receiving40PlusYardTD', // RETD40
    46: 'receiving50PlusYardTD', // RETD50
    // 47-52 appear for receiving players
    // 47-49: 48 is half of 47 (integer divide by 2), 49 is half of 48 (integer divide by 2)
    // 50-52: 51 is half of 50 (integer divide by 2), 52 is half of 51 (integer divide by 2)
    53: 'receivingReceptions', // REC - TODO: figure out what the difference is between 53 and 41
    // 54-55 appear for receiving players
    // 54-55: 55 is half of 54 (integer divide by 2)
    56: 'receiving100To199YardGame', // REY100
    57: 'receiving200PlusYardGame', // REY200
    58: 'receivingTargets', // RET
    59: 'receivingYardsAfterCatch',
    60: 'receivingYardsPerReception',
    61: 'receivingYards', // REY - TODO: figure out what the difference is between 61 and 42
    62: '2PtConversions',
    63: 'fumbleRecoveredForTD', // FTD
    64: 'passingTimesSacked', // SK

    68: 'fumbles', // FUM

    72: 'lostFumbles', // FUML
    73: 'turnovers',

    // Kicking Stats
    74: 'madeFieldGoalsFrom50Plus', // FG50 (does not map directly to FG50 as FG50 does not include 60+)
    75: 'attemptedFieldGoalsFrom50Plus', // FGA50 (does not map directly to FGA50 as FG50 does not include 60+)
    76: 'missedFieldGoalsFrom50Plus', // FGM50 (does not map directly to FGM50 as FG50 does not include 60+)
    77: 'madeFieldGoalsFrom40To49', // FG40
    78: 'attemptedFieldGoalsFrom40To49', // FGA40
    79: 'missedFieldGoalsFrom40To49', // FGM40
    80: 'madeFieldGoalsFromUnder40', // FG0
    81: 'attemptedFieldGoalsFromUnder40', // FGA0
    82: 'missedFieldGoalsFromUnder40', // FGM0
    83: 'madeFieldGoals', // FG
    84: 'attemptedFieldGoals', // FGA
    85: 'missedFieldGoals', // FGM
    86: 'madeExtraPoints', // PAT
    87: 'attemptedExtraPoints', // PATA
    88: 'missedExtraPoints', // PATM

    // Defensive Stats
    89: 'defensive0PointsAllowed', // PA0
    90: 'defensive1To6PointsAllowed', // PA1
    91: 'defensive7To13PointsAllowed', // PA7
    92: 'defensive14To17PointsAllowed', // PA14
    93: 'defensiveBlockedKickForTouchdowns', // BLKKRTD
    94: 'defensiveTouchdowns', // Does not include defensive blocked kick for touchdowns (BLKKRTD)
    95: 'defensiveInterceptions', // INT
    96: 'defensiveFumbles', // FR
    97: 'defensiveBlockedKicks', // BLKK
    98: 'defensiveSafeties', // SF
    99: 'defensiveSacks', // SK
    // 100: This appears to be defensiveSacks * 2
    101: 'kickoffReturnTouchdowns', // KRTD
    102: 'puntReturnTouchdowns', // PRTD
    103: 'interceptionReturnTouchdowns', // INTTD
    104: 'fumbleReturnTouchdowns', // FRTD
    105: 'defensivePlusSpecialTeamsTouchdowns', // Includes defensive blocked kick for touchdowns (BLKKRTD) and kickoff/punt return touchdowns
    106: 'defensiveForcedFumbles', // FF
    107: 'defensiveAssistedTackles', // TKA
    108: 'defensiveSoloTackles', // TKS
    109: 'defensiveTotalTackles', // TK

    113: 'defensivePassesDefensed', // PD
    114: 'kickoffReturnYards', // KR
    115: 'puntReturnYards', // PR

    118: 'puntsReturned', // PTR

    120: 'defensivePointsAllowed', // PA
    121: 'defensive18To21PointsAllowed', // PA18
    122: 'defensive22To27PointsAllowed', // PA22
    123: 'defensive28To34PointsAllowed', // PA28
    124: 'defensive35To45PointsAllowed', // PA35
    125: 'defensive45PlusPointsAllowed', // PA46

    127: 'defensiveYardsAllowed', // YA
    128: 'defensiveLessThan100YardsAllowed', //YA100
    129: 'defensive100To199YardsAllowed', // YA199
    130: 'defensive200To299YardsAllowed', // YA299
    131: 'defensive300To349YardsAllowed', // YA349
    132: 'defensive350To399YardsAllowed', // YA399
    133: 'defensive400To449YardsAllowed', // YA449
    134: 'defensive450To499YardsAllowed', // YA499
    135: 'defensive500To549YardsAllowed', // YA549
    136: 'defensive550PlusYardsAllowed', // YA550

    // Punter Stats
    138: 'netPunts', // PT
    139: 'puntYards', // PTY
    140: 'puntsInsideThe10', // PT10
    141: 'puntsInsideThe20', // PT20
    142: 'blockedPunts', // PTB
    145: 'puntTouchbacks', // PTTB
    146: 'puntFairCatches', //PTFC
    147: 'puntAverage',
    148: 'puntAverage44.0+', // PTA44
    149: 'puntAverage42.0-43.9', //PTA42
    150: 'puntAverage40.0-41.9', //PTA40
    151: 'puntAverage38.0-39.9', //PTA38
    152: 'puntAverage36.0-37.9', //PTA36
    153: 'puntAverage34.0-35.9', //PTA34
    154: 'puntAverage33.9AndUnder', //PTA33

    // Head Coach Stats
    155: 'teamWin', // TW
    156: 'teamLoss', // TL
    157: 'teamTie', // TIE
    158: 'pointsScored', // PTS

    160: 'pointsMargin',
    161: '25+pointWinMargin', // WM25
    162: '20-24pointWinMargin', // WM20
    163: '15-19pointWinMargin', // WM15
    164: '10-14pointWinMargin', // WM10
    165: '5-9pointWinMargin', // WM5
    166: '1-4pointWinMargin', // WM1
    167: '1-4pointLossMargin', // LM1
    168: '5-9pointLossMargin', // LM5
    169: '10-14pointLossMargin', // LM10
    170: '15-19pointLossMargin', // LM15
    171: '20-24pointLossMargin', // LM20
    172: '25+pointLossMargin', // LM25
    174: 'winPercentage', // Value goes from 0-1

    187: 'defensivePointsAllowed', // TODO: figure out what the difference is between 187 and 120

    201: 'madeFieldGoalsFrom60Plus', // FG60
    202: 'attemptedFieldGoalsFrom60Plus', // FGA60
    203: 'missedFieldGoalsFrom60Plus', // FGM60

    205: 'defensive2PtReturns', // 2PTRET
    206: 'defensive2PtReturns', // 2PTRET - TODO: figure out what the difference is between 206 and 205
]

@Field Map SETTINGS_SCORING_FORMAT_MAP = [
    0: [ 'abbr': 'PA', 'label': 'Each Pass Attempted' ],
    1: [ 'abbr': 'PC', 'label': 'Each Pass Completed' ],
    2: [ 'abbr': 'INC', 'label': 'Each Incomplete Pass' ],
    3: [ 'abbr': 'PY', 'label': 'Passing Yards' ],
    4: [ 'abbr': 'PTD', 'label': 'TD Pass' ],
    5: [ 'abbr': 'PY5', 'label': 'Every 5 passing yards' ],
    6: [ 'abbr': 'PY10', 'label': 'Every 10 passing yards' ],
    7: [ 'abbr': 'PY20', 'label': 'Every 20 passing yards' ],
    8: [ 'abbr': 'PY25', 'label': 'Every 25 passing yards' ],
    9: [ 'abbr': 'PY50', 'label': 'Every 50 passing yards' ],
    10: [ 'abbr': 'PY100', 'label': 'Every 100 passing yards' ],
    11: [ 'abbr': 'PC5', 'label': 'Every 5 pass completions' ],
    12: [ 'abbr': 'PC10', 'label': 'Every 10 pass completions' ],
    13: [ 'abbr': 'IP5', 'label': 'Every 5 pass incompletions' ],
    14: [ 'abbr': 'IP10', 'label': 'Every 10 pass incompletions' ],
    15: [ 'abbr': 'PTD40', 'label': '40+ yard TD pass bonus' ],
    16: [ 'abbr': 'PTD50', 'label': '50+ yard TD pass bonus' ],
    17: [ 'abbr': 'P300', 'label': '300-399 yard passing game' ],
    18: [ 'abbr': 'P400', 'label': '400+ yard passing game' ],
    19: [ 'abbr': '2PC', 'label': '2pt Passing Conversion' ],
    20: [ 'abbr': 'INTT', 'label': 'Interceptions Thrown' ],
    21: [ 'abbr': 'CPCT', 'label': 'Passing Completion Pct' ],
    22: [ 'abbr': 'PYPG', 'label': 'Passing Yards Per Game' ],
    23: [ 'abbr': 'RA', 'label': 'Rushing Attempts' ],
    24: [ 'abbr': 'RY', 'label': 'Rushing Yards' ],
    25: [ 'abbr': 'RTD', 'label': 'TD Rush' ],
    26: [ 'abbr': '2PR', 'label': '2pt Rushing Conversion' ],
    27: [ 'abbr': 'RY5', 'label': 'Every 5 rushing yards' ],
    28: [ 'abbr': 'RY10', 'label': 'Every 10 rushing yards' ],
    29: [ 'abbr': 'RY20', 'label': 'Every 20 rushing yards' ],
    30: [ 'abbr': 'RY25', 'label': 'Every 25 rushing yards' ],
    31: [ 'abbr': 'RY50', 'label': 'Every 50 rushing yards' ],
    32: [ 'abbr': 'R100', 'label': 'Every 100 rushing yards' ],
    33: [ 'abbr': 'RA5', 'label': 'Every 5 rush attempts' ],
    34: [ 'abbr': 'RA10', 'label': 'Every 10 rush attempts' ],
    35: [ 'abbr': 'RTD40', 'label': '40+ yard TD rush bonus' ],
    36: [ 'abbr': 'RTD50', 'label': '50+ yard TD rush bonus' ],
    37: [ 'abbr': 'RY100', 'label': '100-199 yard rushing game' ],
    38: [ 'abbr': 'RY200', 'label': '200+ yard rushing game' ],
    39: [ 'abbr': 'RYPA', 'label': 'Rushing Yards Per Attempt' ],
    40: [ 'abbr': 'RYPG', 'label': 'Rushing Yards Per Game' ],
    41: [ 'abbr': 'RECS', 'label': 'Receptions' ],
    42: [ 'abbr': 'REY', 'label': 'Receiving Yards' ],
    43: [ 'abbr': 'RETD', 'label': 'TD Reception' ],
    44: [ 'abbr': '2PRE', 'label': '2pt Receiving Conversion' ],
    45: [ 'abbr': 'RETD40', 'label': '40+ yard TD rec bonus' ],
    46: [ 'abbr': 'RETD50', 'label': '50+ yard TD rec bonus' ],
    47: [ 'abbr': 'REY5', 'label': 'Every 5 receiving yards' ],
    48: [ 'abbr': 'REY10', 'label': 'Every 10 receiving yards' ],
    49: [ 'abbr': 'REY20', 'label': 'Every 20 receiving yards' ],
    50: [ 'abbr': 'REY25', 'label': 'Every 25 receiving yards' ],
    51: [ 'abbr': 'REY50', 'label': 'Every 50 receiving yards' ],
    52: [ 'abbr': 'RE100', 'label': 'Every 100 receiving yards' ],
    53: [ 'abbr': 'REC', 'label': 'Each reception' ],
    54: [ 'abbr': 'REC5', 'label': 'Every 5 receptions'],
    55: [ 'abbr': 'REC10', 'label': 'Every 10 receptions' ],
    56: [ 'abbr': 'REY100', 'label': '100-199 yard receiving game' ],
    57: [ 'abbr': 'REY200', 'label': '200+ yard receiving game' ],
    58: [ 'abbr': 'RET', 'label': 'Receiving Target' ],
    59: [ 'abbr': 'YAC', 'label': 'Receiving Yards After Catch' ],
    60: [ 'abbr': 'YPC', 'label': 'Receiving Yards Per Catch' ],
    61: [ 'abbr': 'REYPG', 'label': 'Receiving Yards Per Game' ],
    62: [ 'abbr': 'PTL', 'label': 'Total 2pt Conversions' ],
    63: [ 'abbr': 'FTD', 'label': 'Fumble Recovered for TD' ],
    64: [ 'abbr': 'SKD', 'label': 'Sacked' ],
    65: [ 'abbr': 'PFUM', 'label': 'Passing Fumbles' ],
    66: [ 'abbr': 'RFUM', 'label': 'Rushing Fumbles' ],
    67: [ 'abbr': 'REFUM', 'label': 'Receiving Fumbles' ],
    68: [ 'abbr': 'FUM', 'label': 'Total Fumbles' ],
    69: [ 'abbr': 'PFUML', 'label': 'Passing Fumbles Lost' ],
    70: [ 'abbr': 'RFUML', 'label': 'Rushing Fumbles Lost' ],
    71: [ 'abbr': 'REFUML', 'label': 'Receiving Fumbles Lost' ],
    72: [ 'abbr': 'FUML', 'label': 'Total Fumbles Lost' ],
    73: [ 'abbr': 'TT', 'label': 'Total Turnovers' ],
    74: [ 'abbr': 'FG50P', 'label': 'FG Made (50+ yards)' ],
    75: [ 'abbr': 'FGA50P', 'label': 'FG Attempted (50+ yards)' ],
    76: [ 'abbr': 'FGM50P', 'label': 'FG Missed (50+ yards)' ],
    77: [ 'abbr': 'FG40', 'label': 'FG Made (40-49 yards)' ],
    78: [ 'abbr': 'FGA40', 'label': 'FG Attempted (40-49 yards)' ],
    79: [ 'abbr': 'FGM40', 'label': 'FG Missed (40-49 yards)' ],
    80: [ 'abbr': 'FG0', 'label': 'FG Made (0-39 yards)' ],
    81: [ 'abbr': 'FGA0', 'label': 'FG Attempted (0-39 yards)' ],
    82: [ 'abbr': 'FGM0', 'label': 'FG Missed (0-39 yards)' ],
    83: [ 'abbr': 'FG', 'label': 'Total FG Made' ],
    84: [ 'abbr': 'FGA', 'label': 'Total FG Attempted' ],
    85: [ 'abbr': 'FGM', 'label': 'Total FG Missed' ],
    86: [ 'abbr': 'PAT', 'label': 'Each PAT Made' ],
    87: [ 'abbr': 'PATA', 'label': 'Each PAT Attempted' ],
    88: [ 'abbr': 'PATM', 'label': 'Each PAT Missed' ],
    89: [ 'abbr': 'PA0', 'label': '0 points allowed' ],
    90: [ 'abbr': 'PA1', 'label': '1-6 points allowed' ],
    91: [ 'abbr': 'PA7', 'label': '7-13 points allowed' ],
    92: [ 'abbr': 'PA14', 'label': '14-17 points allowed' ],
    93: [ 'abbr': 'BLKKRTD', 'label': 'Blocked Punt or FG return for TD' ],
    94: [ 'abbr': 'DEFRETTD', 'label': 'Fumble or INT Return for TD' ],
    95: [ 'abbr': 'INT', 'label': 'Each Interception' ],
    96: [ 'abbr': 'FR', 'label': 'Each Fumble Recovered' ],
    97: [ 'abbr': 'BLKK', 'label': 'Blocked Punt, PAT or FG' ],
    98: [ 'abbr': 'SF', 'label': 'Each Safety' ],
    99: [ 'abbr': 'SK', 'label': 'Each Sack' ],
    100: [ 'abbr': 'HALFSK', 'label': '1/2 Sack' ],
    101: [ 'abbr': 'KRTD', 'label': 'Kickoff Return TD' ],
    102: [ 'abbr': 'PRTD', 'label': 'Punt Return TD' ],
    103: [ 'abbr': 'INTTD', 'label': 'Interception Return TD' ],
    104: [ 'abbr': 'FRTD', 'label': 'Fumble Return TD' ],
    105: [ 'abbr': 'TRTD', 'label': 'Total Return TD' ],
    106: [ 'abbr': 'FF', 'label': 'Each Fumble Forced' ],
    107: [ 'abbr': 'TKA', 'label': 'Assisted Tackles' ],
    108: [ 'abbr': 'TKS', 'label': 'Solo Tackles' ],
    109: [ 'abbr': 'TK', 'label': 'Total Tackles' ],
    110: [ 'abbr': 'TK3', 'label': 'Every 3 Total Tackles' ],
    111: [ 'abbr': 'TK5', 'label': 'Every 5 Total Tackles' ],
    112: [ 'abbr': 'STF', 'label': 'Stuffs' ],
    113: [ 'abbr': 'PD', 'label': 'Passes Defensed' ],
    114: [ 'abbr': 'KR', 'label': 'Kickoff Return Yards' ],
    115: [ 'abbr': 'PR', 'label': 'Punt Return Yards' ],
    116: [ 'abbr': 'KR10', 'label': 'Every 10 kickoff return yards' ],
    117: [ 'abbr': 'KR25', 'label': 'Every 25 kickoff return yards' ],
    118: [ 'abbr': 'PR10', 'label': 'Every 10 punt return yards' ],
    119: [ 'abbr': 'PR25', 'label': 'Every 25 punt return yards' ],
    120: [ 'abbr': 'PTSA', 'label': 'Points Allowed' ],
    121: [ 'abbr': 'PA18', 'label': '18-21 points allowed' ],
    122: [ 'abbr': 'PA22', 'label': '22-27 points allowed' ],
    123: [ 'abbr': 'PA28', 'label': '28-34 points allowed' ],
    124: [ 'abbr': 'PA35', 'label': '35-45 points allowed' ],
    125: [ 'abbr': 'PA46', 'label': '46+ points allowed' ],
    126: [ 'abbr': 'PAPG', 'label': 'Points Allowed Per Game' ],
    127: [ 'abbr': 'YA', 'label': 'Yards Allowed' ],
    128: [ 'abbr': 'YA100', 'label': 'Less than 100 total yards allowed' ],
    129: [ 'abbr': 'YA199', 'label': '100-199 total yards allowed' ],
    130: [ 'abbr': 'YA299', 'label': '200-299 total yards allowed' ],
    131: [ 'abbr': 'YA349', 'label': '300-349 total yards allowed' ],
    132: [ 'abbr': 'YA399', 'label': '350-399 total yards allowed' ],
    133: [ 'abbr': 'YA449', 'label': '400-449 total yards allowed' ],
    134: [ 'abbr': 'YA499', 'label': '450-499 total yards allowed' ],
    135: [ 'abbr': 'YA549', 'label': '500-549 total yards allowed' ],
    136: [ 'abbr': 'YA550', 'label': '550+ total yards allowed' ],
    137: [ 'abbr': 'YAPG', 'label': 'Yards Allowed Per Game' ],
    138: [ 'abbr': 'PT', 'label': 'Net Punts' ],
    139: [ 'abbr': 'PTY', 'label': 'Punt Yards' ],
    140: [ 'abbr': 'PT10', 'label': 'Punts Inside the 10' ],
    141: [ 'abbr': 'PT20', 'label': 'Punts Inside the 20' ],
    142: [ 'abbr': 'PTB', 'label': 'Blocked Punts' ],
    143: [ 'abbr': 'PTR', 'label': 'Punts Returned' ],
    144: [ 'abbr': 'PTRY', 'label': 'Punt Return Yards' ],
    145: [ 'abbr': 'PTTB', 'label': 'Touchbacks' ],
    146: [ 'abbr': 'PTFC', 'label': 'Fair Catches' ],
    147: [ 'abbr': 'PTAVG', 'label': 'Punt Average' ],
    148: [ 'abbr': 'PTA44', 'label': 'Punt Average 44.0+' ],
    149: [ 'abbr': 'PTA42', 'label': 'Punt Average 42.0-43.9' ],
    150: [ 'abbr': 'PTA40', 'label': 'Punt Average 40.0-41.9' ],
    151: [ 'abbr': 'PTA38', 'label': 'Punt Average 38.0-39.9' ],
    152: [ 'abbr': 'PTA36', 'label': 'Punt Average 36.0-37.9' ],
    153: [ 'abbr': 'PTA34', 'label': 'Punt Average 34.0-35.9' ],
    154: [ 'abbr': 'PTA33', 'label': 'Punt Average 33.9 or less' ],
    155: [ 'abbr': 'TW', 'label': 'Team Win' ],
    156: [ 'abbr': 'TL', 'label': 'Team Loss' ],
    157: [ 'abbr': 'TIE', 'label': 'Team Tie' ],
    158: [ 'abbr': 'PTS', 'label': 'Points Scored' ],
    159: [ 'abbr': 'PPG', 'label': 'Points Scored Per Game' ],
    160: [ 'abbr': 'MGN', 'label': 'Margin of Victory' ],
    161: [ 'abbr': 'WM25', 'label': '25+ point Win Margin' ],
    162: [ 'abbr': 'WM20', 'label': '20-24 point Win Margin' ],
    163: [ 'abbr': 'WM15', 'label': '15-19 point Win Margin' ],
    164: [ 'abbr': 'WM10', 'label': '10-14 point Win Margin' ],
    165: [ 'abbr': 'WM5', 'label': '5-9 point Win Margin' ],
    166: [ 'abbr': 'WM1', 'label': '1-4 point Win Margin' ],
    167: [ 'abbr': 'LM1', 'label': '1-4 point Loss Margin' ],
    168: [ 'abbr': 'LM5', 'label': '5-9 point Loss Margin' ],
    169: [ 'abbr': 'LM10', 'label': '10-14 point Loss Margin' ],
    170: [ 'abbr': 'LM15', 'label': '15-19 point Loss Margin' ],
    171: [ 'abbr': 'LM20', 'label': '20-24 point Loss Margin' ],
    172: [ 'abbr': 'LM25', 'label': '25+ point Loss Margin' ],
    173: [ 'abbr': 'MGNPG', 'label': 'Margin of Victory Per Game' ],
    174: [ 'abbr': 'WINPCT', 'label': 'Winning Pct' ],
    175: [ 'abbr': 'PTD0', 'label': '0-9 yd TD pass bonus' ],
    176: [ 'abbr': 'PTD10', 'label': '10-19 yd TD pass bonus' ],
    177: [ 'abbr': 'PTD20', 'label': '20-29 yd TD pass bonus' ],
    178: [ 'abbr': 'PTD30', 'label': '30-39 yd TD pass bonus' ],
    179: [ 'abbr': 'RTD0', 'label': '0-9 yd TD rush bonus' ],
    180: [ 'abbr': 'RTD10', 'label': '10-19 yd TD rush bonus' ],
    181: [ 'abbr': 'RTD20', 'label': '20-29 yd TD rush bonus' ],
    182: [ 'abbr': 'RTD30', 'label': '30-39 yd TD rush bonus' ],
    183: [ 'abbr': 'RETD0', 'label': '0-9 yd TD rec bonus' ],
    184: [ 'abbr': 'RETD10', 'label': '10-19 yd TD rec bonus' ],
    185: [ 'abbr': 'RETD20', 'label': '20-29 yd TD rec bonus' ],
    186: [ 'abbr': 'RETD30', 'label': '30-39 yd TD rec bonus' ],
    187: [ 'abbr': 'DPTSA', 'label': 'D/ST Points Allowed' ],
    188: [ 'abbr': 'DPA0', 'label': 'D/ST 0 points allowed' ],
    189: [ 'abbr': 'DPA1', 'label': 'D/ST 1-6 points allowed' ],
    190: [ 'abbr': 'DPA7', 'label': 'D/ST 7-13 points allowed' ],
    191: [ 'abbr': 'DPA14', 'label': 'D/ST 14-17 points allowed' ],
    192: [ 'abbr': 'DPA18', 'label': 'D/ST 18-21 points allowed' ],
    193: [ 'abbr': 'DPA22', 'label': 'D/ST 22-27 points allowed' ],
    194: [ 'abbr': 'DPA28', 'label': 'D/ST 28-34 points allowed' ],
    195: [ 'abbr': 'DPA35', 'label': 'D/ST 35-45 points allowed' ],
    196: [ 'abbr': 'DPA46', 'label': 'D/ST 46+ points allowed' ],
    197: [ 'abbr': 'DPAPG', 'label': 'D/ST Points Allowed Per Game' ],
    198: [ 'abbr': 'FG50', 'label': 'FG Made (50-59 yards)' ],
    199: [ 'abbr': 'FGA50', 'label': 'FG Attempted (50-59 yards)' ],
    200: [ 'abbr': 'FGM50', 'label': 'FG Missed (50-59 yards)' ],
    201: [ 'abbr': 'FG60', 'label': 'FG Made (60+ yards)' ],
    202: [ 'abbr': 'FGA60', 'label': 'FG Attempted (60+ yards)' ],
    203: [ 'abbr': 'FGM60', 'label': 'FG Missed (60+ yards)' ],
    204: [ 'abbr': 'O2PRET', 'label': 'Offensive 2pt Return' ],
    205: [ 'abbr': 'D2PRET', 'label': 'Defensive 2pt Return' ],
    206: [ 'abbr': '2PRET', 'label': '2pt Return' ],
    207: [ 'abbr': 'O1PSF', 'label': 'Offensive 1pt Safety' ],
    208: [ 'abbr': 'D1PSF', 'label': 'Defensive 1pt Safety' ],
    209: [ 'abbr': '1PSF', 'label': '1pt Safety' ],
    210: [ 'abbr': 'GP', 'label': 'Games Played' ],
    211: [ 'abbr': 'PFD', 'label': 'Passing First Down' ],
    212: [ 'abbr': 'RFD', 'label': 'Rushing First Down' ],
    213: [ 'abbr': 'REFD', 'label': 'Receiving First Down' ],
    214: [ 'abbr': 'FGY', 'label': 'FG Made Yards' ],
    215: [ 'abbr': 'FGMY', 'label': 'FG Missed Yards' ],
    216: [ 'abbr': 'FGAY', 'label': 'FG Attempt Yards' ],
    217: [ 'abbr': 'FGY5', 'label': 'Every 5 FG Made yards' ],
    218: [ 'abbr': 'FGY10', 'label': 'Every 10 FG Made yards' ],
    219: [ 'abbr': 'FGY20', 'label': 'Every 20 FG Made yards' ],
    220: [ 'abbr': 'FGY25', 'label': 'Every 25 FG Made yards' ],
    221: [ 'abbr': 'FGY50', 'label': 'Every 50 FG Made yards' ],
    222: [ 'abbr': 'FGY100', 'label': 'Every 100 FG Made yards' ],
    223: [ 'abbr': 'FGMY5', 'label': 'Every 5 FG Missed yards' ],
    224: [ 'abbr': 'FGMY10', 'label': 'Every 10 FG Missed yards' ],
    225: [ 'abbr': 'FGMY20', 'label': 'Every 20 FG Missed yards' ],
    226: [ 'abbr': 'FGMY25', 'label': 'Every 25 FG Missed yards' ],
    227: [ 'abbr': 'FGMY50', 'label': 'Every 50 FG Missed yards' ],
    228: [ 'abbr': 'FGMY100', 'label': 'Every 100 FG Missed yards' ],
    229: [ 'abbr': 'FGAY5', 'label': 'Every 5 FG Attempt yards' ],
    230: [ 'abbr': 'FGAY10', 'label': 'Every 10 FG Attempt yards' ],
    231: [ 'abbr': 'FGAY20', 'label': 'Every 20 FG Attempt yards' ],
    232: [ 'abbr': 'FGAY25', 'label': 'Every 25 FG Attempt yards' ],
    233: [ 'abbr': 'FGAY50', 'label': 'Every 50 FG Attempt yards' ],
    234: [ 'abbr': 'FGAY100', 'label': 'Every 100 FG Attempt yards' ]
]