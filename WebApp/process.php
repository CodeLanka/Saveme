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
    	<th>Datetime</th>
        <th>Victim Name</th>
        <th>Location</th>
        <th>Process</th>
    </tr>
    <?php
	
	include("connection.php");
	
	if(!empty($_GET["process"])){
		mysqli_query($sql_connect,"UPDATE reports SET proceed='1' WHERE id='".(int)$_GET["process"]."'");
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
			$person_name = "Not sure";
		}
		
		
	?>
    
    <tr>
    	<td><?php echo $data["datetime"] ?></td>
        <td><?php echo $person_name; ?></td>
        <td><a href="http://maps.google.com/?q=<?php echo $data["location"] ?>">Click to View</a></td>
        <td><a href="admin.php?process=<?php echo $data["id"] ?>">Process</a></td>
    </tr>
    
    <?php
	}
	?>
 
  </table>
</div>


<?php



?>

        
</div>

</body>

</html>