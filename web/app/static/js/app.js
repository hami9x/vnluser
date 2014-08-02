// Declare app level module which depends on filters, and services
angular.module('vnluser', ['ngResource', 'ngRoute', 'ui.bootstrap', 'ui.date'])
  .config(['$routeProvider', '$locationProvider', '$httpProvider', function ($routeProvider, $locationProvider, $httpProvider) {
    $routeProvider
      .when('/', {
        templateUrl: 'views/home/home.html', 
        controller: 'HomeController'})
      .when('/dropbox_login', {
        templateUrl: 'views/home/dropbox_redirect.html', 
        controller: 'DropboxController'})
      .when('/dashboard', {
        templateUrl: 'views/home/dashboard.html', 
        controller: 'DashboardController'})
      .when('/posts', {
        templateUrl: 'views/home/posts.html', 
        controller: 'PostsController'})
      // .otherwise({redirectTo: '/'});
       $httpProvider.defaults.useXDomain = true;
      delete $httpProvider.defaults.headers.common['X-Requested-With'];
      $locationProvider.html5Mode(true);
  }]);
