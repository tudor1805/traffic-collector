<?php
header("Content-type: text/xml");

require_once("SingletonDB.class.php");

$db = SingletonDB::connect();

session_start();

$res = $db->query("SELECT * FROM history WHERE id=".$_GET['id']);
$line = $res->fetch_assoc();

echo "<markers>";
$fh = fopen("/home/cipsm/TrafficServer/logs/".$line['file'], "r");
$line = fgets($fh, 4096);
while (($line = fgets($fh, 4096)) !== false) 
{
	$elements = explode(" ", $line);
	$speed = (float)$elements[2] * 3.6;
	if($speed > 80) {
		$fastest[] = $elements[0].",".$elements[1].",0";
		$marker = 'fastest';
	}
	else if($speed >= 40 && $speed <= 80) {
		$fast[] = $elements[0].",".$elements[1].",0";
		$marker = 'fast';
	}
	else if($speed > 1 && $speed < 40) {
		$slow[] = $elements[0].",".$elements[1].",0";
		$marker = 'slow';
	}
	else if($speed <= 1) {
		$stop[] = $elements[0].",".$elements[1].",0";
		$marker = 'stop';
	}
	
	/*$lat = $elements[0];
	$lng = $elements[1];
	echo '<marker ';
	echo 'name="Datetime: '.date("F d, Y H:i:s", round(trim($elements[3])/1000)).' lat='.$elements[0].' lon='.$elements[1].'" ';
	echo 'address="Speed: '.$speed.' km/h " ';
	echo 'lat="'.$lat.'" ';
	echo 'lng="'.$lng.'" ';
	
		
	echo 'type="'.$marker.'" ';
	echo '/>';
	<?xml version="1.0" encoding="UTF-8"?>
<kml xmlns="http://earth.google.com/kml/2.1">
  <Document>
    <name>Chicago Transit Map</name>
    <description>Chicago Transit Authority train lines</description>

    <Style id="blueLine">
      <LineStyle>
        <color>ffff0000</color>
        <width>4</width>
      </LineStyle>
    </Style>
	<Style id="greenLine">
      <LineStyle>
        <color>ff009900</color>
        <width>4</width>
      </LineStyle>
    </Style>
    <Style id="redLine">
      <LineStyle>
        <color>ff0000ff</color>
        <width>4</width>
      </LineStyle>
    </Style>
    <Style id="blackLine">
      <LineStyle>
        <color>ffffffff</color>
        <width>4</width>
      </LineStyle>
    </Style>
    
    <Placemark>
      <name>Fastest</name>
      <styleUrl>#blueLine</styleUrl>
      <LineString>
        <altitudeMode>relative</altitudeMode>
        <coordinates>
			-87.89289951324463,41.97881025520548,0
        </coordinates>
      </LineString>
    </Placemark>
	
	<Placemark>
      <name>Fast</name>
      <styleUrl>#greenLine</styleUrl>
      <LineString>
        <altitudeMode>relative</altitudeMode>
        <coordinates>
			-87.89289951324463,41.97881025520548,0
        </coordinates>
      </LineString>
    </Placemark>
	
	<Placemark>
      <name>Blue Line</name>
      <styleUrl>#blueLine</styleUrl>
      <LineString>
        <altitudeMode>relative</altitudeMode>
        <coordinates>
			-87.89289951324463,41.97881025520548,0
        </coordinates>
      </LineString>
    </Placemark>
	
	<Placemark>
      <name>Blue Line</name>
      <styleUrl>#blueLine</styleUrl>
      <LineString>
        <altitudeMode>relative</altitudeMode>
        <coordinates>
			-87.89289951324463,41.97881025520548,0
        </coordinates>
      </LineString>
    </Placemark>
	
 </Document>
</kml>
	*/
}
fclose($fh);
echo "</markers>";
?>