<?php
require_once("SingletonDB.class.php");

session_start();
function currentScript() {
	return (isset($_SERVER['HTTPS']) ? ($_SERVER['HTTPS'] != '' ? "https://" : "http://") : "").$_SERVER['SERVER_NAME'].$_SERVER['REQUEST_URI'];
}

function currentSite() {
	return (isset($_SERVER['HTTPS']) ? ($_SERVER['HTTPS'] != '' ? "https://" : "http://") : "").$_SERVER['SERVER_NAME'];
}

function curPageName() {
	return substr($_SERVER["SCRIPT_NAME"],strrpos($_SERVER["SCRIPT_NAME"],"/")+1);
}

function getScript() {
	return curPageName()."?".$_SERVER["QUERY_STRING"];
}

function curPageName2()
{
	$uri = explode("/", $_SERVER["SCRIPT_NAME"]);
	return $uri[count($uri) - 1];
}

if(!isset($_SESSION['user_id'])) {
	if(curPageName2() != "login.php") {
		header("Location: login.php");
	}
	else {		
		$db = SingletonDB::connect();
		$db->query("INSERT INTO stats (server_info, type, page, login_date) VALUES ('".$db->real_escape_string(serialize($_SERVER))."', 0, '".currentScript()."', NOW())");
	}
}
else {
	$db = SingletonDB::connect();
	$db->query("INSERT INTO stats (id_user, server_info, page, type, login_date) VALUES ('".$_SESSION['user_id']."', '".$db->real_escape_string(serialize($_SERVER))."', '".currentScript()."', 1, NOW())");
}
?>