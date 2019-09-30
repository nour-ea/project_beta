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
			url : '/api/accounts',
			data : angular.toJson($scope.accountForm),
			headers : {
				'Content-Type' : 'application/json'
			}
		}).then(_success, _error);
	};

	//handle server answer 
	function _success(res) {
		var status = res.status;
		var data = res.data;
		
		//if status is OK then show confirmation
		if(status==201){
			createAlert("success","account successfully created for user : "+ data.userName 
					+ ". <br> <strong> Please confirm your email by clicking on the link we sent you.</string>");
			$scope.userForm.userName.$error.alreadyUsed = false;
			$scope.userForm.userName.$invalid = false;
			$scope.userForm.userName.$error.email = false;
			$(".se-pre-con").show();
			setTimeout(function(){
				$(".se-pre-con").fadeOut("slow");
				_clearFormData();
				window.location.href = '/login'; 
				}, 10000);

		}
	
		
	}


	function _error(res) {
		var status = res.data.status;
		var message = res.data.message;
		var errors = res.data.errors;
		
		//if status is BAD REQUEST then highlight where form error are
		if(errors[0] == 'userName: Duplicate.account.userName'){
				$scope.userForm.userName.$invalid = true;
				$scope.userForm.userName.$error.alreadyUsed = true;
		}
		else if(errors[0] == 'userName: Pattern.account.email'){
				$scope.userForm.userName.$error.alreadyUsed = false;
				$scope.userForm.userName.$invalid = true;
				$scope.userForm.userName.$error.email = true;
		}
		else
			createAlert("danger","Sorry: " + status + "\n" + message + "\n" + errors.toString());
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