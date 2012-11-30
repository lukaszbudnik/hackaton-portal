initialize = (a) ->
    mapDiv = document.getElementById('map-canvas')
    map = new google.maps.Map(mapDiv, 
        center: new google.maps.LatLng(0, 0),
        zoom: 15,
        mapTypeId: google.maps.MapTypeId.ROADMAP
    )

    markerPlanningImg = '/assets/images/googlemap_h_blue.png'
    markerInProgressImg = '/assets/images/googlemap_h_yellow.png'
    markerFinishedImg = '/assets/images/googlemap_h_green.png'
    markerImages = {1: markerPlanningImg, 2: markerInProgressImg, 3: markerFinishedImg}

    selector = "dl[data-hackathon-id]"

    hackathonId = $(selector).attr('data-hackathon-id') 

    google.maps.event.addListenerOnce(map, 'tilesloaded', addMarker(hackathonId, map, markerImages))

addMarker = (hackathonId, map, markerImages) ->
    $.getJSON('/hackathon.json/' + hackathonId, (data) ->
        latlngbounds = new google.maps.LatLngBounds()
        for lc in data.locations
        	latLng = new google.maps.LatLng(lc.latitude, lc.longitude)
        	latlngbounds.extend(latLng)
        	marker = new google.maps.Marker(position: latLng, map: map, icon: markerImages[data.status])
        if (data.locations.length > 1)
        	map.fitBounds(latlngbounds)
        else
        	map.setCenter(new google.maps.LatLng(data.locations[0].latitude, data.locations[0].longitude))
 			  	
    )

google.maps.event.addDomListener(window, 'load', initialize);

