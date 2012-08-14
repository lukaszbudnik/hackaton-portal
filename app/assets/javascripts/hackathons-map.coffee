initialize = () ->
    
    $.getJSON '/hackathons.json', (data) ->
    
    	mapDiv = document.getElementById('map-canvas')
    	if not data.length
    		$(mapDiv).hide()
	    	return
	    	
	    map = new google.maps.Map(mapDiv, 
	        center: new google.maps.LatLng(0, 0),
	        zoom: 1,
	        mapTypeId: google.maps.MapTypeId.ROADMAP
	    )
	
	    markerPlanningImg = '/assets/images/googlemap_h_blue.png'
	    markerInProgressImg = '/assets/images/googlemap_h_yellow.png'
	    markerFinishedImg = '/assets/images/googlemap_h_green.png'
	    markerImages = {1: markerPlanningImg, 2: markerInProgressImg, 3: markerFinishedImg}
	
	    google.maps.event.addListenerOnce(map, 'tilesloaded', addMarkers(map, markerImages, data))

addMarkers = (map, markerImages, data) ->

    latlngbounds = new google.maps.LatLngBounds()
    $.each(data, (key, hInfo) ->
    	i = 0
    	for lc in hInfo.locations
            latLng = new google.maps.LatLng(lc.latitude, lc.longitude)
            marker = new google.maps.Marker(position: latLng, map: map, icon: markerImages[hInfo.status])
            latlngbounds.extend(latLng)
            infoBubble = new InfoBubble({map: map, 
            content:  tmpl("hackathonDescTmpl", {idx : i, h : hInfo, map : map}),
            shadowStyle: 1,
            minHeight: 70,
            minWidth: 230,
            arrowPosition: 30,
            padding: 15,
            arrowSize: 6
            })
            i++
            ((m, b) -> 
            	google.maps.event.addListener(m, 'click', () ->
            		b.open(map, m) if !b.isOpen()
            	)
            )(marker, infoBubble)
                  
    )
    map.fitBounds(latlngbounds)
    

google.maps.event.addDomListener(window, 'load', initialize);

