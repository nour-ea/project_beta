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

