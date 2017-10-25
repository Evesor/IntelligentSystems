
function agentClickedOn(agentData) {
	let agent_data_box = document.getElementById("agent-data");
	agent_data_box.innerHTML='';

	listDataVisualisation(agentData, data_list);
	addNegotiationMethods("1013122298");//Add id
}

// Recursively add list items to list.
function listDataVisualisation(list, node) {
	let data_list = document.createElement('ul');
	list.forEach(function(element) {
		let item = document.createElement('li');
		if (Array.isArray(element)) listDataVisualisation(element, item);
		else item.appendChild(document.createTextNode(element));
		node.appendChild(item);	
	});
	node.appendChild(data_list);
}


var agent_negotiation_methods = {
	"HoldForFirstOfferPrice",
	"LinearUtilityDecentNegotiator",
	"BoulwareNegotiator"
}

function pushToJade (input) {

}


//Used to add a list of things we can send to the agent.
function addNegotiationMethods (agentId) {
	let agent_data_box = document.getElementById("agent-data");
	let list = document.createElement("ul");
	agent_negotiation_methods.map(function(element) {
		let li = document.createElement(li);
		let button = document.createElement("BUTTON");
		//button.className = ; TODO, change to be pretty later
		let text = document.createTextNode(element);
		button.appendChild(text);
		//TODO, add styles.
		button.addEventListner('click', function(event) {
			//TODO, find way to add agent id to list.
			let response = prompt("Params for " + element + " : ");
			response = element + ' ' +response;
			pushToJade(response);
		}, false);	
		li.appendChild(node);
		list.appendChild(li);
	});
}
// 	agent_data_box.appendChild(list);
// 	/*
// <div class="dropdown">
//   <button class="btn btn-primary dropdown-toggle" type="button" data-toggle="dropdown">Dropdown Example
//   <span class="caret"></span></button>
//   <ul class="dropdown-menu">
//     <li><a href="#">HTML</a></li>
//     <li><a href="#">CSS</a></li>
//     <li><a href="#">JavaScript</a></li>
//   </ul>
// </div>
// */
// }



