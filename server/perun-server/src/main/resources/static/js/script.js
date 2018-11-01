var map;
var payloadMarker, userMarker;
var payloadPosition, userPosition;
var mapViewSet = false;


$(function () {
    createSensorTable();
    loadLastData();
    setInterval(loadLastData, 10000);
    setupMap();
    setInterval(function () {
        $("#current-time").html(new Date().toLocaleTimeString());
    }, 500);
    navigator.geolocation.watchPosition(showUserPosition);
});

function loadLastData() {
    $.get("/data/last", function (data) {
        var lat, long, alt;
        data.forEach(function (element) {
            $("#sensor-" + element.sensor.id + "-value").html(+element.value.toFixed(3));
            $("#sensor-" + element.sensor.id + "-timestamp").html(formatDateTime(element.timestamp));
            if (element.sensor.type === "LAT") {
                lat = element.value;
            } else if (element.sensor.type === "LONG") {
                long = element.value;
            } else if (element.sensor.type === "ALT") {
                alt = element.value;
            }
        });
        $("#last-time").html(new Date().toLocaleTimeString());
        if (lat && long) {
            if (!mapViewSet) {
                map.setView([lat, long], 16);
                mapViewSet = true;
            }
            if (payloadMarker) {
                payloadMarker.setLatLng(new L.LatLng(lat, long));
            } else {
                payloadMarker = L.circle([lat, long], {
                    color: '#0a0',
                    fillColor: '#3a3',
                    radius: 4
                }).addTo(map);
            }
            payloadPosition = [lat, long, alt];
            refreshDistance();
        }
    });
}

function showUserPosition(position) {
    if (userMarker) {
        userMarker.setLatLng(new L.LatLng(position.coords.latitude, position.coords.longitude));
    } else {
        userMarker = L.circle([position.coords.latitude, position.coords.longitude], {
            color: '#00a',
            fillColor: '#33a',
            radius: 4
        }).addTo(map);
    }
    userPosition = [position.coords.latitude, position.coords.longitude, position.coords.altitude];
    refreshDistance();
}

function createSensorTable() {
    $.get("/sensor/list", function (data) {
        data.forEach(function (element) {
            $("#sensor-table tbody").append("<tr><td>" + getDescriptionForType(element.type) +
                    "</td><td>" + element.name
                    + "</td><td><span id='sensor-" + element.id + "-value'></span> " + getUnitForType(element.type)
                    + "</td><td><span id='sensor-" + element.id + "-timestamp'></span>"
                    + "</td></tr>");
        });
    });
}

function setupMap() {
    map = L.map('map').setView([48.15, 17.1], 11);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(map);
}

function refreshDistance() {
    if (!userPosition || !payloadPosition) {
        return;
    }
    var horizontalDistance = calculateDistance(userPosition[0], userPosition[1], payloadPosition[0], payloadPosition[1]);
    horizontalDistance = +horizontalDistance.toFixed(3);
    var bearing = calculateBearing(userPosition[0], userPosition[1], payloadPosition[0], payloadPosition[1]).toFixed(0);
    var verticalDistance = (payloadPosition[2] - userPosition[2]).toFixed(0);
    $("#direction").html("Payload is " + horizontalDistance + " km away from you, in direction " + bearing + "°, " + verticalDistance + " m higher than you");
}