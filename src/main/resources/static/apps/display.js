var app = angular.module('DisoplayManagement', ['ui.grid','ui.grid.pagination']);


// Controller Part
app.controller('DisplayCtrl', ['$scope','DisplayService', 
    function ($scope, DisplayService) {
        var paginationOptions = {
            pageNumber: 1,
            pageSize: 5,
        sort: null
        };
 
    DisplayService.getDisplays(
      paginationOptions.pageNumber,
      paginationOptions.pageSize).success(function(data){
        $scope.gridOptions.data = data.content;
        $scope.gridOptions.totalItems = data.totalElements;
      });
 
    $scope.gridOptions = {
        paginationPageSizes: [5, 10, 20],
        paginationPageSize: paginationOptions.pageSize,
        enableColumnMenus:false,
    useExternalPagination: true,
        columnDefs: [
           { name: 'id' },
           { name: 'name' },
           { name: 'address' },
           { name: 'brand' },
           { name: 'size' },
           { name: 'shopCoverage' },
           { name: 'mac' },
           { name: 'smart' }
        ],
        onRegisterApi: function(gridApi) {
           $scope.gridApi = gridApi;
           gridApi.pagination.on.paginationChanged(
             $scope, 
             function (newPage, pageSize) {
               paginationOptions.pageNumber = newPage;
               paginationOptions.pageSize = pageSize;
               DisplayService.getDisplays(newPage,pageSize)
                 .success(function(data){
                   $scope.gridOptions.data = data.content;
                   $scope.gridOptions.totalItems = data.totalElements;
                 });
            });
        }
    };
}]);

app.service('DisplayService',['$http', function ($http) {
	 
    function getDisplays(pageNumber,size) {
        pageNumber = pageNumber > 0?pageNumber - 1:0;
        return $http({
          method: 'GET',
            url: '/gdisplays?page='+pageNumber+'&size='+size
        });
    return {
        getDisplays: getDisplays
    };
}]);