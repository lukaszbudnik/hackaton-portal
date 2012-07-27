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
        latLng = new google.maps.LatLng(data.location.latitude, data.location.longitude);
        marker = new google.maps.Marker(position: latLng, map: map, icon: markerImages[data.status])
        map.setCenter(latLng)
    )

google.maps.event.addDomListener(window, 'load', initialize);

