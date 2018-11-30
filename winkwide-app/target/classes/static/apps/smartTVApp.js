var app = angular.module('smartTVApp', []);


// CRUD CONTROLLER
// -------------------------------------------------------------------------
// All mighty controller
app.controller('smartCtrl', ['$scope', 'CRUDService', 
	    function ($scope, CRUDService) {
	
	//function at initialization to setup all the variables
	$scope.init = function(){
		console.log('started init');
		
		//Static PERIOD parameters
		//--------------------------------
		$scope.showNextMediaPERIOD = 3000;
		$scope.runPERIOD = 30000;
		$scope.refreshPERIOD = 120000;
		//--------------------------------
		
		//Application variables
		$scope.display = {};
		$scope.programs = [];
		$scope.loops = [];
		$scope.reports = [];
		
		//Synchronization variables
		$scope.remoteDataOk = false;
		$scope.currentProgramOK = false;
		
		//Timers process IDs
		$scope.runPID = -1;
		$scope.timeoutPID = -1;
		$scope.loopPID = -1;
		
		//Current Program and Loop variables
		$scope.currentLoopPID = 0;
		$scope.currentProgram = {};
		$scope.loopCounter = 0;
		console.log('ended init');
		
	};
	
	//Function to check if remote Data was fetched
	$scope.isRemoteDataOK = function(){
		return $scope.remoteDataOk;
	};
	
	//Main Function
	$scope.winkwide = function() {
		console.log('started winkwide');
		$scope.openFullscreen();
		$scope.startup();
		setInterval( $scope.refresh, $scope.refreshPERIOD);
		
	};
	
	//Function to Refresh all App Data
	$scope.refresh = function() {
		console.log('trying a refresh');
		
		//if last startup succeeded to get remoteData
		if($scope.isRemoteDataOK()){
			console.log('doing a refresh');
			//reset synchronization variables
			$scope.remoteDataOk = false;
		
			//reset current program loop definition
			$scope.stopCurrentProgramLoop();			
			
			//clear run timer
			clearInterval($scope.runPID);
			
			//reinitialize run variables
			$scope.currentProgram = {};
			$scope.loopCounter = 0;
			
			//update & run
			$scope.startup();
			console.log('done with the refresh');

		}
	
	};
	
	//Function to manage the programs displaying
	$scope.run = function(){
		console.log('trying to run with remote data : ' + $scope.isRemoteDataOK());

			if($scope.isRemoteDataOK()){
				
				//clearing run timers
				console.log('clearing run timers');
				clearInterval($scope.loopPID);
				clearTimeout($scope.timeoutPID);
				
				if($scope.currentProgramOK){
					console.log('setting loop timers');

					//define timeout for the current program loop
					console.log('setting timeout : '+  moment($scope.currentProgram.endTime, 'YYYY-MM-DD HH:mm a').diff(moment()));
					$scope.timeoutPID = setTimeout( $scope.stopCurrentProgramLoop, moment($scope.currentProgram.endTime, 'YYYY-MM-DD HH:mm a').diff(moment()) );
					//play the current program loop
					$scope.showNextMedia();
					$scope.loopPID = setInterval( $scope.showNextMedia, $scope.showNextMediaPERIOD);
						
				}else{
					//set current program
					$scope.setCurrentProgram();
				}
			}			
	};
	
	//Function to Stop current program loop
	$scope.stopCurrentProgramLoop = function(){
		console.log('stopping current program loop');

		//clear run timers
		clearInterval($scope.loopPID);
		clearTimeout($scope.timeoutPID);
		
		//ask for reset program definition
		$scope.currentProgramOK = false;
	};
		

	//Function to show next program media
	$scope.showNextMedia = function() {
		console.log('showing next media');
		
		//showMedia smoothly
		//$scope.mediaChanging = true;
		angular.element(mainMedia).attr('src',$scope.currentProgram.medias[$scope.loopCounter].localUrl);
		//$scope.mediaChanging = false;
		
		//increment counter
		if($scope.loopCounter < $scope.currentProgram.medias.length - 1)
			$scope.loopCounter++;
		else
			$scope.loopCounter = 0;
					
		//report	
		// ----
		
	};
	
	//Function to set the current Program 
	$scope.setCurrentProgram = function() {
		
		console.log('setting current program');
		
		angular.forEach($scope.programs, function(value, key) {
			if( moment().isBetween( moment(value.startTime, 'YYYY-MM-DD HH:mm a') , moment(value.endTime, 'YYYY-MM-DD HH:mm a') ) ){
				$scope.currentProgram = value;
				$scope.currentProgramOK = true;
				console.log('current program successfully set');
				//launch run
				$scope.run();
				return;
			}
		});
	};
	
	//The startup function
	$scope.startup = function() {
		console.log('launched startup');
		
		//Check Connection
		$scope.checkConnection();
		
		//Sync reports
		//-----------
		
		//Sync Programs
		$scope.getPrograms();
		
	};
	
	
	//Check connection
	$scope.checkConnection = function(){
		
		// à compléters
	};
	
	//Function to get Programs and their Medias
	$scope.getPrograms = function() { 
		console.log('trying to get Programs and their Medias');
		
		CRUDService.getData('/sync/programs/'+ angular.element(displayId).text())
		.success(function(data){
	
		    $scope.programs = data;
		
			angular.forEach($scope.programs, function(prog, key) {
				angular.forEach(prog.medias, function(med, key) {
					med.localUrl = $scope.cacheMedia(med);
					console.log('media cached');
				});
				console.log('this program media cached');
			});
			
			console.log('got programs and cached medias for all');
			$scope.remoteDataOk = true;
			
			//launch run loop
			$scope.run();
			$scope.runPID = setInterval($scope.run, $scope.runPERIOD);
			});
	};
	
	//Function to locally cache medias 
	$scope.cacheMedia = function(media) {
		console.log('caching media');
		
		//check if local storage is supported
		if (typeof(Storage) !== "undefined") {
			
			return media.url;
		} else {
		    // Sorry! No Web Storage support...
			return media.url;
		}
		return media.url;
	};
	


	
	//FullScreen Utils
	$scope.openFullscreen = function() {
		console.log('opening full screen');
			
		  var elem = document.documentElement;
		
		  if (elem.requestFullscreen) {
		    elem.requestFullscreen();
		  } else if (elem.mozRequestFullScreen) {  //Firefox 
		    elem.mozRequestFullScreen();
		  } else if (elem.webkitRequestFullscreen) {  //Chrome, Safari and Opera 
		    elem.webkitRequestFullscreen();
		  } else if (elem.msRequestFullscreen) {  //IE/Edge 
		    elem.msRequestFullscreen();
		  }
		  
	};
	
	$scope.isFullscreen = function() {
		console.log('testing if full screen');
		
		  if (document.fullscreen !== undefined) {  //Firefox 
			  return document.fullscreen;
		  } else if (document.webkitIsFullScreen !== undefined) {  //Chrome, Safari and Opera 
			  return document.webkitIsFullScreen;
		  } else if (document.msIsFullScreen !== undefined) {  //IE/Edge 
			  return document.msIsFullScreen;
		  }
	};
	
	
	
}]);











// SERVICE LAYER
// --------------------------------------------------------
//list of CRUD calls
app.service('CRUDService',['$http', function ($http) {

	    
	    function getData(url) {
	        return $http({
	          method: 'GET',
	            url: url
	        });
	    };
	    
	    function createOne(target, data) {
	        return $http({
	          method: 'POST',
	          	url: '/api/'+target,
	            data: data
	        });
	    };
	    
	    return {
	    		getData: 						getData,
	    		createOne: 						createOne,
	    };
    
}]);
