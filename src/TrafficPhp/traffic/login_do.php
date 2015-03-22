<?php
require_once("SingletonDB.class.php");

$db = SingletonDB::connect();

session_start();

$res = $db->query("SELECT * FROM users WHERE username='".$db->real_escape_string($_POST['username'])."' AND password='".sha1($db->real_escape_string($_POST['password']))."'");

if($res->num_rows != 0)
{
	$line = $res->fetch_assoc();
	$_SESSION['user_id'] = $line['uuid'];
	header("Location: table.php");
}
else header("Location: login.php?ok=0");

$db->close();
?>
