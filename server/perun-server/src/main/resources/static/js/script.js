var map;
var payloadMarker, userMarker;
var payloadPosition, userPosition;
var mapViewSet = false;
var sensors = {};
var graph;

function loadLastData() {
    $.get("/data/last", function (data) {
        var lat, long, alt;
        data.forEach(function (element) {
            $("#sensor-" + element.sensorId + "-value").html(+element.value.toFixed(3));
            $("#sensor-" + element.sensorId + "-timestamp").html(formatDateTime(element.timestamp));
            switch (sensors[element.sensorId].type) {
                case "LAT":
                    lat = element.value;
                    break;
                case "LONG":
                    long = element.value;
                    break;
                case "ALT":
                    alt = element.value;
                    break;
            }

            var chart = sensors[element.sensorId].chart;
            var data = chart.data.datasets[0].data;
            var date = Date.parse(element.timestamp);
            if (data.length === 0 || data[data.length - 1].x !== date) {
                data.push({
                    x: date,
                    y: element.value
                });

                if (data.length > 20) {
                    data.shift();
                }
                chart.update();
            }
        });
        $("#last-time").html(new Date().toLocaleTimeString());
        if (lat && long && alt) {
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
                    weight: 6
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
            weight: 6
        }).addTo(map);
    }
    userPosition = [position.coords.latitude, position.coords.longitude, position.coords.altitude];
    refreshDistance();
}

function createSensorTable() {
    $.get("/sensor/list", function (data) {
        data.forEach(addSensorToTable);
        loadLastData();
    });
}

function addSensorToTable(sensor) {
    sensors[sensor.id] = sensor;
    $("#sensor-table tbody").append("<tr><td>" + getDescriptionForType(sensor.type) +
            "</td><td><a href='/graph.html?sensor=" + sensor.id + "' target='_blank'>" + sensor.name + "</a>"
            + "</td><td><span id='sensor-" + sensor.id + "-value'></span> " + getUnitForType(sensor.type)
            + "</td><td><span id='sensor-" + sensor.id + "-timestamp'></span>"
            + "</td><td><canvas id='sensor-" + sensor.id + "-graph' class='sensor-graph' height='70' width='250'></canvas>"
            + "</td></tr>");

    var ctx = document.getElementById("sensor-" + sensor.id + "-graph").getContext('2d');
    sensors[sensor.id].chart = new Chart(ctx, {
        type: 'scatter',
        options: {
            legend: {
                display: false
            },
            animation: {
                duration: 0
            },
            tooltips: {
                callbacks: {
                    label: function (tooltipItem, data) {
                        var date = new Date(tooltipItem.xLabel);
                        return date.toTimeString().split(' ')[0] + ": " + tooltipItem.yLabel + " " + getUnitForType(sensor.type);
                    }
                }
            },
            scales: {
                xAxes: [{
                        display: false
                    }]
            }
        },
        data: {
            datasets: [{
                    borderColor: "blue",
                    showLine: true,
                    data: []
                }]
        }
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

function createBigGraph() {
    var canvas = document.getElementById("graph");
    canvas.width = window.innerWidth - 10;
    canvas.height = window.innerHeight - 40;
    var ctx = canvas.getContext('2d');
    graph = new Chart(ctx, {
        type: 'scatter',
        options: {
            legend: {
                display: true
            },
            animation: {
                duration: 0
            },
            tooltips: {
                callbacks: {
                    label: function (tooltipItem, data) {
                        var sensor = data.datasets[tooltipItem.datasetIndex].sensor;
                        var date = new Date(tooltipItem.xLabel);
                        return date.toTimeString().split(' ')[0] + ": " + tooltipItem.yLabel + " " + getUnitForType(sensor.type);
                    }
                }
            },
            scales: {
                xAxes: [{
                        type: "time",
                        time: {
                            unit: 'minute'
                        }
                    }]
            }
        },
        data: {
            datasets: []
        }
    });
}

function toggleBigGraphData(sensorId, checked) {
    if (checked) {
        $.get("/sensor/history?from=2018-11-15&to=2018-12-01&sensorId=" + sensorId, function (data) {
            var elements = data.map(x => {
                return {x: Date.parse(x.timestamp), y: x.value};
            });
            var sensor = sensors[sensorId];
            graph.data.datasets.push({
                borderColor: "blue",
                showLine: true,
                data: elements,
                label: getDescriptionForType(sensor.type) + " / " + sensor.name,
                sensor: sensor
            });
            graph.update();
        });
    } else {
        var datasets = graph.data.datasets;
        datasets = datasets.filter(x => x.sensor.id !== sensorId);
        graph.data.datasets = datasets;
        graph.update();

    }
}

function loadSensorsToCheckboxes() {
    $.get("/sensor/list", function (data) {
        data.forEach(function (sensor) {
            $("#checkboxes").append("<input type='checkbox' id='sensor-" + sensor.id + "' value='" + sensor.id + "' onchange='toggleBigGraphData(" + sensor.id + ", this.checked);'/>"
                    + "<label for='sensor-" + sensor.id + "'>" + getDescriptionForType(sensor.type) + " / " + sensor.name + "</label>");
            sensors[sensor.id] = sensor;
        });
    });
}