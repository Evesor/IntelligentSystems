// Random range bcuz javascript doesn't have one?
function getRandomInt(min, max) {
	return Math.floor(Math.random() * (max - min + 1)) + min;
}

function getHomeAgent() {
	$.getJSON( "../JSON/home-agent.json", function( data ) {
		alert(
			data.homeAgent.type 				+ "\n" +
			data.homeAgent.currentPowerUsage	+ "\n" +
			data.homeAgent.predictedPowerUsage	+ "\n" +
			data.homeAgent.appliances
		);
	});
}

function getApplianceAgent() {
	$.getJSON( "../JSON/appliance-agent.json", function( data ) {
		alert( "Appliance Agent\n" + 										// Temp string.
			data.applianceAgent.currentPowerUsage	+ "\n" +
			data.applianceAgent.predictedPowerUsage	+ "\n" +
			data.applianceAgent.switch
		);
	});
}

function getPowerplantAgent() {
	$.getJSON( "../JSON/powerplant-agent.json", function( data ) {
		alert( "Powerplant Agent\n" +										// Temp string.
			data.powerplantAgent.currentSellPrice 	+ "\n" +
			data.powerplantAgent.maximumProduction	+ "\n" +
			data.powerplantAgent.minimumPrice		+ "\n" +
			data.powerplantAgent.currentProduction
		);
	});
}

function getResellerAgent() {
	$.getJSON( "../JSON/reseller-agent.json", function( data ) {
		alert( "Reseller Agent\n" +											// Temp string.
			data.resellerAgent.currentSellPrice + "\n" +
			data.resellerAgent.currentBuyPrice	+ "\n" +
			data.resellerAgent.currentCustomers
		);
	});
}

function LoadResellerAgents() {
	$.getJSON( "../JSON/reseller-agent.json", function( data ) {
		var link = document.getElementById("agent-data");
		console.log(data.Agents.resellerAgent.length);

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