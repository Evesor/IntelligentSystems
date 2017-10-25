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
        let nodes = jsonData.nodes;

        let links = jsonData.nodes.flatMap((x) => {
            return x.links;
        });

        nodes.forEach(node => graph.addNode(node.id, node.agent));

        links
            .filter(link => graph.validateLink(link))
            .map(link => graph.createLink(link.source, link.target, link.value))
            .forEach(link => graph.addLink(link));

    };

    webSocket.onclose = function () {
        console.log("Websocket Closed.");
    };


})();