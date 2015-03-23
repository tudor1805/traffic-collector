<?php
require_once("authorize.php");
?>
<!DOCTYPE html >
	<head>
		<meta name="viewport" content="initial-scale=1.0, user-scalable=no" />
		<meta http-equiv="content-type" content="text/html; charset=UTF-8"/>
		<title>Google Maps Example</title>
		<style>
			#map {
				position: absolute; left: 0;top: 0; right: 0; bottom: 0;
			}
		</style>
		<link rel="stylesheet" type="text/css" href="http://141.85.164.61/traffic/css/screen_.css" media="all">
		<script type="text/javascript" src="http://maps.google.com/maps/api/js?sensor=false&key=AIzaSyB_JRyT6pqtylf36u66OEPp0M8Qkpp0G8g"></script>
		<script type="text/javascript" src="http://141.85.164.61/traffic/js/jquery/jquery-1.4.1.min.js"></script>
		<script type="text/javascript" src="js/StyledMarker.js"></script>
		<script type="text/javascript">
			//<![CDATA[

			var customIcons = {
				fastest: {
					icon: 'images/c_blue.png'
				},
				fast: {
					icon: 'images/c_green.png'
				},
				slow: {
					icon: 'images/c_red.png'
				},
				stop: {
					icon: 'images/c_black.png'
				}
			};

			var geocoder;
			var tableid = 260197;
			var no_paths = 10;
			var g_paths = [];
			
			if (typeof(Number.prototype.toRad) === "undefined") {
				Number.prototype.toRad = function() {
					return this * Math.PI / 180;
				}
			}

			function distance_haversine(lat1, lon1, lat2, lon2, param)
			{
				var R = 6371; // Radius of the earth in km
				var dLat = (lat2-lat1).toRad();  // Javascript functions in radians
				var dLon = (lon2-lon1).toRad();
				var a = Math.sin(dLat/2) * Math.sin(dLat/2) +
						Math.cos(lat1.toRad()) * Math.cos(lat2.toRad()) *
						Math.sin(dLon/2) * Math.sin(dLon/2);
				var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
				var d = R * c; // Distance in km
				return d;
			}

			function drawCustomLine(coordinates, color) {
				var path = new google.maps.Polyline({
					  path: coordinates,
					  strokeColor: color,
					  strokeOpacity: 1.0,
					  strokeWeight: 3
					});

				return path;
			}
			
			function getData(map, index, show) {
				// Change this depending on the name of your PHP file
				downloadUrl("marker.php?id=<?php echo $_GET['id']?>&type=" + index, function(data) {
					var xml = data.responseXML;
					var markers = xml.documentElement.getElementsByTagName("marker");
					var lastPoint;
					for (var i = 0; i < markers.length; i++) {
						var name = markers[i].getAttribute("name");
						var address = markers[i].getAttribute("address");
						var type = markers[i].getAttribute("type");
						var address_geo = "";
						var speed = markers[i].getAttribute("speed");
						var point = new google.maps.LatLng(
						parseFloat(markers[i].getAttribute("lat")),
						parseFloat(markers[i].getAttribute("lng")));

						var innerColor = borderColor = "#FFFFFF";//"FE7569";

						var step = 1;
						if(speed > 80)
						{
							//blue
							var r = 0, g = 0, b = 255;
							b -= (Math.round(speed) - 80) * 4;
							if(b < 40)
								b = 40;
							innerColor = "#0000" + b.toString(16);
						}
						if(speed >= 40 && speed <= 80)
						{
							//green
							var r = 0, g = 255, b = 0;
							g -= (Math.round(speed) - 40) * 4;
							innerColor = "#00" + g.toString(16) + "00";
						}
						if(speed > 1 && speed < 40)
						{
							//red
							var r = 255, g = 0, b = 0;
							r -= (Math.round(speed) - 1) * 4;
							innerColor =  "#" + r.toString(16) + "0000";
						}
						borderColor = innerColor;
						
						if(index == 2)
							borderColor = innerColor = "#000000";
						var distance = 0;
						if(i > 0) {
							distance = distance_haversine(lastPoint.lat(), lastPoint.lng(), point.lat(), point.lng(), "m");
						}

						if(i > 0 && distance < 0.1) {
							path = drawCustomLine([lastPoint, point], innerColor);
							g_paths[index].push(path);
							path.setMap(map);
							if(!show)
								path.setVisible(false);
						}
						lastPoint = point;
					}
				});
			}

			function load() {
				//geocoder = new google.maps.Geocoder();

				var map = new google.maps.Map(document.getElementById("map"), {
					center: new google.maps.LatLng(44.44417, 26.05382),
					zoom: 11,
					mapTypeId: 'roadmap'
				});
				
				for(i = 1; i <= no_paths; i++)
					g_paths[i] = [];

				$("a[rel=toggleLayer]").click(function() {
					var layer_ = $(this);
					var id = $(this).attr("layer-id");
					if(!layer_.hasClass("active")) {
						layer_.addClass("active");
						for(i = 0; i < g_paths[id].length; i++)
							g_paths[id][i].setVisible(true);
					}
					else {
						layer_.removeClass("active");
						for(i = 0; i < g_paths[id].length; i++)
							g_paths[id][i].setVisible(false);
					}
				});

				$("#layers").fadeTo("slow", 0.7);
				$("#layers").mouseover(function() {
					$(this).stop().fadeTo("fast", 1);
				});
				$("#layers").mouseout(function() {
					$(this).stop().fadeTo("slow", 0.7);
				});

				var layer = new google.maps.FusionTablesLayer(tableid);
				layer.setMap(map);
				// Create the legend and display on the map
				var legendDiv = document.createElement('DIV');
				var legend = new Legend(legendDiv, map);
				legendDiv.index = 1;
				map.controls[google.maps.ControlPosition.RIGHT_BOTTOM].push(legendDiv);

				var infoWindow = new google.maps.InfoWindow;

				// Change this depending on the name of your PHP file
				getData(map, 1, true);
				getData(map, 2, false);
				/* downloadUrl("marker.php?id=<?php echo $_GET['id']?>", function(data) {
					var xml = data.responseXML;
					var markers = xml.documentElement.getElementsByTagName("marker");
					var lastPoint;
					for (var i = 0; i < markers.length; i++) {
						var name = markers[i].getAttribute("name");
						var address = markers[i].getAttribute("address");
						var type = markers[i].getAttribute("type");
						var address_geo = "";
						var speed = markers[i].getAttribute("speed");
						var point = new google.maps.LatLng(
						parseFloat(markers[i].getAttribute("lat")),
						parseFloat(markers[i].getAttribute("lng")));

						var innerColor = borderColor = "#FFFFFF";//"FE7569";

						var step = 1;
						if(speed > 80)
						{
							//blue
							var r = 0, g = 0, b = 255;
							b -= (Math.round(speed) - 80) * 4;
							if(b < 40)
								b = 40;
							innerColor = "#0000" + b.toString(16);
						}
						if(speed >= 40 && speed <= 80)
						{
							//green
							var r = 0, g = 255, b = 0;
							g -= (Math.round(speed) - 40) * 4;
							innerColor = "#00" + g.toString(16) + "00";
						}
						if(speed > 1 && speed < 40)
						{
							//red
							var r = 255, g = 0, b = 0;
							r -= (Math.round(speed) - 1) * 4;
							innerColor =  "#" + r.toString(16) + "0000";
						}
						borderColor = innerColor;

						var distance = 0;
						if(i > 0) {
							distance = distance_haversine(lastPoint.lat(), lastPoint.lng(), point.lat(), point.lng(), "m");
						}

						if(i > 0 && distance < 0.1) {
							path = drawCustomLine([lastPoint, point], innerColor);
							g_paths[1].push(path);
							path.setMap(map);
						}
						
						lastPoint = point;
					}
				}); */
			}

			function bindInfoWindow(marker, map, infoWindow, html) {
				google.maps.event.addListener(marker, 'click', function() {
					infoWindow.setContent(html);
					infoWindow.open(map, marker);
				});
			}

			function downloadUrl(url, callback) {
				var request = window.ActiveXObject ? new ActiveXObject('Microsoft.XMLHTTP') : new XMLHttpRequest;

				request.onreadystatechange = function() {
					if (request.readyState == 4) {
						request.onreadystatechange = doNothing;
						callback(request, request.status);
					}
				};

				request.open('GET', url, true);
				request.send(null);
			}

			function Legend(controlDiv, map) {
			  // Set CSS styles for the DIV containing the control
			  // Setting padding to 5 px will offset the control
			  // from the edge of the map
			  controlDiv.style.padding = '5px';

			  // Set CSS for the control border
			  var controlUI = document.createElement('DIV');
			  controlUI.style.backgroundColor = 'white';
			  controlUI.style.borderStyle = 'solid';
			  controlUI.style.borderWidth = '1px';
			  controlUI.title = 'Legend';
			  controlDiv.appendChild(controlUI);

			  // Set CSS for the control text
			  var controlText = document.createElement('DIV');
			  controlText.style.fontFamily = 'Arial,sans-serif';
			  controlText.style.fontSize = '12px';
			  controlText.style.paddingLeft = '4px';
			  controlText.style.paddingRight = '4px';

			  // Add the text
			  controlText.innerHTML = '<b>Speeds*</b><br />' +
				'<img src="images/slowest.png" /> stoped<br />' +
				'<img src="images/slow.png" /> &lt;40 km/h<br />' +
				'<img src="images/fast.png" /> 40 - 80 km/h<br />' +
				'<img src="images/fastest.png" /> &gt;80 km/h<br />';
			  controlUI.appendChild(controlText);
			}

			function doNothing() {}

			//]]>

		</script>

	</head>

	<body onload="load()">
		<div id="map"></div>
		<div id="layers" style="opacity: 0.7; ">
			<div id="layersContainer">
				<ul id="layersList">
					<li>
						<a rel="toggleLayer" class="active" id="layer_1" layer-id="1" href="#layer_1">Raw data</a>
						<a rel="toggleLayer" id="layer_2" layer-id="2" href="#layer_2">Corrected data</a>
						<a rel="toggleLayer" id="layer_3" layer-id="3" href="#layer_3">OSM roads</a>
					</li>
				</ul>
			</div>
		</div>
	</body>
</html>
