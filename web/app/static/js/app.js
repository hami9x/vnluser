// Declare app level module which depends on filters, and services
angular.module('vnluser', ['ngResource', 'ngRoute', 'ui.bootstrap', 'ui.date'])
  .config(['$routeProvider', function ($routeProvider) {
    $routeProvider
      .when('/', {
        templateUrl: 'views/home/home.html', 
        controller: 'HomeController'})
      .when('/dropbox_login', {
        templateUrl: 'views/home/dropbox_redirect.html', 
        controller: 'DropboxController'})
      })
      .otherwise({redirectTo: '/'});
  }]);
