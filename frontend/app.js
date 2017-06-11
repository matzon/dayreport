var map;

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
    var report = staticdata;
    //$.getJSON( 'reports/FYN/2017-06-01.json', function( report ) {
      console.log('Retrieved dayreport: ' + report.region + '/' + report.date )
      setMarkers(report.dayReportEntries);
    //});
}

function pad(s) {
    return (s < 10) ? '0' + s : s;
}

function formatEpoch(_date) {
    if(_date) {
        var dateObj = new Date(_date);
        return dateObj.getFullYear() + '-' + pad((dateObj.getMonth()+1)) + '-' + pad(dateObj.getDay());
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

      var coords = dayReportEntries[i].latlng;
      var latLng = new google.maps.LatLng(coords[0],coords[1]);
      var marker = new google.maps.Marker({
        position: latLng,
        map: map,
        clickable: true
      });
      var infowindow = new google.maps.InfoWindow();

      google.maps.event.addListener(marker, 'click', (function(marker, content, infowindow) {
        return function() {
            infowindow.setContent(content);
            infowindow.open(map, marker);
        };
      })(marker, contentString, infowindow));
    }
}

var staticdata = {
                   "id": "9c948ec5-fb0b-4c15-9310-8854a698aa58",
                   "region": "FYN",
                   "date": 1496268000000,
                   "dayReportEntries": [
                     {
                       "id": "03bce3d0-7bc9-4444-9758-d9237d6fc524",
                       "region": "FYN",
                       "type": "Indbrud i forretning",
                       "zipCode": "5000",
                       "city": "Odense",
                       "location": "-",
                       "reported": 1496304900000,
                       "started": 1496251800000,
                       "ended": 1496304000000,
                       "description": "Indstigning bagdør, som var sparket ind. Bagdør gav adgang til baglokale, som også fungerer som lager. Alt var gennemrodet, kasser var smidt på gulvet, alle kasser med værdi var tømt. Kaffemaskine var ødelagt. Indgang til bagdør var via baggård, som var aflåst, men gerningsmand formentlig hoppet over hegnet. Stjålet skærmglas til iPads.",
                       "latlng": [55.4098, 10.4033]
                     },
                     {
                       "id": "083e69ed-e7aa-4473-9764-678fc23174ad",
                       "region": "FYN",
                       "type": "Brugstyveri af reg. motorcykel/scooter",
                       "zipCode": "5000",
                       "city": "Odense",
                       "location": "-",
                       "reported": 1496320560000,
                       "started": 1496037600000,
                       "ended": 1496257200000,
                       "description": "Tyveri af AB23410, sort motorcykel af mrk. Ledow Borelli, årgang 2007. Stjålet uden rette nøgle. ",
                       "latlng": [55.4098, 10.4033]
                     },
                     {
                       "id": "10297a70-5f8a-4102-8c26-c81b3c8e5324",
                       "region": "FYN",
                       "type": "Vold mod polititjenestemand",
                       "zipCode": "5000",
                       "city": "Odense",
                       "location": "-",
                       "reported": 1496358900000,
                       "started": 1496357580000,
                       "ended": null,
                       "description": "I forbindelse med løsladelse bed sigtede den forurettede polititjenestemand 3 gange. ",
                       "latlng": [55.4098, 10.4033]
                     },
                     {
                       "id": "390ae059-0c21-4e37-b666-b42b33dd5241",
                       "region": "FYN",
                       "type": "Butikstyveri",
                       "zipCode": "5000",
                       "city": "Odense",
                       "location": "-",
                       "reported": 1496307000000,
                       "started": 1496307000000,
                       "ended": null,
                       "description": "Butikstyv havde været inde i forretning i ca. 3 kvarter. Hun betalte for en beklædningsgenstand, hvorefter hun forsøgte at forlade butikken med tasken fuld af tøj, der ikke var betalt for. Alarm blev udløst da hun forsøgte at forlade stedet.",
                       "latlng": [55.4098, 10.4033]
                     },
                     {
                       "id": "43156f83-0a2e-4b99-9169-0ae431bd2a75",
                       "region": "FYN",
                       "type": "Indbrud i privat beboelse",
                       "zipCode": "5550",
                       "city": "Langeskov",
                       "location": "Grøntoften",
                       "reported": 1496307540000,
                       "started": 1496235600000,
                       "ended": 1496266200000,
                       "description": "Indstigning via hoveddør hvor gerningsmænd havde ødelagt låsen for at komme ind. Der var stjålet dab radio, smykker, sparegris, medicin, sølvtøj. ",
                       "latlng": [55.3598167, 10.593153]
                     },
                     {
                       "id": "4d806858-e69c-443e-9a84-8a8f331200e9",
                       "region": "FYN",
                       "type": "Hærværk, graffiti",
                       "zipCode": "5700",
                       "city": "Svendborg",
                       "location": "Møllergade",
                       "reported": 1496318700000,
                       "started": 1495058400000,
                       "ended": 1495092900000,
                       "description": "På træ port ind til gård blev med grøn spraymaling skrevet diverse tegn/tags. På rød mur til højre for porten blev med grøn spraymaling skrevet diverse tegn.",
                       "latlng": [55.0674, 10.6073]
                     },
                     {
                       "id": "5f41069f-a14c-4384-8553-7302c71886d2",
                       "region": "FYN",
                       "type": "Indbrud i kælder-/lofts-/pulterrum",
                       "zipCode": "5000",
                       "city": "Odense",
                       "location": "-",
                       "reported": 1496340600000,
                       "started": 1496262600000,
                       "ended": 1496326500000,
                       "description": "Stjålet fluestang mrk. Hardy Demon, fluestang mrk. Scierra, fluestang mrk. Guideline, fluestang mrk. TFO, fiskehjul mrk. Sage, fiskehjul mrk. Lamson, div. fiskegrej (fluebokse m.v.), løbesko mrk. Asics, løbesko mrk. Saucony, bore-/skruemaskine mrk. Dewalt, en kasse Cider. ",
                       "latlng": [55.4098, 10.4033]
                     },
                     {
                       "id": "5ff8e950-c87f-4008-bd46-869ffe792f46",
                       "region": "FYN",
                       "type": "Hærværk",
                       "zipCode": "5000",
                       "city": "Odense",
                       "location": "-",
                       "reported": 1496321460000,
                       "started": 1496246400000,
                       "ended": 1496296800000,
                       "description": "Fjernet printplade i kompressor så køleanlæg til blomsterbutik blev stoppet. ",
                       "latlng": [55.4098, 10.4033]
                     },
                     {
                       "id": "92957aeb-b8e7-4751-b9df-7f0827cab15c",
                       "region": "FYN",
                       "type": "Indbrud i forretning",
                       "zipCode": "5000",
                       "city": "Odense",
                       "location": "-",
                       "reported": 1496293620000,
                       "started": 1496239200000,
                       "ended": 1496293200000,
                       "description": "Indstigning: lås på port ind til gård var opbrudt. Stjålne genstande: Intet stjålet. ",
                       "latlng": [55.4098, 10.4033]
                     },
                     {
                       "id": "9b2a36ca-6227-45d4-a5f8-deb67d7d0967",
                       "region": "FYN",
                       "type": "Indbrud i privat beboelse",
                       "zipCode": "5240",
                       "city": "Odense",
                       "location": "-",
                       "reported": 1496301960000,
                       "started": 1496293200000,
                       "ended": 1496301600000,
                       "description": "Indstigning: Formodentlig gennem terrassedør, blodspor på terrassedør og gulvet efter gerningsmanden som formentlig var blevet bidt af hunden i huset.",
                       "latlng": [55.4166429, 10.454395]
                     },
                     {
                       "id": "aa740ce8-10c5-4b19-80c9-64464bfdde09",
                       "region": "FYN",
                       "type": "Brugstyveri af reg. personbil",
                       "zipCode": "5220",
                       "city": "Odense",
                       "location": "-",
                       "reported": 1496290080000,
                       "started": 1496235900000,
                       "ended": 1496290080000,
                       "description": "Hvid Toyota Yaris fra hjemmeplejen, stjålet med rette nøgle. Fundet på Herluf Trolles vej, Odense.",
                       "latlng": [55.3632242, 10.4896986]
                     },
                     {
                       "id": "c3bd60ad-8ad6-40dd-9e6b-75940a68bddb",
                       "region": "FYN",
                       "type": "Tyveri fra lastbil/varebil",
                       "zipCode": "5580",
                       "city": "Nørre",
                       "location": "-",
                       "reported": 1496310840000,
                       "started": 1496253600000,
                       "ended": 1496296800000,
                       "description": "Plombe opbrudt på anhænger. Stjålet tøj fra firmaet Stylepit.",
                       "latlng": [55.4724, 9.8733]
                     },
                     {
                       "id": "cdbd6729-a0c1-4097-9e6d-7fb5a4fd1c11",
                       "region": "FYN",
                       "type": "Indbrud i privat beboelse",
                       "zipCode": "5700",
                       "city": "Svendborg",
                       "location": "Svinget",
                       "reported": 1496339100000,
                       "started": 1496124000000,
                       "ended": 1496332800000,
                       "description": "Kældervindue forsøgt opbrudt. Rude knust og tegn på opbrydning af vinduet. Intet stjålet. ",
                       "latlng": [55.0674, 10.6073]
                     },
                     {
                       "id": "e2e00a29-6dd5-431b-977a-bb29f2194ab2",
                       "region": "FYN",
                       "type": "Tyveri fra lejlighed/værelse/etageejendom",
                       "zipCode": "5881",
                       "city": "Skårup",
                       "location": "-",
                       "reported": 1496328240000,
                       "started": 1496311200000,
                       "ended": 1496325600000,
                       "description": "Chatol opbrudt. Stjålet lydkort mrk. Digidesign 003, samt konvolut med kontanter.",
                       "latlng": [55.0829881, 10.6911431]
                     }
                   ]
                 };