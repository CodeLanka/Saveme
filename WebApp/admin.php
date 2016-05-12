<?php

session_start();

if (empty($_SESSION['login'])) {
    header("Location: index.php");
    exit();
} else {
    $privilege = $_SESSION["login"];
}

?>
<!doctype html>
<html>

<head>
    <meta charset="utf-8">
    <title>SaveMe | Administration</title>
    <link href="css/bootstrap.min.css" rel="stylesheet">
</head>

<body>

    <nav class="navbar navbar-default">
        <div class="container-fluid">
            <div class="navbar-header">
                <p class="navbar-text">Welcome,
                    <?php
echo $privilege;
?> | <a href="logout.php">Logout</a></p>
            </div>
        </div>
    </nav>

<div class="container">

<h1>Hello, <?php echo $privilege ?>!</h1>
<p>Active Reports.</p>

<div class="panel panel-default">
  <!-- Default panel contents -->
  <div class="panel-heading">Active Reports</div>

  <!-- Table -->
  <table class="table">
   	<tr>
    	<th width="15%">Datetime</th>
        <th width="25%">Victim Name</th>
        <th width="35%">Location</th>
        <th width="25%">Process</th>
    </tr>
    <?php
	
	include("connection.php");
	include_once 'libs/sms/SmsSender.php';
	
	if(!empty($_GET["reqamb"])){
		$coords = $_GET["coords"];
		$message = "There's an accident near, ".getLocation($coords).". Please send an ambulance asap.";
		
		// Create the sender object server url
		$sender = new SmsSender("https://api.dialog.lk/sms/send");
		
		//sending a one message
		$applicationId = "APP_021670";
		$encoding = "0";
		$version =  "1.0";
		$password = "password";
		$sourceAddress = "77100";
		$deliveryStatusRequest = "1";
		$charging_amount = ":15.75";
		$destinationAddresses = array("tel:94772540020");
		$binary_header = "";
		$res = $sender->sms($message, $destinationAddresses, $password, $applicationId, $sourceAddress, $deliveryStatusRequest, $charging_amount, $encoding, $version, $binary_header);
		
		echo "Message sent to Ambulance Service!";
		header("Location: admin.php");
			
	}
	
	if(!empty($_GET["remove"])){
		mysqli_query($sql_connect,"UPDATE reports SET proceed='1' WHERE id='".$_GET["remove"]."'");
		header("Location: admin.php");
	}
	
	if(!empty($_GET["police"])){
		$coords = $_GET["coords"];
		$message = "There's an accident near, ".getLocation($coords).". Please report on duty asap.";
		
		// Create the sender object server url
		$sender = new SmsSender("https://api.dialog.lk/sms/send");
		
		//sending a one message
		$applicationId = "APP_021670";
		$encoding = "0";
		$version =  "1.0";
		$password = "password";
		$sourceAddress = "77100";
		$deliveryStatusRequest = "1";
		$charging_amount = ":15.75";
		$destinationAddresses = array("tel:94772540020");
		$binary_header = "";
		$res = $sender->sms($message, $destinationAddresses, $password, $applicationId, $sourceAddress, $deliveryStatusRequest, $charging_amount, $encoding, $version, $binary_header);
		
		echo "Message sent to Police Service!";
		header("Location: admin.php");
	}
	
	$query = mysqli_query($sql_connect,"SELECT* FROM reports WHERE proceed='0'");
	while($data = mysqli_fetch_array($query)){
		
		if($data["saveme"]){
			$saveme_q = mysqli_query($sql_connect,"SELECT* FROM users WHERE savehash='".$data["saveme"]."'");
			$saveme_d = mysqli_fetch_array($saveme_q);
			$person_name = $saveme_d["fullname"];
		} else if($data["name"]){
			$person_name = $data["name"];
		} else {
			$person_name = "Unknown";
		}
		
		
	?>
    
    <tr>
    	<td><?php echo $data["datetime"] ?></td>
        <td><?php echo $person_name; ?></td>
        <td><?php echo getLocation($data["location"]); ?><br/><a href="http://maps.google.com/?q=<?php echo $data["location"] ?>">Click to View</a></td>
        <td>
        	<a href="admin.php?reqamb=<?php echo $data["id"] ?>&coords=<?php echo $data["location"] ?>">Request Ambulance</a><br/>
        	<a href="admin.php?relative=<?php echo $data["id"] ?>&coords=<?php echo $data["location"] ?>">Inform his relatives</a><br/>
            <a href="admin.php?police=<?php echo $data["id"] ?>&coords=<?php echo $data["location"] ?>">Inform Police</a><br/>
            <a href="admin.php?remove=<?php echo $data["id"] ?>">Remove Accident</a>
        </td>
    </tr>
    
    <?php
	}
	?>
 
  </table>
</div>


<?php


function getLocation($coords){
	$url = "http://maps.googleapis.com/maps/api/geocode/json?latlng=".$coords."&sensor=false";
	// Make the HTTP request
	$data = @file_get_contents($url);
	// Parse the json response
	$jsondata = json_decode($data,true);
		
	$address = $jsondata["results"][0]["formatted_address"];
	return $address;
}
?>

        
</div>

</body>

</html>