<?php
require_once("SingletonDB.class.php");

header("Content-type: text/xml");

$db = SingletonDB::connect();

$res = $db->query("SELECT * FROM users WHERE uuid='".$db->real_escape_string($_POST['id_user'])."'");
if($res->num_rows != 0)
	$user = $res->fetch_assoc();
else
	$user['name'] = "";

$res = $db->query("SELECT lat, lng, speed FROM location WHERE id_user='".$db->real_escape_string($_POST['id_user'])."' AND stop = 0");
$line = $res->fetch_row();

$emp = "false";

if (empty($line[0])) { $line[0] = 44.44417; $emp="true"; }
if (empty($line[1])) { $line[1] = 26.05382; $emp="true"; }

$number = mt_rand(10,999); 
$number='.0'.$number; 
$number=floatval($number); 

/*
Testing issues
echo "<markers>";
echo '<marker ';
echo 'name="lat='.$line[0].' lon='.($line[1] + $number).'" ';
echo 'address="" ';
echo 'lat="'.$line[0].'" ';
echo 'lng="'.($line[1] + $number).'" ';
echo 'type="car" ';
echo 'unknown="'.$emp.'" ';
echo '/>';
echo "</markers>";*/
echo "<markers>";
echo '<marker ';
echo 'name="Latitude:'.$line[0].' &lt;br/&gt;Longitude:'.$line[1].' &lt;br/&gt;Speed:'.$line[2].'" ';
echo 'address="" ';
echo 'lat="'.$line[0].'" ';
echo 'lng="'.$line[1].'" ';
echo 'speed="'.($line[2] <= 0 ? 0 : $line[2]).'" ';
echo 'username="'.$user['name'].'" ';
echo 'type="car" ';
echo 'unknown="'.$emp.'" ';
echo '/>';
echo "</markers>";
?>