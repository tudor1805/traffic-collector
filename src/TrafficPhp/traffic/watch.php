<?php
require_once("SingletonDB.class.php");

$db = SingletonDB::connect();



$db->close();
?>