//THIS IS A WORK IN PROGRESS AND CURRENTLY DOES NOT WORK

//Parts Working: Menu 
//Setting Light color a set time

/**
 *  Change Color With Time
 *
 *  Copyright 2016 Dbhjed
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
 */
definition(
    name: "Change Color With Time",
    namespace: "dbhjed",
    author: "Dbhjed",
    description: "This SmartApp will change the color of your selected based on either a color list or Random Color using time to turn the lights off and on",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
page(name: "timeIntervalInput", title: "Only during a certain time") {
		section {
			input "starting", "time", title: "Starting", required: false
            //TODO: have an option to set with Sunset/Rise with offset
			input "ending", "time", title: "Ending", required: false
            //TODO: have an option to set with Sunset/Rise with offset
		}
}
	section("Choose lights you wish to control?") {
            input "hues", "capability.colorControl", title: "Which Color Changing Bulbs?", multiple:true, required: true
        	input "brightnessLevel", "number", title: "Brightness Level (1-100)?", required:false, defaultValue:100 //Select brightness
	}
    
    	section("Choose light effects..."){
			input "color", "enum", title: "Hue Color?", required: false, multiple:true, defaultValue: "Soft White", options: [
					["Soft White":"Soft White"],
					["White":"White"],
					["Daylight":"Daylight"],
					["Warm White":"Warm White"],
					["Red":"Red"],
                	["Green":"Green"],
                	["Blue":"Blue"],
                	["Yellow":"Yellow"],
                	["Orange":"Orange"],
                	["Purple":"Purple"],
                	["Pink":"Pink"]
                ]
                //TO DO: have this set if no values are selected
            input "randomMode","bool", title: "Enable Random Mode?", required: true, defaultValue: false
	}
    
section("Choose cycle time between color changes? ") {
            input "cycletime", "enum", title: "Cycle time in minutes?" , options: [
                "5",
				"10", 
				"15", 
				"30", 
				"1 hour", 
				"3 hours"
			], required: true, defaultValue: "10"
	}
    

section("Schedule", ) {
			input "starting", "time", title: "Starting", required: false
			input "ending", "time", title: "Ending", required: false
	}
    
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
    log.debug(" in initialize() for $app.label with settings: ${settings}")
    subscribe(hues, "switch.on", changeHandler) 

    
	schedule(starting, scheduledTimeHandler)
    schedule(ending, scheduledTimeHandler)

	
    switch (settings.cycletime)
    {
     case "5":
     	log.debug "switching color every 5 minutes."
     	runEvery5Minutes(changeHandler)
     break;
     case "10":
     	log.debug "switching color every 10 minutes."
     	runEvery10Minutes(changeHandler)
     break;
     case "15":
     	log.debug "switching color every 15 minutes."
     	runEvery15Minutes(changeHandler)
     break;
     case "30":
     	log.debug "switching color every 30 minutes."
     	runEvery30Minutes(changeHandler)
     break;
     case "1 hour":
     	log.debug "switching color every hour."
     	runEvery1Hour(changeHandler)
     break;
     case  "3 hours":
     	log.debug "switching color every 3 hours"
     	runEvery3Hours(changeHandler)
     break;
      
     default:
     	log.debug "switching color every 30 minutes."
     	runEvery30Minutes(changeHandler)
     break;
     
    }
}

// TODO: implement event handlers

def subscribeToEvents() {

schedule(starting, scheduledTimeHandler)
schedule(ending, scheduledTimeHandler)

}

def scheduledTimeHandler() {
	log.trace "scheduledTimeHandler()"
    log.debug "handler called at ${new Date()}"
    
    def sunRiseSet = getSunriseAndSunset()
    def sunriseTime = sunRiseSet.sunrise
    def sunsetTime = sunRiseSet.sunset
        
    log.debug "sunrise time ${sunriseTime}"
    log.debug "sunset time ${sunsetTime}"
    
}

def takeAction(evt) {

	def hueColor = 0
	def saturation = 100

	switch(color) {
		case "White":
			hueColor = 52
			saturation = 19
			break;
		case "Daylight":
			hueColor = 53
			saturation = 91
			break;
		case "Soft White":
			hueColor = 23
			saturation = 56
			break;
		case "Warm White":
			hueColor = 20
			saturation = 80 //83
			break;
		case "Blue":
			hueColor = 70
			break;
		case "Green":
			hueColor = 39
			break;
		case "Yellow":
			hueColor = 25
			break;
		case "Orange":
			hueColor = 10
			break;
		case "Purple":
			hueColor = 75
			break;
		case "Pink":
			hueColor = 83
			break;
		case "Red":
			hueColor = 100
			break;
	}

	state.previous = [:]

	hues.each {
		state.previous[it.id] = [
			"switch": it.currentValue("switch"),
			"level" : it.currentValue("level"),
			"hue": it.currentValue("hue"),
			"saturation": it.currentValue("saturation")
		]
	}

	log.debug "current values = $state.previous"

	def newValue = [hue: hueColor, saturation: saturation, level: lightLevel as Integer ?: 100]
	log.debug "new value = $newValue"

	hues*.setColor(newValue)
}

def changeHandler(evt) {

	log.debug "in change handler"
  	
    if (hues)
    {
    	 def currSwitches = hues.currentSwitch
         def onHues = currSwitches.findAll { switchVal -> switchVal == "on" ? true : false }
         def numberon = onHues.size();
         def onstr = numberon.toString() 
         
       log.debug "found $onstr that were on!"
    
    if (onHues.size() > 0)
    {
      def newColor = ""
      if (settings.randomMode == true)
       {
        def int nextValue = new Random().nextInt(16)
        def colorArray = ["Red","Brick Red","Safety Orange","Orange","Amber","Yellow","Green","Turquoise","Aqua","Navy Blue","Blue","Indigo","Purple","Pink","Rasberry","White"]
             
        log.debug "Random Number = $nextValue"
        newColor = colorArray[nextValue]    
       }
       
      else
      
      { // not random
      
	  def currentColor = state.currentColor
      
    log.debug " in changeHandler got current color = $currentColor"
		//set color user set values
        //Loop through array to do it
    } // end random or not
    
      log.debug "After Check new color = $newColor"

      hues.on()
      sendcolor(newColor)
      }
   }
}


def sendcolor(color)
{
log.debug "In send color"
	//Initialize the hue and saturation
	def hueColor = 0
	def saturation = 100

	//Use the user specified brightness level. If they exceeded the min or max values, overwrite the brightness with the actual min/max
	if (brightnessLevel<1) {
		brightnessLevel=1
	}
    else if (brightnessLevel>100) {
		brightnessLevel=100
	}

	//Set the hue and saturation for the specified color.
	switch(color) {
		case "White":
			hueColor = 0
			saturation = 0
			break;
		case "Daylight":
			hueColor = 53
			saturation = 91
			break;
		case "Soft White":
			hueColor = 23
			saturation = 56
			break;
		case "Warm White":
			hueColor = 20
			saturation = 80 
			break;
        case "Navy Blue":
            hueColor = 61
            break;
		case "Blue":
			hueColor = 65
			break;
		case "Green":
			hueColor = 33
			break;
        case "Turquoise":
        	hueColor = 47
            break;
        case "Aqua":
            hueColor = 50
            break;
        case "Amber":
            hueColor = 13
            break;
		case "Yellow":
			//hueColor = 25
            hueColor = 17
			break; 
        case "Safety Orange":
            hueColor = 7
            break;
		case "Orange":
			hueColor = 10
			break;
        case "Indigo":
            hueColor = 73
            break;
		case "Purple":
			hueColor = 82
			saturation = 100
			break;
		case "Pink":
			hueColor = 90.78
			saturation = 67.84
			break;
        case "Rasberry":
            hueColor = 94
            break;
		case "Red":
			hueColor = 0
			break;
         case "Brick Red":
            hueColor = 4
            break;                
	}

	//Change the color of the light
	def newValue = [hue: hueColor, saturation: saturation, level: brightnessLevel]  
	hues*.setColor(newValue)
        state.currentColor = color
        mysend("$app.label: Setting Color = $color")
        log.debug "$app.label: Setting Color = $color"

}
 
 def TurnOff()
{

      mysend("$app.label: Turning Off!")
	
	hues.off()
}    

def TurnOn()
{
   // log.debug "In turn on"

     mysend("$app.label: Turning On!")
     hues.on()
    
}
