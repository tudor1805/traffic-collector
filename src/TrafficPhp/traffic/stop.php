<?php
require_once("SingletonDB.class.php");

$db = SingletonDB::connect();

if(isset($_POST['anti']) && $_POST['anti'] == "10101" && isset($_POST['id_user']))
	$db->query("UPDATE location SET stop = '".$db->real_escape_string($_POST['stop'])."' WHERE id_user='".$db->real_escape_string(str_replace(" ", "+", $_POST['id_user']))."'");
print_r($_POST);
$db->close();
?>