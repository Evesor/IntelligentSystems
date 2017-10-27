let AgentGraph = () => {

    /**
     * Adds a node to the graph
     * @param {string} id
     * @param {ws-agent} agentData
     * @param {ws-group} group
     */
    this.addNode = function (id, agentData, group) {
        Array.of(id)
            .filter(val => !nodes.some(node => node.id === val))
            .forEach(id => nodes.push({"id": id})) 
        this.findNode(id).agentData = agentData;
        this.findNode(id).group = group;
        update();
    };

    this.removeNode = function (id) {
        var i = 0;
        var n = this.findNode(id);
        while (i < links.length) {
            if ((links[i]["source"] == n) || (links[i]["target"] == n)) {
                links.splice(i, 1);
            }
            else i++;
        }
        nodes.splice(this.findNodeIndex(id), 1);
        update();
    };

    this.removeLink = function (source, target) {
        for (var i = 0; i < links.length; i++) {
            if (links[i].source.id == source && links[i].target.id == target) {
                links.splice(i, 1);
                break;
            }
        }
        update();
    };

    this.removeallLinks = function () {
        links.splice(0, links.length);
        update();
    };

    this.removeAllNodes = function () {
        nodes.splice(0, links.length);
        update();
    };

    this.createLink = function (source, target, value) {
        return {"source": this.findNode(source), "target": this.findNode(target), "value": value};
    };

    this.addLink = function (link) {
        links.push(link);
        update();
    };

    /**
     * @param {ws-link} link
     */
    this.validateLink = function (link) {
        let result = Array.of(link)
            .filter(element => nodes.some(node => node.id === element.source))
            .filter(element => nodes.some(node => node.id === element.target))
            .shift();

        return !!result; 
    };

    /**
     * Finds the first node with id
     * @param {string} id 
     * @returns {(node|undefined)} the node from the node list
     */
    this.findNode = function (id) {
        return nodes.find(node => node.id === id);
    };

    /**
     * Finds the index of the node id
     * @param {string} id 
     * @returns {number} index of the node, -1 if it doesn't exist
     */
    this.findNodeIndex = function (id) {
        return nodes.findIndex(node => node.id === id);
    };

    // set up the D3 visualisation in the specified element
    var w = 960,
        h = 600;

    var size = 12;

    var color = d3.scale.category10();
    var size = 12;

    var vis = d3.select("svg")
        .attr("width", w)
        .attr("height", h)
        .attr("id", "svg")
        .attr("pointer-events", "all")
        .attr("viewBox", "0 0 " + w + " " + h)
        .attr("perserveAspectRatio", "xMinYMid")
        .append("svg:g");

    var force = d3.layout.force();

    var links = force.links(),
        nodes = force.nodes();

    var update = function () {
        var link = vis.selectAll("line")
            .data(links, function (d) { return d.source.id + "-" + d.target.id; });

        link.enter().append("line")
            .attr("id", function (d) { return d.source.id + "-" + d.target.id; })
            .attr("class", "link")
            .attr("stroke-width", function (d) { return d.value / 2; });

        link.append("title")
            .text(function (d) { return d.value; });
        link.exit().remove();

        var node = vis.selectAll("g.node")
            .data(nodes, function (d) { return d.id; });

        var nodeEnter = node.enter().append("g")
            .attr("class", "node")
            .call(force.drag)
            .on("mouseover", function() {
              d3.select(this).attr('r', size)
                .style("stroke","red");
            })
            .on("mouseleave", function() {
              d3.select(this).attr('r', size)
                .style("stroke","white");
            })
            .on("click", function(r) {
              agentClickedOn(r.agentData, r.id);
            });

        nodeEnter.append("svg:circle")
            .attr("r", size)
            .attr("id", function (d) {
                return "Node;" + d.id;
            })
            .attr("class", "nodeStrokeClass")
            .attr("fill", function(r) {
                a = r.agentData;
                console.log(r);
                return color(r.group); ////// HUAN Please find a way to obtain the group
            });

        nodeEnter.append("svg:text")
            .attr("class", "textClass")
            .attr("x", 14)
            .attr("y", ".31em")
            .text(function (d) {
                return d.agentData.name;
            });

        node.exit().remove();

        force.on("tick", function () {
            node.attr("transform", function (d) {
                return "translate(" + d.x + "," + d.y + ")";
            });

            link.attr("x1", function (d) { return d.source.x; })
                .attr("y1", function (d) { return d.source.y; })
                .attr("x2", function (d) { return d.target.x; })
                .attr("y2", function (d) { return d.target.y; });
        });

        // Restart the force layout.
        force
            .gravity(.01)
            .charge(-80000)
            .friction(0)
            .linkDistance(100)
            .size([w, h])
            .start();
    };

    // Make it all go
    update();

    return this;
};