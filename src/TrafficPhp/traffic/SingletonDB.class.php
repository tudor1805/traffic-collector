<?php
require_once("constants.inc.php");
class SingletonDB
{
	private static $instance;
	
	private function __construct() { }
	
	public static function connect() 
	{
		if (!isset(self::$instance))
		{
			self::$instance = new mysqli(ADDRESS, USERNAME, PASSWORD, DATABASE);
        }
        return self::$instance;
	}
}
?>
