$ ->
	entities = ['team', 'problem']
	actions = ['verify', 'approve', 'suspend', 'block', 'delete']
	for entity in entities
		do (entity) ->
			for action in actions
				do (action) ->
					$('.' + entity + '-' + action).live('click', ->
    					hackathonId = $(this).attr('data-hackathon-id')
    					entityId = $(this).attr('data-' + entity + '-id')
    					updateEntity(entity, hackathonId, entityId, action, $(this))
					)

doUpdateEntity = (entity, hackathonId, entityId, action, source) ->
	entities = entity + 's'
	$.ajax '/hackathons/' + hackathonId + '/' + entities + '/' + entityId + '/' + action,
		type: 'POST'
		success: (data, textStatus, jqXHR) ->
			if (action == 'delete')
				alert(Messages(entities + '.delete.success'))
			else
				alert(Messages(entities + '.update.success'))

			if (action == 'delete')
				selector = "div." + entity + "[data-" + entity + "-id='" + entityId + "']"
				$(selector).remove()
			else if (action == 'verify')
				successToDanger(entity, source, 'verify', 'block')
				updateStatusMessage(entity, entityId, Messages(entities + '.' + entity + 'Status.Approved'))
				addSuspendButton(entity, source, hackathonId, entityId)
			else if (action == 'suspend')
				dangerToSuccess(entity, source, 'suspend', 'approve')
				updateStatusMessage(entity, entityId, Messages(entities + '.' + entity + 'Status.Suspended'))
			else if (action == 'approve')
				successToDanger(entity, source, 'approve', 'suspend')
				updateStatusMessage(entity, entityId, Messages(entities + '.' + entity + 'Status.Approved'))
			else if (action == 'block')
				dangerToSuccess(entity, source, 'block', 'verify')
				updateStatusMessage(entity, entityId, Messages(entities + '.' + entity + 'Status.Blocked'))
				selectors = ['approve','suspend']
				for s in selectors
					do (s) ->
						$("div." + entity + "[data-" + entity + "-id='" + entityId + "'] ." + entity + "-" + s).remove()
		error: (data, textStatus, errorThrown) ->
			alert(Messages(entities + '.update.error'))	
						

updateEntity = (entity, hackathonId, entityId, action, source) ->

	if action == 'delete'
		HConfirmationModal.askConfirm.call this, (result) ->
			if result
				doUpdateEntity(entity, hackathonId, entityId, action, source)
	else
		doUpdateEntity(entity, hackathonId, entityId, action, source)
	
	

updateStatusMessage = (entity, entityId, message) ->
	selector = "div." + entity + "[data-" + entity + "-id='" + entityId + "'] ." + entity + "-status"
	$(selector).text(message)

successToDanger = (entity, source, success, danger) ->
	entities = entity + 's'
	source.removeClass(entity + '-' + success)
	source.removeClass('btn-success')
	source.addClass(entity + '-' + danger)
	source.addClass('btn-danger')
	source.text(Messages(entities + '.' + danger + '.label'))

dangerToSuccess = (entity, source, danger, success) ->
	entities = entity + 's'
	source.removeClass(entity + '-' + danger)
	source.removeClass('btn-danger')
	source.addClass(entity + '-' + success)
	source.addClass('btn-success')
	source.text(Messages(entities + '.' + success + '.label'))
	
addSuspendButton = (entity, source, hackathonId, entityId) ->
	entities = entity + 's'
	suspendButton = $('<a class="btn btn-danger ' + entity + '-suspend"  data-hackathon-id="' + hackathonId + '" data-' + entity + '-id="' + entityId + '" style="margin-right: 7px">' + Messages(entities + '.suspend.label') + '</a>')
	suspendButton.insertBefore(source)