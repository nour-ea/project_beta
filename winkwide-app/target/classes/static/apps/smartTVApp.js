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
		$scope.runPERIOD = 300000; //30000
		$scope.showNextMediaPERIOD = 5000; //5000
		//--------------------------------
		
		//Setting up the local Database
		STOREService.setupIDB();
		
		//Application variables
		$scope.displayId = angular.element(displayId).text();
		$scope.programs = [];
		$scope.reports = [];
		$scope.reportsToSend = [];
		
		//Synchronization variables
		$scope.programsSyncOK = false;
		$scope.reportsSyncOK = false;
		$scope.setCurrentProgramOK = false;
		
		//Timers process IDs
		$scope.runPID = -1;
		$scope.setCurrentProgramPID = -1;
		$scope.showNextMediaPID = -1;
		
		//Current Program and Loop variables
		$scope.currentProgram = {};
		$scope.currentMedia = {};
		$scope.loopCounter = 0;
		
		console.log('ended init');
		
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
		$scope.startup();
		setInterval( $scope.refresh, $scope.refreshPERIOD);		
	};
	
	//Function to Refresh all App Data
	$scope.refresh = function() {
		console.log('trying a refresh');
		
		//if last startup succeeded to get remoteData and send reports
		if($scope.isprogramsSyncOK() && $scope.isreportsSyncOK()){
			console.log('doing a refresh');
			
			//reset synchronization variables
			$scope.programsSyncOK = false;
			$scope.reportsSyncOK = false;
		
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
		console.log('trying to run with remote data : ' + $scope.isprogramsSyncOK());

			if($scope.isprogramsSyncOK()){
				
				//clearing run timers
				console.log('clearing showNextMedia and setCurrentProgram timers');
				clearInterval($scope.showNextMediaPID);
				clearTimeout($scope.setCurrentProgramPID);
				
				if($scope.setCurrentProgramOK){
					console.log('setting setCurrentProgram and showNextMedia timers');

					//define timeout for the current program
					console.log('setting current program timeout : '+  moment($scope.currentProgram.endTime, 'YYYY-MM-DD HH:mm a').diff(moment()));
					$scope.setCurrentProgramPID = setTimeout( $scope.stopCurrentProgramLoop, moment($scope.currentProgram.endTime, 'YYYY-MM-DD HH:mm a').diff(moment()) );

					//display the current program media
					$scope.showNextMedia();
					$scope.showNextMediaPID = setInterval( $scope.showNextMedia, $scope.showNextMediaPERIOD);
						
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
	$scope.stopCurrentProgramLoop = function(){
		console.log('stopping current program loop');

		//clear run timers
		clearInterval($scope.showNextMediaPID);
		clearTimeout($scope.setCurrentProgramPID);
		
		//ask for reset program definition
		$scope.setCurrentProgramOK = false;
	};
		

	//Function to show next program media
	$scope.showNextMedia = function() {
		
		//Define current media to be shown
		$scope.currentMedia = $scope.currentProgram.medias[$scope.loopCounter]
		
		//Show Media from IDB if available
		STOREService.getFileIDB($scope.currentMedia.url, function(data){
		
			//Hide all 3 types of tags (img, video, html)
			angular.element(mainImage).attr('style','display:none');
			angular.element(mainVideo).attr('style','display:none');
			angular.element(mainHTML).attr('style','display:none');
			
			if (data === undefined){				
				//Use the Tag that corresponds to the media format (img, video, html)
				switch($scope.currentMedia.format){		
					case 'video/mp4':
						angular.element(mainVideo).attr('src',$scope.currentMedia.url);
						angular.element(mainVideo).attr('style','display:block');
						break;
					case 'app/html':
						angular.element(mainHTML).attr('src',$scope.currentMedia.url);
						angular.element(mainHTML).attr('style','display:block');
						break;
					default :
						angular.element(mainImage).attr('src',$scope.currentMedia.url);
						angular.element(mainImage).attr('style','display:block');
					}
				
				console.log('showing next media from remote server');			
			}
			else{
				//Use the Element/Tag that corresponds to the media format (img, video, html)
				switch($scope.currentMedia.format){		
					case 'video/mp4':
						angular.element(mainVideo).attr('src',data);
						angular.element(mainVideo).attr('style','display:block');
						break;
					case 'app/html':
						angular.element(mainHTML).attr('src',data);
						angular.element(mainHTML).attr('style','display:block');
						break;
					default :
						angular.element(mainImage).attr('src',data);
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
	

	//The startup function
	$scope.startup = function() {
		console.log('launched startup');
		
		//Check Connection
		$scope.checkConnection();
		
		//Sync reports
		$scope.sendReports();
	
		//Sync Programs
		$scope.getPrograms();
			
	};
	
	
	// Function to check the internet connection
	$scope.checkConnection = function(){		
		
		// à compléters
		
	};
	
	//Function to send Reports to server
	$scope.sendReports = function() { 
		console.log('trying to send Reports to server');
		
		//Empty reports object into reportsToSend object
		$scope.reportsToSend.push.apply($scope.reportsToSend, $scope.reports);
		$scope.reports = [];
		
		CRUDService.setData('/sync/reports/'+ $scope.displayId, $scope.reportsToSend)
		.success(function(data){
			console.log('sent Reports successfully');
			$scope.reportsSyncOK = true;
			$scope.reportsToSend = [];
			});
	};
	
	//Function to get Programs and their Medias
	$scope.getPrograms = function() { 
		console.log('trying to get Programs and their Medias');
		
		CRUDService.getData('/sync/programs/'+ $scope.displayId)
		.success(function(data){
	
		    $scope.programs = data;
		
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


//Local Storage Caching service
app.service('STOREService',['$http', function($http){
	
	
	function storeJSONinLS(objectName, object){
		// Stores the JavaScript object as a string
		localStorage.setItem(objectName, JSON.stringify(object));
	};
	
	function storeFileLS(fileUrl, fileType){
	
			//Check if the file is already stored
			if(localStorage.getItem(fileUrl) !== null){
				console.log("File :"+ fileUrl +" already stored ");
				return;
			}
				
		
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

		                // Store Data URL in localStorage
		                try {
		                    localStorage.setItem(fileUrl, result);
		                    console.log("Storage of file :"+ fileUrl +" succeeded ");
		                }
		                catch (e) {
		                    console.log("Storage failed: " + e);
		                }
		            };
		            // Load blob as Data URL
		            fileReader.readAsDataURL(blob);
		        }
		    }, false);
		    // Send XHR
		    xhr.send();
		
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
    	storeFileLS: 					storeFileLS,
    	storeFileIDB: 					storeFileIDB,
    	getFileIDB:						getFileIDB,
    	setupIDB: 						setupIDB,
};
	
}]);
