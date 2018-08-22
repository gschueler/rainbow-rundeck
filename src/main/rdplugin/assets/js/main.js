jQuery(function(){

	var pluginName = MY_PLUGINS['rainbow-rundeck'];
	var icon_ref = rundeckPage.pluginBaseUrl(pluginName) + '/icon.png';
	jQuery('.rdicon.app-logo').each(function(elem){
		jQuery(this).addClass('rainbow-delite');
	});
});