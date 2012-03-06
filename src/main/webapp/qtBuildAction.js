it.getSeverities(function (t) {
    sev = t.responseObject();

    YUI().use('charts', function (Y) {
        // Create data
        var myDataValues = [
            {severity: "Info", amount: sev.infos},
            {severity: "Warnings", amount: sev.warnings},
            {severity: "Errors", amount: sev.errors},
            {severity: "Orphans", amount: sev.orphans}
        ];

        var pieGraph = new Y.Chart({
            render: "#severity_chart",
            categoryKey: "severity",
            seriesKeys: ["amount"],
            dataProvider: myDataValues,
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
                                "#cc0000",
                                "#cccccc"
                            ]
                        }
                    }
                }
            ]
        });
    });

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
                case 3:
                    return '<div style="background-color: #cccccc; height:11px; width:11px; border: 1px solid black; margin-left:auto; margin-right:auto;">&nbsp;</div>';
                    break;
            }
        };

        var cols = [{key: "Color", formatter: calcColor}, "Severity", "Amount", { key: "Improvement", formatter: calcImprovement } ],
        data = [
            {Severity: "Info", Amount: sev.infos, Previous: sev.infos_prev},
            {Severity: "Warnings", Amount: sev.warnings, Previous: sev.warnings_prev},
            {Severity: "Errors", Amount: sev.errors, Previous: sev.errors_prev},
            {Severity: "Orphans", Amount: sev.orphans, Previous: sev.orphans_prev}
        ],
        dt = new Y.DataTable.Base({
            columnset: cols,
            recordset: data
        }).render("#severity_table");
    });
});
