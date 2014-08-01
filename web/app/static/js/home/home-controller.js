'use strict';

var API_URL = 'https://m.chk.vn:5000';

function url(path) {
    return API_URL + path;
}

function getQueryStringValue (key) {
  return unescape(window.location.search.replace(new RegExp("^(?:.*[&\\?]" + escape(key).replace(/[\.\+\*]/g, "\\$&") + "(?:\\=([^&]*))?)?.*$", "i"), "$1"));  
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

    $http.get(url("/chk/login"), {withCredentials: true})
    .success(function(data) {
        console.log(data);
        $scope.needslogin = false;
        $scope.loginurl = '';
        if (data.status == "forbidden") {
            $scope.needslogin = true;
            $scope.loginurl = data.login_url;
        }
    });
  }])
  .controller('DropboxController', ['$scope', '$http', function ($scope, $http) {
        /*var params = "";
        var paramsd = location.search;
        var i = 0;
        for (var param in paramsd) {
            if (i > 0) {
                params += "&"+ param +"="+ encodeURIComponent(paramsd[param]);
            } else {
                params += param + "=" + encodeURIComponent(paramsd[param]);
            }
            i++;
        }

        $http.get(url("/auth/login" + paramsd))
            .success(function(data) {
                localStorage.setItem('logged', 'true');
                // window.close();
            });*/
        window.close();
  }]);

var chk_call = function(rs) {
        console.log(rs);
    }