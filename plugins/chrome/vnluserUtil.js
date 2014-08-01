String.prototype.replaceAll = function(stringToFind, stringToReplace) {
  var temp = this;
  var index = temp.indexOf(stringToFind);
  while (index != -1) {
    temp = temp.replace(stringToFind, stringToReplace);
    index = temp.indexOf(stringToFind);
  }
  return temp;
};

// postMessage HTML5
window.addEventListener("message", function(event) {  
  // ...
}, false);


var vnluserUtil = function(path, callback) {
    var script = document.createElement('script');
    script.type = 'text/javascript';
    script.src = path;   
    if (callback instanceof Function) {
        script.onload = function(){
      callback.apply({}, []);
      console.log(path + ' loaded');
    };
    } 
  document.getElementsByTagName('head')[0].appendChild(script);
};

var vnluserUtil = {selectedHtml : ''};

vnluserUtil.cookie = function(key, value, options) {
  if (arguments.length > 1 && String(value) !== "[object Object]") {
    options = (typeof options === 'object') ? options : {};
    if (value === null || value === undefined) {
      options.expires = -1;
    }
    if (typeof options.expires === 'number') {
      var days = options.expires, t = options.expires = new Date();
      t.setDate(t.getDate() + days);
    }
    value = String(value);
    return (document.cookie = [encodeURIComponent(key), '=', options.raw ? value : encodeURIComponent(value),
      options.expires ? '; expires=' + options.expires.toUTCString() : '',
      options.path ? '; path=' + options.path : '', options.domain ? '; domain=' + options.domain : '',
      options.secure ? '; secure' : ''].join(''));
  }
  options = value || {};
  var result, decode = options.raw ? function(s) {
    return s;
  } : decodeURIComponent;
  return (result = new RegExp('(?:^|; )' + encodeURIComponent(key) + '=([^;]*)').exec(document.cookie)) ? decode(result[1]) : null;
};

vnluserUtil.getSelectedHtml = function() {
  var html = "";
  
  if (typeof window.getSelection != "undefined") {
    var sel = window.getSelection();
    if (sel.rangeCount) {
      var container = document.createElement("div");
      for (var i = 0, len = sel.rangeCount; i < len; ++i) {
        container.appendChild(sel.getRangeAt(i).cloneContents());
      }
      html = container.innerHTML;
    }
  } else if (typeof document.selection != "undefined") {
    if (document.selection.type == "Text") {
      html = document.selection.createRange().htmlText;
    }
  }
  if(html == "" && vnluserUtil.selectedHtml != ""){
    return vnluserUtil.selectedHtml;
  }
  return html;
};

vnluserUtil.selectedNodeHandler = function(e) {
  if (e.which === 3) {
    vnluserUtil.selectedHtml = jQuery("<p>").append(jQuery(this).eq(0).clone()).html();
    if( jQuery(this).get(0).nodeName === 'IMG' ){
      //TODO
    } else if( jQuery(this).get(0).nodeName === 'A' ){
      //TODO
    }
  }
};

vnluserUtil.getKeywords = function(){
  var meta = jQuery('meta[name="keywords"]');
  if(meta.length === 1 )
    return meta.attr('content');
  return '';
};

vnluserUtil.toAbsoluteHref = function(link, host) {
  //console.log("link:"+link);
  //console.log("host:"+host);

  var lparts = link.split('/');
  if (/http:|https:|ftp:/.test(lparts[0])) {
    // already abs, return
    return link;
  }

  var i, hparts = host.split('/');
    if (hparts.length > 3) {
    hparts.pop(); // strip trailing thingie, either scriptname or blank 
  }

  if (lparts[0] === '') { // like "/here/dude.png"
    host = hparts[0] + '//' + hparts[2];
    hparts = host.split('/'); // re-split host parts from scheme and domain only
    delete lparts[0];
  }

  for(i = 0; i < lparts.length; i++) {
    if (lparts[i] === '..') {
      // remove the previous dir level, if exists
      if (typeof lparts[i - 1] !== 'undefined') { 
      delete lparts[i - 1];
      } else if (hparts.length > 3) { // at least leave scheme and domain
      hparts.pop(); // stip one dir off the host for each /../
      }
      delete lparts[i];
    }
    if(lparts[i] === '.') {
      delete lparts[i];
    }
  }

  // remove deleted
  var newlinkparts = [];
  for (i = 0; i < lparts.length; i++) {
    if (typeof lparts[i] !== 'undefined') {
      newlinkparts[newlinkparts.length] = lparts[i];
    }
  }

  var absoluteHref = hparts.join('/') + '/' + newlinkparts.join('/');
  //console.log(absoluteHref);
  return absoluteHref;
}

if(location.href.indexOf('http') ===  0){
  var baseURL = location.href.replace(location.search,"");
  var hashIndex = baseURL.indexOf('#');
  if(hashIndex > 8){
    baseURL = baseURL.substring(0,hashIndex);
  }
  if(jQuery('base').length == 1){
    var baseHref = jQuery('base').attr('href');
    if(baseHref.indexOf('http')>=0){
      baseURL = baseHref; 
    }
  }

  jQuery('img[src],a[href]').mousedown(vnluserUtil.selectedNodeHandler);
  var imgs = jQuery('img:not([src^="http"])');
  imgs.each(function(){
    var img = jQuery(this);
    var src = img.attr('src');

    if(src){
      src = src.trim();
      if( src.indexOf('#') < 0 && src.indexOf(':') < 0 && src.indexOf('//') != 0){
        var fullSrc = vnluserUtil.toAbsoluteHref(src, baseURL);
        img.attr('src',fullSrc);
      } else if(src.indexOf('//') == 0 ){
        var fullSrc = location.protocol + src;
        img.attr('src',fullSrc);
      }
    }
  }); 
    
  var aNodes = jQuery('a:not([href^="http"])');
  
  aNodes.each(function(){
    var aNode = jQuery(this);
    var href = aNode.attr('href');
    if(href){
      href = href.trim();
      if( href.indexOf('#') < 0 && href.indexOf(':') < 0 && href.indexOf('//') != 0){
        var fullHref = vnluserUtil.toAbsoluteHref(href.trim(), baseURL);
        aNode.attr('href',fullHref);
      } else if(href.indexOf('//') == 0 ){
        var fullHref = location.protocol + href;
        aNode.attr('href',fullHref);
      }
    }   
  });
}

chrome.extension.onRequest.addListener(function(request, sender, sendResponse) {
  // alert('method: '+request.method);
  var m = request.method;
  if(m === 'getSelectedHtml'){
    var data = {};
    data.title = document.title.trim();
    data.html = vnluserUtil.getSelectedHtml();
    data.keywords = vnluserUtil.getKeywords();
    data.url = location.href;
    console.log(data);
    sendResponse({
      'data' : JSON.stringify(data)
    });
  } else if (m === 'getCurrentUrl') {
    sendResponse({
      href : location.href
    });
  }  else {
    sendResponse({}); // snub them.
  }
});

