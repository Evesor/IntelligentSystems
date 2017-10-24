(() => {
// Establish the WebSocket connection and set up event handlers
    let webSocket = new WebSocket("ws://localhost:4567/ws");
    var graph = AgentGraph();

    webSocket.onmessage = function (msg) {
        let jsonData = JSON.parse(msg.data);
        if ("nodes" in jsonData){
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
            .map(link => graph.createLink(link.source, link.target, link.value))
            .forEach(link => graph.addLink(link));

    };

    webSocket.onclose = function () {
        console.log("Websocket Closed.");
    };


})();