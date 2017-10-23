(() => {
// Establish the WebSocket connection and set up event handlers
    let webSocket = new WebSocket("ws://localhost:4567/ws");
    var graph = AgentGraph("#svgdiv");

    graph.addNode("Home1@192.168.0.13:1099/JADE");
    graph.addNode("Home2@192.168.0.13:1099/JADE");
    graph.addLink(graph.createLink("Home1@192.168.0.13:1099/JADE", "Home2@192.168.0.13:1099/JADE", 5));

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

        // console.log(jsonData);
        // console.log(links);
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