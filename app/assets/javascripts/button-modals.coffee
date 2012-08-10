$ ->
	$('.confirm-action').click (evt, data) ->
		
		that = $(this)
		if not data
			HConfirmationModal.askConfirm.call this, (result) ->
				if result
					that.trigger('click', true)
				else
					
			false

HConfirmationModal =
	askConfirm : (handler) ->
		bootbox.confirm Messages("global.confirm"), Messages("global.cancel"), Messages("global.ok"), handler
					
