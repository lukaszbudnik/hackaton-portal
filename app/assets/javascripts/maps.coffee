initialize = () ->
    mapDiv = document.getElementById('map-canvas')
    map = new google.maps.Map(mapDiv, 
        center: new google.maps.LatLng(0, 0),
        zoom: 1,
        mapTypeId: google.maps.MapTypeId.ROADMAP
    )

    markerRedImg = '/assets/images/h_marker_red.png'
    markerGreenImg = '/assets/images/h_marker_green.png'
    markerBlueImg = '/assets/images/h_marker_blue.png'
    markerImages = {1: markerRedImg, 2: markerGreenImg, 3: markerBlueImg}

    google.maps.event.addListenerOnce(map, 'tilesloaded', addMarkers(map, markerImages))

addMarkers = (map, markerImages) ->
    $.getJSON('/hackathons.json', (data) ->
        latlngbounds = new google.maps.LatLngBounds()
        $.each(data, (key, val) ->
            latLng = new google.maps.LatLng(val.location.latitude, val.location.longitude);
            marker = new google.maps.Marker(position: latLng, map: map, icon: markerImages[val.status])
            latlngbounds.extend(latLng)
        )
        map.fitBounds(latlngbounds)
    )

google.maps.event.addDomListener(window, 'load', initialize);