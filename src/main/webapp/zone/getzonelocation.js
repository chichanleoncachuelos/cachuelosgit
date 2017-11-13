var currentMarker = null;

var currentLat = null;
var currentLng = null;

if (navigator.geolocation) {
        checkGeolocationByHTML5();
    } else {
        checkGeolocationByLoaderAPI(); // HTML5 not supported! Fall back to Loader API.
    }

    function checkGeolocationByHTML5() {
        navigator.geolocation.getCurrentPosition(function(position) {
            setMapCenter(position.coords.latitude, position.coords.longitude);
        }, function() {
            checkGeolocationByLoaderAPI(); // Error! Fall back to Loader API.
        });
    }

    function checkGeolocationByLoaderAPI() {
        if (google.loader.ClientLocation) {
            setMapCenter(google.loader.ClientLocation.latitude, google.loader.ClientLocation.longitude);
        } else {
            // Unsupported! Show error/warning?
        }
    }

    function setMapCenter(latitude, longitude) {
        w_gmap.getMap().setCenter(new google.maps.LatLng(latitude, longitude));
        
        document.getElementById('create:lat').value = latitude;
        document.getElementById('create:lng').value = longitude;
        
        currentLat=latitude;
        currentLng= longitude;
        document.getElementById('create:btnrefresh').click();
        document.getElementById('headerbuttons:btnrefreshmarker').click();
    }
    
    
    function checkGeolocationByHTML5Second() {
    	 setMapCenterSecond();
    }
    
    function setMapCenterSecond() {
        w_gmap.getMap().setCenter(new google.maps.LatLng(currentLat, currentLng));
    }
    
    
    function centermap() {    	    	
        checkGeolocationByHTML5Second();
    }
