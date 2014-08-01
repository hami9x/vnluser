'use strict';

var API_URL = 'http://m.chk.vn:5000';

function url(path) {
    return API_URL + path;
}

angular.module('vnluser')
  .controller('HomeController', ['$scope', '$http', function ($scope, $http) {
    var popupClose = function() {
        console.log('popup close');
    }

    $scope.loginPopup = function() {
        var popup = window.open($scope.loginurl,'Dropbox OAuth', 'width=700&height=auto');
        popup.onunload = popupClose;
    }

    $http.get(url("/chk/login")).success(function(data) {
        $scope.needslogin = false;
        $scope.loginurl = '';
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
        for (var param in paramsd) {
            params += "&"+ param +"="+paramsd[param];
        }
        $http.get("/auth/login?"+params)
            .success(function(data) {
                localStorage.setItem('logged', 'true');
                window.close();
            });
  }])
    .controller('DashboardController', ['$scope', '$http',
      function ($scope, $http) {
        var updates = [
            {
                name: 'Mr. A',
                content: 'Sed ut perspiciatis unde omnis iste natus.'
            },
            {
                name: 'Mr. A',
                content: 'ullam corporis suscipit laboriosam'
            },
        ]
      }
    ]);