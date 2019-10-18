var app = angular.module('portalApp', ['ui.grid','ui.grid.pagination', 'ui.grid.selection', 'ui.grid.edit', 'ui.grid.exporter', 'ui.grid.pinning']);


// CRUD CONTROLLER
// -------------------------------------------------------------------------
// All mighty controller
app.controller('crudCtrl', ['$scope','objectModel', 'CRUDService', 'uiGridConstants',
	    function ($scope, objectModel, CRUDService, uiGridConstants) {
		
		$scope.init = function(objModel){
			// Get current path
			$scope.currentPath = window.location.pathname;
			// Define the Object Target for the CRUD App (Display, Media...)
			$scope.targetObject = objModel;
			$scope.targetCollection = $scope.targetObject + 's';
			
			// Define columnList object (for the table column titles)
			$scope.columnList = {};
			
			// Define Pagination options & Specific filters for GetAll Request to
			// fill the UI Grid  
			$scope.paginationOptions = {pageNumber: 1, pageSize: 20, sortColumns: [], filterColumns: []};
			$scope.specificFilters = {};
					
			// Define Edit / Delete target object url link and fill $scope.formData
			var targetObjectUrl = '/api/'+$scope.targetCollection;
			$scope.schema = {};
			$scope.formData = {};
			$scope.isCreateModalType = true; // to differentiate create and edit modal
			$scope.isDeleteModalType = true; // to differentiate delete and view modal			
		}
		
		$scope.setFormData = function(url, operation){
			if(operation == 'edit' || operation == 'delete' || operation =='view') {
				targetObjectUrl = url;
				console.log('setting target object url');
				CRUDService.getOne(url).success(function(data){
					$scope.formData = data;
					console.log('setting form data');
					
					// SPECIFIC for media edition and deletion
					if($scope.targetObject == 'media'){	
			    		$scope.resetPreview();
			    		angular.element(thumbPreview).attr("class", "d-block");
			    		if($scope.formData.type == 'App')
			    			$scope.generateHTMLPreview();
					}
					
					// ------------------------------------------
					// SPECIFIC for playlist edition and deletion
					if($scope.targetObject == 'playlist'){
						$scope.setFormSelectionData('selectedSpotsGrid',$scope.formData._links.spots.href, 'spots');
						$scope.setFormSelectionData('viewSelectedSpotsGrid',$scope.formData._links.spots.href, 'spots');
					}
					// ------------------------------------------
					
					// SPECIFIC for program edition and deletion
					if($scope.targetObject == 'program'){
						$scope.setFormSelectionData('selectedDisplaysGrid',$scope.formData._links.displays.href, 'displays');
						$scope.setFormSelectionData('selectedPlaylistsGrid',$scope.formData._links.playlists.href, 'playlists');
						
						$scope.setFormSelectionData('viewSelectedDisplaysGrid',$scope.formData._links.displays.href, 'displays');
						$scope.setFormSelectionData('viewSelectedPlaylistsGrid',$scope.formData._links.playlists.href, 'playlists');
					}
					// ------------------------------------------
				});
			}
			
			// check if createEditModal is of type Create
			$scope.isCreateModalType = (operation == 'create');
			
			// check if viewDeleteModal is of type Delete
			$scope.isDeleteModalType = (operation == 'delete');
			
			// set clear-formData-on-hide if the modal is of type edit or delete
			if(operation == 'create' || operation == 'edit')
				angular.element(createEditObjectModal).on('hidden.bs.modal', function (e) {
					$scope.clearFormData();
					$scope.clearObjectFormValidation();
				});
			if(operation == 'delete' || operation =='view')
				angular.element(viewDeleteObjectModal).on('hidden.bs.modal', function (e) {
					$scope.clearFormData();
				});
			
			
			// initiate DateTime Pickers in Modal
			
			if (angular.element(".form_datetime")[0])
				initiateDateTimePickers();
		};
		

		// Define HTML template for edition buttons
		var viewButtonHTML = '<button ng-click="grid.appScope.setFormData(row.entity.actionLink, \'view\')" type="button" class="btn btn-sm btn-primary ml-1" data-toggle="modal" data-target="#viewDeleteObjectModal" ><i class="fa fa-eye fa-fw"></i></button>';
		var editButtonHTML = '<button ng-click="grid.appScope.setFormData(row.entity.actionLink, \'edit\')" type="button" class="btn btn-sm btn-secondary ml-1" data-toggle="modal" data-target="#createEditObjectModal" > <i class="fa fa-pencil-alt fa-fw"></i></button>';
		var deleteButtonHTML = '<button ng-click="grid.appScope.setFormData(row.entity.actionLink, \'delete\')" type="button" class="btn btn-sm btn-danger ml-1" data-toggle="modal" data-target="#viewDeleteObjectModal" > <i class="fa fa-trash-alt fa-fw"></i></button>';
		var actionButtonsHTML = '<div class="m-1">' + viewButtonHTML + editButtonHTML + deleteButtonHTML + '</div>';
		
		// Define HTML template for media thumbnails
		var thumbnailHTML = '<img ng-src="{{row.entity.thumbUrl}}" alt="No Image Found" class="m-1" height="80%" >'
		
		// Define a function to Get Data Scheme from REST Api
		$scope.getColumnList = function(){
			var columnList = [];
			CRUDService.getScheme($scope.targetObject).success(function(data){
				$scope.schema = data;
				
				// SPECIFIC for programs and reports list (as they have related
				// objects : display and media)
				angular.forEach(data, function(value, key) {
					if(value.type=='Display')
						this.push({ field: 'display' , name: 'Display', enableFiltering:false, width: '*', minWidth:100 });
					if(value.type=='Media')
						this.push({ field: 'media' , name: 'Media', enableFiltering:false, width: '*', minWidth:100 });
				}, columnList);
				// ---------------------------------------
				
				angular.forEach(data, function(value, key) {
					
					// SPECIFIC for media thumbnail
					if(value.name=='thumbUrl' && $scope.targetObject == 'media')
						this.unshift({ field: value.name , name: 'Thumbnail', cellTemplate: thumbnailHTML, enableFiltering:false, pinnedLeft:true, width: '*', minWidth:100 });
					// ---------------------------------------
					
					// enabling filtering only for string fields and boolean
					else if(value.type=='Date' || value.name=='mac')
						this.push({ field: value.name , name: value.title, enableFiltering:false, width: '*', minWidth:150 });
					else if(value.name=='name')
						this.push({ field: value.name , name: value.title, enableFiltering:true, pinnedLeft:true, width: '*', minWidth:150 });
					else if(value.name=='userRole' )
						this.push({ field: value.name , name: value.title, enableFiltering:true, width: '*', minWidth:100,
							filter: {
						          type: uiGridConstants.filter.SELECT,
						          selectOptions: [ { value: 'ROLE_ADMIN', label: 'Admin' }, 
						        	  				{ value: 'ROLE_CLIENT', label: 'Client' },
						        	  				{ value: 'ROLE_PARTNER', label: 'Partner' }, 
						        	  				{ value: 'ROLE_MACHINE', label: 'Machine' }]}
						          });
					else if(value.type=='String' && ![ 'url', 'password', 'confirmPassword'].includes(value.name))
						this.push({ field: value.name , name: value.title, enableFiltering:true, width: '*', minWidth:100 });
					else if(value.name!=='id' && [ 'int', 'Long', 'BigDecimal'].includes(value.type))
						this.push({ field: value.name , name: value.title, enableFiltering:false, width: '*', minWidth:100 });
					else if(value.type=='boolean')
						this.push({ field: value.name , name: value.title, enableFiltering:true, width: '*', minWidth:100,
							filter: {
						          type: uiGridConstants.filter.SELECT,
						          selectOptions: [ { value: true, label: 'true' }, 
						        	  				{ value: false, label: 'false' }]}
						          });
					
					}, columnList);
				
				//put Ids at the end of the table
				angular.forEach(data, function(value, key) {
					if(value.name=='id')
						this.push({ field: value.name , name: value.title, enableFiltering:false, width: '*', width:100 });

					}, columnList);

				
				// create an Actions column (view, edit, delete)
				columnList.push({ field: 'actionLink', name: 'Actions', cellTemplate: actionButtonsHTML, enableFiltering: false, pinnedRight:true, width:130});
				
			});
			
			$scope.columnList = columnList;
		};
		
		$scope.getDisplayName = function(target){
			return CRUDService.getLinkedObjects(target).name
		};

		// Define a function to Get Data Collection from REST Api and put it in
		// some grid options
		$scope.getCollectionData = function(options, target, page, size, sortCols, filterCols, specificFilters) {
			
		     CRUDService.getAll(target, page, size, sortCols, filterCols, specificFilters)
				.success(function(data){
										
		          	$scope[options].data = data._embedded[target];
		
					angular.forEach($scope[options].data, function(value, key) {
						value.actionLink =  CRUDService.getRelativePath(value._links.self.href);
						
						// Specific to reports (getting display.id and media.id)
						if(target=='reports'){					
							CRUDService.getLinkedObjects(value._links.display.href)
								.success(function(display){
									value.display = display.id;
							});
					
							CRUDService.getLinkedObjects(value._links.media.href)
								.success(function(media){
									value.media = media.id;
							});
						}
						// ------
						
						});
		            $scope[options].totalItems = data.page.totalElements;
		         });                            
		};
		
		// Define a function to set Grid Options data
	    $scope.setGridData = function() {
		
		     $scope.getCollectionData(
				'mainGrid',
				$scope.targetCollection, 
				$scope.paginationOptions.pageNumber, 
				$scope.paginationOptions.pageSize, 
				$scope.paginationOptions.sortColumns,
				$scope.paginationOptions.filterColumns,
				$scope.specificFilters);                            
		 };
	 	
		// Define UI grid options & define update function
		$scope.setMainGrid = function(){
		    $scope.mainGrid = {
				rowHeight: 40,
			    enableGridMenu: true,
			    gridMenuCustomItems: [{ title: 'Delete Selection',
			          				action: function ($event) {
			          					angular.element(deleteSelectionModal).modal('show');
			          				}, order: 1}],
			    enableSelectAll: true,
			    exporterMenuCsv: false,
			    exporterMenuPdf: false,
			    exporterExcelFilename: 'export.xlsx',
			    exporterExcelSheetName: 'Sheet1',
		        paginationPageSizes: [5, 10, 20, 50, 100, 500, 1000, 10000],
		        paginationPageSize: $scope.paginationOptions.pageSize,
		        enableColumnMenus:false,
		    	useExternalPagination: true,
				useExternalSorting: true,
				enableFiltering: true,
				useExternalFiltering: true,
		        columnDefs: $scope.columnList,
		        onRegisterApi: function(gridApi) {
		           	$scope.gridApi = gridApi;
		           	$scope.gridApi.pagination.on.paginationChanged(
		             	$scope, function (newPage, pageSize) {
					     	$scope.paginationOptions.pageNumber = newPage;
			     		 	$scope.paginationOptions.pageSize = pageSize;
							$scope.setGridData();
					 	});
					$scope.gridApi.core.on.sortChanged(
						$scope, function (grid, sortColumns) {
							$scope.paginationOptions.sortColumns = sortColumns;
							$scope.setGridData();
					 	});
			        $scope.gridApi.core.on.filterChanged(
			        	$scope, function() {
			        		$scope.paginationOptions.filterColumns = this.grid.columns.filter(c => c.enableFiltering == true);
							$scope.setGridData();
		        			});
				}
		    };	
		}

	
		// Define the Create function
		$scope.createTargetObject = function(){
	
			// Animate loader in screen
			$(".se-pre-con").show();
			
			// SPECIFIC for playlist and program creation
			if($scope.targetObject == 'playlist')
				$scope.formData.spots = $scope.selectedSpotsGrid.data;
			if($scope.targetObject == 'program'){
				$scope.formData.displays = $scope.selectedDisplaysGrid.data;
				$scope.formData.playlists = $scope.selectedPlaylistsGrid.data;
			}
		
			CRUDService.createOne($scope.targetCollection, $scope.formData).success(function(data){
				console.log('created object');
				createAlert(main_nav, 'success', 'successfully created '+ $scope.targetObject, 5000);
				$scope.setGridData();
				angular.element(createEditObjectModal).modal('hide');
				// Animate loader off screen
				$(".se-pre-con").fadeOut("slow");
	
			})
			.error(function(data){
				console.log('Did not create object correctly');
				
				//Validate Form with remote answer errors & Alert 
				$scope.validateObjectForm(data);
				
				$scope.setGridData();
				//angular.element(createEditObjectModal).modal('hide');
				// Animate loader off screen
				$(".se-pre-con").fadeOut("slow");
			});
	
		};
		
		// Define the Edit function
		$scope.updateTargetObject = function(){	
			
			// Animate loader in screen
			$(".se-pre-con").show();
			
			// SPECIFIC for playlist update
			if($scope.targetObject == 'playlist')
				$scope.formData.spots = $scope.selectedSpotsGrid.data;	
			if($scope.targetObject == 'program'){
				$scope.formData.displays = $scope.selectedDisplaysGrid.data;
				$scope.formData.playlists = $scope.selectedPlaylistsGrid.data;
			}
			
			CRUDService.updateOne(targetObjectUrl, $scope.formData).success(function(data){
				console.log('updated object');
				createAlert(main_nav, 'success', 'successfully updated '+ $scope.targetObject, 5000);
				$scope.clearFormData();
				$scope.setGridData();
				angular.element(createEditObjectModal).modal('hide');
				// Animate loader off screen
				$(".se-pre-con").fadeOut("slow");
			})
			.error(function(data){
				console.log('Did not update object correctly');
				
				//Validate Form with remote answer errors & Alert 
				$scope.validateObjectForm(data);
				
				$scope.setGridData();
				//angular.element(createEditObjectModal).modal('hide');
				// Animate loader off screen
				$(".se-pre-con").fadeOut("slow");
			});
		};
		
		// Define the Delete function
		$scope.deleteTargetObject = function(){		
			
			// Animate loader in screen
			$(".se-pre-con").show();
			
			CRUDService.deleteOne(targetObjectUrl).success(function(data){
				console.log('deleted object');
				createAlert(main_nav, 'success', 'successfully deleted '+ $scope.targetObject, 5000);
				$scope.clearFormData();
				$scope.setGridData();
				angular.element(viewDeleteObjectModal).modal('hide');
				// Animate loader off screen
				$(".se-pre-con").fadeOut("slow");
			})
			.error(function(data){
				console.log('Did not delete object correctly');
				createAlert(viewDeleteObjectModalBody, 'danger', 'an issue occured deleting '+ $scope.targetObject +'<br> reason: '
						+((typeof data.message == 'undefined') ? data.toString() : data.message), null);
				//$scope.clearFormData();
				$scope.setGridData();
				// angular.element(viewDeleteObjectModal).modal('hide');
				// Animate loader off screen
				$(".se-pre-con").fadeOut("slow");
			});
		};
	
		// Define the Clean form function
		$scope.clearFormData = function(){			
			$scope.formData = {};
			$scope.objectForm.$setPristine();
			
			if($scope.targetObject == 'media'){
	
	    		angular.element(file)[0].value = '';
				angular.element(thumbFile)[0].value = '';
	
				angular.element(imagePreview).attr('src', '');
				angular.element(videoPreview).attr('src', '');
				angular.element(audioPreview).attr('src', '');
				angular.element(htmlPreview).html('');
				$('.modal-backdrop').remove();
				angular.element(thumbPreview).attr('src', '');
							
				$scope.resetPreview();
			}
		};
		
		// Define function to Delete a whole selection of objects (selected rows)
		$scope.deleteSelection = function(){
			$scope.gridApi.selection.getSelectedRows().forEach(function(obj){
				CRUDService.deleteOne(CRUDService.getRelativePath(obj._links.self.href))
				.success(function(data){
					console.log('deleted object: ' + obj._links.self.href);
					createAlert(main_nav, 'success', 'successfully deleted '+ $scope.targetObject+'s', 5000);
					$scope.setGridData();		
				})
				.error(function(data){
					createAlert(main_nav, 'danger', 'an issue occured deleting '+ $scope.targetObject +'s <br> reason: '
							+((typeof data.message == 'undefined') ? data.toString() : data.message), null);
				});
			});
			angular.element(deleteSelectionModal).modal('hide');
		};
	
		//Function to Validate Form with remote answer errors
		$scope.validateObjectForm = function(data){		
			
			if (typeof(data.errors) !== 'undefined'){
			
				/*if(data.errors[0].includes('\"name\"')){
					$scope.objectForm.name.$invalid = true;
					$scope.objectForm.name.$error.nameInvalid = true;
					angular.element(nameInvalidMessage).html('&nbsp' + data.message);
				}else if(data.errors[0].includes('\"mac\"')){
					$scope.objectForm.mac.$invalid = true;
					$scope.objectForm.mac.$error.macInvalid = true;
					angular.element(macInvalidMessage).html('&nbsp' + data.message);
				}else{
					$scope.clearObjectFormValidation();
					createAlert(createEditObjectModalBody, 'danger', 'an issue occured creating '+ $scope.targetObject +'<br> reason: '
							+ data.message, null);
				}*/
				
				
				angular.forEach($scope.objectForm, function(field, key) {
					if(data.errors[0].includes('\"'+key+'\"')){
						field.$invalid = true;
						document.getElementById(key+'InvalidMessage').innerHTML = '&nbsp' + data.message;
					}
				});
				
			}
			else {
				
				$scope.clearObjectFormValidation();
				createAlert(createEditObjectModalBody, 'danger', 'an issue occured creating '+ $scope.targetObject +'<br> reason: '
						+ data.toString(), null);
			}
		};
		
		//Function to Clear validateObjectForm(data) messages
		$scope.clearObjectFormValidation = function(){
			if(typeof($scope.objectForm.name) !== 'undefined'){
				angular.element(nameInvalidMessage).html('');
				$scope.objectForm.name.$invalid = false;
				$scope.objectForm.name.$error = {};
			} 
			if(typeof($scope.objectForm.mac) !== 'undefined'){
				angular.element(macInvalidMessage).html('');
				$scope.objectForm.mac.$invalid = false;
				$scope.objectForm.mac.$error = {};
			}
			clearModalAlert();
		};
		
		//Function to refresh App view from outside controller (for example in Settings View)
		$scope.refreshAppView = function (objModel){
			
			// Initalize controller variables
			$scope.init(objModel);
			
			// Build columnList (titles of the table columns)
			$scope.getColumnList();
			
			// Get Grid Data from REST Api
			$scope.setGridData();
			
			// Define UI grid options & define update function
			$scope.setMainGrid();
	
		};
		
		// Refresh App view at controller load
		$scope.refreshAppView(objectModel);
		
	// ----------------------------------------------------------------------
	// ----------------------------------------------------------------------
	// END OF GENERIC
	// ----------------------------------------------------------------------
	// ----------------------------------------------------------------------

	
	
	
	
	// ----------------------------------------------------------------------
	// ----------------------------------------------------------------------
	// START OF SPECIFIC
	// ----------------------------------------------------------------------
	// ----------------------------------------------------------------------
	
	// Specific to Media File Upload
	// START OF MEDIA MANAGEMENT STUFF
	$scope.previewFile = function (input) { 

			    var reader = new FileReader();
			    reader.onload = function(e) {
			    	
			    	if(input.name == 'file'){
			    		
			    		//auto-fill form file and name
						$scope.formData.file = input.files[0];
						$scope.formData.name = input.files[0].name;
						
						//reset media form
						$scope.objectForm.name.$pristine = false;
						$scope.formData.verified = false;
			    		
			    		//set Media type
				    	if($scope.formData.file.type.includes('image')){
				    		$scope.formData.type = 'Image';
				    		$scope.formData.url = null;
				    		angular.element(imagePreview).attr("src", e.target.result);
				    	}else
				    	if($scope.formData.file.type.includes('video')){
				    		$scope.formData.type = 'Video';
				    		$scope.formData.url = null;
				    		angular.element(videoPreview).attr("src", e.target.result);
				    		angular.element(thumbPreview).attr("src", "/img/misc/defaultVideoThumb.png"); //default video thumb in image to generate thumb
				    		$scope.urltoFile("/img/misc/defaultVideoThumb.png", "image/png", "defaultVideoThumb.png", 
				    				function(file){$scope.formData.thumbFile = file});
				    	}else
				    	if( $scope.formData.file.type.includes('audio')){
				    		$scope.formData.type = 'Audio';
				    		$scope.formData.url = null;
				    		angular.element(audioPreview).attr("src", e.target.result);
				    		angular.element(thumbPreview).attr("src", "/img/misc/defaultAudioCover.gif"); //default audio cover in image to generate thumb
				    		$scope.urltoFile("/img/misc/defaultAudioCover.gif", "image/gif", "defaultAudioCover.gif", 
				    				function(file){$scope.formData.thumbFile = file});
				    	}else
				    	if($scope.formData.file.type.includes('html')){
				    		$scope.formData.type = 'App';
				    		$scope.formData.url = null;
				    		//read html file into preview
				    		$scope.generateHTMLPreview();
				    		
				    		angular.element(thumbPreview).attr("src", "/img/misc/defaultAppThumb.png"); //default app cover in image to generate thumb
				    		$scope.formData.thumbFile = $scope.urltoFile("/img/misc/defaultAppThumb.png", "image/png", "defaultAppThumb.png", 
				    				function(file){$scope.formData.thumbFile = file});
				    	}
				    	
				    	//reset Preview area
				    	$scope.resetPreview();
					    $scope.$apply();
			    	}
			    	if(input.name == 'thumbFile'){
			    		//auto-fill form file
						$scope.formData.thumbFile = input.files[0];

				    	if($scope.formData.thumbFile.type.includes('image')){
				    		angular.element(thumbPreview).attr("src", e.target.result);
				    		angular.element(thumbPreview).attr("class", "d-block");
				    	}
			    	}
			    	angular.element(thumbPreview).attr("class", "d-block");
				    $scope.$apply();
				}
			    
			    reader.readAsDataURL(input.files[0]);

	};
	
	$scope.resetPreview = function () {
		//hide all previews
		angular.element(thumbPreview).attr("class", "d-none");
		angular.element(imagePreview).attr("class", "w-100 d-none");
		angular.element(videoPreview).attr("class", "w-100 d-none");
		angular.element(audioPreview).attr("class", "w-100 d-none");
		angular.element(htmlPreview).attr("class", "w-100 d-none");
		
		//show relevant preview
		if(typeof($scope.formData.type) !== 'undefined'){
			switch ($scope.formData.type) {
			case 'Image':
				angular.element(imagePreview).attr("class", "w-100 d-block");
				break;
			case 'Video':
				angular.element(videoPreview).attr("class", "w-100 d-block");
				break;
			case 'Audio':
				angular.element(audioPreview).attr("class", "w-100 d-block");
				break;
			case 'App':
				angular.element(htmlPreview).attr("class", "w-100 d-block");
				break;
			}	
		}
	}
	
	$scope.generateThumbFile = function (element) {

		//stop if element is hidden or no file selected and video not played
		if(//element.getAttribute('class') !== 'w-100 d-block'
			element.getAttribute('src') == ''
			|| (element.tagName == 'IMG' && typeof(angular.element(file)[0].files[0]) == 'undefined' )
			|| (element.tagName == 'VIDEO' && element.currentTime == 0 ) )
			return;
		
    	var canvas = document.getElementById('thumbnailCanvas'),
		context = canvas.getContext("2d");

		if( element.tagName == 'IMG'){
			canvas.width = 500;
			canvas.height = 500*element.naturalHeight/element.naturalWidth;
			context.drawImage(element, 0, 0, 500, 500*element.naturalHeight/element.naturalWidth);			
		}
			
		if( element.tagName == 'VIDEO'){
			canvas.width = 500;
			canvas.height = 500*element.videoHeight/element.videoWidth;
			context.drawImage(element, 0, 0, 500, 500*element.videoHeight/element.videoWidth);
		}

		// Setting thumbnail file in formData
		canvas.toBlob(function(blob) {
			$scope.formData.thumbFile = new File([blob], 'thumbnail.jpeg', {type: "image/jpeg"});
			});
		
		angular.element(thumbFile)[0].value = '';
		angular.element(thumbPreview).attr("src", canvas.toDataURL());
		$scope.$apply();
	};
	
	$scope.urltoFile = function(fileUrl, fileType, fileName, callback){
	
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
	                if(callback !== null)
	                	callback(new File([blob], fileName, {type: fileType}));

	                console.log('done url to file')
	            };
	            // Load blob as Data URL
	            fileReader.readAsDataURL(blob);
	          
	        } else return null;
	    }, false);
	    // Send XHR
	    xhr.send();
	}
	
	$scope.generateHTMLPreview = function(){
		var file;
		var reader = new FileReader();
		if($scope.formData.url != null)
			$scope.urltoFile($scope.formData.url, 'text/html', 'preview.html',
				function(file){
				    //read file text into htmlPreview
				    reader.readAsText(file, "UTF-8");
				    reader.onload = function (evt) {
				    	angular.element(htmlPreview).html(evt.target.result);
						setTimeout(startAppPreview, 1000);
				    }
				    reader.onerror = function (evt) {
				        createLog("error reading HTML file");
				    }
			});
			
		else {
		    //read file text into htmlPreview
		    reader.readAsText($scope.formData.file, "UTF-8");
		    reader.onload = function (evt) {
		    	angular.element(htmlPreview).html(evt.target.result);
				setTimeout(startAppPreview, 1000);
		    }
		    reader.onerror = function (evt) {
		        createLog("error reading HTML file");
		    }
		}
	}	
	
	// END OF MEDIA MANAGEMENT STUFF
	
	
	
	
	
	
	
	
	// Specific UI tables and data for programs (displays and playlists) and
	// playlists (medias and spots) management
	
	// Define All Displays table
	$scope.displaysPaginationOptions = {pageNumber: 1, pageSize: 10, sortColumns: [], filterColumns: []};
	$scope.displaysGrid = {
			rowHeight:40,
		    enableSelectAll: true,
	        paginationPageSizes: [5, 10, 20, 50],
	        paginationPageSize: $scope.displaysPaginationOptions.pageSize,
	        enableColumnMenus:false,
	    	useExternalPagination: true,
			useExternalSorting: true,
			enableFiltering: true,
			useExternalFiltering: true,
	        columnDefs: [	{ field: 'id', name: 'id', minWidth:100 },
							{ field: 'name', name: 'Name', minWidth:100 },
							{ field: 'area', name: 'area', minWidth:100}],
	        onRegisterApi: function(gridApi) {
	           	$scope.displaysGridApi = gridApi;
	           	$scope.displaysGridApi.pagination.on.paginationChanged(
	             	$scope, function (newPage, pageSize) {
				     	$scope.displaysPaginationOptions.pageNumber = newPage;
		     		 	$scope.displaysPaginationOptions.pageSize = pageSize;
						$scope.setFormCollectionData('displaysGrid','displays', $scope.displaysPaginationOptions);
				 	});
				$scope.displaysGridApi.core.on.sortChanged(
					$scope, function (grid, sortColumns) {
						$scope.displaysPaginationOptions.sortColumns = sortColumns;
						$scope.setFormCollectionData('displaysGrid','displays', $scope.displaysPaginationOptions);
				 	});
		        $scope.displaysGridApi.core.on.filterChanged(
		        	$scope, function() {
		        		$scope.displaysPaginationOptions.filterColumns = this.grid.columns;
						$scope.setFormCollectionData('displaysGrid','displays', $scope.displaysPaginationOptions);
	        		});
			}
	    };
	
	// Define All Playlists table
	$scope.playlistsPaginationOptions = {pageNumber: 1, pageSize: 10, sortColumns: [], filterColumns: []};
	$scope.playlistsGrid = {
			rowHeight:40,
		    enableSelectAll: true,
	        paginationPageSizes: [5, 10, 20, 50],
	        paginationPageSize: $scope.playlistsPaginationOptions.pageSize,
	        enableColumnMenus:false,
	    	useExternalPagination: true,
			useExternalSorting: true,
			enableFiltering: true,
			useExternalFiltering: true,
	        columnDefs: [	{ field: 'id', name: 'id', minWidth:100 },
							{ field: 'name', name: 'Name', minWidth:100},
							{ field: 'duration', name: 'Duration', minWidth:100}] ,
	        onRegisterApi: function(gridApi) {
	           	$scope.playlistsGridApi = gridApi;
	           	$scope.playlistsGridApi.pagination.on.paginationChanged(
	             	$scope, function (newPage, pageSize) {
				     	$scope.playlistsPaginationOptions.pageNumber = newPage;
		     		 	$scope.playlistsPaginationOptions.pageSize = pageSize;
						$scope.setFormCollectionData('playlistsGrid','playlists', $scope.playlistsPaginationOptions);
				 	});
				$scope.playlistsGridApi.core.on.sortChanged(
					$scope, function (grid, sortColumns) {
						$scope.playlistsPaginationOptions.sortColumns = sortColumns;
						$scope.setFormCollectionData('playlistsGrid','playlists', $scope.playlistsPaginationOptions);
				 	});
		        $scope.playlistsGridApi.core.on.filterChanged(
		        	$scope, function() {
		        		$scope.playlistsPaginationOptions.filterColumns = this.grid.columns;
						$scope.setFormCollectionData('playlistsGrid','playlists', $scope.playlistsPaginationOptions);
	        		});
			}
	    };

	
	
	// Define All Verified Medias table
	$scope.mediasPaginationOptions = {pageNumber: 1, pageSize: 10, sortColumns: [], filterColumns: [{field:'verified', filters:[{term:'true'}]}]};
	$scope.mediasGrid = {
			rowHeight:40,
		    enableSelectAll: true,
	        paginationPageSizes: [5, 10, 20, 50],
	        paginationPageSize: $scope.mediasPaginationOptions.pageSize,
	        enableColumnMenus:false,
	    	useExternalPagination: true,
			useExternalSorting: true,
			enableFiltering: true,
			useExternalFiltering: true,
	        columnDefs: [	{ field: 'thumbUrl', name: 'Thumbnail', cellTemplate: thumbnailHTML, enableFiltering: false, minWidth:100 },
							{ field: 'name', name: 'Name', minWidth:100},
							{ field: 'category', name: 'Category', minWidth:100},
							{ field: 'type', name: 'Type', minWidth:100}] ,
	        onRegisterApi: function(gridApi) {
	           	$scope.mediasGridApi = gridApi;
	           	$scope.mediasGridApi.pagination.on.paginationChanged(
	             	$scope, function (newPage, pageSize) {
				     	$scope.mediasPaginationOptions.pageNumber = newPage;
		     		 	$scope.mediasPaginationOptions.pageSize = pageSize;
						$scope.setFormCollectionData('mediasGrid','medias', $scope.mediasPaginationOptions);
				 	});
				$scope.mediasGridApi.core.on.sortChanged(
					$scope, function (grid, sortColumns) {
						$scope.mediasPaginationOptions.sortColumns = sortColumns;
						$scope.setFormCollectionData('mediasGrid','medias', $scope.mediasPaginationOptions);
				 	});
		        $scope.mediasGridApi.core.on.filterChanged(
		        	$scope, function() {
		        		$scope.mediasPaginationOptions.filterColumns = this.grid.columns;
						$scope.setFormCollectionData('mediasGrid','medias', $scope.mediasPaginationOptions);
	        		});
			}
	    };
	
	// Define a function to set Form Grid Options data (ALL collection)
	$scope.setFormCollectionData = function(options, target, pOptions) {
		
		     $scope.getCollectionData(
				options,
				target, 
				pOptions.pageNumber, 
				pOptions.pageSize, 
				pOptions.sortColumns,
				pOptions.filterColumns,
				[]);                            
		 };
		
	// Define Selected Displays table  (for create/edit and view/delete)
	$scope.selectedDisplaysPaginationOptions = {pageNumber: 1, pageSize: 10, sortColumns: [], filterColumns: []};
	$scope.selectedDisplaysGrid = {
			rowHeight:40,
		    enableSelectAll: true,
	        paginationPageSizes: [5, 10, 20, 50],
	        paginationPageSize: $scope.selectedDisplaysPaginationOptions.pageSize,
		    enableColumnMenus:false,
		    useExternalPagination: true,
			useExternalSorting: true,
			enableFiltering: true,
			useExternalFiltering: false,
		    columnDefs: [	{ field: 'id', name: 'id', minWidth:100 },
							{ field: 'name', name: 'Name', minWidth:100 },
							{ field: 'area', name: 'area', minWidth:100}],
			onRegisterApi: function(gridApi) {
								$scope.selectedDisplaysGridApi = gridApi;
							}
								
		};
	
	$scope.viewSelectedDisplaysGrid = {
			rowHeight:40,
		    enableSelectAll: true,
	        paginationPageSizes: [5, 10, 20, 50],
	        paginationPageSize: $scope.selectedDisplaysPaginationOptions.pageSize,
		    enableColumnMenus:false,
		    useExternalPagination: true,
			useExternalSorting: true,
			enableFiltering: true,
			useExternalFiltering: false,
		    columnDefs: [	{ field: 'id', name: 'id', minWidth:100 },
							{ field: 'name', name: 'Name', minWidth:100 },
							{ field: 'area', name: 'area', minWidth:100}],
			onRegisterApi: function(gridApi) {
								$scope.viewSelectedDisplaysGridApi = gridApi;
							}
								
		};
	

	// Define Selected Playlists table (for create/edit and view/delete)
	$scope.selectedPlaylistsPaginationOptions = {pageNumber: 1, pageSize: 10, sortColumns: [], filterColumns: []};
	$scope.selectedPlaylistsGrid = {
			rowHeight:40,
		    enableSelectAll: true,
	        paginationPageSizes: [5, 10, 20, 50],
	        paginationPageSize: $scope.selectedPlaylistsPaginationOptions.pageSize,
		    enableColumnMenus:false,
		    useExternalPagination: true,
			useExternalSorting: true,
			enableFiltering: true,
			useExternalFiltering: false,
		    columnDefs: [	{ field: 'id', name: 'id', minWidth:100 },
							{ field: 'name', name: 'Name', minWidth:100},
							{ field: 'duration', name: 'Duration', minWidth:100}] ,
			onRegisterApi: function(gridApi) {
								$scope.selectedPlaylistsGridApi = gridApi;
							}
								
		};
	
	$scope.viewSelectedPlaylistsGrid = {
			rowHeight:40,
		    enableSelectAll: true,
	        paginationPageSizes: [5, 10, 20, 50],
	        paginationPageSize: $scope.selectedPlaylistsPaginationOptions.pageSize,
		    enableColumnMenus:false,
		    useExternalPagination: true,
			useExternalSorting: true,
			enableFiltering: true,
			useExternalFiltering: false,
		    columnDefs: [	{ field: 'id', name: 'id', minWidth:100 },
							{ field: 'name', name: 'Name', minWidth:100},
							{ field: 'duration', name: 'Duration', minWidth:100}] ,
			onRegisterApi: function(gridApi) {
								$scope.viewSelectedPlaylistsGridApi = gridApi;
							}
								
		};
	
	// Define reorder buttons and functions for Playlist Spots
	var moveUpButtonHTML = '<button ng-click="grid.appScope.moveUpRow(row)" type="button" class="btn btn-sm btn-primary ml-1"> <i class="fa fa-arrow-up fa-fw"></i></button>';
	var moveDownButtonHTML = '<button ng-click="grid.appScope.moveDownRow(row)" type="button" class="btn btn-sm btn-primary ml-1"> <i class="fa fa-arrow-down fa-fw"></i></button>';
	var reorderButtonsHTML = '<div class="m-1">' + moveUpButtonHTML + moveDownButtonHTML + '</div' ; 
	
	$scope.moveUpRow = function(row){
		if(row.grid.rows.length > 1){
			let index = row.grid.rows.indexOf(row);
			if( index >0){
				let obj = $scope.selectedSpotsGrid.data[index];
				$scope.selectedSpotsGrid.data[index] = $scope.selectedSpotsGrid.data[index-1];
				$scope.selectedSpotsGrid.data[index-1] = obj;
				
				$scope.recalculateOrder();
				$scope.selectedSpotsGridApi.core.handleWindowResize();
			}			
		}
	};
	
	$scope.moveDownRow = function(row){
		if(row.grid.rows.length > 1){
			let index = row.grid.rows.indexOf(row);
			if( index < row.grid.rows.length -1){
				let obj = $scope.selectedSpotsGrid.data[index];
				$scope.selectedSpotsGrid.data[index] = $scope.selectedSpotsGrid.data[index+1];
				$scope.selectedSpotsGrid.data[index+1] = obj;
				
				$scope.recalculateOrder();
				$scope.selectedSpotsGridApi.core.handleWindowResize();
			}			
		}
	};
	
	$scope.recalculateOrder = function(){
		$scope.selectedSpotsGrid.data.forEach(function(entity){
			entity.playOrder = $scope.selectedSpotsGrid.data.indexOf(entity);
		});
	};
	
	// Define HTML template for spots thumbnails
	var spotThumbnailHTML = '<img ng-src="{{row.entity.media.thumbUrl}}" alt="No Image Found" class="m-1" height="80%" >'

	// Define Selected Spots table (for create/edit and view/delete)
	$scope.selectedSpotsPaginationOptions = {pageNumber: 1, pageSize: 10, sortColumns: [], filterColumns: []};
	$scope.selectedSpotsGrid = {
			rowHeight:40,
		    enableSelectAll: true,
	        paginationPageSizes: [5, 10, 20, 50],
	        paginationPageSize: $scope.selectedSpotsPaginationOptions.pageSize,
	        enableColumnMenus:false,
	    	useExternalPagination: true,
			useExternalSorting: true,
			enableFiltering: true,
			useExternalFiltering: false,
			columnDefs: [	{ field: 'media.thumbUrl', name: 'Thumbnail', cellTemplate: spotThumbnailHTML, enableFiltering: false, enableCellEdit: false, minWidth:100},
							{ field: 'media.name', name: 'Name', enableCellEdit: false, minWidth:100},
							{ field: 'media.category', name: 'Category', enableCellEdit: false, minWidth:100},
							{ field: 'media.type', name: 'Type', enableCellEdit: false, minWidth:100},
							{ field: 'duration', name: 'Duration', enableCellEdit: true, type: 'number', minWidth:100},
							{ field: 'playOrder', name: 'Play Order', cellTemplate: reorderButtonsHTML, enableFiltering: false, minWidth:100}],
			onRegisterApi: function(gridApi) {
								$scope.selectedSpotsGridApi = gridApi;
							}
	    };	
	
	$scope.viewSelectedSpotsGrid = {
			rowHeight:40,
		    enableSelectAll: true,
	        paginationPageSizes: [5, 10, 20, 50],
	        paginationPageSize: $scope.selectedSpotsPaginationOptions.pageSize,
	        enableColumnMenus:false,
	    	useExternalPagination: true,
			useExternalSorting: true,
			enableFiltering: true,
			useExternalFiltering: false,
			columnDefs: [	{ field: 'media.thumbUrl', name: 'Thumbnail', cellTemplate: spotThumbnailHTML, enableFiltering: false, enableCellEdit: false, minWidth:100},
							{ field: 'media.name', name: 'Name', enableCellEdit: false, minWidth:100},
							{ field: 'media.category', name: 'Category', enableCellEdit: false, minWidth:100},
							{ field: 'media.type', name: 'Type', enableCellEdit: false, minWidth:100},
							{ field: 'duration', name: 'Duration', enableCellEdit: true, type: 'number', minWidth:100}],
			onRegisterApi: function(gridApi) {
								$scope.viewSelectedSpotsGridApi = gridApi;
							}
	    };	

	
	

	
	// Define a function to set Form Grid Options data (Selected collection)
	$scope.setFormSelectionData = function(options, target, linkedType) {
			
		CRUDService.getLinkedObjects(target)
				.success(function(data){
						$scope[options].data = data._embedded[linkedType];
						// Specific to playlists spots rendering
						if(linkedType=='spots'){
							
							//sort by playOrder
							$scope[options].data.sort(function(a, b) {
							    return parseFloat(a.playOrder) - parseFloat(b.playOrder);
							});
							
							//fetch and populate media info
							$scope[options].data.forEach(function(spot){
								spot.actionLink = CRUDService.getRelativePath(spot._links.self.href);
								
								CRUDService.getOne(CRUDService.getRelativePath(spot._links.media.href))
									.success(function(media){
										spot.media = media;
									});								
							});
						}

		         })
				.error(function(data){
					console.log("nothing found on : " + target );
				});                                                      
		 };		

	// Program CreateEditModal filling and ViewDeleteModal refreshing
	if($scope.targetObject == 'program'){
		// Get Form Grids Data from REST Api on create/edit modal show
		angular.element(createEditObjectModal).on('shown.bs.modal', function (e) {
			$scope.setFormCollectionData('displaysGrid','displays', $scope.displaysPaginationOptions);
			$scope.setFormCollectionData('playlistsGrid','playlists', $scope.playlistsPaginationOptions);

			// refesh grids
			$scope.displaysGridApi.core.handleWindowResize();
			$scope.playlistsGridApi.core.handleWindowResize();
			$scope.selectedDisplaysGridApi.core.handleWindowResize();
			$scope.selectedPlaylistsGridApi.core.handleWindowResize();
			});

		// Remove Form Grids Data from REST Api on create/edit modal hide
		angular.element(createEditObjectModal).on('hidden.bs.modal', function (e) {
			$scope.displaysGrid.data = [];
			$scope.playlistsGrid.data = [];
			$scope.selectedDisplaysGrid.data = [];
			$scope.selectedPlaylistsGrid.data = [];
			});
		
		// Refresh selected Grids on view/delete modal show
		angular.element(viewDeleteObjectModal).on('shown.bs.modal', function (e) {
			$scope.viewSelectedDisplaysGridApi.core.handleWindowResize();
			$scope.viewSelectedPlaylistsGridApi.core.handleWindowResize();
			});
		
		// Remove Form Grids Data from REST Api on view/delete modal hide
		angular.element(viewDeleteObjectModal).on('hidden.bs.modal', function (e) {
			$scope.viewSelectedDisplaysGrid.data = [];
			$scope.viewSelectedPlaylistsGrid.data = [];
			});

	}

	// Playlist CreateEditModal filling and ViewDeleteModal refreshing
	if($scope.targetObject == 'playlist'){
		// Get Form Grids Data from REST Api on create/edit modal show
		angular.element(createEditObjectModal).on('shown.bs.modal', function (e) {
			$scope.setFormCollectionData('mediasGrid','medias', $scope.mediasPaginationOptions);

			// refesh grids
			$scope.mediasGridApi.core.handleWindowResize();
			$scope.selectedSpotsGridApi.core.handleWindowResize();
			});

		// Remove Form Grids Data from REST Api on create/edit modal hide
		angular.element(createEditObjectModal).on('hidden.bs.modal', function (e) {
			$scope.mediasGrid.data = [];
			$scope.selectedSpotsGrid.data = [];
			});
		
		// Refresh selected Grids on view/delete modal show
		angular.element(viewDeleteObjectModal).on('shown.bs.modal', function (e) {
			$scope.viewSelectedSpotsGridApi.core.handleWindowResize();
			});
		
		// Remove Form Grids Data from REST Api on view/delete modal hide
		angular.element(viewDeleteObjectModal).on('hidden.bs.modal', function (e) {
			$scope.viewSelectedSpotsGrid.data = [];
			});
	}

	// Define functions for select/de-select display and medias
	$scope.addSelectedObjects = function(object, sourceGridApi, targetGrid){

			if(sourceGridApi.selection.getSelectedCount() > 0){
				sourceGridApi.selection.getSelectedRows().forEach(function(obj){
					if(targetGrid.data.filter(m => m.name == obj.name).length < 1){
						if(object == 'medias'){
							var spot = {};
							spot.duration = 5;
							spot.media = obj;
							obj = spot;
						}
						targetGrid.data.push(obj);
					}
						
				});
				sourceGridApi.selection.clearSelectedRows();
				if(object == 'medias')
					$scope.recalculateOrder();
			}	
	};
	
	$scope.removeSelectedObjects = function(object, targetGridApi, targetGrid){

			if(targetGridApi.selection.getSelectedCount() > 0){
				targetGridApi.selection.getSelectedRows().forEach(function(obj){
					targetGrid.data.splice(
							targetGrid.data.indexOf(obj), 1);
				});
				
				targetGridApi.selection.clearSelectedRows();
				if(object == 'medias')
					$scope.recalculateOrder();
			}
	};

	// Define a function to create or update or delete program (displays and
	// playlists) and playlist (medias and spots) (via links)
	$scope.addLinkedObjects = function(target, selected, callback){

		if(selected.length > 0){
				CRUDService.setLinkedObjects(target, $scope.selectedToLinks(selected)).success(function(data){
					console.log("set linked objects: " + $scope.selectedToLinks(selected) + "\n" + "on link: " + target);
					createAlert(main_nav, 'success', 'successfully set '+ $scope.targetObject +' linked objects', 5000);
					callback();
				})
				.error(function(data){
					createAlert(main_nav, 'danger', 'an issue occured setting '+ $scope.targetObject +' linked objects <br> reason: '
							+((typeof data.message == 'undefined') ? data.toString() : data.message), null);
				});
			}								
	};
	
	$scope.updateLinkedObjects = function(target, selected, callback){
			
			if(selected.length > 0){
				CRUDService.setLinkedObjects(target, $scope.selectedToLinks(selected)).success(function(data){
					console.log("set linked objects: " + $scope.selectedToLinks(selected) + "\n" + "on link: " + target);
					callback();
				})
				.error(function(data){
					createAlert(main_nav, 'danger', 'an issue occured setting '+ $scope.targetObject +' linked objects <br> reason: '
							+((typeof data.message == 'undefined') ? data.toString() : data.message), null);
				});
				
			}else{
				CRUDService.deleteLinkedObjects(target).success(function(data){
					console.log("deleted linked objects on link: " + target);
					callback();
				})
				.error(function(data){
					createAlert(main_nav, 'danger', 'an issue occured deleting '+ $scope.targetObject +' linked objects <br> reason: '
							+((typeof data.message == 'undefined') ? data.toString() : data.message), null);
				});
			}
					
	};

	// Utility function to transform selected list into string
	$scope.selectedToLinks = function(selectedObjects){
		var str ='';
		selectedObjects.forEach(function(obj){
			str = str + obj._links.self.href + "\n";
		});
		return str;
	};
	



	// --------------------------------------------------------------------
	// END OF SPECIFIC
	// --------------------------------------------------------------------

}]);











// SERVICE LAYER
// --------------------------------------------------------
// list of CRUD calls
app.service('CRUDService',['$http', function ($http) {
    
	    function getScheme(target) {
	        return $http({
	          method: 'GET',
	            url: '/api/schema/'+target,
	            headers: {'Accept' : 'application/schema+json'}
	        });
	    };
	    
	    function getAll(target, page, size, sortCols, filterCols, specificFilters) {
	        page = page > 0?page - 1:0;
	        // build sort string
	        var sort = '';
	        angular.forEach(sortCols, function(value, key) {
				  sort = sort + '&sort=' + value.field + ',' + value.sort.direction;
				});
	        // build filter string
	        var filter = '';
	        angular.forEach(filterCols, function(value, key) {
				  if(value.filters[0].term != null)
				  	filter = filter + '&' + value.field + '=' + value.filters[0].term;
				});
	        angular.forEach(specificFilters, function(value, key) {
				  if(value != null)
				  	filter = filter + '&' + key + '=' + value;
				});
	        return $http({
	          method: 'GET',
	            url: '/api/'+target+'/search/customFilters?page='+page+'&size='+size+sort+filter
	        });
	    };
	    
	    function getOne(url) {
	        return $http({
	          method: 'GET',
	            url: url
	        });
	    };
	    
	    function createOne(target, data) {
	    	if(target=='medias'){
	    		var formData = new FormData();
	    		formData.append("name", data.name);
	    		formData.append("category", data.category);
	    		formData.append("verified", data.verified);
	    		formData.append("file", data.file);
	    		formData.append("thumbFile", data.thumbFile);
		        return $http({
			          method: 'POST',
			          	url: '/api/medias/uploads',
			            data: formData,
			            transformRequest: angular.identity,
			            headers: {'Content-Type': undefined}
			        });
	    	}
	    	
	    	if(target=='playlists'
	    		|| target=='programs'){
		        return $http({
			          method: 'POST',
			          	url: '/api/'+target+'/creates',
			            data: data
			        });	
	    	}
	    		
	    		
	        return $http({
	          method: 'POST',
	          	url: '/api/'+target,
	            data: data
	        });
	    };
	
	    function updateOne(url, data) {
	    	if(url.includes('/api/medias')){
	    		var formData = new FormData();
	    		formData.append("id", data.id);
	    		formData.append("name", data.name);
	    		formData.append("category", data.category);
	    		formData.append("verified", data.verified);
	    		formData.append("file", data.file); 
	    		formData.append("thumbFile", data.thumbFile);
		        return $http({
			          method: 'POST',
			          	url: '/api/medias/updates',
			            data: formData,
			            transformRequest: angular.identity,
			            headers: {'Content-Type': undefined}
			        });	
	    	}

	    	if(url.includes('/api/playlists')){
		        return $http({
			          method: 'POST',
			          	url: '/api/playlists/updates',
			            data: data
			        });	
	    	}
	    	
	    	if(url.includes('/api/programs')){
		        return $http({
			          method: 'POST',
			          	url: '/api/programs/updates',
			            data: data
			        });	
	    	}	
	    	
	        return $http({
	          method: 'PATCH',
	            url: url,
	            data: data
	        });
	    };
	
	    function deleteOne(url) {
	        return $http({
	          method: 'DELETE',
	            url: url
	        });
	    };
	    
	    function getLinkedObjects(fullTarget) {
	        return $http({
	          method: 'GET',
	            url: getRelativePath(fullTarget)
	        });
	    };
	    
	    function setLinkedObjects(fullTarget, links) {
	        return $http({
	          method: 'PUT',
	            url: getRelativePath(fullTarget),
	            data: links,
	            headers: {'Content-Type' : 'text/uri-list'}
	        });
	    };
	    
	    function deleteLinkedObjects(fullTarget) {
	        return $http({
	          method: 'DELETE',
	            url: getRelativePath(fullTarget)
	        });
	    };
	    
	    // tool to go from URL to URI
	    function getRelativePath(absolutePath){
	    	    var l = document.createElement("a");
	    	    l.href = absolutePath;
	    	    var relativePath = l.pathname;
	    	    delete l;
	    	    return relativePath;
	    };
	    
	    return {
	    		getScheme: 						getScheme,
	    		getAll:							getAll,
	    		getOne: 						getOne,
	    		createOne: 						createOne,
	    		updateOne: 						updateOne,
	    		deleteOne: 						deleteOne,
	    		getLinkedObjects:				getLinkedObjects,
	    		setLinkedObjects:				setLinkedObjects,
	    		deleteLinkedObjects:			deleteLinkedObjects,
	    		getRelativePath:				getRelativePath,

	    };
    
}]);
