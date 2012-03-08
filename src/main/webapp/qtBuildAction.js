var currentSeverities;

it.getSeverities(function (t) {
    sev = t.responseObject();

    YUI().use('charts', function (Y) {
        // Create data
        currentSeverities = [
            {severity: "Info", amount: sev.info},
            {severity: "Warnings", amount: sev.warnings},
            {severity: "Errors", amount: sev.errors}
        ];

        var pieGraph = new Y.Chart({
            render: "#severity_chart",
            categoryKey: "severity",
            seriesKeys: ["amount"],
            dataProvider: currentSeverities,
            type: "pie",
            seriesCollection: [
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
});

it.getSeverities(function (t) {
    psev = t.responseObject();

    YUI().use('datatable-base', function(Y) {
        var calcImprovement = function (o) {
            value = o.record.getValue("Amount") - o.record.getValue("Previous");

            color = "black";
            if (value > 0) color = "red";
            if (value < 0) color = "green";

            return '<font color="' + color + '">' + value + '</font>'
        };

        var calcColor = function (o) {
            switch(o.rowindex) {
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

        var cols = [{key: "Color", formatter: calcColor}, "Severity", "Amount", { key: "Improvement", formatter: calcImprovement }],
        data = [
            {Severity: "Info",     Amount: currentSeverities[0].amount, Previous: psev.info},
            {Severity: "Warnings", Amount: currentSeverities[1].amount, Previous: psev.warnings},
            {Severity: "Errors",   Amount: currentSeverities[2].amount, Previous: psev.errors}
        ],
        dt = new Y.DataTable.Base({
            columnset: cols,
            recordset: data
        }).render("#severity_table");
    });
});

it.getParsers(function (t) {
    par = t.responseObject();

    YUI().use('charts', function (Y) {
        // Create data
        currentParsers = par;

        parserPieGraph = new Y.Chart({
            render: "#parser_chart",
            categoryKey: "parser",
            seriesKeys: ["amount"],
            dataProvider: currentParsers,
            type: "pie",
            seriesCollection: [
                {
                    categoryKey: "parser",
                    valueKey: "amount"
                }
            ]
        });
    });

    it.getPreviousParsers(function (t) {
        ppar = t.responseObject();

        // Find all the parsers
        cParsers = new Hash();
        pParsers = new Hash();
        for (i = 0; i < par.length; i++) {
            cParsers.set(par[i].parser, par[i].amount);
        }
        for (i = 0; i < par.length; i++) {
            pParsers.set(ppar[i].parser, ppar[i].amount);
        }
        allParsers = cParsers.merge(pParsers);

        parserData = [];

        allParsers.each(function(pair) {
            parser = pair.key;

            current = 0;
            if (cParsers.keys().indexOf(parser) >= 0) {
                current = cParsers.get(parser);
            }

            previous = 0;
            if (pParsers.keys().indexOf(parser) >= 0) {
                previous = pParsers.get(parser);
            }

            parserData.push({Parser: parser, Current: current, Previous: previous});
        });

        YUI().use('datatable-base', function(Y) {
            var calcParserImprovement = function (o) {
                        value = o.record.getValue("Current") - o.record.getValue("Previous");

                        color = "black";
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

            var cols = [{key: "Color", formatter: calcParserColor}, "Parser", "Current", {key: "Improvement", formatter: calcParserImprovement}],
                    dt = new Y.DataTable.Base({
                        columnset: cols,
                        recordset: parserData
                    }).render("#parser_table");
        });
    });
});
