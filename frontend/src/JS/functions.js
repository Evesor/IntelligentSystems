// Websocket Server
var ws = new WebSocket("ws://localhost:4567/ws");

ws.onopen = function(data)
{
	console.log("Connected to websocket...");
};

ws.onmessage = function(data) 
{ 
	try {
		var jsonData = JSON.parse(data.data);
		console.log(jsonData);
		drawMessages(jsonData);
		drawStats(jsonData);
		//testMyClickedOn(jsonData)
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