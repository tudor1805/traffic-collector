<?php
require_once("constants.inc.php");
require_once("SingletonDB.class.php");

$db = SingletonDB::connect();

if(isset($_POST['anti']) && $_POST['anti'] == "10101" && isset($_POST['id_user']) && isset($_POST['val1']) && isset($_POST['val2']) && isset($_POST['val3'])) {
	$name = str_replace(" ", "+", urldecode($_POST['val1']));
	$user = str_replace(" ", "+", urldecode($_POST['val2']));
	$pass = str_replace(" ", "+", urldecode($_POST['val3']));
	$id_user = str_replace(" ", "+", urldecode($_POST['id_user']));
	
	exec("cd " . TC_SERV_PATH . "; java -cp commons-codec-1.6.jar:mysql-connector-java-5.1.7-bin.jar:TrafficCollectorServer.jar ro.pub.acs.traffic.collector.CryptTool 1 '".$name."'", $result1);
	$name = $result1[0];
	
	exec("cd " . TC_SERV_PATH . " ; java -cp commons-codec-1.6.jar:mysql-connector-java-5.1.7-bin.jar:TrafficCollectorServer.jar ro.pub.acs.traffic.collector.CryptTool 1 '".$user."'", $result2);
	$user = $result2[0];
	
	exec("cd " . TC_SERV_PATH . " ; java -cp commons-codec-1.6.jar:mysql-connector-java-5.1.7-bin.jar:TrafficCollectorServer.jar ro.pub.acs.traffic.collector.CryptTool 1 '".$pass."'", $result3);
	$pass = $result3[0];
	
	exec("cd " . TC_SERV_PATH . " ; java -cp commons-codec-1.6.jar:mysql-connector-java-5.1.7-bin.jar:TrafficCollectorServer.jar ro.pub.acs.traffic.collector.CryptTool 1 '".$id_user."'", $result4);
	$id_user = $result4[0];
	
	$test = $db->query("SELECT * FROM users WHERE uuid='".$db->real_escape_string($id_user)."'");
	if($test->num_rows != 0)
		$db->query("UPDATE users SET name='".$db->real_escape_string(base64_decode($name))."', username='".$db->real_escape_string($user)."', password = '".$db->real_escape_string($pass)."' WHERE uuid='".$db->real_escape_string($id_user)."'");
}

echo "1";

$db->close();
?>
