// Websocket Server
var ws = new WebSocket("ws://localhost:4567/ws");

ws.onmessage = function(data) 
{ 
	try {
		var jsonData = JSON.parse(data.data);
		if (jsonData.hasOwnProperty('averagePrice')) {
			drawStats(jsonData);
		}
	} catch (err) {}
};

function two1dto2d(a) {
	var list = [];
	var c = [];
	for (var i = 0; i < a.length; i++) {
    	list.push(i);
	}
	console.log(list);
  	for (var i = 0; i < a.length; i++) {
    	c.push([list[i], a[i]]);
  	}
  	return c
}

function drawStats(data) {
	//console.log(data);
	console.log(two1dto2d(data.averagePrice));
	drawChart(two1dto2d(data.averagePrice), "Average Price", "average-price-chart");


}


function drawChart(chart_data, chart_name, chart_backend_name) {

	var data = google.visualization.arrayToDataTable(chart_data);

    var options = {
          title: chart_name,
          curveType: 'function',
          legend: { position: 'none' },
      };

    var chart = new google.visualization.LineChart(document.getElementById(chart_backend_name));

    chart.draw(data, options);
}



// google.charts.load('current', {'packages':['corechart']});
// google.charts.setOnLoadCallback(drawChart);



// function drawChart() {

// 	var data = google.visualization.arrayToDataTable([
//         ['Time', 'Price'],
//         ['1',  1000  ],
//         ['2',  1170  ],
//         ['3',  660   ],
//         ['4',  1030  ]
//     ]);

//     var options = {
//           title: 'Average Price for electricity',
//           curveType: 'function',
//           legend: { position: 'none' },
//       };

//     var chart = new google.visualization.LineChart(document.getElementById('average-price-chart'));

//     chart.draw(data, options);
// }


