var app = angular.module("AccountManagement", []);

//Directive for form Validation, password confirmation
app.directive("matchPassword", function () {
    return {
        require: "ngModel",
        scope: {
            otherModelValue: "=matchPassword"
        },
        link: function(scope, element, attributes, ngModel) {

            ngModel.$validators.matchPassword = function(modelValue) {
                return modelValue == scope.otherModelValue.$viewValue;
            };

            scope.$watch("otherModelValue", function() {
                ngModel.$validate();
            });
        }
    };
});


// Controller Part
app.controller("CreateAccountController", function($scope, $http) {


	// HTTP POST methods for add account  
	// Call: http://localhost:8080/account
	$scope.createAccount = function() {

		$http({
			method : "POST",
			url : '/account',
			data : angular.toJson($scope.accountForm),
			headers : {
				'Content-Type' : 'application/json'
			}
		}).then(_success, _error);
	};

	//handle server answer 
	function _success(res) {
		var status = res.data.status;
		var message = res.data.message;
		var errors = res.data.errors;
		
		//if status is OK then show confirmation
		if(status=='OK'){
			alert("account created : \n" + message);
			_clearFormData();
			window.location.href = '/login';
		}
		
		//if status is BAD_REQUEST then highlight where form error are
		else {
			if(errors[0] == 'userName: Duplicate.accountForm.userName'){
				$scope.userForm.userName.$invalid = true;
				$scope.userForm.userName.$error.alreadyUsed = true;
			}
			else if(errors[0] == 'userName: Pattern.accountForm.email'){
				$scope.userForm.userName.$error.alreadyUsed = false;
				$scope.userForm.userName.$invalid = true;
				$scope.userForm.userName.$error.email = true;
			}
			else
				alert("your form contains errors : " + message);
		}
		
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
		$scope.accountForm.firstName = "";
		$scope.accountForm.lastName = "";
		$scope.accountForm.userName = "";
		$scope.accountForm.password = "";
		$scope.accountForm.confirmPassword = "";
		$scope.accountForm.conditionsAccepted = "";
		$scope.userForm.$setPristine();
		
	}

});