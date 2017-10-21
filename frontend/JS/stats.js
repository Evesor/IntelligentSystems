// Websocket Server
var ws = new WebSocket("ws://localhost:4567/ws");

ws.onopen = function(data)
{
	alert("Connected...");
};

ws.onmessage = function(data) 
{ 
	try {
		var jsonData = JSON.parse(data.data);
		drawStats(jsonData);
	} catch (err) {}
};

ws.onclose = function()
{ 
	// Websocket is closed.
	alert("Connection is closed..."); 
};

google.charts.load('current', {'packages':['corechart']});
google.charts.setOnLoadCallback(drawChart);

function drawChart() {

	var data = google.visualization.arrayToDataTable([
        ['Time', 'Price'],
        ['1',  1000  ],
        ['2',  1170  ],
        ['3',  660   ],
        ['4',  1030  ]
    ]);

    var options = {
          title: 'Average Price for electricity',
          curveType: 'function',
          legend: { position: 'none' },
      };

    var chart = new google.visualization.LineChart(document.getElementById('average-price-chart'));

    chart.draw(data, options);
}


