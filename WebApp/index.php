<?php

session_start();

$errors = "";

if(!empty($_SESSION['login'])){
	header("Location: admin.php");
	exit();
}

if(isset($_POST["submit"])){
	
	$username = strip_tags($_POST["inputUsername"]);
	$password = $_POST["inputPassword"];
	
	if(!empty($username) && !empty($password)){
		
		include("connection.php");
		
		$username = mysqli_real_escape_string($sql_connect, $username);
		$password = md5($password);
	
		$query = mysqli_query($sql_connect,"SELECT * FROM admins WHERE username='$username' AND password='$password';");
		$rows = mysqli_num_rows($query);
		
		if($rows == 1){
			
			$_SESSION['login'] = $username;
			header("Location: admin.php");
			exit();
			
		} else {
			$errors = '<div class="alert alert-danger" role="alert">Login failed, Incorrect Username/Password.</div>';
		}
		
	} else {
		$errors = '<div class="alert alert-danger" role="alert">Please enter your username and password!</div>';
	}
	
}

?>

<!doctype html>
<html>
   <head>
      <meta charset="utf-8">
      <title>SaveMe | Administration</title>
      <link href="css/bootstrap.min.css" rel="stylesheet">
      <link href="css/signin.css" rel="stylesheet">
   </head>
   <body>
      <div class="container">
      	<?php echo $errors; ?>
         <form class="form-signin" action="#" method="post">
            <h2 class="form-signin-heading">SaveMe Administrator</h2>
            <label for="inputUsername" class="sr-only">Username</label>
            <input type="text" id="inputUsername" name="inputUsername" class="form-control" placeholder="Username" required autofocus>
            <label for="inputPassword" class="sr-only">Password</label>
            <input type="password" id="inputPassword" name="inputPassword" class="form-control" placeholder="Password" required>
            <input name="submit" class="btn btn-lg btn-primary btn-block" type="submit"/>
         </form>
      </div>
      <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
      <!-- Include all compiled plugins (below), or include individual files as needed -->
      <script src="js/bootstrap.min.js"></script>
   </body>
</html>