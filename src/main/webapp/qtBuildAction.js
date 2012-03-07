var currentSeverities;

it.getSeverities(function (t) {
    sev = t.responseObject();

    YUI().use('charts', function (Y) {
        // Create data
        currentSeverities = [
            {severity: "Info", amount: sev.data[0].INFO},
            {severity: "Warnings", amount: sev.data[0].WARNINGS},
            {severity: "Errors", amount: sev.data[0].ERRORS}
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
            {Severity: "Info",     Amount: currentSeverities[0].amount, Previous: psev.data[0].INFO},
            {Severity: "Warnings", Amount: currentSeverities[1].amount, Previous: psev.data[0].WARNINGS},
            {Severity: "Errors",   Amount: currentSeverities[2].amount, Previous: psev.data[0].ERRORS}
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
        currentParsers = par.data;

        var pieGraph = new Y.Chart({
            render: "#parser_chart",
            categoryKey: "PARSER",
            seriesKeys: ["AMOUNT"],
            dataProvider: currentParsers,
            type: "pie",
            seriesCollection: [
                {
                    categoryKey: "PARSER",
                    valueKey: "AMOUNT"
                }
            ]
        });
    });
});
