var hostVar;
var exchangeVar;
var keyVar;
var flagVar=1;
var abortVar=1;
var response;
var response2;
$(document).ready(function() {
	var exchangeVal = $("#exchangeVal").val();
	var keyVal = $("#keyVal").val();
	if(exchangeVal!="" && keyVal!=""){
		$("#dropdown1").val(exchangeVal);
		$("#dropdown2").val(keyVal);
	}
	flagVar=1;
	$("#result").hide();
	$("#pauseSubmit").hide();
	$("#dropdown2").hide();
	$("#dropdown3").hide();
	$("#keylabel").hide();
	$("#spanOr").hide();
	$("#key1").hide();
	$("#channelStatus").blink();
	$("#channelStatus").text("");
});
function showSCMBMSMB(){
	var exchangeValChange = $("#dropdown1").val();
	if(exchangeValChange!=null && exchangeValChange!=""){
		$("#keylabel").show();
		$("#result").hide();
		$("#pauseSubmit").hide();
		$("#channelStatus").text("");
		if(exchangeValChange=="scmb"){
			$("#dropdown2").show();
			$("#dropdown3").val("");
			$("#dropdown3").hide();
		}else if(exchangeValChange=="msmb"){
			$("#dropdown3").show();
			$("#dropdown2").val("");
			$("#dropdown2").hide();
		}
		$("#spanOr").show();
		$("#key1").show();
	}
}
function blinker() {
    $('.blink_me').fadeOut(500);
    $('.blink_me').fadeIn(500);
}


function clearSCMBMSMB(){
	var textboxKeyval = $("#key1").val();
	if(textboxKeyval!=null && textboxKeyval!=""){
		$("#key1").val("Enter Custom Key here");
	}
}
function logout(){
	window.location.href = "/HPEOVMsgBus/Logout.jsp";
}

function pollSubmit(){
	flagVar=1;
	abortVar=1;
	$("#pauseSubmit").hide();
	$("#errorExchange").text("");
	$("#errorKey").text("");
	hostVar= $("#host").val();
	exchangeVar= $("#dropdown1").val();
	if($("#dropdown2").val()!=null && $("#dropdown2").val()!="")
		keyVar =$("#dropdown2").val();
	else if($("#dropdown3").val()!=null && $("#dropdown3").val()!="")
		keyVar =$("#dropdown3").val();
	else keyVar =$("#key1").val();
	if(hostVar==null || hostVar==""){
		$("#errorHost").text("Please enter the host name or ip address");
		$("#result").hide();
		return false;
	}else $("#errorHost").text("");
	if(exchangeVar==""){
		$("#result").hide();
		$("#errorExchange").text("Please select the Exchange Name");
		return false;
	}else $("#errorExchange").text("");
	if(keyVar==null || keyVar=="" || keyVar=="Enter Custom Key here"){
		$("#result").hide();
		$("#errorKey").text("Please select or enter the Key");
		$("#channelStatus").text("");
		return false;
	}else $("#errorKey").text("");
	if(keyVar!=null && keyVar!="" && keyVar=="msmb.#")
		alert("Metric configuration automatically set to 300sec interval if no configuration found!");
	poll();

}
function pollPause(){
	flagVar=2;
	abortVar=2;
	$("#channelStatus").text("Paused...");
	poll();
}
function pollResume(){
	flagVar=2;
	abortVar=1;
	$("#channelStatus").text("Listening...");
	poll();
}

function viewJSON(resp){
	var output = resp.replace(/,/g, '<br>');
	var myWindow = window.open("", "MsgWindow", "width=800, height=600");
	myWindow.document.write(output);
}
  
function linkDownload(a, filename, content) {
	contentType =  'data:application/octet-stream,';
	uriContent = contentType + encodeURIComponent(content);
	a.setAttribute('href', uriContent);
	a.setAttribute('download', filename);
}

function download(filename, content) {
	var a = document.createElement('a');
	var json = JSON.stringify(content);
	json=json.replace(/,/g, ',\r\n').replace(/}/g,'}\r\n').replace(/\\/gi,"");
	linkDownload(a, filename, json);
	document.body.appendChild(a);
	a.click();
	document.body.removeChild(a);
}
function poll()
{
	$.ajax({
		url: "ReceiveMessageServlet",
		type: "GET",
		contentType: 'application/json; charset=utf-8',
		data: { host: hostVar, exchange : exchangeVar, key: keyVar,flag: flagVar } ,
		success: function(data){
			response = $.parseJSON(data);
			$('#result').empty();
			if(response!=null){
				$("#result").show();
				$("#pauseSubmit").show();
				var header= "<tr><th>Type</th><th>Category</th><th>Modification</th><th>Resource Uri</th><th>TimeStamp</th><th>View JSON</th></tr>";
				$('#result').append(header);
				$(function() {
					//$.each(response, function(i, item) {
					var respLength = response.length;
					for(i=respLength-1;i>=0;i--){
						var response1 = $.parseJSON(response[i]);
						response2= response[i];
						var td1,td2,td3,td4,td5;
						if(response1.resource.type!=null)
							td1=response1.resource.type;
						else td1="";
						if(response1.resource.category!=null)
							td2=response1.resource.category;
						else td2="";
						if(response1.changeType!=null)
							td3=response1.changeType;
						else td3="";
						if(response1.resourceUri!=null)
							td4=response1.resourceUri;
						else td4="";
						if(response1.timestamp!=null)
							td5=response1.timestamp;
						else td5="";

						var input = document.createElement("input");
						input.type = "button";
						input.className="button";
						input.value = "Click here";
						input.onclick = (function(response2){
							return function(){
								viewJSON(response2);
							}
						})(response2);
						var $tr = $('<tr>').append(
								$('<td>').text(td1),
								$('<td>').text(td2),
								$('<td>').text(td3),
								$('<td>').text(td4),
								$('<td>').text(td5),	
								$('<td>').append(input)
						); 
						$tr.appendTo('#result');

					}
				});
			}else{
				$("#pauseSubmit").hide();
				$("#channelStatus").text("Listening...");
				setInterval(blinker, 1000); //Runs every second
			}
			setTimeout(function() {flagVar=2;
			if(abortVar!=null && abortVar==1)
				poll();
			else
				ajax.abort();
			}, 5000);

		}
	});
}

function excelFun(){
	alert("inside table to excel...");
$("#result").table2excel({
	exclude: ".noExl",
	name: "Excel Document Name"
}); 
}