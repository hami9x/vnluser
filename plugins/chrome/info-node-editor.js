jQuery.extend({
  getUrlVars: function(){
	var vars = [], hash;
	var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
	for(var i = 0; i < hashes.length; i++)
	{
	  hash = hashes[i].split('=');
	  vars.push(hash[0]);
	  vars[hash[0]] = decodeURIComponent(hash[1]);
	}
	return vars;
  },
  getUrlVar: function(name){
	return jQuery.getUrlVars()[name];
  }
});

var check_status = false;
var baseUrl = 'http://m.chk.vn:5000/chk';
var LOGIN_URL = baseUrl + '/login';
var SAVE_URL = baseUrl + '/save';
var logged = false;


function checkLogin(){
	jQuery.get(LOGIN_URL,{}, function(rs) {
		if(rs.status !== 'forbidden'){
			// check_status = false;
			logged = true;
		} else {
			// check_status = true;
			logged = false;
			popupCenter(rs.login_url, function() {
				logged = true;
			});
			// addInfoNode();
		}
	} );
}
checkLogin();

chrome.extension.onRequest.addListener(
	function(request, sender, sendResponse) {
		{
			//alert(JSON.stringify(request));
			jQuery('#selected_html').html( decodeURI(request.html) );
			jQuery('#title').html(request.title);
			document.title += request.title;
			jQuery('#keywords').html(request.keywords);
			jQuery('#url').html(request.url);
			jQuery('#share_to_facebook').attr('href', 'https://www.facebook.com/sharer/sharer.php?u=' + request.url);
			var h = function(arr){
				var guessedKeywords = arr.join();
				var ehtml = jQuery('#keywords').html().trim();
				if(ehtml.length == 0){
					jQuery('#keywords').html(guessedKeywords);
				} else {
					jQuery('#keywords').html(ehtml + "," + guessedKeywords);
				}
				
			};
			//TODO improve tracking data
			var trackingData = {url : request.url, referer : request.referer};
			trackingData.title = request.title;
			trackingData.content = request.html;
			jQuery.getJSON("http://a.chk.vn:8888/keywords",trackingData,h);
		}
	}
);

function popupCenter(pageURL, w, h, top, left, nofocus, callBack) {
	if(typeof left === 'undefined' )
		left = (screen.width/2)-(w/2);
	if(typeof top === 'undefined' )
		top = (screen.height/2)-(h/2);
	if(top > 120 ) top -= 70;
	var params = 'menubar=0,resizable=1,toolbar=no,location=no,directories=no,status=yes,menubar=no,scrollbars=no,resizable=yes,copyhistory=no,'; 
	params = params + ('width='+w+', height='+h+', top='+top+', left='+left);
	var win = window.open (pageURL, "_blank", params);
	if(typeof win.focus === 'function' && !nofocus) {
		win.focus();
	}

	win.onunload = callBack;

	return win;
}

var autoPost = false;

function addInfoNode(){
	jQuery('#loading').show();
	var f = function(rs){
		jQuery('#loading').hide();
		jQuery('.alert-danger').hide();
		setTimeout(function(){
			window.close();
		}, 1500)
	};
	var keywordsArr = jQuery('#keywords').text().trim().split(',');
	for (var i = 0; i < keywordsArr.length; i++) {
		keywordsArr[i] = keywordsArr[i].trim();
	}

	var data = {
		'content' : jQuery('#selected_html').html(),
		'title' : jQuery('#title').text() ? jQuery('#title').text() : '',
		'link' : jQuery('#url').text() ? jQuery('#url').text() : '',
		'keywords' : keywordsArr.length ? keywordsArr  : [],
		'private' : jQuery('#private').is(':checked') ? true : false
	};
	

	if (logged) {
			jQuery.ajax({
				type: 'POST',
				url: SAVE_URL,
				data: JSON.stringify(data),
				contentType: 'application/json',
				dataType:'json'})
			.done(f)
			.fail(function() {
				jQuery('.alert-danger').append('<p>Error connect to server. Please try later !</p>').show();
			});
	}
	// jQuery.post(baseUrl + '/cloud_storage/add_info_node',data, f);
}

jQuery(function() {
	jQuery("#btn_save2dropbox").button().click(addInfoNode);
});