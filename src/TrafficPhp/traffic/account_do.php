<?php
require_once("authorize.php");
require_once("SingletonDB.class.php");

$db = SingletonDB::connect();

if($_POST['username'] == '')
{
	header("Location: account.php?ok=0");
	exit;
}
$res = $db->query("UPDATE users SET name = '".$db->real_escape_string($_POST['name'])."',
						username = '".$db->real_escape_string($_POST['username'])."'
					WHERE uuid='".$_SESSION['user_id']."'");
					
if(!$res)
	header("Location: account.php?ok=0");
if(isset($_POST['password']) && $_POST['password'] != '')
{
	if($_POST['password'] != $_POST['password_re'])
		header("Location: account.php?ok=0");
	else
	{
		$db->query("UPDATE users SET password='".sha1($db->real_escape_string($_POST['password']))."'");
		header("Location: account.php?ok=1");
	}
}

header("Location: account.php?ok=1");

$db->close();
?>