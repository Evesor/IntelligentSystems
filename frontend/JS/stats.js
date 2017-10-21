// Websocket Server
var ws = new WebSocket("ws://localhost:4567/ws");

google.charts.load('current', {'packages':['corechart']});

ws.onmessage = function(data) 
{ 
	try {
		var jsonData = JSON.parse(data.data);
		if (jsonData.hasOwnProperty('averagePrice')) {
			drawStats(jsonData);
		}
	} catch (err) {}
};

function two1dto2d(a, name) {
	var list = [];
	var c = [];
	c.push(['time' , name]);
	for (var i = 0; i < a.length; i++) {
    	list.push(i.toString());
	}
  	for (var i = 0; i < a.length; i++) {
    	c.push([list[i], a[i]]);
  	}
  	return c
}

function drawStats(data) {
	drawChart(two1dto2d(data.averagePrice, 'Price'), "Average price", "average-price-chart");
	drawChart(two1dto2d(data.averageVolume, 'Sales'), "Average volume of sales", "average-volume-chart");
	drawChart(two1dto2d(data.averageTime, 'Length'), "Average time of contracts", "average-time-chart");
}


function drawChart(chart_data, chart_name, chart_backend_name) {	
	try {
		var data = google.visualization.arrayToDataTable(chart_data);
	} catch (error) {
		console.log(error);
	}
	var line_color = '#C70039';
	switch (chart_backend_name) {
		case "average-price-chart":
			line_color = '#1E8449';
			break;
		case "average-volume-chart":
			line_color = '#F5B041';
			break;
		case "average-time-chart":
			line_color = '#AF7AC5';
			break;
	}
    var options = {
          title: chart_name,
          curveType: 'function',
          legend: { position: 'none' },
          lineWidth: 2,
          width: 600,
          height: 400,
          colors: [line_color]
      };

    var chart = new google.visualization.LineChart(document.getElementById(chart_backend_name));

    chart.draw(data, options);
}

