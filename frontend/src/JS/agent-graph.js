var agentGraph = (() => {

    /**
     * Adds a node to the graph
     * @param {string} id
     * @param {ws-agent} agentData
     * @param {ws-group} group
     */
    this.addNode = function (id, agentData, group) {
        Array.of(id)
            .filter(val => !nodes.some(node => node.id === val))
            .forEach(id => nodes.push({ "id": id }))
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
        return { "source": this.findNode(source), "target": this.findNode(target), "value": value };
    };

    /**
     * @param {string} sourceId
     * @param {string} targetId
     * @returns {(link|undefined)} the link 
    */
    this.findLink = function (sourceId, targetId) {
        return links.map(link => { return { "linkSourceId": link.source.id, "linkTargetId": link.target.id } })
            .filter(it => sourceId === it.linkSourceId)
            .filter(it => targetId === it.linkTargetId)
            .shift()
    }

    this.addLink = function (link) {
        let existingLink = findLink(link.source.id, link.target.id)
        if (existingLink == null) {
            links.push(link);
        } else {
            existingLink.value = link.value;
        }

        update();
    };


    this.getLinks = function () {
        return links;
    }

    this.getNodes = function () {
        return nodes;
    }

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

    var color = d3.scaleOrdinal(d3.schemeCategory10);
    var links = [];
    var nodes = [];
    var radius = 10;

    var svg = d3.select("svg")
        .attr("width", w)
        .attr("height", h)
    var link = svg.append("g").selectAll(".link"),
        node = svg.append("g").selectAll(".node");

    var simulation = d3.forceSimulation()
        .force("link", d3.forceLink().id(function (d) { return d.id; }))
        .force("charge", d3.forceManyBody().strength(function (d) { return -500; }))
        .force("center", d3.forceCenter(w / 2, h / 2))
        .force("xAxis", d3.forceX(w/2))
        .force("yAxis", d3.forceY(h/2));

    var update = function () {
        /** Update nodes */
        node = node.data(nodes, function (d) { return d.id })
        node.exit().remove();
        /** Enter */
        var newNode = node.enter().append("svg:circle")
            .attr("class", "node")
            .attr("r", radius)
            .attr("fill", function (d) { return color(d.group) })
            .call(d3.drag()
                .on("start", dragstarted)
                .on("drag", dragged)
                .on("end", dragended)
            )
            .on("click", function (d) {
                agentClickedOn(d.agentData, d.id)
            })
            .on("mouseover", function (d) {
                d3.select(this).attr('r', radius).style("stroke", "red")
            })
            .on("mouseleave", function (d) {
                d3.select(this).attr('r', radius).style("stroke", "white")
            });

        newNode.append("svg:text")
            .attr("class", "textClass")
            .attr("x", 14)
            .attr("y", ".31em")
            .text(function (d) {
                return d.agentData.name;
            });

        //	ENTER + UPDATE
        node = node.merge(newNode);

        // Links
        //	UPDATE
        link = link.data(links, function (d) { return d.id; });
        //	EXIT
        link.exit().remove();
        //	ENTER
        newLink = link.enter().append("line")
            .attr("id", function (d) { return d.source.id + "-" + d.target.id })
            .attr("class", "link")
            .attr("stroke-width", function (d) { d.value / 2 });

        //	ENTER + UPDATE
        link = link.merge(newLink);

        //	update simulation nodes, links, and alpha
        simulation
            .nodes(nodes)
            .on("tick", ticked);

        simulation.force("link")
            .links(links);

        simulation.alpha(1).alphaTarget(0).restart();
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

    //	tick event handler with bounded box
    function ticked() {
        node
            .attr("cx", function (d) { return d.x = Math.max(radius, Math.min(w - radius, d.x)); })
            .attr("cy", function (d) { return d.y = Math.max(radius, Math.min(w - radius, d.y)); });

        link
            .attr("x1", function (d) { return d.source.x; })
            .attr("y1", function (d) { return d.source.y; })
            .attr("x2", function (d) { return d.target.x; })
            .attr("y2", function (d) { return d.target.y; });
    }
    update();

    return this;
})();