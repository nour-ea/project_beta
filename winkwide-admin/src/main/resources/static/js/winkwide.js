// Loading animation
$(window).on('load', function(){
	
	// Animate loader off screen
	$(".se-pre-con").fadeOut("slow");
	
	//initiate Date Time Pickers
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
	
});

//Alert Management
function createAlert(type, message, timeout){
	angular.element(mainAlert).attr('class','fixed-top mx-auto w-50 m-0 alert alert-dismissible fade show alert-'+type);
	angular.element(mainAlert).html(message 
			+ '<button type=\"button\" class=\"close\" data-dismiss=\"alert\" aria-label=\"Close\"><span aria-hidden=\"false\">&times\;</span></button>');
	angular .element(mainAlert).attr('style','display:block');
	
	if(timeout !== null)
		setTimeout( clearAlert, 10000);
} 

function clearAlert(){
	angular.element(mainAlert).attr('style','display:none');
}


