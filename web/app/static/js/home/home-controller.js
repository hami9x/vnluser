'use strict';

var API_URL = 'http://m.chk.vn:5000';
var APP_KEY = 'fsdlsyzqfzmzf05';

function url(path) {
    return API_URL + path;
}

function getContents(http, data) {
    for (var i in data) {
        data[i].content = "";
        if (data[i].dp_link != "") {
            http.get(data[i].dp_link).success(function(d) {
                data[i].content = $(d.content).text();
            });
        }
    }

    return data;
}

angular.module('vnluser')
  .controller('HomeController', ['$scope', '$http', function ($scope, $http) {

    var popupClose = function() {
        location.reload();
    }

    $scope.loginPopup = function() {
        var popup = window.open($scope.loginurl,'Dropbox OAuth', 'width=700&height=auto');
        popup.onunload = popupClose;
    }

    $http.get(url("/chk/login"), {withCredentials: true})
    .success(function(data) {
        $scope.needslogin = false;
        $scope.loginurl = '';
        if (data.status == "forbidden") {
            $scope.needslogin = true;
            $scope.loginurl = data.login_url;
        } else {
            window.location = "https://st.chk.vn/dashboard";
        }
    });

    $scope.tabid = 'home';
  }])
  .controller('DropboxController', ['$scope', '$http', function ($scope, $http) {
    var params = location.search
    $http.get(url('/auth/login' + params), {withCredentials: true})
        .success(function() {
        window.close();
    })
  }])
    .controller('DashboardController', ['$scope', '$http',
      function ($scope, $http) {
        $scope.tabid = "dashboard";
        $scope.cshow = [];
        $scope.posts = $http.get(url('/lists/public_post'), {withCredentials: true}).success(function(data) {
            $scope.posts = getContents($http, data);
        });
        $scope.repub = function(index) {
            var post = $scope.posts[index];
            var currentDate = new Date();
            var utcDate = new Date( currentDate.getUTCFullYear(), currentDate.getUTCMonth(), currentDate.getUTCDate(), currentDate.getUTCHours(), currentDate.getUTCMinutes(), currentDate.getUTCSeconds());
            post.unixtime = utcDate.getTime();
            delete post.post_id;
            delete post.dp_link;
            console.log(post);

            $http.post(url("/chk/save"), post, {withCredentials: true})
                .success(function(resp) {
                console.log(resp);
            });

        };
      }
    ])
    .controller('PostsController', ['$scope', '$http',
      function ($scope, $http) {
        $scope.tabid = "posts";
        $http.get(url('/lists/post'), {withCredentials: true}).success(function(data) {
            $scope.posts = getContents($http, data);
        });
      }
    ]);

