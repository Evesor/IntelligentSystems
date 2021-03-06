// var data = readAgentJSON
// d3
// websocket handler -> update central with messages
// call d3 callback to update the graph view

function drawAgents() {
  var svg = d3.select("svg"),
      width = +svg.attr("width"),
      height = +svg.attr("height");
}

function update(graph) {
  var color = d3.scaleOrdinal(d3.schemeCategory20);
  var size = 7;

  var simulation = d3.forceSimulation()
      .force("link", d3.forceLink().id(function(d) { return d.id; }))
      .force("charge", d3.forceManyBody())
      .force("center", d3.forceCenter(width/2, height/2));

  //d3.json("../JSON/agent-log.json", function(error, graph) {
    //if (error) throw error;

    var linksArray = [];

    for (var i = graph.nodes.length - 1; i >= 0; i--) {
      linksArray = linksArray.concat(graph.nodes[i].links);
    }

    var link = svg.append("g")
        .attr("class", "links")
        .selectAll("line")
        .data(linksArray)
        .enter().append("line")
        .attr("stroke-width", function(d) { return Math.sqrt(d.value); });

    var node = svg.append("g")
        .attr("class", "nodes")
        .selectAll("circle")
        .data(graph.nodes)
        .enter().append("circle")
        .attr("r", size)
          .style("stroke","black")
        .attr("fill", function(d) { return color(d.group); })
        .call(d3.drag()
            .on("start", dragstarted)
            .on("drag", dragged)
            .on("end", dragended))
            .on("mouseover", function() {
              d3.select(this).attr('r', size)
                .style("stroke","red");
            })
            .on("mouseleave", function() {
              d3.select(this).attr('r', size)
                .style("stroke","black");
            })
            .on("click", function(r) {
              a = r.agent;
              getAgentsFromNodes(a);
            });

    node.append("title")
        .text(function(d) { return d.id; });

    simulation
        .nodes(graph.nodes)
        .on("tick", ticked);

    simulation.force("link")
        .links(linksArray);
}

function ticked() {
  link
    .attr("x1", function(d) { return d.source.x; })
    .attr("y1", function(d) { return d.source.y; })
    .attr("x2", function(d) { return d.target.x; })
    .attr("y2", function(d) { return d.target.y; });

  node
    .attr("cx", function(d) { return d.x; })
    .attr("cy", function(d) { return d.y; });
}

function dragstarted(d) {
  if (!d3.event.active) simulation.alphaTarget(0.3).restart();
  d.fx = d.x;
  d.fy = d.y;
}

function dragged(d) {
  d.fx = d3.event.x;
  d.fy = d3.event.y;
}

function dragended(d) {
  if (!d3.event.active) simulation.alphaTarget(0);
  d.fx = null;
  d.fy = null;
}
//}