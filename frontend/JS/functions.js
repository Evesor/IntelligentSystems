// Websocket Server
var ws = new WebSocket("ws://localhost:4567/ws");

ws.onopen = function(data)
{
	alert("Connected...");
};

ws.onmessage = function(data) 
{ 
	try {
		var jsonData = JSON.parse(data.data);
		//console.log(jsonData);
		//console.log(jsonData.verboseLogs);
		//console.log(jsonData.verboseLogs[0].log);
		drawMessages(jsonData);
		console.log(update);
		update(jsonData);
	} catch (err) {}
};

ws.onclose = function()
{ 
	// Websocket is closed.
	alert("Connection is closed..."); 
};

// Random range bcuz javascript doesn't have one?
// Might not even be needed anymore...
function getRandomInt(min, max) {
	return Math.floor(Math.random() * (max - min + 1)) + min;
}

var prettyStringMap = {
	"name": "Name",
	"currentbuy_price": "Current Buy Price",
	"currentsell_price": "Current Sell Price",
	"currentpurchase_volume": "Current Purchase Volume",
	"currentsales_volume": "Current Sales Volume",
	"type": "Type"
}

function getAgentsFromNodes(a) {
	var textString = "";

	var link = document.getElementById("agent-data");
	var node = document.createElement("pre");

	for (var property in a) {
	    if (a.hasOwnProperty(property)) {
	        textString = textString + prettyStringMap[property];
	        textString = textString + ": " + a[property] + "\n";
	    }
	}

	var textNode = document.createTextNode(textString);

	node.appendChild(textNode);

	if (link.childNodes.length < 1) {
		link.appendChild(node);
	} else {
		link.replaceChild(node, link.childNodes[0]);
	}
}

//Draw the logging messages to the screen 
function drawMessages(messages) {
	var verboseLogs = messages.verboseLogs;
	var errorLogs = messages.errorLogs;
	var debugLogs = messages.debugLogs;
	var verboseList = document.getElementById("verbose-message-list");
	var errorList = document.getElementById("error-message-list");
	var debugList = document.getElementById("debug-message-list");
	addMessagesToList(verboseLogs, verboseList);
	addMessagesToList(errorLogs, errorList);
	addMessagesToList(debugLogs, debugList);
	document.getElementById("number-verbose-messages").innerHTML=verboseLogs.length;
	document.getElementById("number-error-messages").innerHTML=errorLogs.length;
	document.getElementById("number-debug-messages").innerHTML=debugLogs.length;
};


function addMessagesToList(messages, list) {
	list.innerHTML = '';
	console.log(messages[i]);
	for (var i = messages.length - 1; i > 0; i--) {
		var listItem = document.createElement('li');
		listItem.appendChild(document.createTextNode(messages[i].from + " :: " + "  @timeslice: " + messages[i].timeSlice + " :: " + messages[i].log));
		list.appendChild(listItem);
	}
}


