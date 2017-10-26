function testMyClickedOn(data) {
	if (data.hasOwnProperty('nodes')){
		agentClickedOn(data.nodes[0].agentData);
	}
}


function agentClickedOn(agentData) {
	let agent_data_box = document.getElementById("agent-data");
	agent_data_box.innerHTML='';

	listDataVisualisation(agentData, agent_data_box);
	addChangeNegotiationMethodsButton(agentData.name, agent_data_box);
	//addNegotiationMethods("1013122298");//Add id
}

// Recursively add list items to list.
function listDataVisualisation(list, node) {
	let data_list = document.createElement('ul');
	data_list.id = 'agent-data-list';
	data_list.setAttribute('class', 'list-group');
	addToList(list, data_list);
	node.appendChild(data_list);
}


function addToList (data, list) {
	Object.entries(data).forEach(function(element) {
	 	let listItem = document.createElement('li');
	 	if (Array.isArray(element[1]))  addListToList(element[1], listItem, element[0]);
	 	else listItem.appendChild(document.createTextNode(prettyName(element[0]) + " :: " + element[1]));
	 	listItem.setAttribute('class', 'list-group-item');
	 	list.appendChild(listItem);	
	});
}

function addListToList (data, DOMList, name) {
	let sub_list = document.createElement("ul");
	sub_list.id = name + '-list';
	sub_list.setAttribute('class', 'collapse');

	let show_button = document.createElement("BUTTON");
	show_button.setAttribute('class',"btn btn-primary");
	show_button.setAttribute('data-toggle',"collapse");
	show_button.setAttribute('data-target', '#' + name + '-list');
	show_button.appendChild(document.createTextNode(prettyName(name)));
	DOMList.appendChild(show_button);

	data.forEach(function(element) {
		let listItem = document.createElement('li');
	 	listItem.appendChild(document.createTextNode(element[0]+ " :: " + element[1]));
	 	listItem.setAttribute('class', 'list-group-item');
	 	sub_list.appendChild(listItem);	
	});
}

function prettyName (string) {
	string = string.charAt(0).toUpperCase() + string.slice(1);
	return string.replace(/_/g, " ");
}

function addChangeNegotiationMethodsButton (name, DOMItem) {
	let changeBehaviorButton = document.createElement("BUTTON");
	//changeBehaviorButton.setAttribute('class',"btn btn-primary");
	changeBehaviorButton.setAttribute('onclick', function() {
		alert("potato!");
	});
	// changeBehaviorButton.addEventListner('click', function(event) {
	// 	alert("potato!");

	// });
	changeBehaviorButton.appendChild(document.createTextNode("Change Behavior"));
	DOMItem.appendChild(changeBehaviorButton);
}

function pushToJade (input) {

}


//Used to add a list of things we can send to the agent.
// function addNegotiationMethods (agentId) {
// 	let agent_data_box = document.getElementById("agent-data");
// 	let list = document.createElement("ul");
// 	agent_negotiation_methods.map(function(element) {
// 		let li = document.createElement(li);
// 		let button = document.createElement("BUTTON");
// 		//button.className = ; TODO, change to be pretty later
// 		let text = document.createTextNode(element);
// 		button.appendChild(text);
// 		//TODO, add styles.
// 		button.addEventListner('click', function(event) {
// 			//TODO, find way to add agent id to list.
// 			let response = prompt("Params for " + element + " : ");
// 			response = element + ' ' +response;
// 			pushToJade(response);
// 		}, false);	
// 		li.appendChild(node);
// 		list.appendChild(li);
// 	});
// }
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



