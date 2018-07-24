var app = angular.module('crudApp', ['ui.grid','ui.grid.pagination']);

app.controller('crudCtrl', ['$scope','objectModel', 'CRUDService', 
	    function ($scope, objectModel, CRUDService) {
	    		
		// Define the Object Target for the CRUD App (Display, Media...)
		var objectTarget = objectModel;
		var collectionTarget = objectTarget + 's';
		
		// Define Pagination options & Specific filters for GetAll Request to fill the UI Grid  
		var paginationOptions = {pageNumber: 1, pageSize: 5, sortColumns: [], filterColumns: []};
		$scope.specificFilters = {};
				
		// Define Edit / Delete target object url link and fill $scope.formData
		var targetObjectUrl = '/api/'+collectionTarget;
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
		var viewButtonHTML = '<button type="button" class="btn btn-sm btn-primary" ><i class="fa fa-tv fa-fw"></i></button>';
		var editButtonHTML = '<button ng-click="grid.appScope.setFormData(row.entity.actionLink, \'edit\')" type="button" class="btn btn-sm btn-secondary" data-toggle="modal" data-target="#createEditObjectModal" > <i class="fa fa-pencil fa-fw"></i></button>';
		var deleteButtonHTML = '<button ng-click="grid.appScope.setFormData(row.entity.actionLink, \'delete\')" type="button" class="btn btn-sm btn-danger" data-toggle="modal" data-target="#deleteObjectModal" > <i class="fa fa-remove fa-fw"></i></button>';
		var actionButtonsHTML = viewButtonHTML + editButtonHTML + deleteButtonHTML;
		
		// Define a function to Get Data Scheme from REST Api
		$scope.getColumnList = function(){
			var columnList = [];
			CRUDService.getScheme(collectionTarget).success(function(data){
				angular.forEach(data.properties, function(value, key) {
					if(value.type=='string')
						this.push({ field: key , name: value.title, enableFiltering:true });
					else
						this.push({ field: key , name: value.title, enableFiltering:false });
					}, columnList);
				columnList.push({ fied: 'actionLink', name: 'Actions', cellTemplate: actionButtonsHTML, enableFiltering: false });
			});
			return columnList;
		};

		// Define a function to Get Data Collection from REST Api
	    $scope.getCollectionData = function() {
		
		     CRUDService.getAll(collectionTarget, 
				paginationOptions.pageNumber, 
				paginationOptions.pageSize, 
				paginationOptions.sortColumns,
				paginationOptions.filterColumns,
				$scope.specificFilters)
		        .success(function(data){
		          	$scope.gridOptions.data = data._embedded[collectionTarget];
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
	        paginationPageSizes: [5, 10, 20, 50],
	        paginationPageSize: paginationOptions.pageSize,
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
				     	paginationOptions.pageNumber = newPage;
		     		 	paginationOptions.pageSize = pageSize;
						$scope.getCollectionData();
				 	});
				$scope.gridApi.core.on.sortChanged(
					$scope, function (grid, sortColumns) {
						paginationOptions.sortColumns = sortColumns;
						$scope.getCollectionData();
				 	});
		        $scope.gridApi.core.on.filterChanged(
		        		$scope, function() {
		        			paginationOptions.filterColumns = this.grid.columns;
						$scope.getCollectionData();
	        			});
			}
	    };
	
	//Define the Create function
	$scope.createTargetObject = function(){			
		CRUDService.createOne(collectionTarget, $scope.formData).success(function(data){
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
	};
	
	//Parametrize Create/Edit modal
	$scope.customizeCreateEditModal = function(operation){
	
		if(operation == 'create'){
			angular.element(createEditObjectModalLabel).html("Create " + objectTarget);
			angular.element(createEditObjectModalAction).html("Create");
			angular.element(createEditObjectModalAction).attr("ng-click", "createTargetObject()");
			angular.element(createEditObjectModal).on('hide.bs.modal', function (e) {
				});
			
		}else if(operation == 'edit'){
			angular.element(createEditObjectModalLabel).html("Edit " + objectTarget);
			angular.element(createEditObjectModalAction).html("Update");
			angular.element(createEditObjectModalAction).attr("ng-click", "editTargetObject()");
			angular.element(createEditObjectModal).on('hide.bs.modal', function (e) {
				$scope.cleanFormData();
				});
		}else if(operation == 'delete'){
			angular.element(deleteObjectModalLabel).html("Delete " + objectTarget);
			angular.element(deleteObjectModal).on('hide.bs.modal', function (e) {
				$scope.cleanFormData();
				});
		}
	};
	
}]);


//list of CRUD calls
app.service('CRUDService',['$http', function ($http) {
    
	    function getScheme(target) {
	        return $http({
	          method: 'GET',
	            url: '/api/profile/'+target,
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