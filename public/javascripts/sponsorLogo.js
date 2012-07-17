function updateResourceId(resourceId) {
	$(document).ready(function() {
		
	$('#logoResourceId').val(resourceId);
	})
}

$(function () {
    'use strict';

    // Initialize the jQuery File Upload widget:
    $('#fileupload').fileupload({autoUpload: true});

    // Enable iframe cross-domain access via redirect option:
    $('#fileupload').fileupload(
        'option',
        'redirect',
        window.location.href.replace(
            /\/[^\/]*$/,
            '/cors/result.html?%s'
        )
    );
    $('#fileupload').fileupload('option', {

        maxFileSize: 30000,
        acceptFileTypes: /(\.|\/)(gif|jpe?g|png)$/i,
		multipart : false,
		process: [
                   {
                       action: 'load',
                   },
                   {
                       action: 'resize',
                       maxWidth: 260,
                       maxHeight: 180
                   },
                   {
                       action: 'save'
                   }
               ]
    });
    
 
	$('#fileupload').each(function () {

			
	    var that = this;
	    var resourceId = $('#logoResourceId').val();
	    if(resourceId.length > 0) {
	    	var actionUrl = this.action + '/'  + resourceId + '?' + Math.random();
		
	    	$(this).find('.fileinput-button input')
	        .prop('disabled', true)
	        .parent().addClass('disabled');	
	        $.getJSON(actionUrl , function (result) {
	    	$(that).find('.fileinput-button input')
	            .prop('disabled', false)
	            .parent().removeClass('disabled');			
	            if (result && result.length) {
	                $(that).fileupload('option', 'done')
	                    .call(that, null, {result: result});
	            }
	        });
	    }

	});
 
    
});
