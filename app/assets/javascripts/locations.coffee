class Locations
  constructor: (@params) ->
  
  MODE_CREATE = 0
  
  MODE_EDIT = 1
  
  LOCATION_MODAL_SELECTOR = '.hackathon-location-modal'
  LOCATIONS_CONTAINER_SELECTOR = '#locationsContainer'
  LOCATION_CONTAINER_IDX_ATTR = 'data-lc-id'

  initHackathonLocations : () ->
  	thisObj = this
  	$(document).ready () ->
  		thisObj.initLocationButtons()
  		thisObj.initTypeahead()
  		$(LOCATION_MODAL_SELECTOR).on 'show', () ->
  			lc = thisObj.locationContainerByIndex($(this).attr(LOCATION_CONTAINER_IDX_ATTR))
  			btnSubmit = $('#submitLocation')
  			modalTitle = $('.modal-title')
  			if thisObj.mode == MODE_EDIT
  				btnSubmit.text(Messages('global.update'))
  				modalTitle.text(Messages('locations.edit.title'))
  			else
  				btnSubmit.text(Messages('global.add'))
  				modalTitle.text(Messages('locations.create.title'))						
  
  initStandaloneLocations : () ->
  	thisObj = this
  	$(document).ready () ->
  		map = thisObj.initLocationsMap()

  isAdmin : () ->
  	$('#isAdmin').val() == "true"
  
  formatAdditionalHelp : (locationContainer) ->
  	retArr = []
  	fullAddress = locationContainer.find('[name$=".fullAddress"]').val()
  	retArr.push(fullAddress) if fullAddress
  	
  	city = locationContainer.find('[name$=".city"]').val()
  	retArr.push(city) if city
  	
  	country = locationContainer.find('[name$=".country"]').val()
  	retArr.push(country) if country
  	
  	if retArr.length > 0
  		text = retArr.join(", ")
  		locationContainer.find('.help-block *:not(:has("*"))').text(text)

  locationContainerByIndex : (idx) ->
  	$(LOCATIONS_CONTAINER_SELECTOR + ' .locationContainer[data-index="' + idx + '"]')
  
  locationContainerOf : (element) ->
  	element.closest('.locationContainer')
  
  isLocationContainerSet : (container) ->
  	container.find('[name$=".id"]').val() != '0'
  	
  rememberName : (container) ->
  	container.attr('old-name', container.find('[name$=".name"]').val())
  
  revertName : (container) ->
  	container.find('[name$=".name"]').val(container.attr('old-name'))
  	
  setCurrentValue : (container, value) ->
  	container.data('current-value', value)
  
  getCurrentValue : (container) ->
  	container.data('current-value');
  	

  invalidateEditButtons : () ->
    thisObj = this
    userId = this.params.userHackathonId 
    lsc = $(LOCATIONS_CONTAINER_SELECTOR)
    lsc.find('.locationContainer').each () ->
    	idField = $(this).find('[name$=".submitterId"]')
    	editHackathonLocation = $(this).find('.edit-hackathon-location')
    	if idField.val() is '0' or (idField.val() != userId and not thisObj.isAdmin())
    		editHackathonLocation.hide()
    	else
    		editHackathonLocation.show()
    		
    	
  initTypeahead : () ->
    thisObj = this
    params = this.params
    
    this.invalidateEditButtons()
    
    lsc = $(LOCATIONS_CONTAINER_SELECTOR)
    lsc.find('.locationContainer').each () ->
    	thisObj.formatAdditionalHelp($(this))
    	thisObj.rememberName($(this))
    	
    inputs = lsc.find('[name$=".name"]')
    inputs.typeahead
    	delay : 400
    	,sorter : (items) ->
    		items
    	, source : (typeahead, query) ->
    		term = $.trim query
    		$.getJSON params.findLocationAction
    			, term : term
    		, (data) ->
    			typeahead.process data
    	, onselect: (item, previous_items) ->
    		lc = thisObj.locationContainerOf(this.$element)
    		lc.find('[name$=".id"]').val(item.id)
    		lc.find('[name$=".city"]').val(item.city)
    		lc.find('[name$=".country"]').val(item.country)		
    		lc.find('[name$=".fullAddress"]').val(item.fullAddress)
    		lc.find('[name$=".submitterId"]').val(item.submitterId)
    		
    		thisObj.formatAdditionalHelp(lc)
    		thisObj.rememberName(lc)
    		thisObj.invalidateEditButtons()
    		
    	, noMatchFoundText: params.noMatchFoundText
    	
    	, onlookup : (query) ->
    	
    		thisObj.setCurrentValue(thisObj.locationContainerOf(this.$element), query)
    		
    	, onblur : () ->
    	
    		lc = thisObj.locationContainerOf(this.$element)
    		thisObj.revertName(lc)
    			
    	, onNoMatchFoundClick: () ->
    	
	    	thisObj.mode = MODE_CREATE 
	    	hlModal = $(LOCATION_MODAL_SELECTOR)
	    	lc = thisObj.locationContainerOf(this.$element)
	    	hlModal.attr(LOCATION_CONTAINER_IDX_ATTR, lc.attr('data-index'))
	    	hlModal.modal 'show'
	    	$('#locationBox').load params.createLocationAction, () ->
	    		$('#locationForm').find('#name').val(thisObj.getCurrentValue(lc))
	    		thisObj.initLocationsMap()

	    , render: (items) ->
	    	that = this
	    	items = $(items).map (i, item) ->
	    		i = $(that.options.item).attr('data-value', JSON.stringify item)
	    		if item.nomatchfound
	    			i.find('a').html item.value
	    		else
	    			i.find('a').html(that.highlighter(item.value)).append($("<p>")
	    			.addClass("hackathon-typeahead-address")
	    			.html('('+ that.highlighter(item.fullAddress) + ', ' + that.highlighter(item.city) + ', ' + that.highlighter(item.country) + ')'))
	    		i[0]
	    	items.first().addClass('active')
	    	this.$menu.html(items)
	    	this
	    , matcher : () ->
	    	true
  
  initLocationsMap : () ->
  	
  	positionMarker = (latLng) ->
  		map.setCenter(latLng)
  		map.setZoom(8)
  		marker.setOptions(position: latLng, map: map)
  	
  	errorAutoGeoLocation = (msg) ->
  		alert(msg)
  	
  	successAutoGeoLocation = (position) ->
  		lat = position.coords.latitude
  		lng = position.coords.longitude
  		map.setCenter(new google.maps.LatLng(lat, lng))
  		map.setZoom(7)
  		
  	mapDiv = document.getElementById('map-canvas')
  	geocoder = new google.maps.Geocoder()
  	marker = new google.maps.Marker()
  	map = new google.maps.Map mapDiv,
  		center: new google.maps.LatLng(0, 0),
  		zoom: 2,
  		mapTypeId: google.maps.MapTypeId.ROADMAP
  	
  
  	google.maps.event.addListener(map, 'click', (event) ->
    	$("input[name='latitude']").val(event.latLng.lat())
    	$("input[name='longitude']").val(event.latLng.lng())
    	geocoder.geocode('latLng': event.latLng, (results, status) ->
    		marker.setOptions(position: event.latLng, map: map)
    		if status == google.maps.GeocoderStatus.OK and results.length > 0
    		
    			result = results[0]
    			for ac in result.address_components
    				value = ac.long_name
    				streetNumberFound = true if $.inArray('street_number', ac.types) isnt -1
    				$("input[name='country']").val(value) if $.inArray('country', ac.types) isnt -1
    				$("input[name='city']").val(value) if $.inArray('locality', ac.types) isnt -1
    				$("input[name='postalCode']").val(value) if $.inArray('postal_code', ac.types) isnt -1 and  $.inArray('postal_code_prefix', ac.types) is -1
    				addressIdx = result.formatted_address.indexOf(',')
    				if addressIdx isnt -1 and $.inArray('route', ac.types) and streetNumberFound is true
    					$("input[name='fullAddress']").val(result.formatted_address.substr(0, addressIdx))
    			
    	)
    )
    
    currentLat = $("input[name='latitude']").val()
    currentLng = $("input[name='longitude']").val()
    if currentLat and currentLng
    	positionMarker(new google.maps.LatLng(currentLat, currentLng))
    else
    	if navigator.geolocation
    		navigator.geolocation.getCurrentPosition(successAutoGeoLocation, errorAutoGeoLocation)
    map
  
  	 	
  
  initLocationButtons : () ->
  		params = this.params
  		thisObj = this
  		
  		locationsContainer = $(LOCATIONS_CONTAINER_SELECTOR)
  		
	  	locationsContainer.on 'click', '.delete-hackathon-location', (evt) ->
	  		idx = thisObj.locationContainerOf($(this)).attr('data-index')
	  		target = $(evt.delegateTarget)
	  		target.load params.deleteHackathonLocationAction.replace('0', idx)
	  		, target.find(':input').serializeObject()
	  		, () ->
	  			thisObj.initTypeahead()
	  	locationsContainer.on 'click', '.edit-hackathon-location', (evt) ->
	  		thisObj.mode = MODE_EDIT
	  		lc = thisObj.locationContainerOf($(this))
	  		hlModal = $(LOCATION_MODAL_SELECTOR)	
	  		hlModal.attr(LOCATION_CONTAINER_IDX_ATTR, lc.attr('data-index'))
	  		hlModal.modal 'show'
	  		
	  		$('#locationBox').load '/locations/' +  lc.find('[name$=".id"]').val() + '/edit', () ->
	  			thisObj.initLocationsMap()
		

	  	locationsContainer.on 'click', '.add-hackathon-location', (evt) ->
	  		target = $(evt.delegateTarget)
	  		target.load params.addHackathonLocationAction
	  		, target.find(':input').serializeObject()
	  		, () ->
	  			thisObj.initTypeahead()
	  						
	  	$('#submitLocation').click (evt) ->
	  		
	  		if thisObj.mode == MODE_CREATE
	  			action = params.saveLocationAction
	  		else
	  			hlm = $(LOCATION_MODAL_SELECTOR)
	  			lc = thisObj.locationContainerByIndex(hlm.attr(LOCATION_CONTAINER_IDX_ATTR))		
	  			action = '/locations/' + lc.find('[name$=".id"]').val() + '/update'	
	  		$.ajax
	  			type: 'POST',
	  			url: action,
	  			data: $('#locationForm').serialize(),
	  			dataType : 'html',
	  			complete : (xhr, textStatus) ->
	  		
  					responseText = xhr.responseText
  					xhr.done((r) ->
  						responseText = r)
  					if textStatus is 'error'

  						$('#locationBox').html($("<div>").append(responseText).html())  						
  						thisObj.initLocationsMap()
  						
  					else
  						hlm = $(LOCATION_MODAL_SELECTOR)
  						hlm.modal('hide')
  						lc = thisObj.locationContainerByIndex(hlm.attr(LOCATION_CONTAINER_IDX_ATTR))
  						lcForm = $('#locationForm')
  						nameInput = lc.find('[name$=".name"]')
  						nameInput.val(lcForm.find('[name="name"]').val())
  						if thisObj.mode == MODE_CREATE
  							nameInput.typeahead('lookup')
  						else
  							lc.find('[name$=".city"]').val(lcForm.find('[name="city"]').val())
  							lc.find('[name$=".country"]').val(lcForm.find('[name="country"]').val())
  							lc.find('[name$=".postalCode"]').val(lcForm.find('[name="postalCode"]').val())
  							lc.find('[name$=".fullAddress"]').val(lcForm.find('[name="fullAddress"]').val())
  							thisObj.rememberName(lc)
  							thisObj.formatAdditionalHelp(lc)
	  		false
