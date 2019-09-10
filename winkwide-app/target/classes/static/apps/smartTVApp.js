var app = angular.module('smartTVApp', []);


// CRUD CONTROLLER
// -------------------------------------------------------------------------
// All mighty controller
app.controller('smartCtrl', ['$scope', 'CRUDService', 'STOREService',
	    function ($scope, CRUDService, STOREService) {
	
	//function at initialization to setup all the variables
	$scope.init = function(){
		console.log('started init');
		
		//Static PERIOD parameters
		//--------------------------------
		$scope.refreshPERIOD = 120000; //120000
		$scope.runPERIOD = 30000; //30000
		$scope.showNextMediaPERIOD = 20000; //5000
		//--------------------------------
		
		//Setting up the local Database
		STOREService.setupIDB();
		
		//Application variables
		$scope.displayId = 0;
		$scope.programs = [];
		$scope.reports = [];
		
		//Local Storage variables
		if(localStorage.getItem('displayId') === null)
			localStorage.setItem('displayId', '0');
		if(localStorage.getItem('storedPrograms') === null)
			STOREService.storeJSONinLS('storedPrograms', []);
		if(localStorage.getItem('storedReports') === null)
			STOREService.storeJSONinLS('storedReports', []);
		
		//Set this Display ID
		$scope.setDisplayId();
		
		//Synchronization variables
		$scope.programsSyncOK = false;
		$scope.reportsSyncOK = false;
		$scope.setCurrentProgramOK = false;
		
		//Timers process IDs
		$scope.runPID = -1;
		$scope.stopCurrentProgramPID = -1;
		$scope.showNextMediaPID = -1;
		
		//Current Program and Loop variables
		$scope.currentProgram = {};
		$scope.currentMedia = {};
		$scope.loopCounter = 0;
		
		console.log('ended init');
		
		//Click to Fullscreen
		
	};
	
	//Function to get current Display Id
	$scope.setDisplayId = function(){
		//set it default from LS
		$scope.displayId = localStorage.getItem('displayId');
		console.log('Display Id set from LS to : ' + $scope.displayId);
		
		//set it from server
		CRUDService.getData('/sync/displayId').success(function(data){
			$scope.displayId = data;
			console.log('Display Id set from server to : ' + $scope.displayId);
		});

	};
	
	//Functions to check if remoteData was fetched/ reports were pushed
	$scope.isprogramsSyncOK = function(){
		return $scope.programsSyncOK;
	};
	$scope.isreportsSyncOK = function(){
		return $scope.reportsSyncOK;
	};
	
	//Core Function
	$scope.winkwide = function() {
		console.log('started winkwide');
		$scope.openFullscreen();
		if(window.navigator.onLine)
				$scope.remoteStartup();
		else	$scope.localStartup();
		setInterval( $scope.refresh, $scope.refreshPERIOD);		
	};
	
	//Function to Refresh all App Data
	$scope.refresh = function() {
		console.log('trying a refresh');
		
		//stop Running programs
		$scope.stopCurrentProgram();			
		clearInterval($scope.runPID);
		
		//reinitialize Current Program and Loop variables
		$scope.currentProgram = {};
		$scope.currentMedia = {};
		$scope.loopCounter = 0;
		
		//REFRESH WITH INTERNET CONNECTION
		//&& last startup succeeded to get remoteData and send reports
		if(window.navigator.onLine &&
		($scope.isprogramsSyncOK() && $scope.isreportsSyncOK())){
			console.log('doing a remote refresh');
			
			//reset synchronization variables
			$scope.programsSyncOK = false;
			$scope.reportsSyncOK = false;
			
			//remote update & run
			$scope.remoteStartup();
			console.log('done with the remote refresh');
		}
		 
		//REFRESH WITHOUT INTERNET CONNECTION
		else {
			console.log('doing a local refresh');
			
			//local update & run
			$scope.localStartup();
			console.log('done with the local refresh');
		}
	
	};
	
	//Function to manage the programs displaying
	$scope.run = function(){
		console.log('trying to run with available data : ' + $scope.isprogramsSyncOK());

			if($scope.isprogramsSyncOK()){
				
				//clearing run timers
				console.log('clearing showNextMedia and setCurrentProgram timers');
				clearInterval($scope.showNextMediaPID);
				clearTimeout($scope.stopCurrentProgramPID);
				
				if($scope.setCurrentProgramOK){
					console.log('setting setCurrentProgram and showNextMedia timers');

					//define timeout for the current program (never more than 20 days / largest integer in milliseconds)
					let programTimeout = Math.min(20*24*3600*1000, moment($scope.currentProgram.endTime, 'YYYY-MM-DD HH:mm a').diff(moment())) 
					console.log('setting current program timeout : '+  programTimeout);
					$scope.stopCurrentProgramPID = setTimeout( $scope.stopCurrentProgram, programTimeout);

					//display the current program media
					$scope.showNextMediaPID = setTimeout( function(){
							$scope.showNextMedia();
							$scope.showNextMediaPID = setInterval( $scope.showNextMedia, $scope.showNextMediaPERIOD);
						}, 1000); // petit décalage pour éviter les chevauchements
						
				}else{
					//set current program
					$scope.setCurrentProgram();
				}
			}			
	};
	
	//Function to set the current Program 
	$scope.setCurrentProgram = function() {
		
		console.log('setting current program');
		
		angular.forEach($scope.programs, function(value, key) {
			if( moment().isBetween( moment(value.startTime, 'YYYY-MM-DD HH:mm a') , moment(value.endTime, 'YYYY-MM-DD HH:mm a') ) ){
				$scope.currentProgram = value;
				$scope.setCurrentProgramOK = true;
				console.log('current program successfully set');
				
				//launch run
				$scope.run();
				return;
			}
		});
	};
	
	//Function to Stop current program loop
	$scope.stopCurrentProgram = function(){
		console.log('stopping current program loop');

		//clear run timers
		clearInterval($scope.showNextMediaPID);
		clearTimeout($scope.stopCurrentProgramPID);
		
		//ask for reset program definition
		$scope.setCurrentProgramOK = false;
	};
		

	//Function to show next program media
	$scope.showNextMedia = function() {
		
		//Define current media to be shown
		$scope.currentMedia = $scope.currentProgram.medias[$scope.loopCounter]
		
		//Show Media from IDB if available
		STOREService.getFileIDB($scope.currentMedia.url, function(data){
		
			//Hide and empty all 3 types of tags (img, video, html)
			angular.element(mainImage).attr('style','display:none');
			angular.element(mainVideo).attr('style','display:none');
			angular.element(mainAudio).attr('style','display:none');
			angular.element(mainHTML).attr('style','display:none');

			angular.element(mainImage).attr('src','');
			angular.element(mainVideo).attr('src','');
			angular.element(mainAudio).attr('src','');
			angular.element(mainHTML).attr('src','');

			
			if (data === undefined){				
				//Use the Element/Tag that corresponds to the media format (img, video, html)
				switch($scope.currentMedia.format){		
					case 'video/mp4':
						angular.element(mainVideo).attr('src', $scope.currentMedia.url);
						angular.element(mainVideo).attr('style','display:block');
						break;
					case 'audio/mp3':
						angular.element(mainAudio).attr('src', $scope.currentMedia.url);
						angular.element(mainAudio).attr('style','display:block');
						angular.element(mainImage).attr('src','/misc/audioCover.gif');
						angular.element(mainImage).attr('style','display:block');
						break;
					case 'app/html':
						angular.element(mainHTML).attr('src', $scope.currentMedia.url);
						angular.element(mainHTML).attr('style','display:block');
						break;
					default :
						angular.element(mainImage).attr('src', $scope.currentMedia.url);
						angular.element(mainImage).attr('style','display:block');
					}
				
				console.log('showing next media from remote server');			
			}
			else{
				//Use the Element/Tag that corresponds to the media format (img, video, html)
				switch($scope.currentMedia.format){		
					case 'video/mp4':
						angular.element(mainVideo).attr('src', data);
						angular.element(mainVideo).attr('style','display:block');
						break;
					case 'audio/mp3':
						angular.element(mainAudio).attr('src', data);
						angular.element(mainAudio).attr('style','display:block');
						angular.element(mainImage).attr('src','/img/misc/audioCover.gif');
						angular.element(mainImage).attr('style','display:block');
						break;
					case 'app/html':
						angular.element(mainHTML).attr('src', data);
						angular.element(mainHTML).attr('style','display:block');
						break;
					default :
						angular.element(mainImage).attr('src', data);
						angular.element(mainImage).attr('style','display:block');
					}
	
				console.log('showing next media from IDB');			
			}
						
			//write report
			$scope.reports.push({ 
				startTime: moment().format('YYYY-MM-DD HH:mm:ss a'), 
				endTime: moment().add($scope.showNextMediaPERIOD).format('YYYY-MM-DD HH:mm:ss a'),
				display: null ,
				media: $scope.currentProgram.medias[$scope.loopCounter] });
			
			//increment counter
			if($scope.loopCounter < $scope.currentProgram.medias.length - 1)
				$scope.loopCounter++;
			else
				$scope.loopCounter = 0;
		});
		
	};
	

	//The startup function / WITH INTERNET CONNECTION 
	$scope.remoteStartup = function() {
		console.log('launched a remote Startup');
				
		//Sync reports
		$scope.sendReports();
	
		//Sync Programs
		$scope.getPrograms();
			
	};
	
	//The startup function / NO INTERNET CONNECTION
	$scope.localStartup = function()	{
		console.log('launched a local Startup');

		//store reports in LS
		$scope.storeReports();
		
		//run with locally stored programs
		$scope.programs = STOREService.getJSONfromLS('storedPrograms');
		console.log('running with programs stored in Local Storage');
		
		//launch Run loop : start displaying programs medias
		$scope.run();
		$scope.runPID = setInterval($scope.run, $scope.runPERIOD);			
	}
	
	
	//Function to send Reports to server
	$scope.sendReports = function() { 
		console.log('trying to send Reports to server');
		
		//store Reports in LS and empty reports object
		$scope.storeReports();
		var storedReports = STOREService.getJSONfromLS('storedReports');

		// try to send reports and empty stored reports in LS
		CRUDService.setData('/sync/reports', storedReports)
			.success(function(data){
				console.log('sent Reports successfully');
				$scope.reportsSyncOK = true;
				console.log('clearing LS Reports');
				STOREService.storeJSONinLS('storedReports', [])
				})
			.error(function(errors){
				console.log('sending Reports to server failed for reason: ' + errors);
				$scope.reportsSyncOK = true;
				});
		
	};
	
	//Function to store Reports in LS and empty reports object
	$scope.storeReports = function() { 
		console.log('storing reports in LS');
		
		var storedReports = STOREService.getJSONfromLS('storedReports');
		storedReports.push.apply(storedReports, $scope.reports);		
		STOREService.storeJSONinLS('storedReports', storedReports)
		
		$scope.reports = [];
		};
	
	//Function to get Programs and their Medias from server
	$scope.getPrograms = function() { 
		console.log('trying to get Programs and their Medias');
		
		// try to get programs and update stored programs in LS

		CRUDService.getData('/sync/programs')
			.success(function(data){
		
			    $scope.programs = data;
				STOREService.storeJSONinLS('storedPrograms', data);
			
				//caching all programs medias
				angular.forEach($scope.programs, function(prog, key) {
					angular.forEach(prog.medias, function(med, key) {
						$scope.cacheMedia(med);
					});
				});
				
				console.log('got programs and cached medias for all');
				$scope.programsSyncOK = true;
				
				//launch Run loop : start displaying programs medias
				$scope.run();
				$scope.runPID = setInterval($scope.run, $scope.runPERIOD);	
				})
			.error(function(errors){
				console.log('getting Programs from server failed for reason: ' + errors);
				$scope.programsSyncOK = true;
				});

				
	};
	
	//Function to locally cache medias 
	$scope.cacheMedia = function(media) {
		console.log('trying to cache media in localDB : ' + media.url);
		//store the media file
		STOREService.storeFileIDB(media.url, media.format);
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
		  
		  angular.element(startupdiv).attr('style','display:none');
		  
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






//--------------------------------------------------------
//--------------------------------------------------------
//--------------------------------------------------------
//////////////////// SERVICE LAYER ///////////////////////
//--------------------------------------------------------
//--------------------------------------------------------
//--------------------------------------------------------

//list of CRUD calls
app.service('CRUDService',['$http', function ($http) {
	    
	    function getData(url) {
	        return $http({
	          method: 'GET',
	            url: url
	        });
	    };
	    
	    function setData(url, data) {
	        return $http({
	          method: 'POST',
	          	url: url,
	            data: data
	        });
	    };
	    
	    return {
	    		getData: 						getData,
	    		setData: 						setData,
	    };
    
}]);


//Local Storage / Database Caching service
app.service('STOREService',['$http', function($http){
	
	
	function storeJSONinLS(objectName, object){
		// Stores the JavaScript object as a string
		localStorage.setItem(objectName, JSON.stringify(object));
	};

	function getJSONfromLS(objectName){
		// Get the string as a JavaScript object
		return JSON.parse(localStorage.getItem(objectName));
	};
	

	function setupIDB(){
		let db;
		//stuff
		let request = indexedDB.open('winkwide', 1);

		request.onerror = function(e) {
			console.error('Unable to open database.');
		}

		request.onsuccess = function(e) {
			db = e.target.result;
			console.log('db opened');
		}

		request.onupgradeneeded = function(e) {
			db = e.target.result;
			db.createObjectStore('medias', {keyPath:'url'});
		}
	}
	
	function storeFileIDB(fileUrl, fileType){
		
		//open database then store file if success
		let request = indexedDB.open('winkwide', 1);

		request.onerror = function(e) {
			console.error('Unable to open database.');
		}
		
		request.onsuccess = function(e) {
			db = e.target.result;
		
			//Check if the file is already stored in DB
			let transaction = db.transaction(['medias']); // readonly
			let request = transaction.objectStore('medias').get(fileUrl);
			request.onsuccess = function() {
			  if (request.result !== undefined) {
				  console.log("File :"+ fileUrl +" already stored in IDB");
				  return;
			  } 
			  
			  else {// store file
		
			    // Create XHR, Blob and FileReader objects
			    var xhr = new XMLHttpRequest(),
			        blob,
			        fileReader = new FileReader();
		
			    xhr.open("GET", fileUrl, true);
			    // Set the responseType to arraybuffer. "blob" is an option too, rendering manual Blob creation unnecessary, but the support for "blob" is not widespread enough yet
			    xhr.responseType = "arraybuffer";
		
			    xhr.addEventListener("load", function () {
			        if (xhr.status === 200) {
			            // Create a blob from the response
			            blob = new Blob([xhr.response], {type: fileType});
		
			            // onload needed since Google Chrome doesn't support addEventListener for FileReader
			            fileReader.onload = function (evt) {
			                // Read out file contents as a Data URL
			                var result = evt.target.result;
		
			                // Store Data URL in Database
			            	let transaction = db.transaction(['medias'],'readwrite');
			            	let request = transaction.objectStore('medias').add({url:fileUrl, data:result});
		
			            	request.onsuccess = function() {
			            		console.log("Media added to the medias store", request.result);
			            	};
			            	
			            	transaction.oncomplete = function(e) {
			        			console.log('data stored');
			        		}
		
			            	request.onerror = function() {
			            		console.log("Error", request.error);
			            	};
		
			            };
			            // Load blob as Data URL
			            fileReader.readAsDataURL(blob);
			        }
			    }, false);
			    // Send XHR
			    xhr.send();
			  }
			};
		};
	};

	function getFileIDB(fileUrl, callback){
		
		//open database then get file if success
		let request = indexedDB.open('winkwide', 1);

		request.onerror = function(e) {
			console.error('Unable to open database.');
			
			callback(null);
		}
		
		request.onsuccess = function(e) {
			db = e.target.result;
			
			//Check if the file is already stored in DB
			let transaction = db.transaction(['medias']); // readonly
			let request = transaction.objectStore('medias').get(fileUrl);
			
			request.onsuccess = function() {
			  if (request.result !== undefined) {
				  console.log("getting File :"+ request.result.url +" from IDB");
				  callback(request.result.data);
			  } 
			};
		}
	}
	

	
    return {
    	storeJSONinLS: 					storeJSONinLS,
    	getJSONfromLS:					getJSONfromLS,
    	setupIDB: 						setupIDB,
    	storeFileIDB: 					storeFileIDB,
    	getFileIDB:						getFileIDB,
    };
	
}]);
