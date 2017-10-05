// Random range bcuz javascript doesn't have one?
function getRandomInt(min, max) {
	return Math.floor(Math.random() * (max - min + 1)) + min;
}

function getPowerplantAgent(type, currentSellPrice, maximumProduction, minimumPrice, currentProduction) {
		alert(type + "\n" + currentSellPrice + "\n" + maximumProduction + "\n" + minimumPrice + "\n" + currentProduction);
}

function getResellerAgent() {
	$.getJSON( "../JSON/reseller-agent.json", function( data ) {
		alert( "Reseller Agent\n" +
			data.resellerAgent.currentSellPrice + "\n" +
			data.resellerAgent.currentBuyPrice	+ "\n" +
			data.resellerAgent.currentCustomers
		);
	});
}

function getHomeAgent(type, currentPowerUsage, predictedPowerUsage, appliances) {
	alert(type + "\n" + currentPowerUsage + "\n" + predictedPowerUsage + "\n" + appliances);
}

function getApplianceAgent(type, currentPowerUsage, predictedPowerUsage, switch) {
	alert(type + "\n" + currentPowerUsage + "\n" + predictedPowerUsage + "\n" + switch);
}

function LoadResellerAgents() {
	$.getJSON( "../JSON/reseller-agent.json", function( data ) {
		var link = document.getElementById("agent-data");

		for (var i = 0; i < data.Agents.resellerAgent.length; i++) { 
			var node = document.createElement("pre");
			var textNode = document.createTextNode( "Reseller Agent\n"     +
													"Current Sell Price: " + data.Agents.resellerAgent[i].currentSellPrice + "\n" +
													"Current Buy Price: "  + data.Agents.resellerAgent[i].currentBuyPrice  + "\n" +
													"Current Customers: "  + data.Agents.resellerAgent[i].currentCustomers + "\n" );

			node.appendChild(textNode);
			link.appendChild(node);
		}
	});
}