initialize = () ->
    mapDiv = document.getElementById('map-canvas')
    map = new google.maps.Map(mapDiv, 
        center: new google.maps.LatLng(0, 0),
        zoom: 1,
        mapTypeId: google.maps.MapTypeId.ROADMAP
    )

    markerPlanningImg = '/assets/images/googlemap_h_blue.png'
    markerInProgressImg = '/assets/images/googlemap_h_yellow.png'
    markerFinishedImg = '/assets/images/googlemap_h_green.png'
    markerImages = {1: markerPlanningImg, 2: markerInProgressImg, 3: markerFinishedImg}

    google.maps.event.addListenerOnce(map, 'tilesloaded', addMarkers(map, markerImages))

hackathonDescription = (subject, date, city, fullAddress, id) ->
	"<b>#{subject}</b>  (#{date})
	<br/>#{city} #{fullAddress}<br/>
	>>  <a href=\"/hackathons/#{id}/news\">#{Messages('js.infobubble.more')}</a>"  
	
addMarkers = (map, markerImages) ->
    $.getJSON('/hackathons.json', (data) ->
        latlngbounds = new google.maps.LatLngBounds()
        $.each(data, (key, val) ->
            latLng = new google.maps.LatLng(val.location.latitude, val.location.longitude);
            marker = new google.maps.Marker(position: latLng, map: map, icon: markerImages[val.status])
            latlngbounds.extend(latLng)
            infoBubble = new InfoBubble({map: map, 
            content:  hackathonDescription(val.subject, val.date,
             val.location.city, val.location.fullAddress, 
             val.id)
            shadowStyle: 1,
            arrowPosition: 30,
            padding: 15,
            arrowSize: 6
            })
                      
            google.maps.event.addListener(marker, 'click', () ->
            	infoBubble.open(map, marker) if !infoBubble.isOpen()
            )
        )
        map.fitBounds(latlngbounds)
    )

google.maps.event.addDomListener(window, 'load', initialize);

