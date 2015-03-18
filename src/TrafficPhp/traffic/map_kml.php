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
		<script type="text/javascript" src="http://maps.google.com/maps/api/js?sensor=false&key=AIzaSyAKpTOgdR4K5uxGfjcjIfO74X9beh3vpPU"></script>
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

			function load() {
				

				// Change this depending on the name of your PHP file
				downloadUrl("marker_kml.php?id=<?php echo $_GET['id']?>", function(data) {
						var xml = data.responseXML;
						var map = new google.maps.Map(document.getElementById("map"), {
							center: new google.maps.LatLng(44.44417, 26.05382),
							zoom: 11,
							mapTypeId: 'roadmap'
						});
						var infoWindow = new google.maps.InfoWindow;
						bindInfoWindow(marker, map, infoWindow, html);
						var ctaLayer = new google.maps.KmlLayer(xml);
						ctaLayer.setMap(map);
					}
				});
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

			function doNothing() {}

			//]]>

		</script>

	</head>

	<body onload="load()">
		<div id="map"></div>
	</body>
</html>