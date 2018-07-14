var app = angular.module("ProductManagement", []);

// Controller Part
app.controller("ProductController", function($scope, $http) {

	$scope.products = [];
	$scope.productForm = {
		id : -1,
		name : "",
		price : ""
	};

	// Now load the data from server
	_refreshProductData();

	// HTTP POST/PUT methods for add/edit product  
	// Call: http://localhost:8080/product
	$scope.submitProduct = function() {

		var method = "";
		var url = "";

		if ($scope.productForm.id == -1) {
			method = "POST";
			url = '/product';
		} else {
			method = "PUT";
			url = '/product';
		}

		$http({
			method : method,
			url : url,
			data : angular.toJson($scope.productForm),
			headers : {
				'Content-Type' : 'application/json'
			}
		}).then(_success, _error);
	};

	$scope.createProduct = function() {
		_clearFormData();
	}

	// HTTP DELETE- delete product by Id
	// Call: http://localhost:8080/product/{productId}
	$scope.deleteProduct = function(product) {
		$http({
			method : 'DELETE',
			url : '/product/' + product.id
		}).then(_success, _error);
	};

	// In case of edit
	$scope.editProduct = function(product) {
		$scope.productForm.id = product.id;
		$scope.productForm.name = product.name;
		$scope.productForm.price = product.price;
	};

	// Private Method  
	// HTTP GET- get all products collection
	// Call: http://localhost:8080/products
	function _refreshProductData() {
		$http({
			method : 'GET',
			url : '/products'
		}).then(function(res) { // success
			$scope.products = res.data;
		}, function(res) { // error
			console.log("Error: " + res.status + " : " + res.data);
		});
	}

	function _success(res) {
		_refreshProductData();
		_clearFormData();
	}

	function _error(res) {
		var data = res.data;
		var status = res.status;
		var header = res.header;
		var config = res.config;
		alert("Error: " + status + ":" + data);
	}

	// Clear the form
	function _clearFormData() {
		$scope.productForm.id = -1;
		$scope.productForm.name = "";
		$scope.productForm.price = ""
	}
	;
});