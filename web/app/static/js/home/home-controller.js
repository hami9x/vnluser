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
        $scope.posts = [
            {
                username: 'Hai Thanh Nguyen',
                title: 'Some title',
                content: 'Sed ut perspiciatis unde omnis iste natus.',
                keywords: ['wade.go', 'programming'],
                ncomments: 100,
                date: "1/4/2014",
            },
        ]
      }
    ])
    .controller('ProfileController', ['$scope', '$http',
      function ($scope, $http) {
        $scope.posts = [
            {
                username: 'Le Kien Truc',
                title: 'Some title',
                content: 'zzzzzzzzzzzzzzzzzz',
                keywords: ['programming', 'SocialMedia'],
                ncomments: 10,
                date: "1/3/2014",
            },
        ]
      }
    ]);