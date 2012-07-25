$ -> 
	$('.change-is-admin').live('click', ->
    	userId = $(this).attr('data-user-id')
    	isAdmin = $(this).attr('data-is-admin')
    	if (isAdmin == '1')
    		newIsAdmin = 0
    	else
    		newIsAdmin = 1
    	updateIsAdmin(userId, newIsAdmin, $(this))
	)
	
	$('.change-is-blocked').live('click', ->
    	userId = $(this).attr('data-user-id')
    	isBlocked = $(this).attr('data-is-blocked')
    	if (isBlocked == '1')
    		newIsBlocked = 0
    	else
    		newIsBlocked = 1
    	updateIsBlocked(userId, newIsBlocked, $(this))
	)

updateIsAdmin = (userId, isAdmin, source) ->
	$.ajax '/users/' + userId + '/isAdmin/' + isAdmin,
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

updateIsBlocked = (userId, isBlocked, source) ->
	$.ajax '/users/' + userId + '/isBlocked/' + isBlocked,
		type: 'POST'
		success: (data, textStatus, jqXHR) ->
			source.attr('data-is-blocked', isBlocked)
			if (isBlocked == 1)
				humanReadable = 'true'
			else
				humanReadable = 'false'
			selector = "span.human-readable-is-blocked[data-user-id='" + userId + "']"
			$(selector).html(humanReadable)
			alert(Messages("users.update.success"))
		error: (data, textStatus, errorThrown) ->
			alert(Messages("users.update.error"))
