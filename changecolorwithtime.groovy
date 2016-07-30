//THIS IS A WORK IN PROGRESS, IT CURRENTLY DOES NOT WORK

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
			input "ending", "time", title: "Ending", required: false
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
			//TO DO: Add in sunrise/set with offset 
			input "ending", "time", title: "Ending", required: false
			//TO DO: Add in sunrise/set with offset 
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
	subscribeToEvents()
	
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
}

def scheduledTimeHandler() {
	log.trace "scheduledTimeHandler()"
	eventHandler()
}

private takeAction(evt) {

	def hueColor = 0
	def saturation = 100
	//TO DO: Figure out how to loop the array and set it back the first once it has cycled through all the selected colors
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

//TO DO: Add in if no color select, randomly select a color and change it based on the time interval selected.
