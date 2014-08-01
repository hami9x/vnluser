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
      .when('/dashboard', {
        templateUrl: 'views/home/dashboard.html', 
        controller: 'DashboardController'})
      .when('/profile', {
        templateUrl: 'views/home/profile.html', 
        controller: 'ProfileController'})
      .otherwise({redirectTo: '/'});
      $locationProvider.html5Mode(true);
  }]);
