function getUnitForType(type) {
    switch (type) {
        case "ALT":
        case "POS_ACCURACY":
            return "m";
        case "LONG":
        case "LAT":
        case "BEAR":
            return "°";
        case "PRESSURE":
            return "hPa";
        case "MAG_X":
        case "MAG_Y":
        case "MAG_Z":
            return "μT";
        case "ACC_X":
        case "ACC_Y":
        case "ACC_Z":
            return "m/s²";
        case "BAT_LVL":
        case "PHONE_SIGNAL":
            return "%";
        case "BAT_TEMP":
            return "°C";
        case "SPEED":
            return "m/s";
        default:
            return "";
    }
}

function getDescriptionForType(type) {
    switch (type) {
        case "ALT":
            return "Altitude";
        case "LONG":
            return "Longitude";
        case "LAT":
            return "Latitude";
        case "BEAR":
            return "Bearing";
        case "PRESSURE":
            return "Atmospheric pressure";
        case "MAG_X":
            return "Magnetic field X";
        case "MAG_Y":
            return "Magnetic field Y";
        case "MAG_Z":
            return "Magnetic field Z";
        case "ACC_X":
            return "Acceleration X";
        case "ACC_Y":
            return "Acceleration Y";
        case "ACC_Z":
            return "Acceleration Z";
        case "BAT_LVL":
            return "Battery level";
        case "BAT_TEMP":
            return "Battery temperature";
        case "SPEED":
            return "Speed";
        case "PHONE_SIGNAL":
            return "Phone signal";
        case "POS_ACCURACY":
            return "Position accuracy";
        default:
            return type;
    }
}

function formatDateTime(value) {
    var date = new Date(Date.parse(value));
    if (isNaN(date)) {
        date = new Date(value);
    }
    var late = ((new Date() - date) > 90 * 1000);
    return "<span style='color:" + (late ? "red" : "black") + "'>" + date.toLocaleTimeString() + "</span>";
}

function dateToString(d) {
    month = '' + (d.getMonth() + 1),
            day = '' + d.getDate(),
            year = d.getFullYear();

    if (month.length < 2)
        month = '0' + month;
    if (day.length < 2)
        day = '0' + day;

    return [year, month, day].join('-');
}

function calculateDistance(lat1, lon1, lat2, lon2) {
    var R = 6371; // Radius of the earth in km
    var dLat = deg2rad(lat2 - lat1);  // deg2rad below
    var dLon = deg2rad(lon2 - lon1);
    var a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
    var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    var d = R * c; // Distance in km
    return d;
}

function calculateBearing(lat1, lng1, lat2, lng2) {
    var dLon = (lng2 - lng1);
    var y = Math.sin(dLon) * Math.cos(lat2);
    var x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);
    var brng = rad2deg(Math.atan2(y, x));
    return 360 - ((brng + 360) % 360);
}

function deg2rad(deg) {
    return deg * (Math.PI / 180);
}

function rad2deg(rad) {
    return rad * 180 / Math.PI;
}

