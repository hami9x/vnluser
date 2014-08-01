var  tabId = false;

var getSelectedHandler = function(response) {
	var data = JSON.parse(response.data);
	var selectedHtml = data.html;
	var title = data.title.trim();
	var keywords = data.keywords;
	var url = data.url;
	var viewTabUrl = [ chrome.extension.getURL('info-node-editor.html') ].join('');
	chrome.tabs.create({url : viewTabUrl }, function(tab2) {
		setTimeout(function(){
			chrome.tabs.sendRequest(tab2.id, {'url': url, 'html': selectedHtml, 'title': title, 'keywords' : keywords });
		}, 666);
	});	
};
chrome.contextMenus.create(
{
	"title": "SaveHashTag", 
	"contexts" : [ "selection", "link", "image"],
	"onclick": function(info, tab) {
		//alert("item " + JSON.stringify(info) + " was clicked");
		chrome.tabs.getSelected(null, function(tab) {
			tabId = tab.id;
			chrome.tabs.sendRequest(tab.id, {method : "getSelectedHtml"}, getSelectedHandler);
		});
	}
});
chrome.browserAction.onClicked.addListener(function() {
    chrome.tabs.create({'url': "https://b.chk.vn"});
});