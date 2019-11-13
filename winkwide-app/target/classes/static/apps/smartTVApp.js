//Global Settings Variables
var debugModeActive = true;
var remoteLoggingActive = false;
var offlineModeActive = false;


var app = angular.module('smartTVApp', []);


// CRUD CONTROLLER
// -------------------------------------------------------------------------
// All mighty controller
app.controller('smartCtrl', ['$scope', 'CRUDService', 'STOREService', 'UTILSService',
	    function ($scope, CRUDService, STOREService, UTILSService) {
	
	//function at initialization to setup all the variables
	$scope.init = function(){
		createLog('debug', 'started init');
		
		//Applying Settings and Setting up th local DB if offline mode active (remotely or default)
		$scope.setSettings();
				
		//Application variables
		$scope.programs = [];
		$scope.records = [];
		
		//Local Storage variables
		if(localStorage.getItem('storedPrograms') === null)
			STOREService.storeJSONinLS('storedPrograms', []);
		if(localStorage.getItem('storedRecords') === null)
			STOREService.storeJSONinLS('storedRecords', []);
		if(localStorage.getItem('storedLogs') === null)
			localStorage.setItem('storedLogs', '');
		
		//Set this Display ID
		$scope.setDisplayId();
		
		//Synchronization variables
		$scope.programsSyncDONE = false;
		$scope.recordsSyncDONE = false;
		$scope.setCurrentPlaylistDONE = false;
		
		//Timers process IDs
		$scope.runPID = -1;
		$scope.stopCurrentPlaylistPID = -1;
		$scope.showNextMediaPID = -1;
		
		//Current Programs and Loop variables
		$scope.currentPlaylist = {};
		$scope.currentPlaylistTimeout = 0;
		$scope.currentSpot = {};
		$scope.spotIndex = 0;
		
		createLog('debug', 'ended init');
		
		//Run WinkWide Auto
		
		//Click anywhere to Fullscreen
		
	};
	
	//function to set App Settings (remotely or default)
	$scope.setSettings = function(){
		//Default Static Settings
		$scope.syncPeriod = 122000; //120000
		$scope.refreshPeriod = 60000; //60000
		$scope.recordingActive = true;
		$scope.autoSleepActive = false;
		$scope.autoOnTime = 0;
		$scope.autoOffTime = 0;
		$scope.alternateMediaCategories = false;
		
		remoteLoggingActive = true;
		offlineModeActive = true;
		
		//setting default Media Categories
		$scope.mediaCategories = [{'name':'Private'}, {'name':'Entertain'}, {'name':'Ads'}, {'name':'Default'}];

		createLog('debug', 'Settings set to default');

		//----------------------------------
		
		//set Settings from server
		CRUDService.getData('/sync/settings').success(function(data){
			$scope.syncPeriod = data.syncPeriod;
			$scope.refreshPeriod = data.refreshPeriod;
			$scope.recordingActive = data.recordingActive;
			$scope.autoSleepActive = data.autoSleepActive;
			$scope.autoOnTime = data.autoOnTime;
			$scope.autoOffTime = data.autoOffTime;
			$scope.alternateMediaCategories = data.alternateMediaCategories
		
			remoteLoggingActive = data.remoteLoggingActive;
			offlineModeActive = data.offlineModeActive;
			
			//setting Media Categories from server
			CRUDService.getData('/sync/mediaCategories').success(function(res){										
	          		$scope.mediaCategories = res;
				});
			
			createLog('debug', 'Settings set from server to: ' + JSON.stringify(data));
			
			//Setting up the local Database if offline mode active
			STOREService.setupIDB();
		});
	};
	
	//Function to get current Display Id
	$scope.setDisplayId = function(){
		//set default displayId
		$scope.displayId = 0;
		$scope.displayName = "defaultDisplayName";
		createLog('debug', 'Display set from LS to: id = ' + $scope.displayId + ', name = ' + $scope.displayName);
		
		//set it from server
		CRUDService.getData('/sync/display').success(function(data){
			$scope.displayId = data.id;
			$scope.displayName = data.name;
			createLog('debug', 'Display set from server to: id = ' + $scope.displayId + ', name = ' + $scope.displayName);
		});

	};
	
	//Core Function of WinkWide App
	$scope.winkwide = function() {
		createLog('debug', 'started winkwide');
		$scope.openFullscreen();
		if(window.navigator.onLine)
				$scope.remoteStartup();
		else	$scope.localStartup();
		setInterval( $scope.sync, $scope.syncPeriod);		
	};
	
	//Function to Sync all App Data
	$scope.sync = function() {
		createLog('debug', 'trying a sync');
		
		//stop Running programs
		$scope.stopCurrentPlaylist();			
		clearInterval($scope.runPID);
		
		//reinitialize Current Programs and Loop variables
		$scope.currentPlaylist = {};
		$scope.currentPlaylistTimeout = 0;
		$scope.currentSpot = {};
		$scope.spotIndex = 0;
		
		//SYNC WITH INTERNET CONNECTION
		//&& last startup succeeded to get remoteData and send records
		if(window.navigator.onLine && $scope.programsSyncDONE 
				&& ($scope.recordsSyncDONE || !$scope.recordingActive) ){
			createLog('debug', 'doing a remote sync');
			
			//reset synchronization variables
			$scope.programsSyncDONE = false;
			$scope.recordsSyncDONE = false;
			
			//remote update & run
			$scope.remoteStartup();
			createLog('debug', 'done with the remote sync');
		}
		 
		//REFRESH WITHOUT INTERNET CONNECTION
		else {
			createLog('debug', 'doing a local sync');
			
			//local update & run
			$scope.localStartup();
			createLog('debug', 'done with the local sync');
		}
	
	};
	
	//Function to manage the programs displaying
	$scope.run = function(){
		createLog('debug', 'trying to run with available data : ' + $scope.programsSyncDONE);

			if($scope.programsSyncDONE){
								
				//clearing run timers
				createLog('debug', 'clearing setCurrentPlaylist and showNextMediaPID timers');
				clearInterval($scope.showNextMediaPID);
				clearTimeout($scope.stopCurrentPlaylistPID);
				
				if($scope.setCurrentPlaylistDONE){

					//set timeout for the current programs playlist 
					createLog('debug', 'setting current programs playlist timeout : '+ $scope.currentPlaylistTimeout);
					$scope.stopCurrentPlaylistPID = setTimeout( $scope.stopCurrentPlaylist, $scope.currentPlaylistTimeout);

					//if current Playlist empty return
					if($scope.currentPlaylist.spots.length == 0){
						createLog('debug', 'Current Playlist is empty, waiting for next Run cycle');
						return;						
					}
					//display spots media
					$scope.currentSpot = $scope.currentPlaylist.spots[0];
					$scope.showNextMedia($scope.currentSpot.media);
					var startTime = moment();
					
					$scope.showNextMediaPID = setInterval(function(){

						//Check if spot duration is passed
						if(moment().diff(startTime)  > $scope.currentSpot.duration*1000){

							//write record if recording active
							if($scope.recordingActive)
								$scope.records.push({ 
									startTime: startTime.format('YYYY-MM-DD HH:mm:ss'), 
									endTime: moment().format('YYYY-MM-DD HH:mm:ss'),
									displayId: $scope.displayId, displayName: $scope.displayName,
									mediaId: $scope.currentSpot.media.id, mediaName: $scope.currentSpot.media.name  });
							
							//increment spotIndex and set next current Spot
							if($scope.spotIndex + 1 < $scope.currentPlaylist.spots.length)
									$scope.spotIndex++;
							else	$scope.spotIndex = 0;
							createLog('debug', 'current spotIndex: '+$scope.spotIndex);
							if(typeof($scope.currentPlaylist.spots[$scope.spotIndex]) !== 'undefined')
								$scope.currentSpot = $scope.currentPlaylist.spots[$scope.spotIndex];

							//while next spot is an App while there is no connection, move to next one
							while($scope.currentSpot.media.type == 'App' && !window.navigator.onLine){
								createLog('error', 'spot not played because App and offline: '+$scope.currentSpot.media.name);
								//increment spotIndex and set next current Spot
								if($scope.spotIndex + 1 < $scope.currentPlaylist.spots.length)
										$scope.spotIndex++;
								else	$scope.spotIndex = 0;
								createLog('debug', 'current spotIndex: '+$scope.spotIndex);
								if(typeof($scope.currentPlaylist.spots[$scope.spotIndex]) !== 'undefined')
									$scope.currentSpot = $scope.currentPlaylist.spots[$scope.spotIndex];
							}
						
							//display Next Media and save startTime
							$scope.showNextMedia($scope.currentSpot.media);
							startTime = moment();			

						}/*else{
							createLog('', 'waiting...');
						}*/
																					
						
					}, 10);
									
				}else{
					//set current programs playlist
					$scope.setCurrentPlaylist();
				}
			}			
	};
	
	//Function to set the current programs playlist its timeout
	$scope.setCurrentPlaylist = function() {
		
		createLog('debug', 'setting current programs playlist and its timeout');

		//Reset current playlist timeout to the maximum (never more than 20 days / largest integer in milliseconds)
		$scope.currentPlaylistTimeout = 20*24*3600*1000; 

		//Filter programs to only current ones
		let currentPrograms = $scope.programs.filter(function(program){
			return moment().isBetween( moment(program.startTime, 'YYYY-MM-DD HH:mm') , moment(program.endTime, 'YYYY-MM-DD HH:mm') );
		});
		
		//reset playlist spots
		$scope.currentPlaylist = {};
		$scope.currentPlaylist.spots = [];
		
		angular.forEach(currentPrograms, function(program) {
			
			//Set Timeout to the minimal one (and never more than 20 days / largest integer in milliseconds)
			$scope.currentPlaylistTimeout =  Math.min( $scope.currentPlaylistTimeout, moment(program.endTime, 'YYYY-MM-DD HH:mm').diff(moment())); 
			
			//Merge current programs playlists
			angular.forEach(program.playlists, function(playlist) {
				$scope.currentPlaylist.spots = $scope.currentPlaylist.spots.concat( playlist.spots);
			});
		});
		
		//Build Current Playlist by alterning private/entertain/ad/other Spots
		if($scope.alternateMediaCategories)
			$scope.currentPlaylist.spots = $scope.buildAlternedPlaylist();
				
		
		$scope.setCurrentPlaylistDONE = true;
		createLog('debug', 'current programs playlist successfully set');
		
		//launch run
		$scope.run();
	};
	
	//Function to Build Current Playlist by alterning private/entertain/ad/other Spots
	$scope.buildAlternedPlaylist = function(){
		let spots = $scope.currentPlaylist.spots;
		let alternedSpots = [];
		let spotsCategoryLists = [];
		
		console.log('spots');
		console.log(spots);
		
		//build spots lists by type
		$scope.mediaCategories.forEach(function(mediaCategory){
			let mediaCategorySpots = spots.filter(function(spot){ return spot.media.category == mediaCategory.name; });
			spotsCategoryLists.push(mediaCategorySpots);
		});
		console.log('spotsCategoryLists');
		console.log(spotsCategoryLists);
		
		//fill alterned Playlist spots
		let i=0;
		while( i < spots.length){
			spotsCategoryLists.forEach(function(spotsList){
				if( spotsList.length > 0 ){
					alternedSpots.push(spotsList[0]);
					spotsList.splice(0,1);
					i++;
				}
			});
		}
		console.log('alterned spots');
		console.log(alternedSpots);
		return alternedSpots;
	};
	
	//Function to Stop current playlist
	$scope.stopCurrentPlaylist = function(){
		
		createLog('debug', 'stopping current playlist : clearing setCurrentPlaylist and showNextMediaPID timers');
		clearInterval($scope.showNextMediaPID);
		clearTimeout($scope.stopCurrentPlaylistPID);
		$scope.setCurrentPlaylistDONE = false;
	};
		

	//Function to show next spot media
	$scope.showNextMedia = function(media) {
				
		//Show Media from IDB if available
		STOREService.getFileIDB(media.url, function(data){
		
			//Hide and empty all 3 types of tags (img, video, html)
			[mainImage, mainVideo, mainAudio, mainHTML].forEach(function(el){
				angular.element(el).attr('style','display:none');
				angular.element(el).attr('src','');
				angular.element(el).html('');	
			});
			
			//Use the Element/Tag that corresponds to the media format (img, video, audio, html)
			var mainElement = mainImage;
			switch(media.type){		
				case 'Video': mainElement = mainVideo; break;
				case 'App': mainElement = mainHTML; break;
				case 'Audio': 
					mainElement = mainAudio;
					STOREService.getFileIDB(media.thumbUrl, function(data){
						if (data === undefined)
							angular.element(mainImage).attr('src', media.thumbUrl);							
						else
							angular.element(mainImage).attr('src', data);
						
						angular.element(mainImage).attr('style','display:block');							
					});
					break;
			}

			//show from url if IDB fails
			if (data === undefined){				
				if(media.type !== 'App')
					angular.element(mainElement).attr('src', media.url);
				else
					UTILSService.generateHTMLPreview(mainElement, media.url, null)
				
				angular.element(mainElement).attr('style','display:block');				
				createLog('debug', 'showing next media from remote server: ' + media.name);			
			}
			else{
				if(media.type !== 'App')
					angular.element(mainElement).attr('src', data);
				else
					UTILSService.generateHTMLPreview(mainElement, null, data)
					
				angular.element(mainElement).attr('style','display:block');	
				createLog('debug', 'showing next media from IDB: ' + media.name);			
			}
									
		});		
	};
	

	//The startup function / WITH INTERNET CONNECTION 
	$scope.remoteStartup = function() {
		createLog('debug', 'launched a remote Startup');
				
		//Sync Logs
		if(remoteLoggingActive)
			$scope.sendLogs();
		
		//Sync Records
		if($scope.recordingActive)
			$scope.sendRecords();
		
		//Sync Programs
		$scope.getPrograms();
			
	};
	
	//The startup function / NO INTERNET CONNECTION
	$scope.localStartup = function()	{
		createLog('debug', 'launched a local Startup');

		//store records in LS
		if($scope.recordingActive)
			$scope.storeRecords();
		
		//run with locally stored programs
		$scope.programs = STOREService.getJSONfromLS('storedPrograms');
		createLog('debug', 'running with programs stored in Local Storage');
		
		//launch Run loop : start displaying programs medias
		$scope.run();
		$scope.runPID = setInterval($scope.run, $scope.refreshPeriod);			
	}
	
	
	//Function to send Records to server
	$scope.sendRecords = function() { 
		createLog('debug', 'trying to send Records to server');
		
		//store Records in LS and empty records object
		$scope.storeRecords();
		var storedRecords = STOREService.getJSONfromLS('storedRecords');

		// try to send records and empty stored records in LS
		CRUDService.setData('/sync/records', storedRecords)
			.success(function(data){
				createLog('debug', 'sent Records successfully');
				$scope.recordsSyncDONE = true;
				createLog('debug', 'clearing LS Records');
				STOREService.storeJSONinLS('storedRecords', []);
				})
			.error(function(errors){
				createLog('error', 'sending Records to server failed for reason: ' + errors);
				$scope.recordsSyncDONE = true;
				});
		
	};
	
	//Function to store Records in LS and empty records object
	$scope.storeRecords = function() { 
		createLog('debug', 'storing records in LS');
		
		var storedRecords = STOREService.getJSONfromLS('storedRecords');
		storedRecords.push.apply(storedRecords, $scope.records);		
		STOREService.storeJSONinLS('storedRecords', storedRecords)
		
		$scope.records = [];
		};

			
	//Function to send Logs to server
	$scope.sendLogs = function() { 
		createLog('debug', 'trying to send Logs to server');
		
		var storedLogs = localStorage.getItem('storedLogs');

		// try to send records and empty stored records in LS
		CRUDService.setData('/sync/logs', storedLogs)
			.success(function(data){
				createLog('debug', 'sent Logs successfully');
				createLog('debug', 'clearing LS Logs');
				localStorage.setItem('storedLogs', '');
				})
			.error(function(errors){
				createLog('error', 'sending Logs to server failed for reason: ' + errors);
				});			
		};
	
	
	//Function to get Programs and their Medias from server
	$scope.getPrograms = function() { 
		createLog('debug', 'trying to get Programs and their Medias');
		
		// try to get programs and update stored programs in LS

		CRUDService.getData('/sync/programs')
			.success(function(data){
		
			    $scope.programs = data;
				STOREService.storeJSONinLS('storedPrograms', data);
			
				//caching all programs medias
				angular.forEach($scope.programs, function(prog) {
					angular.forEach(prog.playlists, function(playlist) {
						angular.forEach(playlist.spots, function(spot) {
							$scope.cacheMedia(spot.media);
						});
					});
				});
				
				createLog('debug', 'got programs and cached medias for all');
				$scope.programsSyncDONE = true;
				
				//launch Run loop : start displaying programs medias
				$scope.run();
				$scope.runPID = setInterval($scope.run, $scope.refreshPeriod);	
				})
			.error(function(errors){
				createLog('error', 'getting Programs from server failed for reason: ' + errors);
				createLog('debug', 'launching a local startup after failure of remote one');
				$scope.localStartup();
				$scope.programsSyncDONE = true;
				});

				
	};
	
	//Function to locally cache medias 
	$scope.cacheMedia = function(media) {
		// Animate loader in screen
		$(".se-pre-con").show();
		
		createLog('debug', 'trying to cache media in localDB : ' + media.url);
		//store the media file
		STOREService.storeFileIDB(media.url, media.format);
		if(media.type == 'Audio')
			STOREService.storeFileIDB(media.thumbUrl, 'image/jpeg');
	};
		
	//FullScreen Utils
	$scope.openFullscreen = function() {
		createLog('debug', 'opening full screen');
			
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
		createLog('debug', 'testing if full screen');
		
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
		if(!offlineModeActive){
			createLog('error', 'db NOT opened because offline mode NOT active');
			return;
		}

		let db;
		//stuff
		let request = indexedDB.open('winkwide', 1);

		request.onerror = function(e) {
			createLog('error', "Error",'Unable to open database.');
		}
	
		request.onsuccess = function(e) {
			db = e.target.result;
			createLog('debug', 'db opened');
		}

		request.onupgradeneeded = function(e) {
			db = e.target.result;
			db.createObjectStore('medias', {keyPath:'url'});
		}
	}
	
	function storeFileIDB(fileUrl, fileType){
		if(!offlineModeActive){
			return;
		}
			// Animate loader in screen
		$(".se-pre-con").show();
		
		//open database then store file if success
		let request = indexedDB.open('winkwide', 1);

		request.onerror = function(e) {
			createLog('error', 'Unable to open database.');
		}
		
		request.onsuccess = function(e) {
			db = e.target.result;
		
			//Check if the file is already stored in DB
			let transaction = db.transaction(['medias']); // readonly
			let request = transaction.objectStore('medias').get(fileUrl);
			request.onsuccess = function() {
			  if (request.result !== undefined) {
				  createLog('debug', "File :"+ fileUrl +" already stored in IDB");
				  $(".se-pre-con").fadeOut("slow");
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
			            		createLog('debug', "Media added to the medias store", request.result);
			            		$(".se-pre-con").fadeOut("slow");
			            	};
			            	
			            	transaction.oncomplete = function(e) {
			        			createLog('debug', 'data stored');
			        			$(".se-pre-con").fadeOut("slow");
			        		}
		
			            	request.onerror = function() {
			            		createLog('error', request.error);
			            	};
		
			            };
			            // if not text/html = Load blob as Data URL
			            if(fileType !== 'text/html')
			            		fileReader.readAsDataURL(blob);
			            else	fileReader.readAsText(blob, "UTF-8");
			            
			        }
			    }, false);
			    // Send XHR
			    xhr.send();
			  }
			};
		};
	};

	function getFileIDB(fileUrl, callback){
		if(!offlineModeActive){
			return callback(undefined);
		}
		
		//open database then get file if success
		let request = indexedDB.open('winkwide', 1);

		request.onerror = function(e) {
			createLog('error', 'Unable to open database.');
			
			callback(undefined);
		}
		
		request.onsuccess = function(e) {
			db = e.target.result;
			
			//Check if the file is already stored in DB
			let transaction = db.transaction(['medias']); // readonly
			let request = transaction.objectStore('medias').get(fileUrl);
			
			request.onsuccess = function() {
			  if (request.result !== undefined) {
				  createLog('debug', "getting File :"+ request.result.url +" from IDB");
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





//Utilities service
app.service('UTILSService',['$http', function($http){
	
	//Function that creates a File from a file URL
	function urltoFile(fileUrl, fileType, fileName, callback){
	
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
                // // if not text/html = Read out file contents as a Data URL
                var result = evt.target.result;
                if(callback !== null)
                    if(fileType !== 'text/html')
                    		callback(new File([blob], fileName, {type: fileType}));
                    else	callback(result);

                createLog('debug', 'done url to file');
            };
            // if not text/html = Load blob as Data URL
            if(fileType !== 'text/html')
            		fileReader.readAsDataURL(blob);
            else	fileReader.readAsText(blob, "UTF-8");
          
        } else return null;
    }, false);
    // Send XHR
    xhr.send();
	}

	//function to generate HTML Preview from Url or Data
	function generateHTMLPreview(htmlElement, htmlUrl, htmlData){
		var file;
		var reader = new FileReader();
		if(htmlUrl != null)
			urltoFile(htmlUrl, 'text/html', 'preview.html',
				function(html){
				    	angular.element(mainHTML).html(html);
						setTimeout(startAppPreview, 300);
			});
			
		else {
		    	angular.element(mainHTML).html(htmlData);
				setTimeout(startAppPreview, 1000);
		}
	}	

return {
	urltoFile: 					urltoFile,
	generateHTMLPreview:		generateHTMLPreview,
};

}]);


//Function to manage App logs & store them in LS
function createLog(type, message){
	
	if(debugModeActive){
		switch(type){		
			case 'debug': console.log(moment().format('YYYY-MM-DD HH:mm:ss') + ' : DEBUG : '+ message); break;
			case 'error': console.error(moment().format('YYYY-MM-DD HH:mm:ss') + ' : ERROR : '+ message); break;
		}
	}

	
	if(remoteLoggingActive){
		var storedLogs = localStorage.getItem('storedLogs');
		
		switch(type){		
		case 'debug': storedLogs += '\n' + moment().format('YYYY-MM-DD HH:mm:ss') + ' : DEBUG : '+ message; break;
		case 'error': storedLogs += '\n' + moment().format('YYYY-MM-DD HH:mm:ss') + ' : ERROR : '+ message; break;
		}			
		
		localStorage.setItem('storedLogs', storedLogs);
	}

};	




