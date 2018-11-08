var map;
var reportData = {};
var selectedRegion = 'FYN';
var selectedReport = {};

function initMap() {
    map = new google.maps.Map(document.getElementById('map'), {
        zoom: 7,
        center: new google.maps.LatLng(55.4038,10.4024),
        mapTypeId: 'terrain',
        title: 'Døgnrapporter'
        }
    );
    loadData();
}

function loadData() {
    $.getJSON( 'reports/summary.json', function( report ) {
      var regionCount = Object.keys(report);
      console.log('Retrieved dayreport for ' + regionCount);
      $.each(report, function(region, dates) {
          console.log('Processing ' + region + ' for ' + dates.length + ' dates');
          updateReportData(region, dates);
      });

      // find most recent report
      var promise = findMostRecentReport(reportData[selectedRegion], selectedRegion);
      $.when(promise).then(function (region, reportDate, report) {
        selectedReport = report;
        setMarkers(report.dayReportEntries);
        console.log("Report loaded for " + region + " at " +reportDate + " with " + report.dayReportEntries.length + " entries");
      });
    });
}

function updateReportData(region, dates) {
    reportData[region] = dates.sort();
}

function findMostRecentReport(reports, region) {
    var def = $.Deferred();
    var reportDate = reports[reports.length-1];
    $.getJSON('reports/' + region + '/' + reportDate + '.json', function(data) {
       def.resolve(region, reportDate, data);
    });
    return def.promise();
}

function pad(s) {
    return (s < 10) ? '0' + s : s;
}

function formatEpoch(_date) {
    if(_date) {
        var dateObj = new Date(_date);
        return (dateObj.getFullYear() + '-' +
            pad((dateObj.getMonth()+1)) + '-' +
            pad(dateObj.getDate()) + ' ' +
            pad(dateObj.getHours()) + ':' +
            pad(dateObj.getMinutes()));
    } else {
        return '-';
    }
}

function formatLocation(_report) {
    var location = '';
    if(_report.location !== '-') {
        location = ' - ' + _report.location;
    }
    return _report.zipCode + ', ' + _report.city + location;
}

var lastInfoWindow;

// Loop through the results array and place a marker for each set of coordinates.
function setMarkers (dayReportEntries) {
    for (var i = 0; i < dayReportEntries.length; i++) {

    // handlebar...
    var contentString = '<div class="dayreport-infowindow">' +
                          '<h1 class="dayreport-infowindow-header-type">'  +     dayReportEntries[i].type                    + '</h1>' +
                          '<h3 class="dayreport-infowindow-header-place">' +     formatLocation(dayReportEntries[i])         + '</h3>' +
                          '<p class="dayreport-infowindow-body">' +
                            '<ul class="dayreport-time">' +
                              '<li><span class="dayreport-time-header">Rappoteret: </span><span class="dayreport-time-value">' +     formatEpoch(dayReportEntries[i].reported)   + '</span></li>' +
                              '<li><span class="dayreport-time-header">Start: </span><span class="dayreport-time-value">'      +     formatEpoch(dayReportEntries[i].started)    + '</span></li>' +
                              '<li><span class="dayreport-time-header">Afsluttet: </span><span class="dayreport-time-value">'  +     formatEpoch(dayReportEntries[i].ended)      + '</span></li>' +
                            '</ul>' +
                            '<span class="dayreport-infowindow-description">' +  dayReportEntries[i].description             + '</span>' +
                          '</p>'
                        '</div>';

      console.log("Adding content: " + contentString);

      var latLng = new google.maps.LatLng(dayReportEntries[i].latitude,dayReportEntries[i].longtitude);
      var marker = new google.maps.Marker({
        position: latLng,
        map: map,
        clickable: true
      });
      var infowindow = new google.maps.InfoWindow();

      google.maps.event.addListener(marker, 'click', (function(marker, content, infowindow) {
        return function() {
            if(lastInfoWindow) {
                lastInfoWindow.close();
            }
            lastInfoWindow = infowindow;
            infowindow.setContent(content);
            infowindow.open(map, marker);
        };
      })(marker, contentString, infowindow));
    }
}