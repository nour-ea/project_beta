var app = angular.module('crudApp', ['ui.grid','ui.grid.pagination']);

app.controller('crudCtrl', ['$scope','objectModel', 'CRUDService', 
	    function ($scope, objectModel, CRUDService) {
	    
		// Define the Object Target for the CRUD App (Display, Media...)
		var objectTarget = objectModel;
		var collectionTarget = objectTarget + 's';
		
		// Define Pagination options for the UI Grid  
		 var paginationOptions = {pageNumber: 1, pageSize: 5, sort: null};
				
		// Define Created / Edited / Deleted target object url link
		var targetObjectUrl = '/api/'+collectionTarget;
		$scope.setTargetObjectUrl = function(url){
			targetObjectUrl = url;
			console.log('setting target object url');
		}

		// Define HTML for edition buttons
		var viewButtonHTML = '<button type="button" class="btn btn-sm btn-primary" ><i class="fa fa-tv fa-fw"></i></button>';
		var editButtonHTML = '<button ng-click="grid.appScope.setTargetObjectUrl(row.entity.actionLink)" type="button" class="btn btn-sm btn-secondary" data-toggle="modal" data-target="#editObjectModal" > <i class="fa fa-pencil fa-fw"></i></button>';
		var deleteButtonHTML = '<button ng-click="grid.appScope.setTargetObjectUrl(row.entity.actionLink)" type="button" class="btn btn-sm btn-danger" data-toggle="modal" data-target="#deleteObjectModal" > <i class="fa fa-remove fa-fw"></i></button>';
		var actionButtonsHTML = viewButtonHTML + editButtonHTML + deleteButtonHTML;
		
		// Define a function to Get Data Scheme from REST Api
		$scope.getColumnList = function(){
			var columnList = [];
			CRUDService.getScheme(collectionTarget).success(function(data){
				angular.forEach(data.properties, function(value, key) {
					  this.push({ field: key , name: value.title, enableFiltering:true });
					}, columnList);
				columnList.push({ fied: 'actionLink', name: 'Actions', cellTemplate: actionButtonsHTML });
			});
			return columnList
		};

		// Define a function to Get Data Collection from REST Api
	    $scope.getCollectionData = function(page, size) {
		     paginationOptions.pageNumber = page;
		     paginationOptions.pageSize = size;
		     CRUDService.getAll(collectionTarget, page, size)
		        .success(function(data){
		          	$scope.gridOptions.data = data._embedded[collectionTarget];
					angular.forEach($scope.gridOptions.data, function(value, key) {
						value.actionLink =  value['_links']['self']['href'];
						});
		            $scope.gridOptions.totalItems = data.page.totalElements;
		         });                            
		 };
		
		// Get Data Collection from REST Api
		$scope.getCollectionData(paginationOptions.pageNumber, paginationOptions.pageSize);
	 	
		// Define UI grid options & define update function
	    $scope.gridOptions = {
	        paginationPageSizes: [5, 10, 20, 50],
	        paginationPageSize: paginationOptions.pageSize,
	        enableColumnMenus:false,
	    		useExternalPagination: true,
	        columnDefs: $scope.getColumnList(),
	        onRegisterApi: function(gridApi) {
	           $scope.gridApi = gridApi;
	           gridApi.pagination.on.paginationChanged(
	             $scope, function (newPage, pageSize) {
						$scope.getCollectionData(newPage, pageSize)
				 });
	        }
	    };
	
		//Define the Delete function
		$scope.deleteTargetObject = function(){			
			CRUDService.deleteOne(targetObjectUrl).success(function(data){
				alert('tryed to delete stuff');
			});
		};
	
}]);


//list of CRUD calls
app.service('CRUDService',['$http', function ($http) {
    
	    function getScheme(collectionTarget) {
	        return $http({
	          method: 'GET',
	            url: '/api/profile/'+collectionTarget,
	            headers: {'Accept' : 'application/schema+json'}
	        });
	    };
	    
	    function getAll(collectionTarget, pageNumber, size) {
	        pageNumber = pageNumber > 0?pageNumber - 1:0;
	        return $http({
	          method: 'GET',
	            url: '/api/'+collectionTarget+'/?page='+pageNumber+'&size='+size
	        });
	    };
	    
	    function getOne(url) {
	        return $http({
	          method: 'GET',
	            url: url
	        });
	    };
	    
	    function createOne(collectionTarget, data) {
	        return $http({
	          method: 'POST',
	          	url: '/api/'+collectionTarget,
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