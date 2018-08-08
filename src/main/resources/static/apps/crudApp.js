var app = angular.module('crudApp', ['ui.grid','ui.grid.pagination', 'ui.grid.selection', 'ui.grid.exporter']);

// CRUD CONTROLLER
// -------------------------------------------------------------------------
// All mighty controller
app.controller('crudCtrl', ['$scope','objectModel', 'CRUDService', 
	    function ($scope, objectModel, CRUDService) {
		
		//current path
		$scope.currentPath = window.location.pathname;
		
		// Define the Object Target for the CRUD App (Display, Media...)
		$scope.objectTarget = objectModel;
		$scope.collectionTarget = $scope.objectTarget + 's';
		
		// Define Pagination options & Specific filters for GetAll Request to fill the UI Grid  
		$scope.paginationOptions = {pageNumber: 1, pageSize: 10, sortColumns: [], filterColumns: []};
		$scope.specificFilters = {};
				
		// Define Edit / Delete target object url link and fill $scope.formData
		var targetObjectUrl = '/api/'+$scope.collectionTarget;
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
					if($scope.objectTarget == 'program'){
						$scope.setFormSelectionData('selectedDisplaysGridOptions',$scope.formData._links.display.href, 'display');
						$scope.setFormSelectionData('selectedMediasGridOptions',$scope.formData._links.medias.href, 'medias');
					}
					//------------------------------------------
				});
			}
			
			//check if createEditModal is of type Create
			$scope.isCreateModalType = (operation == 'create'); 
			//set clear-formData-on-hide if the modal is of type edit or delete
			if(operation == 'edit')
				angular.element(createEditObjectModal).on('hide.bs.modal', function (e) {
					$scope.cleanFormData();
				});
			if(operation == 'edit')
				angular.element(deleteObjectModal).on('hide.bs.modal', function (e) {
					$scope.cleanFormData();
				});
		};
		

		// Define HTML for edition buttons
		var viewButtonHTML = '<button type="button" class="btn btn-sm btn-primary ml-1" ><i class="fa fa-eye fa-fw"></i></button>';
		var editButtonHTML = '<button ng-click="grid.appScope.setFormData(row.entity.actionLink, \'edit\')" type="button" class="btn btn-sm btn-secondary ml-1" data-toggle="modal" data-target="#createEditObjectModal" > <i class="fa fa-pencil fa-fw"></i></button>';
		var deleteButtonHTML = '<button ng-click="grid.appScope.setFormData(row.entity.actionLink, \'delete\')" type="button" class="btn btn-sm btn-danger ml-1" data-toggle="modal" data-target="#deleteObjectModal" > <i class="fa fa-remove fa-fw"></i></button>';
		var actionButtonsHTML = '<div class="m-1">' + viewButtonHTML + editButtonHTML + deleteButtonHTML + '</div>';
		
		// Define a function to Get Data Scheme from REST Api
		$scope.getColumnList = function(){
			var columnList = [];
			CRUDService.getScheme($scope.objectTarget).success(function(data){
				$scope.schema = data;
				
				//SPECIFIC for programs and reports list
				angular.forEach(data, function(value, key) {
					if(value.type=='Display')
						this.push({ field: 'display' , name: 'Display', enableFiltering:false });
					if(value.type=='Media')
						this.push({ field: 'media' , name: 'Media', enableFiltering:false });
				}, columnList);
				// ---------------------------------------
				
				angular.forEach(data, function(value, key) {
					if(value.type=='String')
						this.push({ field: value.name , name: value.title, enableFiltering:true });
					else if( ['boolean', 'int', 'Long', 'BigDecimal', 'Date'].indexOf(value.type) !== -1)
						this.push({ field: value.name , name: value.title, enableFiltering:false });

					}, columnList);
				columnList.push({ field: 'actionLink', name: 'Actions', cellTemplate: actionButtonsHTML, enableFiltering: false, pinnedRight:true, width:130 });
			});
			return columnList;
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
						value.actionLink =  value._links.self.href;
						});
		            $scope[options].totalItems = data.page.totalElements;
		         });                            
		};
		
		// Define a function to set Grid Options data
	    $scope.setGridOptionsData = function() {
		
		     $scope.getCollectionData(
				'gridOptions',
				$scope.collectionTarget, 
				$scope.paginationOptions.pageNumber, 
				$scope.paginationOptions.pageSize, 
				$scope.paginationOptions.sortColumns,
				$scope.paginationOptions.filterColumns,
				$scope.specificFilters);                            
		 };
		
		// Get Grid Data from REST Api
		$scope.setGridOptionsData();
	 	
		// Define UI grid options & define update function
	    $scope.gridOptions = {
			rowHeight: 40,
		    enableGridMenu: true,
		    enableSelectAll: true,
		    exporterExcelFilename: 'export.xlsx',
		    exporterExcelSheetName: 'Sheet1',
	        paginationPageSizes: [5, 10, 20, 50],
	        paginationPageSize: $scope.paginationOptions.pageSize,
	        enableColumnMenus:false,
	    	useExternalPagination: true,
			useExternalSorting: true,
			enableFiltering: true,
			useExternalFiltering: true,
	        columnDefs: $scope.getColumnList(),
	        onRegisterApi: function(gridApi) {
	           	$scope.gridApi = gridApi;
	           	$scope.gridApi.pagination.on.paginationChanged(
	             	$scope, function (newPage, pageSize) {
				     	$scope.paginationOptions.pageNumber = newPage;
		     		 	$scope.paginationOptions.pageSize = pageSize;
						$scope.setGridOptionsData();
				 	});
				$scope.gridApi.core.on.sortChanged(
					$scope, function (grid, sortColumns) {
						$scope.paginationOptions.sortColumns = sortColumns;
						$scope.setGridOptionsData();
				 	});
		        $scope.gridApi.core.on.filterChanged(
		        	$scope, function() {
		        		$scope.paginationOptions.filterColumns = this.grid.columns;
						$scope.setGridOptionsData();
	        			});
			}
	    };
	
	//Define the Create function
	$scope.createTargetObject = function(){			
		CRUDService.createOne($scope.collectionTarget, $scope.formData).success(function(data){
			
			//SPECIFIC for program creation
			if($scope.objectTarget == 'program'){
				$scope.addLinkedObjects(data._links.display.href, $scope.selectedDisplaysGridOptions.data);
				$scope.addLinkedObjects(data._links.medias.href, $scope.selectedMediasGridOptions.data);
			}
			//------------------------------
			
			console.log('created object');
			$scope.setGridOptionsData();
			angular.element(createEditObjectModal).modal('hide');
		});
	};
	
	//Define the Edit function
	$scope.updateTargetObject = function(){			
		CRUDService.updateOne(targetObjectUrl, $scope.formData).success(function(data){
			
			//SPECIFIC for program update
			if($scope.objectTarget == 'program'){
				$scope.updateLinkedObjects($scope.formData._link.display.href, $scope.selectedDisplaysGridOptions.data);
				$scope.updateLinkedObjects($scope.formData._link.medias.href, $scope.selectedMediasGridOptions.data);
			}
			//----------------------------
			
			console.log('edited object');
			$scope.cleanFormData();
			$scope.setGridOptionsData();
			angular.element(createEditObjectModal).modal('hide');
		});
	};
	
	//Define the Delete function
	$scope.deleteTargetObject = function(){			
		CRUDService.deleteOne(targetObjectUrl).success(function(data){
			console.log('deleted object');
			$scope.cleanFormData();
			$scope.setGridOptionsData();
			angular.element(deleteObjectModal).modal('hide');
		});
	};

	//Define the Clean form function
	$scope.cleanFormData = function(){			
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
	$scope.displaysGridOptions = {
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
						$scope.setFormCollectionData('displaysGridOptions','displays', $scope.displaysPaginationOptions);
				 	});
				$scope.displaysGridApi.core.on.sortChanged(
					$scope, function (grid, sortColumns) {
						$scope.displaysPaginationOptions.sortColumns = sortColumns;
						$scope.setFormCollectionData('displaysGridOptions','displays', $scope.displaysPaginationOptions);
				 	});
		        $scope.displaysGridApi.core.on.filterChanged(
		        	$scope, function() {
		        		$scope.displaysPaginationOptions.filterColumns = this.grid.columns;
						$scope.setFormCollectionData('displaysGridOptions','displays', $scope.displaysPaginationOptions);
	        		});
			}
	    };
		
	// Define All Medias table
	$scope.mediasPaginationOptions = {pageNumber: 1, pageSize: 10, sortColumns: [], filterColumns: []};
	$scope.mediasGridOptions = {
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
							{ field: 'url', name: 'Url', enableFiltering: false }] ,
	        onRegisterApi: function(gridApi) {
	           	$scope.mediasGridApi = gridApi;
	           	$scope.mediasGridApi.pagination.on.paginationChanged(
	             	$scope, function (newPage, pageSize) {
				     	$scope.mediasPaginationOptions.pageNumber = newPage;
		     		 	$scope.mediasPaginationOptions.pageSize = pageSize;
						$scope.setFormCollectionData('mediasGridOptions','medias', $scope.mediasPaginationOptions);
				 	});
				$scope.mediasGridApi.core.on.sortChanged(
					$scope, function (grid, sortColumns) {
						$scope.mediasPaginationOptions.sortColumns = sortColumns;
						$scope.setFormCollectionData('mediasGridOptions','medias', $scope.mediasPaginationOptions);
				 	});
		        $scope.mediasGridApi.core.on.filterChanged(
		        	$scope, function() {
		        		$scope.mediasPaginationOptions.filterColumns = this.grid.columns;
						$scope.setFormCollectionData('mediasGridOptions','medias', $scope.mediasPaginationOptions);
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
	$scope.selectedDisplaysGridOptions = {
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
	$scope.selectedMediasGridOptions = {
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
							{ field: 'url', name: 'Url', enableFiltering: false }],
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
		
	
	if($scope.objectTarget == 'program'){
		// Get Form Grids Data from REST Api on modal show
		angular.element(createEditObjectModal).on('shown.bs.modal', function (e) {
			$scope.setFormCollectionData('displaysGridOptions','displays', $scope.displaysPaginationOptions);
			$scope.setFormCollectionData('mediasGridOptions','medias', $scope.mediasPaginationOptions);
			});
		
		// Remove Form Grids Data from REST Api on modal hide
		angular.element(createEditObjectModal).on('hidden.bs.modal', function (e) {
			$scope.displaysGridOptions.data = [];
			$scope.mediasGridOptions.data = [];
			$scope.selectedDisplaysGridOptions.data = [];
			$scope.selectedMediasGridOptions.data = [];
			});
	}
	
	// Define functions for select/de-select display and medias
	$scope.addSelectedObjects = function(object){
		
		if(object == 'display'){
			if($scope.displaysGridApi.selection.getSelectedRows().length == 1){
				$scope.displaysGridApi.selection.getSelectedRows().forEach(function(display){
					if($scope.selectedDisplaysGridOptions.data.filter(d => d.name == display.name).length < 1){
						$scope.selectedDisplaysGridOptions.data = [];
						$scope.selectedDisplaysGridOptions.data.push(display);
					}
				});
				$scope.displaysGridApi.selection.clearSelectedRows();
			}

				
		}
		
		if(object == 'medias'){
			if($scope.mediasGridApi.selection.getSelectedRows().length > 0){
				$scope.mediasGridApi.selection.getSelectedRows().forEach(function(media){
					if($scope.selectedMediasGridOptions.data.filter(m => m.name == media.name).length < 1)
						$scope.selectedMediasGridOptions.data.push(media);
				});
				$scope.mediasGridApi.selection.clearSelectedRows();
			}
		}		
	};
	
	$scope.removeSelectedObjects = function(object){
		
		if(object == 'display'){
			if($scope.selectedDisplaysGridApi.selection.getSelectedRows().length > 0){
				$scope.selectedDisplaysGridApi.selection.getSelectedRows().forEach(function(display){
						$scope.selectedDisplaysGridOptions.data.splice(
								$scope.selectedDisplaysGridOptions.data.indexOf(display), 1);
				});
			$scope.selectedDisplaysGridApi.selection.clearSelectedRows();
			}

				
		}
		
		if(object == 'medias'){
			if($scope.selectedMediasGridApi.selection.getSelectedRows().length > 0){
				$scope.selectedMediasGridApi.selection.getSelectedRows().forEach(function(media){
						$scope.selectedMediasGridOptions.data.splice(
								$scope.selectedMediasGridOptions.data.indexOf(media), 1);
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
	            url: fullTarget
	        });
	    };
	    
	    function setLinkedObjects(fullTarget, links) {
	        return $http({
	          method: 'PUT',
	            url: fullTarget,
	            data: links,
	            headers: {'Content-type' : 'text/uri-list'}
	        });
	    };
	    
	    function deleteLinkedObjects(fullTarget) {
	        return $http({
	          method: 'DELETE',
	            url: fullTarget
	        });
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

	    };
    
}]);
