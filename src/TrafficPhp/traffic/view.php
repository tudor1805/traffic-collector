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
	<script type="text/javascript" src="http://maps.google.com/maps/api/js?sensor=false&key=AIzaSyB_JRyT6pqtylf36u66OEPp0M8Qkpp0G8g"></script>
    <script type="text/javascript">
    //<![CDATA[
	
	var map;
	var mapMarker= [];
	
	var tableid = 260197;
	
    var customIcons = {
      restaurant: {
        icon: 'http://labs.google.com/ridefinder/images/mm_20_blue.png',
        shadow: 'http://labs.google.com/ridefinder/images/mm_20_shadow.png'
      },
      bar: {
        icon: 'http://labs.google.com/ridefinder/images/mm_20_red.png',
        shadow: 'http://labs.google.com/ridefinder/images/mm_20_shadow.png'
      },
      unknown: {
        icon: 'http://labs.google.com/ridefinder/images/mm_20_white.png',
        shadow: 'http://labs.google.com/ridefinder/images/mm_20_shadow.png'
      },
	  car: {
		icon: 'images/car.png'
	  }
    };

    function load() {
      
		var infoWindow = new google.maps.InfoWindow;
		
		// Change this depending on the name of your PHP file
		downloadUrl("markers.php?id_user=<?php echo $_GET['id_user']?>", function(data) {
			var xml = data.responseXML;
			var markers = xml.documentElement.getElementsByTagName("marker");
			for (var i = 0; i < markers.length; i++) {
				var name = markers[i].getAttribute("name");
				var username = markers[i].getAttribute("username");
				var address = markers[i].getAttribute("address");
				var speed = markers[i].getAttribute("speed");
				var lat = markers[i].getAttribute("lat");
				var lon = markers[i].getAttribute("lng");
				var type = markers[i].getAttribute("type");
				var point = new google.maps.LatLng(
				parseFloat(markers[i].getAttribute("lat")),
				parseFloat(markers[i].getAttribute("lng")));
				
				var layer = new google.maps.FusionTablesLayer(tableid);
				layer.setMap(map);
				// Create the legend and display on the map
				var legendDiv = document.createElement('DIV');
				var legend = new Legend(legendDiv, username, lat, lon, speed, map);
				legendDiv.index = 1;
				map.controls[google.maps.ControlPosition.RIGHT_BOTTOM].pop();
				map.controls[google.maps.ControlPosition.RIGHT_BOTTOM].push(legendDiv); 
				
				var html = (username != '' ?  "Name: " + username + "<br/><br/>" : "") +
							"Latitude: " + lat + "<br/>" + 
							"Longitude: " + lon + "<br/>" + 
							"Speed: " + speed + "<br/>";
				var myInfoWindowOptions = {
					content: '<div style="width: 300px;" class="info-window-content"><p>' + html + '</p></div>',
					maxWidth: 400
				};
				
				infoWindow = new google.maps.InfoWindow(myInfoWindowOptions);
				
				var icon = customIcons[type] || {};
				marker = new google.maps.Marker({
					map: map,
					position: point,
					icon: icon.icon,
					shadow: icon.shadow
				});
				google.maps.event.addListener(marker, 'click', function() {
					infoWindow.open(map, marker);
				});
				
				/*
				* Leave commented for now
				* infoWindow.open(map, marker);
				*/
				
				var isUnknown = markers[i].getAttribute("unknown") == "true" ? true : false;
				if (isUnknown) {
					html = "<b>unknown position</b>";
					marker = new google.maps.Marker({
						map: map,
						position: point,
						icon: customIcons["unknown"].icon,
						shadow: customIcons["unknown"].shadow
					});
				}
				mapMarker.push(marker);
				if(mapMarker.length > 1) {
					mapMarker[0].setMap(null);
					mapMarker.shift();
				}
				map.setCenter(new google.maps.LatLng(parseFloat(markers[i].getAttribute("lat")),
				parseFloat(markers[i].getAttribute("lng"))));
			}
		});
    }
	
	function watch() {
		map = new google.maps.Map(document.getElementById("map"), {
			center: new google.maps.LatLng(44.44417, 26.05382),
			zoom: 11,
			mapTypeId: 'roadmap'
		});
		load()
		setInterval('load()', 5000);
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
		var params = "id_user=<?php echo $_GET['id_user']?>";
		request.open("POST",url,true);
		request.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
		request.setRequestHeader("Content-length", params.length);
		request.setRequestHeader("Connection", "close");
		request.send(params);
    }
	
	function Legend(controlDiv, name, lat, lon, speed, map) {
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
		controlText.innerHTML = '<b>Information</b><br />' +
		(name != '' ? 'Name: ' + name + '<br />' : '') +
		'Latitude: ' + lat + '<br />' + 
		'Longitude: ' + lon + '<br />' + 
		'Speed: ' + speed + '<br />';
		controlUI.appendChild(controlText);
	}

    function doNothing() {}

    //]]>

  </script>

  </head>

  <body onload="watch();">
    <div id="map"></div>
  </body>

</html>
