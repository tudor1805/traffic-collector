<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	<title>Traffic View - Administrator Console</title>
		
	<script type='text/javascript' src='dwr/engine.js'></script>
	<script type='text/javascript' src='dwr/util.js'></script>
	<script type='text/javascript' src='dwr/interface/ApplicationDwr.js'></script>		

	<script type="text/javascript">
		function clearTrafficData() {
			ApplicationDwr.clearTrafficData({callback: function() {}, async: false});	
		}
	</script>
</head>
<body>
	<h1>Admin Console</h1>
	<button id="btnClearTraffic" onclick="clearTrafficData()">Clear traffic</button>
</body>
</html>