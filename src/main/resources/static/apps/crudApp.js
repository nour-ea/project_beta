var app = angular.module('crudApp', ['ui.grid','ui.grid.pagination', 'ui.grid.selection', 'ui.grid.exporter']);

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

		$scope.setFormData = function(url, operation){
			if(operation == 'edit' || operation == 'delete') {
				targetObjectUrl = url;
				console.log('setting target object url');
				CRUDService.getOne(url).success(function(data){
					$scope.formData = data;
					console.log('setting form data');
				});
			}
			
			$scope.customizeCreateEditModal(operation);
			
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

		// Define a function to Get Data Collection from REST Api
	    $scope.getCollectionData = function() {
		
		     CRUDService.getAll($scope.collectionTarget, 
				$scope.paginationOptions.pageNumber, 
				$scope.paginationOptions.pageSize, 
				$scope.paginationOptions.sortColumns,
				$scope.paginationOptions.filterColumns,
				$scope.specificFilters)
				.success(function(data){
		          	$scope.gridOptions.data = data._embedded[$scope.collectionTarget];
					angular.forEach($scope.gridOptions.data, function(value, key) {
						value.actionLink =  value['_links']['self']['href'];
						});
		            $scope.gridOptions.totalItems = data.page.totalElements;
		         });                            
		 };
		
		// Get Data Collection from REST Api
		$scope.getCollectionData();
	 	
		// Define UI grid options & define update function
	    $scope.gridOptions = {
			rowHeight:40,
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
						$scope.getCollectionData();
				 	});
				$scope.gridApi.core.on.sortChanged(
					$scope, function (grid, sortColumns) {
						$scope.paginationOptions.sortColumns = sortColumns;
						$scope.getCollectionData();
				 	});
		        $scope.gridApi.core.on.filterChanged(
		        		$scope, function() {
		        			$scope.paginationOptions.filterColumns = this.grid.columns;
						$scope.getCollectionData();
	        			});
			}
	    };
	
	//Define the Create function
	$scope.createTargetObject = function(){			
		CRUDService.createOne($scope.collectionTarget, $scope.formData).success(function(data){
			console.log('created object');
			$scope.getCollectionData();
		});
	};
	
	//Define the Edit function
	$scope.updateTargetObject = function(){			
		CRUDService.updateOne(targetObjectUrl, $scope.formData).success(function(data){
			console.log('edited object');
			$scope.cleanFormData();
			$scope.getCollectionData();
		});
	};
	
	//Define the Delete function
	$scope.deleteTargetObject = function(){			
		CRUDService.deleteOne(targetObjectUrl).success(function(data){
			console.log('deleted object');
			$scope.cleanFormData();
			$scope.getCollectionData();
		});
	};

	//Define the Clean form function
	$scope.cleanFormData = function(){			
		$scope.formData = {};
		$scope.objectForm.$setPristine();
	};
	
	//Parametrize Create/Edit modal
	$scope.customizeCreateEditModal = function(operation){
	
		//build modal canvas
		if(operation == 'create'){
			angular.element(createEditObjectModalLabel).html("Create " + $scope.objectTarget);
			angular.element(createEditObjectModalAction).html("Create");
			angular.element(createEditObjectModalAction).attr("ng-click", "createTargetObject()");
			angular.element(createEditObjectModal).on('hide.bs.modal', function (e) {
				});
			
		}else if(operation == 'edit'){
			angular.element(createEditObjectModalLabel).html("Edit " + $scope.objectTarget);
			angular.element(createEditObjectModalAction).html("Update");
			angular.element(createEditObjectModalAction).attr("ng-click", "editTargetObject()");
			angular.element(createEditObjectModal).on('hide.bs.modal', function (e) {
				$scope.cleanFormData();
				});
		}else if(operation == 'delete'){
			angular.element(deleteObjectModalLabel).html("Delete " + $scope.objectTarget);
			angular.element(deleteObjectModal).on('hide.bs.modal', function (e) {
				$scope.cleanFormData();
				});
		}
		
	};
	
	//specific ui tables and data for programs displays and media management
	//Define display table
	$scope.displaysPaginationOptions = {pageNumber: 1, pageSize: 10, sortColumns: [], filterColumns: []};
	$scope.displaysGridOptions = {
	        paginationPageSizes: [5, 10, 20, 50],
	        paginationPageSize: $scope.paginationOptions.pageSize,
	        enableColumnMenus:false,
	    		useExternalPagination: true,
			useExternalSorting: true,
			enableFiltering: true,
			useExternalFiltering: true,
	        columnDefs: [
							{ field: 'name', name: 'Name', },
							{ field: '_link', name: 'Link', enableFiltering: false }] ,
	        onRegisterApi: function(gridApi) {
	           	$scope.gridApi = gridApi;
	           	$scope.gridApi.pagination.on.paginationChanged(
	             	$scope, function (newPage, pageSize) {
				     	$scope.displaysPaginationOptions.pageNumber = newPage;
		     		 	$scope.displaysPaginationOptions.pageSize = pageSize;
						$scope.getDisplaysData();
				 	});
				$scope.gridApi.core.on.sortChanged(
					$scope, function (grid, sortColumns) {
						$scope.displaysPaginationOptions.sortColumns = sortColumns;
						$scope.getDisplaysData();
				 	});
		        $scope.gridApi.core.on.filterChanged(
		        		$scope, function() {
		        			$scope.displaysPaginationOptions.filterColumns = this.grid.columns;
						$scope.getProgramDisplaysData();
	        			});
			}
	    };
		
	    $scope.getDisplaysData = function() {
		
		     CRUDService.getAll($scope.collectionTarget, 
				$scope.paginationOptions.pageNumber, 
				$scope.paginationOptions.pageSize, 
				$scope.paginationOptions.sortColumns,
				$scope.paginationOptions.filterColumns,
				$scope.specificFilters)
				.success(function(data){
		          	$scope.gridOptions.data = data._embedded[$scope.collectionTarget];
					angular.forEach($scope.gridOptions.data, function(value, key) {
						value.actionLink =  value['_links']['self']['href'];
						});
		            $scope.gridOptions.totalItems = data.page.totalElements;
		         });                            
		 };
		
	//Define medias table
	

}]);


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
	        return $http({
	          method: 'POST',
	          	url: '/api/'+target,
	            data: data
	        });
	    };
	
	    function updateOne(url, data) {
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
	    
	    return {
	    		getScheme: 	getScheme,
	    		getAll: 		getAll,
	    		getOne: 		getOne,
	    		createOne: 	createOne,
	    		updateOne: 	updateOne,
	    		deleteOne: 	deleteOne
	    };
    
}]);
