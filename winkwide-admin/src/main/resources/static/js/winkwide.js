// Loading animation
$(window).on('load', function(){
	
	// Animate loader off screen
	$(".se-pre-con").fadeOut("slow");
	
	//initiate Date Time Pickers
	initiateDateTimePickers()
	
});



//Alert Management
//<div id="mainAlert" style="display:none" role="alert"> </div>
function createAlert(position, type, message, timeout){
	var alert = document.createElement('DIV');
	
	angular.element(position).append(alert);
	
	alert.innerHTML = message + '<button type=\"button\" class=\"close\" data-dismiss=\"alert\" aria-label=\"Close\"><span aria-hidden=\"false\">&times\;</span></button>';
	
	if(position.getAttribute('class').indexOf('modal')!==-1){
		alert.setAttribute('class','small mx-auto w-100 m-1 alert alert-dismissible fade show alert-'+type);
		alert.id = 'modalAlert'; 
		if(timeout !== null) setTimeout( clearModalAlert, 7000);
	}else{
		alert.setAttribute('class','small fixed-top mx-auto w-50 m-0 alert alert-dismissible fade show alert-'+type);
		alert.id = 'mainAlert';
		if(timeout !== null) setTimeout( clearMainAlert, 7000);
	}
	
} 

function clearMainAlert(){
	try{
		angular.element(mainAlert).remove();		
	}catch(err){
		console.log(err.toString());
	}
}

function clearModalAlert(){
	try{
		angular.element(modalAlert).remove();		
	}catch(err){
		console.log(err.toString());
	}
}

function initiateDateTimePickers(){
	
	$(".form_datetime").datetimepicker({
										format: 'yyyy-mm-dd HH:ii p',
										startDate: '2018-01-01 00:01 am',
										userCurrent: true,
								        autoclose: true,
										minuteStep: 15});

	$(".icon-arrow-right").addClass("fa fa-arrow-right").removeClass(".glyphicon",".icon-arrow-right");
	$(".glyphicon-arrow-right").addClass("fa fa-arrow-right").removeClass(".glyphicon",".glyphicon-arrow-right");
	$(".icon-arrow-left").addClass("fa fa-arrow-left").removeClass(".glyphicon",".icon-arrow-left");
	$(".glyphicon-arrow-left").addClass("fa fa-arrow-left").removeClass(".glyphicon",".glyphicon-arrow-left");
	
	$('.clockpicker').clockpicker({
	    placement: 'bottom',
	    align: 'left',
	    autoclose: true
	});
	
}

/*function includeHTML() {
	  var z, i, elmnt, file, xhttp;
	   Loop through a collection of all HTML elements: 
	  z = document.getElementsByTagName("*");
	  for (i = 0; i < z.length; i++) {
	    elmnt = z[i];
	    search for elements with a certain atrribute:
	    file = elmnt.getAttribute("w3-include-html");
	    if (file) {
	       Make an HTTP request using the attribute value as the file name: 
	      xhttp = new XMLHttpRequest();
	      xhttp.onreadystatechange = function() {
	        if (this.readyState == 4) {
	          if (this.status == 200) {elmnt.innerHTML = this.responseText;}
	          if (this.status == 404) {elmnt.innerHTML = "Page not found.";}
	           Remove the attribute, and call this function once more: 
	          elmnt.removeAttribute("w3-include-html");
	          includeHTML();
	        }
	      } 
	      xhttp.open("GET", file, true);
	      xhttp.send();
	       Exit the function: 
	      return;
	    }
	  }
	}*/
