
$(() ->
	'use strict'
	$('#fileupload').fileupload autoUpload : true
	$('#fileupload').fileupload 'option', 'redirect', window.location.href.replace(/\/[^\/]*$/, '/cors/result.html?%s') 
	
	$('#fileupload').fileupload 'option',
    	maxFileSize: 30000,
    	acceptFileTypes: /(\.|\/)(gif|jpe?g|png)$/i,
    	multipart : false,
    	process: [action : 'load',
    		action: 'resize', maxWidth: 260, maxHeight: 180,
    			action: 'save'
    	]
    
    $('#fileupload').each () ->	
    	that = this
    	resourceId = $('#logoResourceId').val()
    	
    	if resourceId.length isnt 0
    		actionUrl = this.action + '/'  + resourceId + '?' + Math.random()
    		$(this).find('.fileinput-button input').prop('disabled', true).parent().addClass('disabled')
    		
    		$.getJSON actionUrl, (result) ->
       			$(that).find('.fileinput-button input').prop('disabled', false).parent().removeClass('disabled')
       			if result and result.length
       				
       				$(that).fileupload('option', 'done').call(that, null, result : result)
)	

    			
    		
    	
    

    