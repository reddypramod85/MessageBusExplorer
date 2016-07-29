<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link rel="icon" href="images/favicon.ico" type="image/x-icon">
<link rel="shortcut icon" href="images/favicon.ico" type="image/x-icon">
<title>HPE OneView Message Bus Explorer V1.0</title>
</head>
		<link href="css/bootstrap.min.css" rel="stylesheet" media="screen">
		<link href="css/hpe-main.css?3" rel="stylesheet" media="screen">
		<link href="css/fonts/fonts.css" rel="stylesheet" media="screen">
		<link rel="stylesheet" type="text/css" media="screen and (max-width: 1039px)" href="css/tablet.css" />
		<link rel="stylesheet" type="text/css" media="screen and (max-width: 710px)" href="css/mobile.css" />
<body>
<script src="js/jquery-1.4.2.js"></script>
<script type="text/javascript">
function LoginInfo()
{
	$("#errorHost").text("");
	$("#errorUserName").text("");
	$("#errorPassword").text("");
	var hostVar= $("#host").val();
	var userNameVar= $("#UsrName").val();
	var passwordVar= $("#pswd").val();
	if(hostVar==null || hostVar==""){
		$("#errorHost").text("Please enter the host name or ip address");
		return false;
	}else $("#errorHost").text("");
	if(userNameVar==null || userNameVar==""){
		$("#errorUserName").text("Please enter the User Name");
		return false;
	}else $("#errorUserName").text("");
	if(userNameVar==null || userNameVar==""){
		$("#errorPassword").text("Please enter the password");
		return false;
	}else $("#errorPassword").text("");

}
/* function Login()
{
	alert("login back");
	window.location.href = "/HPEOVMsgBus/Login.jsp";
} */
</script>
<div class="page-container clearfix">
			
			<!-- hpnn-header -->
			<div class="hpnn-header clearfix">
				
				<!-- hpnn-header-logo -->
				<div class="hpnn-header-logo">
					<img src="images/hpe.png" alt="HPE">
					<h3 align="center">HPE OneView Message Bus Explorer V1.0</h3>
					</div><!--/hpnn-header-logo-->
					 <span class="align-right">
       						 <a href="mailto:ci-lab@hpe.com?subject=Feedback for HPE OneView Message Bus Explorer Webapp&body=Please find below comments or suggestions ">by ci-lab@hpe.com</a>
   					 </span>
						
			</div><!--/hpnn-header-->
			
			<!-- page main -->
			<div class="page-main">
				<!-- login form -->
				<form name="Logout" method="post">
				<p>You have logged out of HPE OneView Message Bus Explorer V1.0</p>
              			<!-- <div class="submit-row"><input type="submit" class="btn btn-primary" value="Log Back In" onclick="JavaScript:return Login();"> </div> -->
              		<a class="btn btn-primary"  href="Login.jsp">Log Back In</a>
       			 </form>
       			
			</div><!-- /page main -->
 		
		</div><!--/page-container-->
		<!-- footer -->
		<div class="footer clearfix">
			
			<!-- ft-text -->
			<div class="ft-text">
				
			</div><!--/ft-text-->
			
			<!-- ft-side -->
			<div class="ft-side">
				
				<!-- clearfix -->
				<div class="clearfix">
					<ul class="nav nav-pills">
						<li><a href="javascript:openNewWindow('/hperdlogin/html/hperdlogin_rb_help.html');">Help</a></li>
						<li><a href="javascript:openSecureInfoWindow('/hperdlogin/html/secure_info.html');">Security Information</a></li>
					</ul>
				</div><!--/clearfix-->
				
				<p>&copy;  Copyright  <script> document.write(new Date().getFullYear()) </script> Hewlett Packard Enterprise Development LP </p>
				
			</div><!--/ft-side-->
			
		</div><!--/footer-->
		
		<div id="responsiveBreakpoint"></div>
</body>
</html>