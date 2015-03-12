<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<script type='text/javascript' src='/CAPIM-WebApp/dwr/interface/ApplicationDwr.js'></script>
<script type='text/javascript' src='/CAPIM-WebApp/dwr/engine.js'></script>

<script type="text/javascript">
	ApplicationDwr.getString({callback: getIt});
	
	function getIt(result) {
		alert(result);
	}
</script>