
$(document).ready(function(){

/*priceFiler initiate and update*/
$('#minPriceSliderValue').text($('#minPriceSlider').attr("value")+' €');
$('#maxPriceSliderValue').text($('#maxPriceSlider').attr("value")+' €');

/*put listener*/
$('#minPriceSlider').on('input', function(e) {
  $('#minPriceSlider').attr("value", $(e.target).val());
  $('#minPriceSliderValue').text($('#minPriceSlider').val()+' €');
});

$('#maxPriceSlider').on('input', function(e) {
  $('#maxPriceSlider').attr("value", $(e.target).val());
  $('#maxPriceSliderValue').text($('#maxPriceSlider').val()+' €');
});

});
