package org.jenkins.plugins.qualityTrends.BuildAction

def l = namespace(lib.LayoutTagLib)
def st = namespace("jelly:stapler")

l.layout(title: _("Build Quality Trends")) {
    l.header() {
        script(src: "/plugin/quality-trends/excanvas.js")
        script(src: "/plugin/quality-trends/jquery.js")
        script("jQuery.noConflict();")
        script(src: "/plugin/quality-trends/jquery.flot.js")
        script(src: "/plugin/quality-trends/jquery.flot.pie.js")
        script(src: "/plugin/quality-trends/jquery.dataTables.js")
    }
    include(my.build, "sidepanel")
    l.main_panel {
        h1(_("Build Quality Trends"))
        div(id: "severity") {
            h2(_("Severity"))
            div(id: "severity_chart", style: "width:400px;height:300px;text-align:center;background-color:#ddd;float:right;") {
                text("loading...")
            }
            table(id: "severity_table") {
                thead() {
                    tr() {
                        th(_("Severity"))
                        th(_("Amount"))
                        th(_("Differential"))
                    }
                }
                tbody() {
                    tr() {
                        td(_("Info"))
                        td(id: "info_cell") {
                            text("loading...")
                        }
                        td(id: "info_diff_cell") {
                            text("loading...")
                        }
                    }

                    tr() {
                        td(_("Warning"))
                        td(id: "warning_cell") {
                            text("loading...")
                        }
                        td(id: "warning_diff_cell") {
                            text("loading...")
                        }
                    }
                    tr() {
                        td(_("Error"))
                        td(id: "error_cell") {
                            text("loading...")
                        }
                        td(id: "error_diff_cell") {
                            text("loading...")
                        }
                    }
                    tr() {
                        td(_("Orphan"))
                        td(id: "orphan_cell") {
                            text("loading...")
                        }
                        td(id: "orphan_diff_cell") {
                            text("loading...")
                        }
                    }
                }
            }
        }
        st.bind(var: "it", value: my)
        script("""
            jQuery(function () {

                it.getSeverities(function(t) {
                    sev = t.responseObject();

                    var data = [
                        {label: "Info", data: sev.infos, color: "#ffff66"},
                        {label: "Warnings", data: sev.warnings, color: "#ffcc00"},
                        {label: "Errors", data: sev.errors, color: "#cc0000"},
                        {label: "Orphans", data: sev.orphans, color: "#cccccc"}
                    ];

                    jQuery("#info_cell").html(sev.infos);
                    jQuery("#warning_cell").html(sev.warnings);
                    jQuery("#error_cell").html(sev.errors);
                    jQuery("#orphan_cell").html(sev.orphans);
                    jQuery("#info_diff_cell").html(sev.infos - sev.infos_prev);
                    jQuery("#warning_diff_cell").html(sev.warnings - sev.warnings_prev);
                    jQuery("#error_diff_cell").html(sev.errors - sev.errors_prev);
                    jQuery("#orphan_diff_cell").html(sev.orphans - sev.orphans_prev);

                    jQuery("#severity_table").dataTable();

                    jQuery("#severity_chart").css("background-color", "#fff")

                    jQuery.plot(jQuery("#severity_chart"), data,
                        {
                            series: {
                                pie: {
                                    show: true
                                }
                            }
                        }
                    );
                });

            });""")
    }
}

