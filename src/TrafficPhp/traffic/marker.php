<?php
header("Content-type: text/xml");

require_once("constants.inc.php");
require_once("SingletonDB.class.php");

$db = SingletonDB::connect();

session_start();

if(isset($_GET['type']) && $_GET['type'] == 1)
{
	$res = $db->query("SELECT * FROM history WHERE id=".$_GET['id']);
	$line = $res->fetch_assoc();
}

echo "<markers>";
if(isset($_GET['type']) && $_GET['type'] == 1)
	$fh = fopen(TC_SERV_LOG_PATH . "" . $line['file'], "r");
else if(isset($_GET['type']) && $_GET['type'] == 2)
	$fh = fopen(TC_SERV_LOG_PATH . "" . "journey2012_04_19_19_26_25.log_onRoad", "r");
	
$line = fgets($fh, 4096);
while (($line = fgets($fh, 4096)) !== false) 
{
	$elements = explode(" ", $line);
	$speed = (float)$elements[2] * 3.6;
	if($speed > 80)
		$marker = 'fastest';
	else if($speed >= 40 && $speed <= 80)
		$marker = 'fast';
	else if($speed > 1 && $speed < 40)
		$marker = 'slow';
	else if($speed <= 1)
		$marker = 'stop';
/*
	$content = file_get_contents("http://maps.googleapis.com/maps/api/geocode/xml?latlng=".$elements[0].",".$elements[1]."&sensor=false&key=AIzaSyB_JRyT6pqtylf36u66OEPp0M8Qkpp0G8g");
	$rss = simplexml_load_string($content);
	$location = $rss->result->geometry->location;
	$lat = $location->lat;
	$lng = $location->lng;
	exit;
*/
	$lat = $elements[0];
	$lng = $elements[1];
	echo '<marker ';
	echo 'name="Date: '.date("F d, Y H:i:s", round(trim($elements[3])/1000)).'" ';
	echo 'address="Speed: '.$speed.' km/h " ';
	echo 'lat="'.$lat.'" ';
	echo 'lng="'.$lng.'" ';
	echo 'speed="'.$speed.'" ';
		
	echo 'type="'.$marker.'" ';
	echo '/>';
}
fclose($fh);
echo "</markers>";
?>
