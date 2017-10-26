function testMyClickedOn(data) {
	if (data.hasOwnProperty('nodes')){
		agentClickedOn(data.nodes[0].agentData);
	}
}
/*
*	agentData: The agentData from input
*	agentId: the id used by d3
*/
function agentClickedOn(agentData, agentId) {
	let agent_data_box = document.getElementById("agent-data");
	agent_data_box.innerHTML='';

	listDataVisualisation(agentData, agent_data_box);
	addChangeNegotiationMethodsButton(agentId, agent_data_box);
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

let makeMyButtonClickEvent = ((name) => {
	return (() => { 
		let response = prompt ("Please enter the agent behavior and paramaters with spaces");
		pushToJade(name + " " + response);
	});
});

function addChangeNegotiationMethodsButton (name, DOMItem) {
	let changeBehaviorButton = document.createElement("BUTTON");
	changeBehaviorButton.setAttribute('onclick', makeMyButtonClickEvent(name));
	changeBehaviorButton.onclick = makeMyButtonClickEvent(name);
	changeBehaviorButton.appendChild(document.createTextNode("Change Behavior"));
	DOMItem.appendChild(changeBehaviorButton);
}
/*
*	input : A string to go the jade behavior
*/
function pushToJade (input) {

}


