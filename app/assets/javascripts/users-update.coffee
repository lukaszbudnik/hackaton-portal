$ -> 
	$('.change-is-admin').live('click', ->
    	userId = $(this).attr('data-user-id')
    	isAdmin = $(this).attr('data-is-admin')
    	if (isAdmin == '1')
    		newIsAdmin = 0
    	else
    		newIsAdmin = 1
    	updateUser(userId, newIsAdmin, $(this))
	)

updateUser = (userId, isAdmin, source) ->
	$.ajax '/users/' + userId + '/update/' + isAdmin,
		type: 'POST'
		success: (data, textStatus, jqXHR) ->
			source.attr('data-is-admin', isAdmin)
			if (isAdmin == 1)
				humanReadable = 'true'
			else
				humanReadable = 'false'
			selector = "span.human-readable-is-admin[data-user-id='" + userId + "']"
			$(selector).html(humanReadable)
			alert(Messages("users.update.success"))
		error: (data, textStatus, errorThrown) ->
			alert(Messages("users.update.error"))
