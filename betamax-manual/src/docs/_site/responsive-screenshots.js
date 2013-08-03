/**
* Based on https://github.com/phase2/casperjs-responsive
*/
var casper = require("casper").create({
    logLevel: "debug",
    verbose: false,
        viewportSize: {
        width: 1900,
        height: 1080
    }
});

var responsiveSizes = [
                    [1200, 1024],
                    [980, 1280],
                    [768, 1024],
                    [767, 1024],
                    [480, 320]
                    ];

var pathToSave = 'responsive-screenshot';

var d=new Date();

var currentDate = d.getDate();
var currentMonth = d.getMonth() + 1; //Months are zero based
var currentYear = d.getFullYear();
var currentHour = d.getHours();
var currentMinute = d.getMinutes();
var currentSeconds = d.getSeconds();

var current = currentYear + '-' +
            (currentMonth < 10 ? '0' : '') + currentMonth + '-' +
            (currentDate < 10 ? '0' : '') + currentDate + ' at ' +
            (currentHour < 10 ? '0' : '') + currentHour + '.' +
            (currentMinute < 10 ? '0' : '') + currentMinute + '.' +
            (currentSeconds < 10 ? '0' : '') + currentSeconds;


if (!casper.cli.has("url") || casper.cli.get("url") === true) {
    casper
        .echo("No url option passed")
        .echo("Usage: $ casperjs responsive-screenshot.js --url=URL_TO_VISIT")
        .exit();
}

var BASE_URL = casper.cli.get("url");

var slug = BASE_URL.replace(/[^a-zA-Z0-9]/gi, '-').replace(/^https?-+/, '');

casper.start();

casper.each(responsiveSizes, function(self, responsiveSize, i) {
    var width = responsiveSize[0];
    var height = responsiveSize[1];

    casper.wait(3000, function() {
        this.viewport(width, height);

        casper.thenOpen(BASE_URL, function() {
            var filename = pathToSave + '/' + current + '/' + slug + '-' + width + '-' + height + ".png";
            this.captureSelector(filename, 'body');
            this.echo(filename);
        });
    });

});

casper.run(function() {
    this.echo('Done.').exit();
});