var currentPage = 1;
var lastPage = 1;

function drawPager(totalNumber, limit, elementId) {
    var p = document.createElement('p');
    var prev = '';
    var next = '';
    if (currentPage > 1) {
        prev = '<img id="qtFirst" src="/plugin/quality-trends/go-first.png"/><img id="qtPrevious" src="/plugin/quality-trends/go-previous.png"/> ';
    }
    if (currentPage < Math.ceil(totalNumber/limit)) {
        next = ' <img id="qtNext" src="/plugin/quality-trends/go-next.png"/><img id="qtLast" src="/plugin/quality-trends/go-last.png"/>';
    }
    p.innerHTML = prev + 'Page ' + currentPage + ' of ' + Math.ceil(totalNumber/limit) + next;
    $(elementId).appendChild(p);
    if(prev != '') {
        $('qtFirst').observe('click', goToFirstPage);
        $('qtPrevious').observe('click', goToPreviousPage);
    }
    if(next != '') {
        $('qtLast').observe('click', goToLastPage);
        $('qtNext').observe('click', goToNextPage);
    }
}

YUI().use('node', function(Y) {

    function init() {
        // The DOM is ready
        draw();
    }

     Y.on("domready", init);
});

function draw() {
    it.getSeverities(function (t) {
        var sev = t.responseObject();

        var currentSeverities;

        YUI().use('charts', function (Y) {
            // Create data
            currentSeverities = [
                {severity: "Info", amount: sev.info},
                {severity: "Warnings", amount: sev.warnings},
                {severity: "Errors", amount: sev.errors}
            ];

            new Y.Chart({
                render: "#severity_chart",
                categoryKey: "severity",
                seriesKeys: ["amount"],
                dataProvider: currentSeverities,
                type: "pie",
                seriesCollection:[
                    {
                        categoryKey: "severity",
                        valueKey: "amount",
                        styles: {
                            fill: {
                                colors: [
                                    "#ffff66",
                                    "#ffcc00",
                                    "#cc0000"
                                ]
                            }
                        }
                    }
                ]
            });
        });

        it.getPreviousSeverities(function (t2) {
            var psev = t2.responseObject();

            YUI().use('datatable-base', function (Y) {
                var calcImprovement = function (o) {
                    var value = o.record.getValue("Amount") - o.record.getValue("Previous");

                    var color = "black";
                    if (value > 0) color = "red";
                    if (value < 0) color = "green";

                    return '<font color="' + color + '">' + value + '</font>';
                };

                var calcColor = function (o) {
                    switch (o.rowindex) {
                        case 0:
                            return '<div style="background-color: #ffff66; height:11px; width:11px; border: 1px solid black; margin-left:auto; margin-right:auto;">&nbsp;</div>';
                            break;
                        case 1:
                            return '<div style="background-color: #ffcc00; height:11px; width:11px; border: 1px solid black; margin-left:auto; margin-right:auto;">&nbsp;</div>';
                            break;
                        case 2:
                            return '<div style="background-color: #cc0000; height:11px; width:11px; border: 1px solid black; margin-left:auto; margin-right:auto;">&nbsp;</div>';
                            break;
                    }
                };

                var cols = [
                        {key: "Color", formatter: calcColor},
                        "Severity",
                        "Amount",
                        { key: "Improvement", formatter: calcImprovement }
                    ];
                var data = [
                        {Severity: "Info", Amount: currentSeverities[0].amount, Previous: psev.info},
                        {Severity: "Warnings", Amount: currentSeverities[1].amount, Previous: psev.warnings},
                        {Severity: "Errors", Amount: currentSeverities[2].amount, Previous: psev.errors}
                    ];
                new Y.DataTable.Base({
                        columnset: cols,
                        recordset: data
                    }).render("#severity_table");
            });
        });
    });

    it.getParsers(function (t) {
        var par = t.responseObject();

        var currentParsers;

        var parserPieGraph;

        YUI().use('charts', function (Y) {
            // Create data
            currentParsers = par;

            parserPieGraph = new Y.Chart({
                render:"#parser_chart",
                categoryKey:"parser",
                seriesKeys:["amount"],
                dataProvider:currentParsers,
                type:"pie",
                seriesCollection:[
                    {
                        categoryKey:"parser",
                        valueKey:"amount"
                    }
                ]
            });
        });

        it.getPreviousParsers(function (t2) {
            var ppar = t2.responseObject();

            // Find all the parsers
            var cParsers = new Hash();
            var pParsers = new Hash();
            for (var i = 0; i < par.length; i++) {
                cParsers.set(par[i].parser, par[i].amount);
            }
            for (i = 0; i < par.length; i++) {
                pParsers.set(ppar[i].parser, ppar[i].amount);
            }
            var allParsers = cParsers.merge(pParsers);

            var parserData = [];

            allParsers.each(function (pair) {
                var parser = pair.key;

                var current = 0;
                if (cParsers.keys().indexOf(parser) >= 0) {
                    current = cParsers.get(parser);
                }

                var previous = 0;
                if (pParsers.keys().indexOf(parser) >= 0) {
                    previous = pParsers.get(parser);
                }

                parserData.push({Parser:parser, Current:current, Previous:previous});
            });

            YUI().use('datatable-base', function (Y) {
                var calcParserImprovement = function (o) {
                    var value = o.record.getValue("Current") - o.record.getValue("Previous");

                    var color = "black";
                    if (value > 0) color = "red";
                    if (value < 0) color = "green";

                    return '<font color="' + color + '">' + value + '</font>'
                };

                var calcParserColor = function (o) {
                    var p = o.record.getValue("Parser");

                    var index = cParsers.keys().indexOf(p);

                    if (index >= 0) {
                        var defaultColors = parserPieGraph.get('seriesCollection')[0]._styles.marker.fill.colors;
                        return '<div style="background-color: ' + defaultColors[index] + '; height:11px; width:11px; border: 1px solid black; margin-left:auto; margin-right:auto;">&nbsp;</div>';
                    }
                };

                var cols = [
                        {key:"Color", formatter:calcParserColor},
                        "Parser",
                        "Current",
                        {key:"Improvement", formatter:calcParserImprovement}
                    ];
                new Y.DataTable.Base({
                    columnset:cols,
                    recordset:parserData
                }).render("#parser_table");
            });
        });
    });

    it.getOrphans(function (t) {
        var orp = t.responseObject();
        var currentOrphans;

        YUI().use('charts', function (Y) {
            // Create data
            currentOrphans = [
                {Type:"Regular", Amount:orp.total - orp.orphans},
                {Type:"Orphans", Amount:orp.orphans}
            ];

            new Y.Chart({
                render:"#orphan_chart",
                categoryKey:"Type",
                seriesKeys:["Amount"],
                dataProvider:currentOrphans,
                type:"pie",
                seriesCollection:[
                    {
                        categoryKey:"Type",
                        valueKey:"Amount",
                        styles:{
                            fill:{
                                colors:[
                                    "#00cc00",
                                    "#cc0000"
                                ]
                            }
                        }
                    }
                ]
            });
        });

        YUI().use('datatable-base', function (Y) {
            var calcColor = function (o) {
                switch (o.rowindex) {
                    case 0:
                        return '<div style="background-color: #00cc00; height:11px; width:11px; border: 1px solid black; margin-left:auto; margin-right:auto;">&nbsp;</div>';
                        break;
                    case 1:
                        return '<div style="background-color: #cc0000; height:11px; width:11px; border: 1px solid black; margin-left:auto; margin-right:auto;">&nbsp;</div>';
                        break;
                }
            };

            var cols = [
                    {key:"Color", formatter:calcColor},
                    "Type",
                    "Amount"
                ];
            new Y.DataTable.Base({
                columnset:cols,
                recordset:currentOrphans
            }).render("#orphan_table");
        });

    });

    it.getEntries(1, 50, 'severity', 'DESC', function (t) {
        var entries = t.responseObject();

        var calcFileName = function (o) {
            var fileName = o.record.getValue("file_name");
            if (fileName.length > 25) {
                return '[...]' + fileName.substring(fileName.length - 20);
            } else {
                return fileName;
            }
        };

        var calcLineNumber = function (o) {
            return o.record.getValue("line_number");
        };

        var calcParser = function (o) {
            return o.record.getValue("parser");
        };

        var calcSeverity = function (o) {
            return o.record.getValue("severity");
        };

        YUI().use('datatable-base', function (Y) {
            var cols = [
                {key: "File Name", formatter: calcFileName},
                {key: "Line Number", formatter: calcLineNumber},
                {key: "Parser", formatter: calcParser},
                {key: "Severity", formatter: calcSeverity}];

            new Y.DataTable.Base({
                columnset: cols,
                recordset: entries.data
            }).render("#entry_table");
        });

        lastPage = Math.ceil(entries.totalNumber / 50);

        if (entries.totalNumber > 50) {
            drawPager(entries.totalNumber, 50, 'entry_table_container');
        }

    });
}

function goToLastPage() {
    currentPage = lastPage;

    $('entry_table').innerHTML = '';
    $('entry_table_container').select('p')[0].remove();

    it.getEntries(lastPage, 50, 'severity', 'DESC', function (t) {
        var entries = t.responseObject();

        var calcFileName = function (o) {
            var fileName = o.record.getValue("file_name");
            if (fileName.length > 25) {
                return '[...]' + fileName.substring(fileName.length - 20);
            } else {
                return fileName;
            }
        };

        var calcLineNumber = function (o) {
            return o.record.getValue("line_number");
        };

        var calcParser = function (o) {
            return o.record.getValue("parser");
        };

        var calcSeverity = function (o) {
            return o.record.getValue("severity");
        };

        YUI().use('datatable-base', function (Y) {
            var cols = [
                {key: "File Name", formatter: calcFileName},
                {key: "Line Number", formatter: calcLineNumber},
                {key: "Parser", formatter: calcParser},
                {key: "Severity", formatter: calcSeverity}];

            new Y.DataTable.Base({
                columnset: cols,
                recordset: entries.data
            }).render("#entry_table");
        });

        if (entries.totalNumber > 50) {
            drawPager(entries.totalNumber, 50, 'entry_table_container');
        }
    });
}

function goToNextPage() {
    currentPage++;

    $('entry_table').innerHTML = '';
    $('entry_table_container').select('p')[0].remove();

    it.getEntries(currentPage, 50, 'severity', 'DESC', function (t) {
        var entries = t.responseObject();

        var calcFileName = function (o) {
            var fileName = o.record.getValue("file_name");
            if (fileName.length > 25) {
                return '[...]' + fileName.substring(fileName.length - 20);
            } else {
                return fileName;
            }
        };

        var calcLineNumber = function (o) {
            return o.record.getValue("line_number");
        };

        var calcParser = function (o) {
            return o.record.getValue("parser");
        };

        var calcSeverity = function (o) {
            return o.record.getValue("severity");
        };

        YUI().use('datatable-base', function (Y) {
            var cols = [
                {key: "File Name", formatter: calcFileName},
                {key: "Line Number", formatter: calcLineNumber},
                {key: "Parser", formatter: calcParser},
                {key: "Severity", formatter: calcSeverity}];

            new Y.DataTable.Base({
                columnset: cols,
                recordset: entries.data
            }).render("#entry_table");
        });

        if (entries.totalNumber > 50) {
            drawPager(entries.totalNumber, 50, 'entry_table_container');
        }
    });
}

function goToPreviousPage() {
    currentPage--;

    $('entry_table').innerHTML = '';
    $('entry_table_container').select('p')[0].remove();

    it.getEntries(currentPage, 50, 'severity', 'DESC', function (t) {
        var entries = t.responseObject();

        var calcFileName = function (o) {
            var fileName = o.record.getValue("file_name");
            if (fileName.length > 25) {
                return '[...]' + fileName.substring(fileName.length - 20);
            } else {
                return fileName;
            }
        };

        var calcLineNumber = function (o) {
            return o.record.getValue("line_number");
        };

        var calcParser = function (o) {
            return o.record.getValue("parser");
        };

        var calcSeverity = function (o) {
            return o.record.getValue("severity");
        };

        YUI().use('datatable-base', function (Y) {
            var cols = [
                {key: "File Name", formatter: calcFileName},
                {key: "Line Number", formatter: calcLineNumber},
                {key: "Parser", formatter: calcParser},
                {key: "Severity", formatter: calcSeverity}];

            new Y.DataTable.Base({
                columnset: cols,
                recordset: entries.data
            }).render("#entry_table");
        });

        if (entries.totalNumber > 50) {
            drawPager(entries.totalNumber, 50, 'entry_table_container');
        }
    });
}

function goToFirstPage() {
    currentPage = 1;

    $('entry_table').innerHTML = '';
    $('entry_table_container').select('p')[0].remove();

    it.getEntries(currentPage, 50, 'severity', 'DESC', function (t) {
        var entries = t.responseObject();

        var calcFileName = function (o) {
            var fileName = o.record.getValue("file_name");
            if (fileName.length > 25) {
                return '[...]' + fileName.substring(fileName.length - 20);
            } else {
                return fileName;
            }
        };

        var calcLineNumber = function (o) {
            return o.record.getValue("line_number");
        };

        var calcParser = function (o) {
            return o.record.getValue("parser");
        };

        var calcSeverity = function (o) {
            return o.record.getValue("severity");
        };

        YUI().use('datatable-base', function (Y) {
            var cols = [
                {key: "File Name", formatter: calcFileName},
                {key: "Line Number", formatter: calcLineNumber},
                {key: "Parser", formatter: calcParser},
                {key: "Severity", formatter: calcSeverity}];

            new Y.DataTable.Base({
                columnset: cols,
                recordset: entries.data
            }).render("#entry_table");
        });

        if (entries.totalNumber > 50) {
            drawPager(entries.totalNumber, 50, 'entry_table_container');
        }
    });
}