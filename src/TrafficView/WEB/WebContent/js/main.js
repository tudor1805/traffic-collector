var GLOBAL = {
		BUCHAREST_CENTER_LAT: 44.437,
		BUCHAREST_CENTER_LON: 26.097,
		
		mapDiv: undefined,
		map: undefined,
		options: undefined,
		startMarker: undefined,
		endMarker: undefined,
		startInfoWindow: undefined,
		endInfoWindow: undefined,
		startLocation: undefined,
		endLocation: undefined,
		markerClusterer: undefined,
		markerManager: undefined,
		isRouting: false,
		congestionViewEnabled: true,
		selectedStatistic: undefined,
		refreshIntervalID: undefined,
		refreshInterval: undefined,
		nrStreetsForStatistics: undefined,
		directionsDisplay: undefined,
		
		markers: new Array(),
		infoWindows: new Array(),
		positions: new Array(),
		following: new Array(),
		followColors: new Array()
};

function createMap() {		
	GLOBAL.mapDiv = document.getElementById("mapDiv");
	GLOBAL.options = {
		    center: new google.maps.LatLng(
		    		GLOBAL.BUCHAREST_CENTER_LAT,
		    		GLOBAL.BUCHAREST_CENTER_LON), 
		    zoom: 12,
		    mapTypeId: google.maps.MapTypeId.ROADMAP,
			disableDefaultUI: false,	
			scaleControl: true,
			streetViewControl: false,
			mapTypeControl: false,
			minZoom: 9
		  };
	GLOBAL.map = new google.maps.Map(GLOBAL.mapDiv, GLOBAL.options); 
	GLOBAL.markerManager = new MarkerManager(GLOBAL.map);
	
	google.maps.event.addListener(GLOBAL.map, 'click', function(e) {
		
		if (GLOBAL.isRouting) {
			if (GLOBAL.startLocation == undefined) {
				GLOBAL.startLocation = e.latLng;
		        GLOBAL.startMarker = new google.maps.Marker({
					position: GLOBAL.startLocation,
					map: GLOBAL.map,
					icon: new google.maps.MarkerImage(
							'img/flag.png',
							null,
							null,
							new google.maps.Point(16, 16)),
					title: 'Start'
				});
		        
		        $.blockUI({ 
					message: '<h2>Click on end location</h2>',
					css: { 
						border: 'none', 
						padding: '15px', 
						backgroundColor: '#000', 
							'-webkit-border-radius': '10px', 
							'-moz-border-radius': '10px', 
						opacity: .5, 
						color: '#fff' 
					}
				}); 
		        setTimeout($.unblockUI, 1000);
			}
			else {
				GLOBAL.endLocation = e.latLng;
		        GLOBAL.endMarker = new google.maps.Marker({
					position: GLOBAL.endLocation,
					map: GLOBAL.map,
					icon: new google.maps.MarkerImage(
							'img/flag.png',
							null,
							null,
							new google.maps.Point(16, 16)),
					title: 'End'
				});
		        
		        GLOBAL.map.setOptions({draggableCursor: 'default'});
		        
		        GLOBAL.isRouting = false;
		        
		        $.blockUI({ 
					message: '<h2>Computing route...</h2>',
					css: { 
						border: 'none', 
						padding: '15px', 
						backgroundColor: '#000', 
							'-webkit-border-radius': '10px', 
							'-moz-border-radius': '10px', 
						opacity: .5, 
						color: '#fff' 
					}
				}); 
		        
		        var request = {
		        		origin: GLOBAL.startLocation,
		        		destination: GLOBAL.endLocation,
		        		travelMode: google.maps.TravelMode.DRIVING,
		        		unitSystem: google.maps.UnitSystem.METRIC,
		        		provideRouteAlternatives: true
		        };
		        
		        var directionsService = new google.maps.DirectionsService();
		        directionsService.route(request, function(result, status) {
		        	if (status == google.maps.DirectionsStatus.OK) {
		        		
		        		var resultString = JSON.stringify(result, function (key, value) {
		        		    if (typeof value === 'number' && !isFinite(value)) {
		        		        return String(value);
		        		    }
		        		    return value;
		        		});
		        		
		        		var targetURL = "http://" + window.location.hostname + ":" +
		        			window.location.port + "/CAPIM-WebApp/rest/getRoute";
		        		
		        		$.post(
		        			targetURL,
		        			resultString,
		        			function (data) {
		        				$.unblockUI();
		        				//alert(data);
		        				
		        				var routeIndex = data.substring(0, data.indexOf(","));
						        var avgSpeed = Math.round(parseFloat(data.substring(
						        			data.indexOf(",") + 1, data.length)) * 100) / 100;
		        				//alert(routeIndex + "|" + avgSpeed);
						        
						        var routeLength = result.routes[0].legs[0].distance.value / 1000;
						        
						        var hours = routeLength / avgSpeed;
						        var minutes = Math.floor(hours * 60) % 60;
						        hours = Math.floor(hours);
						        
		        				// remove the unused routes from the 'result' object
		        				for (var i = 0; i < result.routes.length; i++)
		        					if (i != routeIndex)
		        						delete result.routes[i];
		        				result.routes[0] = result.routes[routeIndex];
		        				
		        				// draw the route
		        				if (GLOBAL.directionsDisplay == undefined)
		        					GLOBAL.directionsDisplay = new google.maps.DirectionsRenderer({
		        						suppressMarkers: true
		        					});
		        				
						        GLOBAL.directionsDisplay.setDirections(result);
						        GLOBAL.directionsDisplay.setMap(GLOBAL.map);
						        
						        // get the start and end addresses
						        var startAddress = result.routes[0].legs[0].start_address;
						        var index = startAddress.indexOf(",");
						        if (index != -1)
						        	startAddress = startAddress.substring(0, index);
						        
						        var endAddress = result.routes[0].legs[0].end_address;
						        index = endAddress.indexOf(",");
						        if (index != -1)
						        	endAddress = endAddress.substring(0, index);
						        
						        // create markers and infoWindows for route endpoints
						        GLOBAL.startInfoWindow = new google.maps.InfoWindow({
						        	content: createStartInfoWindow({
						        		address: startAddress,
						        		lat: Math.round(GLOBAL.startLocation.lat() * 10000) / 10000,
						        		lon: Math.round(GLOBAL.startLocation.lng() * 10000) / 10000,
						        		length: routeLength,
						        		speed: avgSpeed,
						        		time: hours + "h " + minutes + "m"
						        	})
						        });
						        GLOBAL.endInfoWindow = new google.maps.InfoWindow({
						        	content: createEndInfoWindow({
						        		address: endAddress,
						        		lat: Math.round(GLOBAL.endLocation.lat() * 10000) / 10000,
						        		lon: Math.round(GLOBAL.endLocation.lng() * 10000) / 10000,
						        		length: routeLength,
						        		speed: avgSpeed,
						        		time: hours + "h " + minutes + "m"
						        	})
						        });
						        google.maps.event.addListener(GLOBAL.startMarker, 'click', function() {
									GLOBAL.startInfoWindow.open(GLOBAL.map, GLOBAL.startMarker);
								});
						        google.maps.event.addListener(GLOBAL.endMarker, 'click', function() {
									GLOBAL.endInfoWindow.open(GLOBAL.map, GLOBAL.endMarker);
								});
						        
						        GLOBAL.startLocation = undefined;
						        GLOBAL.endLocation = undefined;
		        			}
		        		);
		        	}
		        });
			}
		}
	});
	
	getTrafficData();
	
	GLOBAL.refreshInterval = 10000;
	GLOBAL.refreshIntervalID = 
		setInterval(getTrafficData, GLOBAL.refreshInterval);
}

function getTrafficData() {
	ApplicationDwr.getTrafficData({callback: handleTrafficData, async: false});
}

function handleTrafficData(list) {
	for (var j = 0; j < list.length; j++) {
		(function (i) {
			var data = list[i];

			var infoWindowContent = {
				id: data.id,
				lat: data.lat,
				lon: data.lon,
				speed: Math.round(data.speed * 100) / 100,
				follow: undefined
			};

			if (GLOBAL.markers[data.id] != undefined) {
				GLOBAL.markers[data.id].setPosition(new google.maps.LatLng(data.lat, data.lon));
				
				infoWindowContent.follow = GLOBAL.following[data.id];
				var infoWindow = createInfoWindow(infoWindowContent);
				GLOBAL.infoWindows[data.id].setContent(infoWindow);

				if (GLOBAL.following[data.id]) {
					var lineCoords = [GLOBAL.positions[data.id], 
					                  new google.maps.LatLng(data.lat, data.lon)];			
					if (GLOBAL.followColors[data.id] == undefined)
						GLOBAL.followColors[data.id] = generateColor();
					var path = new google.maps.Polyline({
						path: lineCoords,
						strokeColor: GLOBAL.followColors[data.id],
						strokeOpacity: 1.0,
						strokeWeight: 5
					});
					path.setMap(GLOBAL.map);
				}
			}
			else {
				GLOBAL.markers[data.id] = new google.maps.Marker({
					position: new google.maps.LatLng(data.lat, data.lon),
					map: GLOBAL.map,
					icon: new google.maps.MarkerImage(
							'img/car1_hoover.png',
							null,
							null,
							new google.maps.Point(16, 16)),
					title: 'Car'
				});
				if (!GLOBAL.congestionViewEnabled) {
					GLOBAL.markers[data.id].setMap(GLOBAL.map);
				}			
				
				infoWindowContent.follow = false;
				var infoWindow = createInfoWindow(infoWindowContent);
				GLOBAL.infoWindows[data.id] = new google.maps.InfoWindow({content: infoWindow});
				
				GLOBAL.following[data.id] = false;
				
				google.maps.event.addListener(GLOBAL.markers[data.id], 'click', function() {
					GLOBAL.infoWindows[data.id].open(GLOBAL.map, GLOBAL.markers[data.id]);
				});
				
				google.maps.event.addListener(GLOBAL.markers[data.id], 'mouseover', function() {
					var img = GLOBAL.following[data.id] == true ?
							'img/car2.png' :
							'img/car1.png';
						
					this.setIcon(new google.maps.MarkerImage(
							img,
							null,
							null,
							new google.maps.Point(16, 16))
					);
				});
				
				google.maps.event.addListener(GLOBAL.markers[data.id], 'mouseout', function() {
					var img = GLOBAL.following[data.id] == true ?
							'img/car2_hoover.png' :
							'img/car1_hoover.png';
						
					this.setIcon(new google.maps.MarkerImage(
							img,
							null,
							null,
							new google.maps.Point(16, 16))
					);
				});
			}
			
			GLOBAL.positions[data.id] = new google.maps.LatLng(data.lat, data.lon);
		}) (j);
	}
	
	if (GLOBAL.congestionViewEnabled && GLOBAL.markerClusterer == undefined)
		GLOBAL.markerClusterer = new MarkerClusterer(GLOBAL.map, GLOBAL.markers);
	
	GLOBAL.markerManager = new MarkerManager(GLOBAL.map);
	//GLOBAL.markerManager.addMarkers(GLOBAL.markers, 1);
	GLOBAL.markerManager.refresh();
}

function createInfoWindow(windowData) {
	var followText = windowData.follow == false ? 'Follow' : 'Unfollow';
	
	return '<div id="info' + windowData.id + '">' + 
		'<h3><img src="img/info.png" alt="Car info"/></h3>' +   
		'<b>Id:</b><i> ' + windowData.id + '</i><br/>' + 
		'<b>Latitude:</b><i> ' + windowData.lat + '</i><br/>' +
		'<b>Longitude:</b><i> ' + windowData.lon + '</i><br/>' +
		'<b>Speed:</b><i> ' + windowData.speed + ' km/h</i><br/></br>' +
		'<b><a id="follow' + windowData.id + '" href="#" onclick="' +
			'followCar(' + windowData.id + ')">' + followText + '</a></br>' +
		'</div>';
}

function createStartInfoWindow(info) {
	return '<div id="infoStart">' +    
		'<b>Source:</b><i> ' + info.address + '</i><br/><br/>' + 
		'<b>Latitude:</b><i> ' + info.lat + '</i><br/>' +
		'<b>Longitude:</b><i> ' + info.lon + '</i><br/><hr/>' +
		'<b>Route length:</b><i> ' + info.length + ' km</i><br/>' +
		'<b>Average speed:</b><i> ' + info.speed + ' km/h</i><br/>' +
		'<b>Estimated time:</b><i> ' + info.time + '</i><br/>' +
		'</div>';
}

function createEndInfoWindow(info) {
	return '<div id="infoEnd">' +    
		'<b>Destination:</b><i> ' + info.address + '</i><br/><br/>' + 
		'<b>Latitude:</b><i> ' + info.lat + '</i><br/>' +
		'<b>Longitude:</b><i> ' + info.lon + '</i><br/><hr/>' +
		'<b>Route length:</b><i> ' + info.length + ' km</i><br/>' +
		'<b>Average speed:</b><i> ' + info.speed + ' km/h</i><br/>' +
		'<b>Estimated time:</b><i> ' + info.time + '</i><br/>' +
		'</div>';
}

function followCar(id) {
	if (GLOBAL.following[id] == true) {
		GLOBAL.following[id] = false;
		GLOBAL.markers[id].setIcon(new google.maps.MarkerImage(
				'img/car1_hoover.png',
				null,
				null,
				new google.maps.Point(16, 16)));
		document.getElementById('follow' + id).innerHTML = "Follow";
	} 
	else {
		GLOBAL.following[id] = true;
		GLOBAL.markers[id].setIcon(new google.maps.MarkerImage(
				'img/car2_hoover.png',
				null,
				null,
				new google.maps.Point(16, 16)));
		document.getElementById('follow' + id).innerHTML = "Unfollow";
	}
}

function generateColor() {
	var ret = "#";
	var digits = ['0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 
	              	'A', 'B', 'C', 'D', 'E', 'F'];
	
	for (var i = 1; i <= 6; i++)
		ret += digits[Math.floor(Math.random() * 16)]; 
	
	return ret;
}

$(document).ready(function() {	
	$("#goToLink").click(function() {
		$.blockUI({
			message: '<input type="text" id="goToInput" ' +
						'style="background-color: #111111; width: 200px; ' +
						'color: #FFFF00" ' +
						'value="Type location here">' +
						'</input>',
				css: { 
					border: 'none', 
					padding: '15px', 
					backgroundColor: '#000', 
						'-webkit-border-radius': '10px', 
						'-moz-border-radius': '10px', 
					opacity: .8, 
					color: '#fff' 
				}
		}); 
        $('.blockOverlay').click($.unblockUI); 
	});
	$('#goToInput').live('click', function() {
		var inputText = $('#goToInput').val();
		if (inputText == 'Type location here')
			$('#goToInput').val('');
	});
	$('#goToInput').live('keypress', function(e) {
		var code = (e.keyCode ? e.keyCode : e.which);
		if(code == 13) {
			($.unblockUI)();

			geocoder = new google.maps.Geocoder();
			geocoder.geocode({address: $('#goToInput').val()}, 
			function(results, status) {
				if (status == google.maps.GeocoderStatus.OK && results.length) {
					if (status != google.maps.GeocoderStatus.ZERO_RESULTS) {
						GLOBAL.map.setCenter(results[0].geometry.location);
						GLOBAL.map.setZoom(17);
					}
				}
			});
		}
	});
	
	$("#routingLink").click(function() {
		$.blockUI({ 
			message: '<h2>Click on start location</h2>',
			css: { 
				border: 'none', 
				padding: '15px', 
				backgroundColor: '#000', 
					'-webkit-border-radius': '10px', 
					'-moz-border-radius': '10px', 
				opacity: .5, 
				color: '#fff' 
			}
		}); 
        setTimeout($.unblockUI, 1000);
        
        GLOBAL.map.setOptions({draggableCursor: 'crosshair'});
        
        // delete the previous route, if one exists
        if (GLOBAL.startMarker) {
        	GLOBAL.startMarker.setMap(null);
        	GLOBAL.startMarker.setVisible(false);
        	delete GLOBAL.startMarker;
        	GLOBAL.startMarker = undefined;
        }
        if (GLOBAL.endMarker) {
        	GLOBAL.endMarker.setMap(null);
        	GLOBAL.endMarker.setVisible(false);
        	delete GLOBAL.endMarker;
        	GLOBAL.endMarker = undefined;
        }
        
        if (GLOBAL.directionsDisplay != undefined)
        	GLOBAL.directionsDisplay.setMap(null);
        
        GLOBAL.isRouting = true;
	});
	
	$("#statisticsLink").click(function() {
		$("#statisticsDiv").dialog({
			title: "View Options",
			modal: true,
			overlay: { 
	            opacity: 0.7, 
	            background: "black" 
	        },
	        buttons: {
	        	'Ok': function () {
	        		$(this).dialog("close");
	        		
	        		$.blockUI({
	        			message: '<div id="chartDiv">Loading...</div>',
	        			css: {
	        				top: 50,
	        				width: 500,
	        				height: 500,
	        				border: 'none',  
	        				backgroundColor: '#FFFFFF', 
	        				'-webkit-border-radius': '10px', 
	        				'-moz-border-radius': '10px', 
	        				opacity: .8, 
	        				color: '#000000' 
	        			}
	        		});

	        		var targetURL = "http://" + window.location.hostname + ":" +
	        		window.location.port + "/CAPIM-WebApp/rest/getStatistics";
	        		
	        		var type = 1, order = 2;
	        		
	        		if (GLOBAL.selectedStatistic == "radioLowestSpeeds") {
	        			type = 1;
	        			order = 1;
	        		}
	        		else if (GLOBAL.selectedStatistic == "radioHighestSpeeds") {
	        			type = 1;
	        			order = 2;
	        		}
	        		else if (GLOBAL.selectedStatistic == "radioFreeStreets") {
	        			type = 2;
	        			order = 1;
	        		}
	        		else if (GLOBAL.selectedStatistic == "radioBusiestStreets") {
	        			type = 2;
	        			order = 2;
	        		}
	        		
	        		if (GLOBAL.nrStreetsForStatistics == undefined)
	        			GLOBAL.nrStreetsForStatistics = 10;
	        		targetURL += "/" + type + "/" + order + "/" + GLOBAL.nrStreetsForStatistics;
	        		
	        		$.get(
	        				targetURL,
	        				function (data) {
	        					//alert(data);
	        					var jsonData = jQuery.parseJSON(data);
	        					var chartData = [];
	        					for (var key in jsonData)
	        						chartData.push([jsonData[key].name, 
	        						    type == 1 ? Math.round(jsonData[key].load * 100) / 100 : 
	        						    			jsonData[key].load]);
	        			
	        					// create the statistics pie chart
	        					var data = new google.visualization.DataTable();
	        					data.addColumn('string', 'Street');
	        					data.addColumn('number', 
	        							type == 1 ? 'Speed (km/h)' : 'Cars');
	        					data.addRows(chartData);

	        					// Instantiate and draw our chart, passing in some options.
	        					var chart = new google.visualization.PieChart(
	        							document.getElementById('chartDiv'));
	        					chart.draw(data, {width: 400, height: 500, is3D: true});
	        				}
	        		);

	        		$('.blockOverlay').click($.unblockUI);

	        	}
	        }
		}); 
	});
	
	$("#radioHighestSpeeds").change(function() {
		GLOBAL.selectedStatistic = "radioHighestSpeeds";
	});
	$("#radioLowestSpeeds").change(function() {
		GLOBAL.selectedStatistic = "radioLowestSpeeds";
	});
	$("#radioBusiestStreets").change(function() {
		GLOBAL.selectedStatistic = "radioBusiestStreets";
	});
	$("#radioFreeStreets").change(function() {
		GLOBAL.selectedStatistic = "radioFreeStreets";
	});
	
	$("#optionsLink").click(function() {
		$("#viewOptionsDiv").dialog({
			title: "Options",
			modal: true,
			overlay: { 
	            opacity: 0.7, 
	            background: "black" 
	        },
	        buttons: {
	        	'Ok': function () {
	        		// change the refresh interval
	        		if (GLOBAL.refreshInterval != undefined) {
	        			getTrafficData();
	        			clearInterval(GLOBAL.refreshIntervalID);
	        			GLOBAL.refreshIntervalID = setInterval(
	        					getTrafficData, GLOBAL.refreshInterval);
	        			GLOBAL.refreshInterval = undefined;
	        		}
	        		
	        		$(this).dialog("close");
	        	}
	        }
		});
	});
	
	$("#cabsLink").click(function() {
		var targetURL = "http://" + window.location.hostname + ":" +
			window.location.port + "/CAPIM-WebApp/rest/getCabs";
		$.get(
    		targetURL,
    		function (data) {
    			handleTrafficData(jQuery.parseJSON(data));
    			/*alert(data.length);
    			for (var j = 0; j < data.length; j++) {
    					var l = data[j];

    					var infoWindowContent = {
    						id: l.id,
    						lat: l.lat,
    						lon: l.lon,
    						follow: undefined
    					};
    					alert(l.id + " " + l.lat + " " + l.lon);
    			}*/

    		}
    	);
	});
	
	$("#aboutLink").click(function() {
		$.blockUI({
			message: '<b>Authors:<b></br>' +
				'<h3><i>Diaconu Virgiliu</i></br>' +
				'<i>Tanase Narcis</i></h3></br>' +
				'Copyright 2011. All rights reserved.',
				css: { 
					border: 'none', 
					padding: '15px', 
					backgroundColor: '#000', 
						'-webkit-border-radius': '10px', 
						'-moz-border-radius': '10px', 
					opacity: .5, 
					color: '#fff' 
				}
		}); 
        $('.blockOverlay').click($.unblockUI); 
	});
	
	$("#radioCongestion").change(function() {
		if (GLOBAL.congestionViewEnabled == false) {
			GLOBAL.congestionViewEnabled = true;
			GLOBAL.markerClusterer = new MarkerClusterer(GLOBAL.map, GLOBAL.markers);
			for (var i in GLOBAL.markers)
				GLOBAL.markers[i].setVisible(true);
		}
	});
	$("#radioAll").change(function() {
		if (GLOBAL.congestionViewEnabled = false) {
			for (var i in GLOBAL.markers)
				GLOBAL.markers[i].setVisible(true);
		}
		else {
			GLOBAL.congestionViewEnabled = false;
			GLOBAL.markerClusterer.clearMarkers();
			for (var i in GLOBAL.markers) {
				GLOBAL.markers[i].setMap(GLOBAL.map);
				GLOBAL.markers[i].setVisible(true);
			}
		}
	});
	$("#radioNone").change(function() {
		if (GLOBAL.congestionViewEnabled = false) {
			for (var i in GLOBAL.markers)
				GLOBAL.markers[i].setVisible(false);
		}
		else {
			GLOBAL.congestionViewEnabled = false;
			GLOBAL.markerClusterer.clearMarkers();
			for (var i in GLOBAL.markers) {
				GLOBAL.markers[i].setMap(GLOBAL.map);
				GLOBAL.markers[i].setVisible(false);
			}
		}
	});
	$("#radioFollowed").change(function() {
		if (GLOBAL.congestionViewEnabled = false) {
			for (var i in GLOBAL.markers)
				GLOBAL.markers[i].setVisible(GLOBAL.following[i]);
		}
		else {
			GLOBAL.congestionViewEnabled = false;
			GLOBAL.markerClusterer.clearMarkers();
			for (var i in GLOBAL.markers) {
				GLOBAL.markers[i].setMap(GLOBAL.map);
				GLOBAL.markers[i].setVisible(GLOBAL.following[i]);
			}
		}
	});
	$("#radioUnfollowed").change(function() {
		if (GLOBAL.congestionViewEnabled = false) {
			for (var i in GLOBAL.markers)
				GLOBAL.markers[i].setVisible(!GLOBAL.following[i]);
		}
		else {
			GLOBAL.congestionViewEnabled = false;
			GLOBAL.markerClusterer.clearMarkers();
			for (var i in GLOBAL.markers) {
				GLOBAL.markers[i].setMap(GLOBAL.map);
				GLOBAL.markers[i].setVisible(!GLOBAL.following[i]);
			}
		}
	});
	
	// initialize the slider for choosing the refresh period
	$(function() {
		$("#refreshSlider").slider({
			min: 2,
			max: 15,
			value: 10,
			slide: function(event, ui) {
				GLOBAL.refreshInterval = 1000 * ui.value;
				$("#refreshInterval").html(
						"Refresh interval: " + ui.value + " seconds");
			}
		});
	});
	
	$(function() {
		$("#nrStreetsSlider").slider({
			min: 5,
			max: 30,
			value: 10,
			slide: function(event, ui) {
				GLOBAL.nrStreetsForStatistics = ui.value;
				$("#nrStreets").html(
						"Show top " + ui.value);
			}
		});
	});
	
});