<?php

/*
maximum speed
average speed
time
length
*/

require_once("constants.inc.php");
require_once("SingletonDB.class.php");

function distance_haversine($lat1, $lon1, $lat2, $lon2, $param) 
{
	$earth_radius = 3960.00;
	$delta_lat = $lat2 - $lat1;
	$delta_lon = $lon2 - $lon1;
	$alpha    = $delta_lat/2;
	$beta     = $delta_lon/2;
	$a        = sin(deg2rad($alpha)) * sin(deg2rad($alpha)) + cos(deg2rad($lat1)) * cos(deg2rad($lat2)) * sin(deg2rad($beta)) * sin(deg2rad($beta)) ;
	$c        = asin(min(1, sqrt($a)));
	$distance = 2*$earth_radius * $c;
	$distance = round($distance, 4);
	
	if($param == "miles")
		return $distance;
	else if($param == "km")
		return $distance * 1.609344;
	else
		return ($distance * 1.609344)/1000;
}

function getTime($seconds) {
	$diff_seconds  = $seconds;
	$diff_weeks    = floor($diff_seconds/604800);
	$diff_seconds -= $diff_weeks   * 604800;
	$diff_days     = floor($diff_seconds/86400);
	$diff_seconds -= $diff_days    * 86400;
	$diff_hours    = floor($diff_seconds/3600);
	$diff_seconds -= $diff_hours   * 3600;
	$diff_minutes  = floor($diff_seconds/60);
	$diff_seconds -= $diff_minutes * 60;
	
	return $diff_hours." h ".$diff_minutes." m ".$diff_seconds." s";
}


$db = SingletonDB::connect();

session_start();

$res = $db->query("SELECT * FROM history WHERE id=".$_GET['id']);
$line = $res->fetch_assoc();

$fh = fopen(TC_SERV_LOG_PATH . "" . $line['file'], "r");
$line = fgets($fh, 4096);
$max_speed = 0;
$length = 0;
$lat_1 = 0;
$lon_1 = 0;
$i = 0;
$time_diffs = 0;
$time0_start = 0;
$time0_end = 0;
$last_speed = 0;
while (($line = fgets($fh, 4096)) !== false) 
{
	$elements = explode(" ", $line);

	
	if($elements[2] != -2)
	{
		if($i == 0)
			$time_start = round(trim($elements[3])/1000);
		else
			$time_end = round(trim($elements[3])/1000);
		
		$speed = (float)$elements[2] * 3.6;
		
		if($speed - $last_speed > 13)
			$speed = $last_speed;
		
		if($speed == 0) {
			$time0_start = round(trim($elements[3])/1000);
			$last_speed = 0;
			$time_last = round(trim($elements[3])/1000);
		}
		if($i != 0 && $speed != 0 && $last_speed == 0) 
		{
			$time0_end = round(trim($elements[3])/1000);
			$time_diff = $time0_end - $time0_start;
			if(isset($_GET['debug']))
				echo "Here1 ".$time0_end. " --- ".$time0_start."<br/>";
			if($time_diff <= 300000 && $time_diff >= 0)
				$time_diffs += $time_diff;
			$time0_start = $time0_end;
			$last_speed = $speed;
			$time_last = round(trim($elements[3])/1000);
		}
		if($i != 0 && $speed != 0 && $last_speed != 0) {
			if(isset($_GET['debug']))
				echo "$i Here2 ".round(trim($elements[3])/1000). " --- ".$time_last."<br/>";
			if(round(trim($elements[3])/1000) - $time_last >= 0 && round(trim($elements[3])/1000) - $time_last <= 3000)
				$time_diffs += round(trim($elements[3])/1000) - $time_last;
			$time_last = round(trim($elements[3])/1000);
			$last_speed = $speed;
		}
		
		$last_speed = $speed;
		
		if($speed > $max_speed) $max_speed = $speed;
		
		if($lat_1 == 0 && $lon_1 == 0)
		{
			$lat_1 = $elements[0];
			$lon_1 = $elements[1];
		}
		else
		{
			$lat_2 = $elements[0];
			$lon_2 = $elements[1];
			
			$len = distance_haversine($lat_1, $lon_1, $lat_2, $lon_2, "km");
			$length += $len;
			$lat_1 = $lat_2;
			$lon_1 = $lon_2;
		}
		$i++;
		if(isset($_GET['debug']))
			echo $time_diffs."<br/>";
	}
}


/*echo "Max Speed: ".$max_speed."<br/>";
echo "Length: ".$length."<br/>";
echo $time_diffs."<br/>";
echo $time_end-$time_start."<br/>";
echo getTime($time_diffs);
echo "<br/>";
echo "Avg Speed: ".round(($length * 3600)/$time_diffs, 3)." km/h";
echo "<br/>";
echo date("F d, Y H:i:s", $time_start);
echo "<br/>";
echo date("F d, Y H:i:s", $time_end);*/
fclose($fh);
?>

<div style="width:400px; font-size: 12px; line-height: 1em;">
	<table border="0" width="100%" cellpadding="0" cellspacing="0" id="product-table">
		<tr>
			<td><strong>Max Speed</strong></td>
			<td><?php echo str_replace(".", ",", round($max_speed,3));?> km/h</td>
		</tr>
		<tr class="alternate-row">
			<td><strong>Trip Length</strong></td>
			<td><?php echo str_replace(".", ",", round($length, 3));?> km</td>
		</tr>
		<tr>
			<td><strong>Trip time</strong></td>
			<td><?php echo getTime($time_diffs);?></td>
		</tr>
		<tr class="alternate-row">
			<td><strong>Average Speed</strong></td>
			<td><?php echo $time_diffs == 0 ? "0" : str_replace(".", ",", round(($length * 3600)/$time_diffs, 3));?> km/h</td>
		</tr>
		
	</table>
</div>
