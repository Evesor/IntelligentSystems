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
	var final_index = a.length; // Limit to 50
	var start_index = a.length > 50 ? a.length - 50: 0;
	var list = [];
	var c = [];
	c.push(['time', name]);
	for (var i = 0; i < a.length; i++) {
    	list.push(i.toString());
	}
  	for (var i = start_index; i < final_index; i++) {
    	c.push([list[i], a[i]]);
  	}
  	return c
}

function drawStats(data) {
	drawChart(two1dto2d(data.averagePrice, 'Price'), "Average price", "average-price-chart");
	drawChart(two1dto2d(data.averageVolume, 'Sales'), "Average volume of sales", "average-volume-chart");
	drawChart(two1dto2d(data.averageTime, 'Length'), "Average time of contracts", "average-time-chart");
	drawChart(two1dto2d(data.numberOfSalesMade, 'Sales'), "Number of sales made", "sales-made-chart");
}


function drawChart(chart_data, chart_name, chart_backend_name) {
	try {
		var data = google.visualization.arrayToDataTable(chart_data);
	} catch (error) {
		console.log(error);
	}
	var line_color = '#AD85BA';
	switch (chart_backend_name) {
		case "average-price-chart":
			line_color = '#95A1C3';
			break;
		case "average-volume-chart":
			line_color = '#74A18E';
			break;
		case "average-time-chart":
			line_color = '#818FB5';
			break;
		case "sales-made-chart":
			line_color = "#E49969";
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

