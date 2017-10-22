(() => {
// Establish the WebSocket connection and set up event handlers
    let webSocket = new WebSocket("ws://localhost:4567/ws");
    var graph = agentGraph("#svgdiv");

    webSocket.onmessage = function (msg) {
        let jsonData = JSON.parse(msg.data);
        if ('nodes' in jsonData){
            handleAgentData(jsonData);
        }
    };

    let handleAgentData = (jsonData) => {
        let ids = jsonData.nodes.map(nodes => {
            return nodes.id;
        }); 

        let links = jsonData.nodes.flatMap((x) => {
            return x.links;
        });

        ids.forEach(id => graph.addNode(id));
        links
            .filter(link => graph.validateLink(link))
            .forEach(link => graph.addLink(link));
    }

    webSocket.onclose = function () {
        console.log("Websocket Closed.");
        updateChat("Websocket Closed");
    };

    //Send message if "Send" is clicked
    id("send").addEventListener("click", function () {
        sendMessage(id("message").value);
    });

    //Send message if enter is pressed in the input field
    id("message").addEventListener("keypress", function (e) {
        if (e.keyCode === 13) {
            sendMessage(e.target.value);
        }
    });

    //Send a message if it's not empty, then clear the input field
    function sendMessage(message) {
        if (message !== "") {
            // webSocket.send(message);
            id("message").value = "";
        }
    }

    //Update the chat-panel
    function updateChat(msg) {
        insert("chat", msg);
        insert("chat", "\n");
        console.log(msg);
    }

    //Helper function for inserting HTML as the first child of an element
    function insert(targetId, message) {
        id(targetId).insertAdjacentHTML("afterbegin", message);
    }

    //Helper function for selecting element by id
    function id(id) {
        return document.getElementById(id);
    }
})();