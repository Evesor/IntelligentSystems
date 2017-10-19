//Draw the logging messages to the screen 
function drawMessages(messages) {
	var list = document.getElementById('message-list');
	for (var i = 0; i < messages.length; i++) {
		var item = document.createElement('li');

		item.appendChild(document.createTextNode(messages[i]));

		list.appendChild(item);
	}
};