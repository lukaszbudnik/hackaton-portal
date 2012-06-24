initialize = () ->
    mapDiv = document.getElementById('map-canvas')
    map = new google.maps.Map(mapDiv, 
        center: new google.maps.LatLng(0, 0),
        zoom: 1,
        mapTypeId: google.maps.MapTypeId.ROADMAP
    )

    google.maps.event.addListenerOnce(map, 'tilesloaded', addMarkers(map))

addMarkers = (map) ->
    $.getJSON('/hackathons.json', (data) ->
        latlngbounds = new google.maps.LatLngBounds()
        $.each(data, (key, val) ->
            latLng = new google.maps.LatLng(val.location[0].latitude, val.location[0].longitude);
            marker = new google.maps.Marker(position: latLng, map: map)
            latlngbounds.extend(latLng)
        )
        map.fitBounds(latlngbounds)
    )

google.maps.event.addDomListener(window, 'load', initialize);