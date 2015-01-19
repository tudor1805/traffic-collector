<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	
	<script type="text/javascript" src="js/json.js"></script>
	
	<script src="http://ajax.googleapis.com/ajax/libs/jquery/1/jquery.min.js" type="text/javascript"></script>
	<script src="js/jquery-ui-1.8.13.custom.min.js" type="text/javascript"></script>
	<script src="js/sliding_effect.js" type="text/javascript"></script>
	<script src="js/jquery.blockUI.js" type="text/javascript"></script>
	
	<script src="https://ajax.googleapis.com/ajax/libs/mootools/1.3.0/mootools-yui-compressed.js" type="text/javascript"></script>
	<script src="js/adapters/mootools-adapter.js" type="text/javascript"></script>
	<script src="js/highcharts.js" type="text/javascript"></script>
	<script type="text/javascript" src="https://www.google.com/jsapi"></script>
	<script type="text/javascript">
    
      // Load the Visualization API and the piechart package.
      google.load('visualization', '1', {'packages':['corechart']});
	</script>
	
	<script type="text/javascript" src="http://maps.google.com/maps/api/js?sensor=false"></script> 
	<script type="text/javascript" src="js/markerclusterer.js"></script>
	<script type="text/javascript" src="js/markermanager.js"></script> 
	
	<script type="text/javascript" src="js/main.js"></script> 
	<script type='text/javascript' src='dwr/engine.js'></script>
	<script type='text/javascript' src='dwr/util.js'></script>
	<script type='text/javascript' src='dwr/interface/ApplicationDwr.js'></script>	
	
	<link type="text/css" href="style/smoothness/jquery-ui-1.8.13.custom.css" rel="stylesheet" />
	<link type="text/css" href="style/main.css" rel="stylesheet" media="all" /> 

	<title>Traffic visualization</title>
</head>
<body onload="createMap()">
	<div id="wrapper">
		<div id="header">
			<h1><b>T</b>raffic <b>V</b>iew</h1>
		</div>
		<div id="content">
			<div id="side">		
				<div id="navigation-block">
            		<ul id="sliding-navigation">
                		<li class="sliding-element"><a id="goToLink" href="#">Go to</a></li>
                		<li class="sliding-element"><a id="routingLink" href="#">Routing</a></li>
                		<li class="sliding-element"><a id="statisticsLink" href="#">Statistics</a></li>
                		<li class="sliding-element"><a id="optionsLink" href="#">Options</a></li>
                		<li class="sliding-element"><a id="cabsLink" href="#">Cabs</a></li>
                		<li class="sliding-element"><a id="aboutLink" href="#">About</a></li>
            		</ul>
        		</div>
			</div>
			
			<div id="mapDiv"></div>
			<div style="display: none" id="invisilbeDiv"></div>
		</div>	
	</div>
	
	<div id="viewOptionsDiv" style="display:none;">
		Show:<br/>
		<input type="radio" name="radio1" id="radioCongestion" checked="true"/>Congestion zones<br/>
		<input type="radio" name="radio1" id="radioAll"/>All cars<br/>
		<input type="radio" name="radio1" id="radioNone"/>No cars<br/>
		<input type="radio" name="radio1" id="radioFollowed"/>Only followed cars<br/>
		<input type="radio" name="radio1" id="radioUnfollowed"/>Only unfollowed cars<br/>
		<hr/>
		<div id="refreshInterval">Refresh interval: 10 seconds</div>
		<div id="refreshSlider"></div>
	</div>
	<div id="statisticsDiv" style="display:none;">
		Show:<br/>
		<input type="radio" name="radio2" id="radioHighestSpeeds" checked="true"/>Highest average speeds<br/>
		<input type="radio" name="radio2" id="radioLowestSpeeds"/>Lowest average speeds<br/>
		<input type="radio" name="radio2" id="radioBusiestStreets"/>Busiest streets<br/>
		<input type="radio" name="radio2" id="radioFreeStreets"/>Least-bussiest streets<br/>
		<hr/>
		<div id="nrStreets">Show top 10</div>
		<div id="nrStreetsSlider"></div>
	</div>
</body>
</html>