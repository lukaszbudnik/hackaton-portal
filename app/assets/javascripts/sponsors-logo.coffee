
$(() ->
	'use strict'
	$('#fileupload').fileupload autoUpload : true
	$('#fileupload').fileupload 'option', 'redirect', window.location.href.replace(/\/[^\/]*$/, '/cors/result.html?%s') 
		
	$('#fileupload').bind 'fileuploadadded', (e, data) ->
		if (data.files.valid)
			$('#fileupload').fileupload 'hideUploadButton'
		

	$('#fileupload').bind 'fileuploaddestroy', (e, data) ->
		$('#fileupload').fileupload 'showUploadButton'
	
	$('#fileupload').fileupload 'option',
    	acceptFileTypes: /(\.|\/)(gif|jpe?g|png)$/i,
    	multipart : true
    
    $('#fileupload').each () ->	
    	that = this
    	resourceId = $('#logoResourceId').val()
    	
    	if resourceId.length isnt 0
    		actionUrl = this.action + '/'  + resourceId + '?' + Math.random()
    		$(that).fileupload 'hideUploadButton'
    			
    		$.getJSON actionUrl, (result) ->
       			if result and result.length       				
       				$(that).fileupload('option', 'done').call(that, null, result : result)    		
    	else
    		$(that).fileupload 'showUploadButton'       				
)

    			
    		
    	
    

    