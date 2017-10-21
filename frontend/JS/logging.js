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
		drawMessages(jsonData);
		//drawStats(jsonData);
		//console.log(update);
		//update(jsonData);
	} catch (err) {}
};

ws.onclose = function()
{ 
	// Websocket is closed.
	alert("Connection is closed..."); 
};

//Draw the logging messages to the screen 
function drawMessages(messages) {
	var verboseLogs = messages.verboseLogs;
	var errorLogs = messages.errorLogs;
	var debugLogs = messages.debugLogs;
	var verboseList = document.getElementById("verbose-message-table");
	var errorList = document.getElementById("error-message-table");
	var debugList = document.getElementById("debug-message-table");
	addMessagesToList(verboseLogs, verboseList);
	addMessagesToList(errorLogs, errorList);
	addMessagesToList(debugLogs, debugList);
	document.getElementById("number-verbose-messages").innerHTML=verboseLogs.length;
	document.getElementById("number-error-messages").innerHTML=errorLogs.length;
	document.getElementById("number-debug-messages").innerHTML=debugLogs.length;
};

// Add the messages to the tables
function addMessagesToList(messages, list) {
	var origional_number_messages_length = list.rows.length;
	// Use old rows first
	for (var i = origional_number_messages_length;  messages.length > i ; i++) {
		var tableRow = list.insertRow(1);
		var tableItem = document.createElement('td');
		tableItem.appendChild(document.createTextNode(messages[i].from));
		tableRow.appendChild(tableItem);
		tableItem = document.createElement('td');
		tableItem.appendChild(document.createTextNode(messages[i].timeSlice));
		tableRow.appendChild(tableItem);
		tableItem = document.createElement('td');
		tableItem.appendChild(document.createTextNode(messages[i].timeLeft));
		tableRow.appendChild(tableItem);
		tableItem = document.createElement('td');
		tableItem.appendChild(document.createTextNode(messages[i].log));
		tableRow.appendChild(tableItem);
	}
}
