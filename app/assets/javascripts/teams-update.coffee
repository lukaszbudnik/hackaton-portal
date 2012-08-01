$ -> 
	$('.team-verify').live('click', ->
    	hackathonId = $(this).attr('data-hackathon-id')
    	teamId = $(this).attr('data-team-id')
    	updateTeam(hackathonId, teamId, 'verify', $(this))
	)
	
	$('.team-approve').live('click', ->
    	hackathonId = $(this).attr('data-hackathon-id')
    	teamId = $(this).attr('data-team-id')
    	updateTeam(hackathonId, teamId, 'approve', $(this))
	)
	
	$('.team-suspend').live('click', ->
    	hackathonId = $(this).attr('data-hackathon-id')
    	teamId = $(this).attr('data-team-id')
    	updateTeam(hackathonId, teamId, 'suspend', $(this))
	)
	
	$('.team-block').live('click', ->
    	hackathonId = $(this).attr('data-hackathon-id')
    	teamId = $(this).attr('data-team-id')
    	updateTeam(hackathonId, teamId, 'block', $(this))
	)
	
	$('.team-delete').live('click', ->
    	hackathonId = $(this).attr('data-hackathon-id')
    	teamId = $(this).attr('data-team-id')
    	updateTeam(hackathonId, teamId, 'delete', $(this))
	)
	

updateTeam = (hackathonId, teamId, action, source) ->
	$.ajax '/hackathons/' + hackathonId + '/teams/' + teamId + '/' + action,
		type: 'POST'
		success: (data, textStatus, jqXHR) ->
			alert(Messages("teams.update.success"))
			if (action == 'delete')
				selector = "div.team[data-team-id='" + teamId + "']"
				$(selector).remove()
			else if (action == 'verify')
				successToDanger(source, 'verify', 'block')
				updateStatusMessage(teamId, Messages('teams.teamStatus.Approved'))
				addSuspendButton(source, hackathonId, teamId)
			else if (action == 'suspend')
				dangerToSuccess(source, 'suspend', 'approve')
				updateStatusMessage(teamId, Messages('teams.teamStatus.Suspended'))
			else if (action == 'approve')
				successToDanger(source, 'approve', 'suspend')
				updateStatusMessage(teamId, Messages('teams.teamStatus.Approved'))
			else if (action == 'block')
				dangerToSuccess(source, 'block', 'verify')
				updateStatusMessage(teamId, Messages('teams.teamStatus.Blocked'))
				selectors = ['approve','suspend']
				for s in selectors
					do (s) ->
						$("div.team[data-team-id='" + teamId + "'] .team-" + s).remove()
		error: (data, textStatus, errorThrown) ->
			alert(Messages("teams.update.error"))

updateStatusMessage = (teamId, message) ->
	selector = "div.team[data-team-id='" + teamId + "'] .team-status"
	$(selector).text(message)

successToDanger = (source, success, danger) ->
	source.removeClass('team-' + success)
	source.removeClass('btn-success')
	source.addClass('team-' + danger)
	source.addClass('btn-danger')
	source.text(Messages('teams.' + danger + '.label'))

dangerToSuccess = (source, danger, success) ->
	source.removeClass('team-' + danger)
	source.removeClass('btn-danger')
	source.addClass('team-' + success)
	source.addClass('btn-success')
	source.text(Messages('teams.' + success + '.label'))
	
addSuspendButton = (source, hackathonId, teamId) ->
	suspendButton = $('<a class="btn btn-danger team-suspend"  data-hackathon-id="' + hackathonId + '" data-team-id="' + teamId + '" style="margin-right: 7px">' + Messages("teams.suspend.label") + '</a>&nbsp; ')
	suspendButton.insertBefore(source)