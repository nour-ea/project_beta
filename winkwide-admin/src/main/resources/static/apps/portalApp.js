var app = angular.module('portalApp', ['ui.grid','ui.grid.pagination', 'ui.grid.selection', 'ui.grid.exporter']);


// CRUD CONTROLLER
// -------------------------------------------------------------------------
// All mighty controller
app.controller('crudCtrl', ['$scope','objectModel', 'CRUDService', 
	    function ($scope, objectModel, CRUDService) {
		
		// Get current path
		$scope.currentPath = window.location.pathname;
		// Define the Object Target for the CRUD App (Display, Media...)
		$scope.targetObject = objectModel;
		$scope.targetCollection = $scope.targetObject + 's';
		
		// Define columnList object (for the table column titles)
		$scope.columnList = {};
		
		// Define Pagination options & Specific filters for GetAll Request to fill the UI Grid  
		$scope.paginationOptions = {pageNumber: 1, pageSize: 20, sortColumns: [], filterColumns: []};
		$scope.specificFilters = {};
				
		// Define Edit / Delete target object url link and fill $scope.formData
		var targetObjectUrl = '/api/'+$scope.targetCollection;
		$scope.schema = {};
		$scope.formData = {};
		$scope.isCreateModalType = true; //to differentiate create and edit modal

		$scope.setFormData = function(url, operation){
			if(operation == 'edit' || operation == 'delete') {
				targetObjectUrl = url;
				console.log('setting target object url');
				CRUDService.getOne(url).success(function(data){
					$scope.formData = data;
					console.log('setting form data');
					
					//SPECIFIC for program creation and edition
					if($scope.targetObject == 'program'){
						$scope.setFormSelectionData('selectedDisplaysGrid',$scope.formData._links.display.href, 'display');
						$scope.setFormSelectionData('selectedMediasGrid',$scope.formData._links.medias.href, 'medias');
					}
					//------------------------------------------
				});
			}
			
			//check if createEditModal is of type Create
			$scope.isCreateModalType = (operation == 'create'); 
			//set clear-formData-on-hide if the modal is of type edit or delete
			if(operation == 'edit')
				angular.element(createEditObjectModal).on('hidden.bs.modal', function (e) {
					$scope.clearFormData();
				});
			if(operation == 'delete')
				angular.element(deleteObjectModal).on('hidden.bs.modal', function (e) {
					$scope.clearFormData();
				});
		};
		

		// Define HTML template for edition buttons
		var viewButtonHTML = '<button type="button" class="btn btn-sm btn-primary ml-1" disabled><i class="fa fa-eye fa-fw"></i></button>';
		var editButtonHTML = '<button ng-click="grid.appScope.setFormData(row.entity.actionLink, \'edit\')" type="button" class="btn btn-sm btn-secondary ml-1" data-toggle="modal" data-target="#createEditObjectModal" > <i class="fa fa-pencil fa-fw"></i></button>';
		var deleteButtonHTML = '<button ng-click="grid.appScope.setFormData(row.entity.actionLink, \'delete\')" type="button" class="btn btn-sm btn-danger ml-1" data-toggle="modal" data-target="#deleteObjectModal" > <i class="fa fa-remove fa-fw"></i></button>';
		var actionButtonsHTML = '<div class="m-1">' + viewButtonHTML + editButtonHTML + deleteButtonHTML + '</div>';
		
		// Define HTML template for media thumbnails
		var thumbnailHTML = '<img src="{{row.entity.url}}" alt="No Image Found" class="m-1" height="80%" >'
		
		// Define a function to Get Data Scheme from REST Api
		$scope.getColumnList = function(){
			var columnList = [];
			CRUDService.getScheme($scope.targetObject).success(function(data){
				$scope.schema = data;
				
				//SPECIFIC for programs and reports list (as they have related objects : display and media)
				angular.forEach(data, function(value, key) {
					if(value.type=='Display')
						this.push({ field: 'display' , name: 'Display', enableFiltering:false });
					if(value.type=='Media')
						this.push({ field: 'media' , name: 'Media', enableFiltering:false });
				}, columnList);
				// ---------------------------------------
				
				angular.forEach(data, function(value, key) {
					
					//SPECIFIC for media thumbnail
					if(value.name=='url' && $scope.targetObject == 'media')
						this.push({ field: value.name , name: 'Preview', cellTemplate: thumbnailHTML, enableFiltering:false });
					// ---------------------------------------
					//enabling filtering only for string fields
					else if(value.type=='String')
						this.push({ field: value.name , name: value.title, enableFiltering:true });
					else if( ['boolean', 'int', 'Long', 'BigDecimal', 'Date'].indexOf(value.type) !== -1)
						this.push({ field: value.name , name: value.title, enableFiltering:false });

					}, columnList);
				
				// create an Actions column (view, edit, delete)
				columnList.push({ field: 'actionLink', name: 'Actions', cellTemplate: actionButtonsHTML, enableFiltering: false, pinnedRight:true, width:130 });
				
			});
			
			$scope.columnList = columnList;
		};
		
		$scope.getDisplayName = function(target){
			return CRUDService.getLinkedObjects(target).name
		};

		// Define a function to Get Data Collection from REST Api and put it in some grid options
		$scope.getCollectionData = function(options, target, page, size, sortCols, filterCols, specificFilters) {
			
		     CRUDService.getAll(target, page, size, sortCols, filterCols, specificFilters)
				.success(function(data){
										
		          	$scope[options].data = data._embedded[target];
		
					angular.forEach($scope[options].data, function(value, key) {
						value.actionLink =  CRUDService.getRelativePath(value._links.self.href);
						
						//Specific to programs and reports (getting display.id and media.id)
						if(target=='programs' || target=='reports'){					
							CRUDService.getLinkedObjects(value._links.display.href)
								.success(function(display){
									value.display = display.id;
							});
						}
						if(target=='reports'){					
							CRUDService.getLinkedObjects(value._links.media.href)
								.success(function(media){
									value.media = media.id;
							});
						}
						//------
						
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
		
		// Build columnList (titles of the table columns)
		$scope.getColumnList();
		
		// Get Grid Data from REST Api
		$scope.setGridData();
	 	
		// Define UI grid options & define update function
	    $scope.mainGrid = {
			rowHeight: 40,
		    enableGridMenu: true,
		    enableSelectAll: true,
		    exporterExcelFilename: 'export.xlsx',
		    exporterExcelSheetName: 'Sheet1',
	        paginationPageSizes: [5, 10, 20, 50, 100, 1000],
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
	
	//Define the Create function
	$scope.createTargetObject = function(){			
		CRUDService.createOne($scope.targetCollection, $scope.formData).success(function(data){
			
			//SPECIFIC for program creation
			if($scope.targetObject == 'program'){
				$scope.addLinkedObjects(data._links.display.href, $scope.selectedDisplaysGrid.data);
				$scope.addLinkedObjects(data._links.medias.href, $scope.selectedMediasGrid.data);
			}
			//------------------------------
			
			console.log('created object');
			$scope.setGridData();
			angular.element(createEditObjectModal).modal('hide');
		});
	};
	
	//Define the Edit function
	$scope.updateTargetObject = function(){			
		CRUDService.updateOne(targetObjectUrl, $scope.formData).success(function(data){
			
			//SPECIFIC for program update
			if($scope.targetObject == 'program'){
				$scope.updateLinkedObjects($scope.formData._links.display.href, $scope.selectedDisplaysGrid.data);
				$scope.updateLinkedObjects($scope.formData._links.medias.href, $scope.selectedMediasGrid.data);
			}
			//----------------------------
			
			console.log('edited object');
			$scope.clearFormData();
			$scope.setGridData();
			angular.element(createEditObjectModal).modal('hide');
		});
	};
	
	//Define the Delete function
	$scope.deleteTargetObject = function(){			
		CRUDService.deleteOne(targetObjectUrl).success(function(data){
			console.log('deleted object');
			$scope.clearFormData();
			$scope.setGridData();
			angular.element(deleteObjectModal).modal('hide');
		});
	};

	//Define the Clean form function
	$scope.clearFormData = function(){			
		$scope.formData = {};
		$scope.objectForm.$setPristine();
	};

	


	
	
	
	
	
	
	
	
	// ----------------------------------------------------------------------
	// ----------------------------------------------------------------------
	// START OF SPECIFIC
	// ----------------------------------------------------------------------
	// ----------------------------------------------------------------------
	
	//Specific to Media File Upload
	$scope.previewFile = function (input) { 
				$scope.formData.file    = input.files[0];
				
			    var reader = new FileReader();
			    reader.onload = function(e) {
			    	angular.element(mediaPreview).attr("src", e.target.result);
				}
			    reader.readAsDataURL(input.files[0]);

	};
	
	
	
	
	// Specific UI tables and data for programs displays and media management
	
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
	        columnDefs: [	{ field: 'name', name: 'Name'},
							{ field: 'smart', name: 'smart', enableFiltering: false}] ,
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
		
	// Define All Medias table
	$scope.mediasPaginationOptions = {pageNumber: 1, pageSize: 10, sortColumns: [], filterColumns: []};
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
	        columnDefs: [	{ field: 'name', name: 'Name'},
							{ field: 'mediaType', name: 'Media type'},
							{ field: 'url', name: 'Preview', cellTemplate: thumbnailHTML, enableFiltering: false }] ,
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
		
	// Define Selected Displays table
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
		    columnDefs: [	{ field: 'name', name: 'Name'},
							{ field: 'smart', name: 'smart', enableFiltering: false}],
			onRegisterApi: function(gridApi) {
								$scope.selectedDisplaysGridApi = gridApi;
							}
								
		};
		
	// Define Selected Medias table
	$scope.selectedMediasPaginationOptions = {pageNumber: 1, pageSize: 10, sortColumns: [], filterColumns: []};
	$scope.selectedMediasGrid = {
			rowHeight:40,
		    enableSelectAll: true,
	        paginationPageSizes: [5, 10, 20, 50],
	        paginationPageSize: $scope.selectedMediasPaginationOptions.pageSize,
	        enableColumnMenus:false,
	    	useExternalPagination: true,
			useExternalSorting: true,
			enableFiltering: true,
			useExternalFiltering: false,
			columnDefs: [	{ field: 'name', name: 'Name'},
							{ field: 'mediaType', name: 'Media type'},
							{ field: 'url', name: 'Preview', cellTemplate: thumbnailHTML, enableFiltering: false }],
			onRegisterApi: function(gridApi) {
								$scope.selectedMediasGridApi = gridApi;
							}
	    };	

	// Define a function to set Form Grid Options data (Selected collection)
	$scope.setFormSelectionData = function(options, target, linkedType) {
			
		CRUDService.getLinkedObjects(target)
				.success(function(data){
					if(linkedType == 'display')
						$scope[options].data = [];
		          		$scope[options].data.push(data);
					if(linkedType == 'medias')
						$scope[options].data = data._embedded.medias;
		         })
				.error(function(data){
					console.log("nothing found on : " + target );
				});                                                      
		 };		
		
	//Program CreateEditmodal filling
	if($scope.targetObject == 'program'){
		// Get Form Grids Data from REST Api on modal show
		angular.element(createEditObjectModal).on('shown.bs.modal', function (e) {
			$scope.setFormCollectionData('displaysGrid','displays', $scope.displaysPaginationOptions);
			$scope.setFormCollectionData('mediasGrid','medias', $scope.mediasPaginationOptions);
			
			//refesh grids
			$scope.displaysGridApi.core.handleWindowResize();
			$scope.mediasGridApi.core.handleWindowResize();
			$scope.selectedDisplaysGridApi.core.handleWindowResize();
			$scope.selectedMediasGridApi.core.handleWindowResize();
			});
		
		// Remove Form Grids Data from REST Api on modal hide
		angular.element(createEditObjectModal).on('hidden.bs.modal', function (e) {
			$scope.displaysGrid.data = [];
			$scope.mediasGrid.data = [];
			$scope.selectedDisplaysGrid.data = [];
			$scope.selectedMediasGrid.data = [];
			});
	}
	
	// Define functions for select/de-select display and medias
	$scope.addSelectedObjects = function(object){
		
		if(object == 'display'){
			if($scope.displaysGridApi.selection.getSelectedCount() == 1){
				$scope.displaysGridApi.selection.getSelectedRows().forEach(function(display){
					if($scope.selectedDisplaysGrid.data.filter(d => d.name == display.name).length < 1){
						$scope.selectedDisplaysGrid.data = [];
						$scope.selectedDisplaysGrid.data.push(display);
					}
				});
				$scope.displaysGridApi.selection.clearSelectedRows();
			}

				
		}
		
		if(object == 'medias'){
			if($scope.mediasGridApi.selection.getSelectedCount() > 0){
				$scope.mediasGridApi.selection.getSelectedRows().forEach(function(media){
					if($scope.selectedMediasGrid.data.filter(m => m.name == media.name).length < 1)
						$scope.selectedMediasGrid.data.push(media);
				});
				$scope.mediasGridApi.selection.clearSelectedRows();
			}
		}		
	};
	
	$scope.removeSelectedObjects = function(object){
		
		if(object == 'display'){
			if($scope.selectedDisplaysGridApi.selection.getSelectedCount() > 0){
				$scope.selectedDisplaysGridApi.selection.getSelectedRows().forEach(function(display){
						$scope.selectedDisplaysGrid.data.splice(
								$scope.selectedDisplaysGrid.data.indexOf(display), 1);
				});
			$scope.selectedDisplaysGridApi.selection.clearSelectedRows();
			}

				
		}
		
		if(object == 'medias'){
			if($scope.selectedMediasGridApi.selection.getSelectedCount() > 0){
				$scope.selectedMediasGridApi.selection.getSelectedRows().forEach(function(media){
						$scope.selectedMediasGrid.data.splice(
								$scope.selectedMediasGrid.data.indexOf(media), 1);
				});
			$scope.selectedMediasGridApi.selection.clearSelectedRows();
			}
				
		}
	};
	
	
	// Define a function to create or update or delete program display or medias (via links)
	$scope.addLinkedObjects = function(target, selected){

		if(selected.length > 0){
				CRUDService.setLinkedObjects(target, $scope.selectedToLinks(selected)).success(function(data){
					console.log("set linked objects: " + $scope.selectedToLinks(selected) + "\n" + "on link: " + target);
				});
			}								
	};
	
	$scope.updateLinkedObjects = function(target, selected){
			
			if(selected.length > 0){
				CRUDService.setLinkedObjects(target, $scope.selectedToLinks(selected)).success(function(data){
					console.log("set linked objects: " + $scope.selectedToLinks(selected) + "\n" + "on link: " + target);
				});
			}else{
				CRUDService.deleteLinkedObjects(target).success(function(data){
					console.log("deleted linked objects on link: " + target);
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
//list of CRUD calls
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
	        //build sort string
	        var sort = '';
	        angular.forEach(sortCols, function(value, key) {
				  sort = sort + '&sort=' + value.field + ',' + value.sort.direction;
				});
	        //build filter string
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
	    		formData.append("mediaType", data.mediaType);
	    		formData.append("file", data.file); 
		        return $http({
			          method: 'POST',
			          	url: '/api/medias/uploads',
			            data: formData,
			            transformRequest: angular.identity,
			            headers: {'Content-Type': undefined}
			        });
	    	}
	    		
	    		
	        return $http({
	          method: 'POST',
	          	url: '/api/'+target,
	            data: data
	        });
	    };
	
	    function updateOne(url, data) {
	    	if(url.indexOf('/api/medias') !== -1){
	    		var formData = new FormData();
	    		formData.append("name", data.name);
	    		formData.append("mediaType", data.mediaType);
	    		formData.append("file", data.file); 
		        return $http({
			          method: 'POST',
			          	url: '/api/medias/updates',
			            data: formData,
			            transformRequest: angular.identity,
			            headers: {'Content-Type': undefined}
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
	            headers: {'Content-type' : 'text/uri-list'}
	        });
	    };
	    
	    function deleteLinkedObjects(fullTarget) {
	        return $http({
	          method: 'DELETE',
	            url: getRelativePath(fullTarget)
	        });
	    };
	    
	    //tool to go from URL to URI
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
