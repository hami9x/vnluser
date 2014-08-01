'use strict';
var apiurl = 'http://m.chk.vn:5000';

test = function() {
    alert(":D")
}

function url(path) {
    return apiurl + path;
}

angular.module('vnluser')
  .controller('HomeController', ['$scope', '$http', function ($scope, $http) {
    $scope.loginPopup = function() {
        $scope.popup = window.open($scope.loginurl);
    }

    $http.get(url("/chk/login")).success(function(data) {
        $scope.needslogin = false;
        if (data.status == "forbidden") {
            $scope.needslogin = true;
            $scope.loginurl = data.login_url;
        }
    });
  }])
  .controller('DropboxController', ['$scope', '$http', '$location',
    function ($scope, $http, $location) {
        var params = "";
        var paramsd = $location.search();
        for (param in paramsd) {
            params += "&"+param+"="+params[param];
        }
        console.log(params);
        $http.get(url("/login?"+params)).success(function(data) {
            window.opener.test();
            window.close();
        });
  }]);
