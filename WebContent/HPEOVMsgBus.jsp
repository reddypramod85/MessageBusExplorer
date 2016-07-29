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
<script src="js/Ajax.js"></script>
<!-- <script src="http://ajax.googleapis.com/ajax/libs/jquery/2.0.0/jquery.min.js"></script> -->
<script src="js/jquery.table2excel.js"></script>
		<!-- page-container -->
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
					 	 <!-- <li> <a href="/HPEOVMsgBus/Logout.jsp">logout</a> </li> -->
					</span>	
			</div><!--/hpnn-header-->
			
			<!-- page main -->
			<div class="page-main">
				<!-- login form -->
				  <form name="HPBus" id="HPBus" method="POST">
				  				   <input type="hidden" name="target" id="tgt2">
                    <%if(request.getAttribute("errorMessage")!=null && request.getAttribute("errorMessage")!=""){
				  	 %>
					   <span id="errormsg" style="color:red"><%=request.getAttribute("errorMessage") %></span>
					 <%} %>
					<label for="Hostname"> Host name or IP Address of OneView Appliance: </label> 
			        	<input type="text" name="host" id="host" value='<%=((request.getParameter("host")==null)?"":request.getParameter("host"))%>' readonly="readonly"/><span id="errorHost" style="color:red"></span><br>
			        <label for="Exchangename"> Please select exchange name: </label>         
			        <select name="exchange" id="dropdown1" onchange="showSCMBMSMB();">
				        <option value="">Please select one
				        <option value="scmb">StateChangeMessageBus
				        <option value="msmb">MetricStreamingMessageBus
			    	</select>
			    	<span id="errorExchange" style="color:red"></span>
			    	<br>
			        <label for="Bindingkey" id="keylabel"> Please select or enter binding key: </label> 
			        <select name="key" id="dropdown2" onchange="clearSCMBMSMB();" >
				        <option value="">Please select one
				        <option value="scmb.server-hardware.#">scmb.server-hardware.# - receive all server hardware changes
				        <option value="scmb.#">scmb.# - receive all messages
				        <option value="scmb.alerts.#">scmb.alerts.# - receive all messages about alerts
				        <option value="scmb.tasks.#">scmb.tasks.# - receive all messages about any tasks
				        <option value="scmb.ethernet-networks.#">scmb.ethernet-networks.# - receive all messages about any Ethernet networks
				        <option value="scmb.fc-networks.#">scmb.fc-networks.# - receive all messages about any FibreChannel networks
				        <option value="scmb.interconnects.#">scmb.interconnects.# - receive all messages about any interconnects
				        <option value="scmb.enclosures.#">scmb.enclosures.# - receive all messages about any enclosures
				        <option value="scmb.*.Created.#">scmb.*.Created.# - receive only create messages about any resources
				        <option value="scmb.server-hardware.#">scmb.server-hardware.# - receive all messages about any servers
				        <option value="scmb.connections.Created.#">scmb.connections.Created.# - receive only delete messages about any connections
				        <option value="scmb.enclosures.Updated./rest/enclosures/Encl1">scmb.enclosures.Updated./rest/enclosures/Encl1 - receive only update messages from Encl1
				        <option value="scmb.enclosures.*./rest/enclosures/Encl1">scmb.enclosures.*./rest/enclosures/Encl1 - Receive all messages for the Encl1
			        </select>
			    	<select name="key" id="dropdown3" onchange="clearSCMBMSMB();" >
				        <option value="">Please select one
				        <option value="msmb.#">msmb.#
			    	</select>
			    	<span id="spanOr">OR</span>
			    	<input type="text" name="key1" id="key1" value="Enter Custom Key here" />
			    	<span id="errorKey" style="color:red"></span>
			    	<br>
			    	<input id="exchangeVal" type="hidden" value='<%= ((request.getAttribute("exchange")==null)?"":request.getAttribute("exchange"))%>'>
			    	<input id="keyVal" type="hidden" value='<%= ((request.getAttribute("key")==null)?"":request.getAttribute("key"))%>'>
			       	<div class="submit-row">
			       		<input type="button" onclick="JavaScript:return pollSubmit();"  class="btn btn-primary" value="Submit" >
			       		<!-- <input type="button" onclick="JavaScript:return logout();"  class="btn btn-primary" value="log out" style="margin-left:80%;margin-top:0%">-->
			       		<input type="button" onclick="JavaScript:return logout();"  class="btn btn-primary" value="Logout">
			       	
			       	 </div>
					<br></br>
					<span class="blink_me" id="channelStatus" style="color:#00B388;font-size: 15pt;font-style:italic Bold;"></span><br></br>
					 <table id="result"  cellpadding="5" class="CSSTableGenerator" >
					</table>		<br>
					<div class="submit-row" id="pauseSubmit" >
					<input type="button" onclick="JavaScript:return pollPause();"  class="btn btn-primary" value="Pause" >
			       	<input type="button" onclick="JavaScript:return pollResume();"  class="btn btn-primary" value="Resume" >
			       	<input type="button" onclick="JavaScript:return download('test.txt',response);"  class="btn btn-primary" value="Download All JSON Messages" >
			       	<input type="button" onclick="JavaScript:return excelFun();"id="excel" class="btn btn-primary" value="Export" >
			       	</div>
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
		
		<script src="js/jquery.min.js"></script>
		<script src="js/bootstrap.min.js"></script>
		<script src="js/scripts.js"></script>
	</body>