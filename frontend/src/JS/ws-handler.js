(() => {
// Establish the WebSocket connection and set up event handlers
    let webSocket = new WebSocket("ws://" +
                        location.hostname +
                        (location.port ? ":" + 4567 : "") +
                        "/ws");


    webSocket.onmessage = function (msg) {
        let jsonData = JSON.parse(msg.data);
        if ("nodes" in jsonData){
            handleAgentData(jsonData);
        }
    };

    let handleAgentData = (jsonData) => {
        let nodes = jsonData.nodes;
        let links = nodes.flatMap(x => x.links);

        nodes.forEach(node => agentGraph.addNode(node.id, node.agentData, node.group));
        links
            .filter(link => agentGraph.validateLink(link))
            .map(link => agentGraph.createLink(link.source, link.target, link.value))
            .forEach(link => agentGraph.addLink(link));

    };

    webSocket.onclose = function () {
        console.log("Websocket Closed.");
    };
})();