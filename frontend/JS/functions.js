// Random range bcuz javascript doesn't have one?
// Might not even be needed anymore...
function getRandomInt(min, max) {
	return Math.floor(Math.random() * (max - min + 1)) + min;
}

function getPowerplantAgent(a) {
	var link = document.getElementById("agent-data");


	var node = document.createElement("pre");
	var textNode = document.createTextNode( a.type 				   + "\n" +
											"Current Sell Price: " + a.currentSellPrice   + "\n" +
											"Maximum Production: " + a.maximumProduction  + "\n" +
											"Minimum Price: "      + a.minimumPrice       + "\n" +
											"Current Production: " + a.currentProduction);

	node.appendChild(textNode);
	link.appendChild(node);
}

function getResellerAgent(a) {
	var link = document.getElementById("agent-data");


	var node = document.createElement("pre");
	var textNode = document.createTextNode( a.type 				   + "\n" +
											"Current Sell Price: " + a.currentSellPrice + "\n" +
											"Current Buy Price: "  + a.currentBuyPrice  + "\n" +
											"Current Customers: "  + a.currentCustomers + "\n" );

	node.appendChild(textNode);
	link.appendChild(node);
}

function getHomeAgent(a) {
	var link = document.getElementById("agent-data");


	var node = document.createElement("pre");
	var textNode = document.createTextNode( a.type 				       + "\n" +
											"Current Power Usage: "    + a.currentPowerUsage   + "\n" +
											"Predicted Power Usage: "  + a.predictedPowerUsage + "\n" +
											"Current Customers: "      + a.appliances          + "\n" );

	node.appendChild(textNode);
	link.appendChild(node);
}

function getApplianceAgent(a) {
	var link = document.getElementById("agent-data");


	var node = document.createElement("pre");
	var textNode = document.createTextNode( a.type 				      + "\n" +
											"Current Power Usage: "   + a.currentPowerUsage   + "\n" +
											"Predicted Power Usage: " + a.predictedPowerUsage + "\n" +
											"Switch: "  			  + a.switch 			  + "\n" );

	node.appendChild(textNode);
	link.appendChild(node);
}