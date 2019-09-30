var app = angular.module('smartTVApp', []);


// CRUD CONTROLLER
// -------------------------------------------------------------------------
// All mighty controller
app.controller('smartCtrl', ['$scope', 'CRUDService', 'STOREService', 'UTILSService',
	    function ($scope, CRUDService, STOREService, UTILSService) {
	
	//function at initialization to setup all the variables
	$scope.init = function(){
		createLog('started init');
		
		//Applying Settings (remotely or default)
		$scope.setSettings();
		
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
		$scope.setCurrentPlaylistOK = false;
		
		//Timers process IDs
		$scope.runPID = -1;
		$scope.stopCurrentPlaylistPID = -1;
		$scope.showNextMediaPID = -1;
		
		//Current Programs and Loop variables
		$scope.currentPlaylist = {};
		$scope.currentPlaylistTimeout = 0;
		$scope.currentSpot = {};
		$scope.spotIndex = 0;
		
		createLog('ended init');
		
		//Run WinkWide Auto
		
		//Click anywhere to Fullscreen
		
	};
	
	//function to set App Settings (remotely or default)
	$scope.setSettings = function(){
		//Default Static Settings
		$scope.refreshPERIOD = 150000; //120000
		$scope.runPERIOD = 46000; //30000
		//--------------------------------
	};
	
	//Function to get current Display Id
	$scope.setDisplayId = function(){
		//set it default from LS
		$scope.displayId = localStorage.getItem('displayId');
		createLog('Display Id set from LS to : ' + $scope.displayId);
		
		//set it from server
		CRUDService.getData('/sync/displayId').success(function(data){
			$scope.displayId = data;
			createLog('Display Id set from server to : ' + $scope.displayId);
		});

	};
	
	//Core Function of WinkWide App
	$scope.winkwide = function() {
		createLog('started winkwide');
		$scope.openFullscreen();
		if(window.navigator.onLine)
				$scope.remoteStartup();
		else	$scope.localStartup();
		setInterval( $scope.refresh, $scope.refreshPERIOD);		
	};
	
	//Function to Refresh all App Data
	$scope.refresh = function() {
		createLog('trying a refresh');
		
		//stop Running programs
		$scope.stopCurrentPlaylist();			
		clearInterval($scope.runPID);
		
		//reinitialize Current Programs and Loop variables
		$scope.currentPlaylist = {};
		$scope.currentPlaylistTimeout = 0;
		$scope.currentSpot = {};
		$scope.spotIndex = 0;
		
		//REFRESH WITH INTERNET CONNECTION
		//&& last startup succeeded to get remoteData and send reports
		if(window.navigator.onLine &&
		($scope.isprogramsSyncOK() && $scope.isreportsSyncOK())){
			createLog('doing a remote refresh');
			
			//reset synchronization variables
			$scope.programsSyncOK = false;
			$scope.reportsSyncOK = false;
			
			//remote update & run
			$scope.remoteStartup();
			createLog('done with the remote refresh');
		}
		 
		//REFRESH WITHOUT INTERNET CONNECTION
		else {
			createLog('doing a local refresh');
			
			//local update & run
			$scope.localStartup();
			createLog('done with the local refresh');
		}
	
	};
	
	//Function to manage the programs displaying
	$scope.run = function(){
		createLog('trying to run with available data : ' + $scope.isprogramsSyncOK());

			if($scope.isprogramsSyncOK()){
				
				//clearing run timers
				createLog('clearing setCurrentPlaylist and showNextMediaPID timers');
				clearInterval($scope.showNextMediaPID);
				clearTimeout($scope.stopCurrentPlaylistPID);
				
				if($scope.setCurrentPlaylistOK){

					//set timeout for the current programs playlist 
					createLog('setting current programs playlist timeout : '+ $scope.currentPlaylistTimeout);
					$scope.stopCurrentPlaylistPID = setTimeout( $scope.stopCurrentPlaylist, $scope.currentPlaylistTimeout);

					//display spots media
					$scope.currentSpot = $scope.currentPlaylist.spots[0];
					$scope.showNextMedia($scope.currentSpot.media);
					var startTime = moment();
					
					$scope.showNextMediaPID = setInterval(function(){

						//Check if spot duration is passed 				
						if(moment().diff(startTime)  > $scope.currentSpot.duration*1000){

							//write report
							$scope.reports.push({ 
								startTime: startTime.format('YYYY-MM-DD HH:mm:ss a'), 
								endTime: moment().format('YYYY-MM-DD HH:mm:ss a'),
								display: null, media: $scope.currentSpot.media });
							
							//increment spotIndex
							if($scope.spotIndex < $scope.currentPlaylist.spots.length)
									$scope.spotIndex++;
							else	$scope.spotIndex = 0;
							
							//display Next Media and save startTime
							if(typeof($scope.currentPlaylist.spots[$scope.spotIndex]) !== 'undefined')
								$scope.currentSpot = $scope.currentPlaylist.spots[$scope.spotIndex];
							$scope.showNextMedia($scope.currentSpot.media);
							startTime = moment();

						}else{
							createLog('waiting...');
						}
																					
						
					}, 10);
									
				}else{
					//set current programs playlist
					$scope.setCurrentPlaylist();
				}
			}			
	};
	
	//Function to set the current programs playlist its timeout
	$scope.setCurrentPlaylist = function() {
		
		createLog('setting current programs playlist and its timeout');

		//Reset current playlist timeout to the maximum (never more than 20 days / largest integer in milliseconds)
		$scope.currentPlaylistTimeout = 20*24*3600*1000; 

		//Filter programs to only current ones
		let currentPrograms = $scope.programs.filter(function(program){
			return moment().isBetween( moment(program.startTime, 'YYYY-MM-DD HH:mm a') , moment(program.endTime, 'YYYY-MM-DD HH:mm a') );
		});
		
		//reset playlist spots
		$scope.currentPlaylist = {};
		$scope.currentPlaylist.spots = [];
		
		angular.forEach(currentPrograms, function(program) {
			
			//Set Timeout to the minimal one (and never more than 20 days / largest integer in milliseconds)
			$scope.currentPlaylistTimeout =  Math.min( $scope.currentPlaylistTimeout, moment(program.endTime, 'YYYY-MM-DD HH:mm a').diff(moment())); 
			
			//Merge current programs playlists
			angular.forEach(program.playlists, function(playlist) {
				$scope.currentPlaylist.spots = $scope.currentPlaylist.spots.concat( playlist.spots);
			});
		});
		
		//Build Current Playlist by alterning private/entertain/ad/other Spots
		$scope.currentPlaylist.spots = $scope.buildAlternedPlaylist();
				
		
		$scope.setCurrentPlaylistOK = true;
		createLog('current programs playlist and  successfully set');
		
		//launch run
		$scope.run();
	};
	
	//Function to Build Current Playlist by alterning private/entertain/ad/other Spots
	$scope.buildAlternedPlaylist = function(){
		let spots = $scope.currentPlaylist.spots;
		let alternedSpots = [];

		//build spots lists by type
		let privateSpots = spots.filter(function(spot){ return spot.media.category == 'private'; });
		let entertainSpots = spots.filter(function(spot){ return spot.media.category == 'entertain'; });
		let adSpots = spots.filter(function(spot){ return spot.media.category == 'ad'; });
		let otherSpots = spots.filter(function(spot){ return spot.media.category == 'other'; });
		
		//fill alterned Playlist spots
		let i=0;
		while( i < $scope.currentPlaylist.spots.length){
			[privateSpots, entertainSpots, adSpots, otherSpots].forEach(function(spotsList){
				if( spotsList.length > 0 ){
					alternedSpots.push(spotsList[0]);
					spotsList.splice(0,1);
					i++;
				}
			});
		}
		
		return alternedSpots;
	};
	
	//Function to Stop current playlist
	$scope.stopCurrentPlaylist = function(){
		
		createLog('stopping current playlist : clearing setCurrentPlaylist and showNextMediaPID timers');
		clearInterval($scope.showNextMediaPID);
		clearTimeout($scope.stopCurrentPlaylistPID);
		$scope.setCurrentPlaylistOK = false;
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
							angular.element(mainImage).attr('src','/img/misc/audioCover.gif');							
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
					UTILSService.generateHTMLPreview(htmlElement, media.url, null)
				
				angular.element(mainElement).attr('style','display:block');				
				createLog('showing next media from remote server: ' + media.name);			
			}
			else{
				if(media.type !== 'App')
					angular.element(mainElement).attr('src', data);
				else
					UTILSService.generateHTMLPreview(mainElement, null, data)
					
				angular.element(mainElement).attr('style','display:block');	
				createLog('showing next media from IDB: ' + media.name);			
			}
									
		});		
	};
	

	//The startup function / WITH INTERNET CONNECTION 
	$scope.remoteStartup = function() {
		createLog('launched a remote Startup');
				
		//Sync reports
		$scope.sendReports();
	
		//Sync Programs
		$scope.getPrograms();
			
	};
	
	//The startup function / NO INTERNET CONNECTION
	$scope.localStartup = function()	{
		createLog('launched a local Startup');

		//store reports in LS
		$scope.storeReports();
		
		//run with locally stored programs
		$scope.programs = STOREService.getJSONfromLS('storedPrograms');
		createLog('running with programs stored in Local Storage');
		
		//launch Run loop : start displaying programs medias
		$scope.run();
		$scope.runPID = setInterval($scope.run, $scope.runPERIOD);			
	}
	
	
	//Function to send Reports to server
	$scope.sendReports = function() { 
		createLog('trying to send Reports to server');
		
		//store Reports in LS and empty reports object
		$scope.storeReports();
		var storedReports = STOREService.getJSONfromLS('storedReports');

		// try to send reports and empty stored reports in LS
		CRUDService.setData('/sync/reports', storedReports)
			.success(function(data){
				createLog('sent Reports successfully');
				$scope.reportsSyncOK = true;
				createLog('clearing LS Reports');
				STOREService.storeJSONinLS('storedReports', [])
				})
			.error(function(errors){
				createLog('sending Reports to server failed for reason: ' + errors);
				$scope.reportsSyncOK = true;
				});
		
	};
	
	//Function to store Reports in LS and empty reports object
	$scope.storeReports = function() { 
		createLog('storing reports in LS');
		
		var storedReports = STOREService.getJSONfromLS('storedReports');
		storedReports.push.apply(storedReports, $scope.reports);		
		STOREService.storeJSONinLS('storedReports', storedReports)
		
		$scope.reports = [];
		};
	
	//Function to get Programs and their Medias from server
	$scope.getPrograms = function() { 
		createLog('trying to get Programs and their Medias');
		
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
				
				createLog('got programs and cached medias for all');
				$scope.programsSyncOK = true;
				
				//launch Run loop : start displaying programs medias
				$scope.run();
				$scope.runPID = setInterval($scope.run, $scope.runPERIOD);	
				})
			.error(function(errors){
				createLog('getting Programs from server failed for reason: ' + errors);
				$scope.programsSyncOK = true;
				});

				
	};
	
	//Function to locally cache medias 
	$scope.cacheMedia = function(media) {
		// Animate loader in screen
		$(".se-pre-con").show();
		
		createLog('trying to cache media in localDB : ' + media.url);
		//store the media file
		STOREService.storeFileIDB(media.url, media.format);
		if(media.type == 'Audio')
			STOREService.storeFileIDB(media.thumbUrl, 'image/jpeg');
	};
	
	//Functions to check if remoteData was fetched/ reports were pushed
	$scope.isprogramsSyncOK = function(){
		return $scope.programsSyncOK;
	};
	$scope.isreportsSyncOK = function(){
		return $scope.reportsSyncOK;
	};
	
		
	//FullScreen Utils
	$scope.openFullscreen = function() {
		createLog('opening full screen');
			
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
		createLog('testing if full screen');
		
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
			createLog("Error",'Unable to open database.');
		}

		request.onsuccess = function(e) {
			db = e.target.result;
			createLog('db opened');
		}

		request.onupgradeneeded = function(e) {
			db = e.target.result;
			db.createObjectStore('medias', {keyPath:'url'});
		}
	}
	
	function storeFileIDB(fileUrl, fileType){
		
		// Animate loader in screen
		$(".se-pre-con").show();
		
		//open database then store file if success
		let request = indexedDB.open('winkwide', 1);

		request.onerror = function(e) {
			createLog("Error",'Unable to open database.');
		}
		
		request.onsuccess = function(e) {
			db = e.target.result;
		
			//Check if the file is already stored in DB
			let transaction = db.transaction(['medias']); // readonly
			let request = transaction.objectStore('medias').get(fileUrl);
			request.onsuccess = function() {
			  if (request.result !== undefined) {
				  createLog("File :"+ fileUrl +" already stored in IDB");
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
			            		createLog("Media added to the medias store", request.result);
			            		$(".se-pre-con").fadeOut("slow");
			            	};
			            	
			            	transaction.oncomplete = function(e) {
			        			createLog('data stored');
			        			$(".se-pre-con").fadeOut("slow");
			        		}
		
			            	request.onerror = function() {
			            		createLog("Error", request.error);
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
		
		//open database then get file if success
		let request = indexedDB.open('winkwide', 1);

		request.onerror = function(e) {
			createLog("Error",'Unable to open database.');
			
			callback(null);
		}
		
		request.onsuccess = function(e) {
			db = e.target.result;
			
			//Check if the file is already stored in DB
			let transaction = db.transaction(['medias']); // readonly
			let request = transaction.objectStore('medias').get(fileUrl);
			
			request.onsuccess = function() {
			  if (request.result !== undefined) {
				  createLog("getting File :"+ request.result.url +" from IDB");
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

                console.log('done url to file')
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
			$scope.urltoFile(htmlUrl, 'text/html', 'preview.html',
				function(html){
				    	angular.element(mainHTML).html(html);
						setTimeout(startAppPreview, 1000);
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

//Function to manage App logs
function createLog(message){
 console.log(moment().format('YYYY-MM-DD HH:mm:ss a') + ' : '+ message);	
};	




