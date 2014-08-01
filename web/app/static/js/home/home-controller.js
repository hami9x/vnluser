'use strict';

var API_URL = 'http://m.chk.vn:5000';
var APP_KEY = 'fsdlsyzqfzmzf05';

function url(path) {
    return API_URL + path;
}


angular.module('vnluser')
  .controller('HomeController', ['$scope', '$http', function ($scope, $http) {

    var popupClose = function() {
        console.log('popup close');
        window.location = "https://st.chk.vn/dashboard";
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
        $scope.genComments = function(){
            var a = [];
            var rand = function() {
                var str = '';
                for (var j=0; j<Math.floor(Math.random()*3); j++) {
                    str += String.fromCharCode(Math.floor(Math.random()*20));
                }
                return str;
            }

            for (var i=0; i<Math.floor(Math.random()*3); i++) {
                a.push({content: "kkdf", author: "kdkf"});
            }

            return a;
        }
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
    .controller('PostsController', ['$scope', '$http',
      function ($scope, $http) {
        $scope.tabid = "posts";
        // $scope.posts = [
        //     {
        //         title: 'Some title',
        //         content: 'zzzzzzzzzzzzzzzzzz',
        //         keywords: ['programming', 'SocialMedia'],
        //         ncomments: 10,
        //         date: "1/3/2014",
        //     },
        // ]
        $http.get(url('/lists/post'), {withCredentials: true}).success(function(data) {
            $scope.posts = data;
        });
      }
    ])
    .controller('StorageViewController', ['$scope', '$http',
      function ($scope, $http) {
        $scope.tabid = 'storage';

        var client = new Dropbox.Client({key: APP_KEY});

        // Try to finish OAuth authorization.
        client.authenticate({interactive: false}, function (error) {
            if (error) {
                alert('Authentication error: ' + error);
            }
        });

        if (client.isAuthenticated()) {
            var datastoreManager = client.getDatastoreManager();
            datastoreManager.openDefaultDatastore(function (error, datastore) {
                if (error) {
                    alert('Error opening default datastore: ' + error);
                }

                var table = datastore.getTable('h-saves');
                $scope.posts = table.query();

            });
        }
      }
    ]);
