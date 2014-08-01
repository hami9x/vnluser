// Declare app level module which depends on filters, and services
angular.module('vnluser', ['ngResource', 'ngRoute', 'ui.bootstrap', 'ui.date'])
  .config(['$routeProvider', '$locationProvider', function ($routeProvider, $locationProvider) {
    $routeProvider
      .when('/', {
        templateUrl: 'views/home/home.html', 
        controller: 'HomeController'})
      .when('/close_popup', {
        templateUrl: 'views/home/dropbox_redirect.html', 
        controller: 'DropboxController'})
      .otherwise({redirectTo: '/'});
      $locationProvider.html5Mode(true);
  }]);
