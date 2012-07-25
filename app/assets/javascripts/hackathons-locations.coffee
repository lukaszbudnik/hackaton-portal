class HackathonLocations
  constructor: (@params) ->
  	@init(@params)
  
  initLocationsMap : () ->
  	
  	positionMarker = (latLng) ->
  		coords = ''
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
  init : (params) ->
  	that = this
  	$(document).ready () ->
  		$('#submitLocation').click () ->
  			$.ajax
  				type: 'POST',
  				url: params.saveLocationAction,
  				data: $('#locationForm').serialize(),
  				dataType : 'html',
  				complete : (xhr, textStatus) ->
  					if textStatus is 'error'
  						$('#locationBox').html($("<div>").append(xhr.responseText).html())
  						HackathonLocations.prototype.initLocationsMap.call(this)
  					else
  						$('#cancelLocation').trigger('click')
  						locationName = $('#locationName')
  						locationName.val($('#locationForm').find('#name').val())
  						locationName.typeahead('lookup')
  			false
  				
  		$('#hackathon-form').click () ->
  			if $('#locationId').val is '0'
  				$('#locationName').val ""
  		$('#locationName').typeahead
  			
  			sorter : (items) -> 
  				items
  			
  			, source : (typeahead, query) ->
  				term = $.trim query
  				$.getJSON params.findLocationAction
  					, term : term
  				, (data) ->
  					typeahead.process data
  			
  			, onselect: (item, previous_items) ->
  				$('#locationId').val item.id
  				
  			, noMatchFoundText: params.noMatchFoundText
  			
  			, onNoMatchFoundClick: () ->
  				$('.hackathon-location-modal').modal 'show'
  				$('#locationBox').load params.createLocationAction
  				HackathonLocations.prototype.initLocationsMap.call(this)
  				
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
  
  
  	  
				
 