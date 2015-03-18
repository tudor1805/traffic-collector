<?php
require_once("SingletonDB.class.php");

$db = SingletonDB::connect();

if(isset($_POST['anti']) && $_POST['anti'] == "10101" && isset($_POST['id_user']))
	$db->query("DELETE FROM location WHERE id_user='".$db->real_escape_string(str_replace(" ", "+", $_POST['id_user']))."'");
print_r($_POST);
$db->close();
?>